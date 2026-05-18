package net.sf.l2jdev.gameserver.model.teleporter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.TeleportType;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcTeleportRequest;
import net.sf.l2jdev.gameserver.model.events.returns.TerminateReturn;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.enums.SpecialItemType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcHtmlMessage;

public class TeleportHolder
{
	private static final Logger LOGGER = Logger.getLogger(TeleportHolder.class.getName());
	private final String _name;
	private final TeleportType _type;
	private final List<TeleportLocation> _teleportData = new ArrayList<>();

	public TeleportHolder(String name, TeleportType type)
	{
		this._name = name;
		this._type = type;
	}

	public String getName()
	{
		return this._name;
	}

	public TeleportType getType()
	{
		return this._type;
	}

	public void registerLocation(StatSet locData)
	{
		this._teleportData.add(new TeleportLocation(this._teleportData.size(), locData));
	}

	public TeleportLocation getLocation(int locationId)
	{
		return this._teleportData.get(locationId);
	}

	public List<TeleportLocation> getLocations()
	{
		return this._teleportData;
	}

	public void showTeleportList(Player player, Npc npc)
	{
		this.showTeleportList(player, npc, "npc_" + npc.getObjectId() + "_teleport");
	}

	public void showTeleportList(Player player, Npc npc, String bypass)
	{
		if (this.isNoblesse() && !player.isNoble())
		{
			LOGGER.warning(player + " requested noblesse teleport without being noble!");
		}
		else
		{
			int questZoneId = this.isNormalTeleport() ? player.getQuestZoneId() : -1;
			StringBuilder sb = new StringBuilder();
			StringBuilder sbF = new StringBuilder();

			for (TeleportLocation loc : this._teleportData)
			{
				String finalName = loc.getName();
				String confirmDesc = loc.getName();
				if (loc.getNpcStringId() != null)
				{
					int stringId = loc.getNpcStringId().getId();
					finalName = "<fstring>" + stringId + "</fstring>";
					confirmDesc = "F;" + stringId;
				}

				if (this.shouldPayFee(player, loc))
				{
					long fee = this.calculateFee(player, loc);
					if (fee != 0L)
					{
						finalName = finalName + " - " + fee + " " + this.getItemName(loc.getFeeId(), true);
					}
				}

				boolean isQuestTeleport = questZoneId >= 0 && loc.getQuestZoneId() == questZoneId;
				if (isQuestTeleport)
				{
					sbF.append("<button align=left icon=\"quest\" action=\"bypass -h " + bypass + " " + this._name + " " + loc.getId() + "\" msg=\"811;" + confirmDesc + "\">" + finalName + "</button>");
				}
				else
				{
					sb.append("<button align=left icon=\"teleport\" action=\"bypass -h " + bypass + " " + this._name + " " + loc.getId() + "\" msg=\"811;" + confirmDesc + "\">" + finalName + "</button>");
				}
			}

			sbF.append(sb.toString());
			NpcHtmlMessage msg = new NpcHtmlMessage(npc.getObjectId());
			msg.setFile(player, "data/html/teleporter/teleports.htm");
			msg.replace("%locations%", sbF.toString());
			player.sendPacket(msg);
		}
	}

