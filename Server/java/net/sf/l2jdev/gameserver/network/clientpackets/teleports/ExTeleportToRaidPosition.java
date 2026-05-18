package net.sf.l2jdev.gameserver.network.clientpackets.teleports;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.holders.TeleportListHolder;
import net.sf.l2jdev.gameserver.data.xml.NpcData;
import net.sf.l2jdev.gameserver.data.xml.RaidTeleportListData;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.DatabaseSpawnManager;
import net.sf.l2jdev.gameserver.managers.GrandBossManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.npc.RaidBossStatus;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.effects.EffectFlag;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.teleports.ExRaidTeleportInfo;

public class ExTeleportToRaidPosition extends ClientPacket
{
	private int _raidId;

	@Override
	protected void readImpl()
	{
		this._raidId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			TeleportListHolder teleport = RaidTeleportListData.getInstance().getTeleport(this._raidId);
			if (teleport == null)
			{
				PacketLogger.warning("No registered teleport location for raid id: " + this._raidId);
			}
			else if (player.isDead())
			{
				player.sendPacket(SystemMessageId.DEAD_CHARACTERS_CANNOT_USE_TELEPORTATION);
			}
			else
			{
				NpcTemplate template = NpcData.getInstance().getTemplate(this._raidId);
				if (template.isType("GrandBoss") && GrandBossManager.getInstance().getStatus(this._raidId) != 0)
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_RIGHT_NOW);
				}
				else if (template.isType("RaidBoss") && DatabaseSpawnManager.getInstance().getStatus(this._raidId) != RaidBossStatus.ALIVE)
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_RIGHT_NOW);
				}
				else if (!player.isCastingNow() && !player.isInCombat() && !player.isImmobilized() && !player.isInInstance() && !player.isOnEvent() && !player.isInOlympiadMode() && !player.inObserverMode() && !player.isInTraingCamp() && !player.isInTimedHuntingZone())
				{
					if ((!PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT || !PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_USE_GK) && player.getReputation() < 0)
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_RIGHT_NOW);
					}
					else if (player.isAffected(EffectFlag.CANNOT_ESCAPE))
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_RIGHT_NOW);
					}
					else
					{
						Location location = teleport.getLocation();
						if (!PlayerConfig.TELEPORT_WHILE_SIEGE_IN_PROGRESS)
						{
							Castle castle = CastleManager.getInstance().getCastle(location.getX(), location.getY(), location.getZ());
							if (castle != null && castle.getSiege().isInProgress())
							{
								player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
								return;
							}
						}

						int price;
						if (System.currentTimeMillis() - player.getVariables().getLong("LastFreeRaidTeleportTime", 0L) > 86400000L)
						{
							player.getVariables().set("LastFreeRaidTeleportTime", System.currentTimeMillis());
							price = 0;
						}
						else
						{
							price = teleport.getPrice();
						}

						if (price > 0)
						{
							if (player.getInventory().getInventoryItemCount(91663, -1) < price)
							{
								player.sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_L_COINS);
								return;
							}

							player.destroyItemByItemId(ItemProcessType.FEE, 91663, price, player, true);
						}

						player.abortCast();
						player.stopMove(null);
						player.setTeleportLocation(location);
						player.castTeleportSkill();
						player.sendPacket(new ExRaidTeleportInfo(player));
					}
				}
				else
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_RIGHT_NOW);
				}
			}
		}
	}
}