	public void doTeleport(Player player, Npc npc, int locId)
	{
		if (!player.isAlikeDead())
		{
			if (this.isNoblesse() && !player.isNoble())
			{
				LOGGER.warning(player + " requested noblesse teleport without being noble!");
			}
			else
			{
				TeleportLocation loc = this.getLocation(locId);
				if (loc == null)
				{
					LOGGER.warning(player + " requested unknown teleport location " + locId + " within list " + this._name + "!");
				}
				else
				{
					if (!PlayerConfig.TELEPORT_WHILE_SIEGE_IN_PROGRESS)
					{
						for (int castleId : loc.getCastleId())
						{
							if (CastleManager.getInstance().getCastleById(castleId).getSiege().isInProgress())
							{
								player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
								return;
							}
						}
					}

					if (this.isNormalTeleport())
					{
						if (!PlayerConfig.TELEPORT_WHILE_SIEGE_IN_PROGRESS && npc.getCastle().getSiege().isInProgress())
						{
							NpcHtmlMessage msg = new NpcHtmlMessage(npc.getObjectId());
							msg.setFile(player, "data/html/teleporter/castleteleporter-busy.htm");
							msg.replace("%npcname%", npc.getName());
							player.sendPacket(msg);
							return;
						}

						if (!PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && player.getReputation() < 0)
						{
							player.sendMessage("Go away, you're not welcome here.");
							return;
						}

						if (player.isCombatFlagEquipped())
						{
							player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
							return;
						}
					}

					if (EventDispatcher.getInstance().hasListener(EventType.ON_NPC_TELEPORT_REQUEST, npc))
					{
						TerminateReturn term = EventDispatcher.getInstance().notifyEvent(new OnNpcTeleportRequest(player, npc, loc), npc, TerminateReturn.class);
						if (term != null && term.terminate())
						{
							return;
						}
					}

					if (this.shouldPayFee(player, loc) && !player.destroyItemByItemId(ItemProcessType.FEE, loc.getFeeId(), this.calculateFee(player, loc), npc, true))
					{
						if (loc.getFeeId() == 57)
						{
							player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
						}
						else
						{
							player.sendMessage("You do not have enough " + this.getItemName(loc.getFeeId(), true) + ".");
						}
					}
					else
					{
						player.teleToLocation(loc);
					}
				}
			}
		}
	}

	private boolean shouldPayFee(Player player, TeleportLocation loc)
	{
		return !this.isNormalTeleport() || (player.getLevel() > PlayerConfig.MAX_FREE_TELEPORT_LEVEL || player.isSubClassActive()) && loc.getFeeId() != 0 && loc.getFeeCount() > 0L;
	}

	private long calculateFee(Player player, TeleportLocation loc)
	{
		if (this.isNormalTeleport())
		{
			if (!player.isSubClassActive() && player.getLevel() <= PlayerConfig.MAX_FREE_TELEPORT_LEVEL)
			{
				return 0L;
			}

			Calendar cal = Calendar.getInstance();
			int hour = cal.get(11);
			int dayOfWeek = cal.get(7);
			if (hour >= 20 && dayOfWeek >= 2 && dayOfWeek <= 3)
			{
				return loc.getFeeCount() / 2L;
			}
		}

		return loc.getFeeCount();
	}

	private boolean isNormalTeleport()
	{
		return this._type == TeleportType.NORMAL || this._type == TeleportType.HUNTING;
	}

	public boolean isNoblesse()
	{
		return this._type == TeleportType.NOBLES_ADENA || this._type == TeleportType.NOBLES_TOKEN;
	}

	protected String getItemName(int itemId, boolean fstring)
	{
		if (fstring)
		{
			if (itemId == 57)
			{
				return "<fstring>1000308</fstring>";
			}

			if (itemId == 5575)
			{
				return "<fstring>1000309</fstring>";
			}
		}

		ItemTemplate item = ItemData.getInstance().getTemplate(itemId);
		if (item != null)
		{
			return item.getName();
		}
		SpecialItemType specialItem = SpecialItemType.getByClientId(itemId);
		if (specialItem != null)
		{
			switch (specialItem)
			{
				case PC_CAFE_POINTS:
					return "Player Commendation Points";
				case CLAN_REPUTATION:
					return "Clan Reputation Points";
				case FAME:
					return "Fame";
				case FIELD_CYCLE_POINTS:
					return "Field Cycle Points";
				case RAIDBOSS_POINTS:
					return "Raid Points";
				case HONOR_COINS:
					return "Honor Points";
			}
		}

		return "Unknown item: " + itemId;
	}
}
