package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.holders.ResurrectByPaymentHolder;
import net.sf.l2jdev.gameserver.data.xml.ClanHallData;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.managers.MapRegionManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.SiegeClan;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.TeleportWhereType;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.listeners.AbstractEventListener;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.residences.ClanHall;
import net.sf.l2jdev.gameserver.model.residences.ResidenceFunctionType;
import net.sf.l2jdev.gameserver.model.script.Event;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.skill.CommonSkill;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class RequestRestartPoint extends ClientPacket
{
	protected int _requestedPointType;
	protected boolean _continuation;
	protected int _resItemID;
	protected int _resCount;

	@Override
	protected void readImpl()
	{
		this._requestedPointType = this.readInt();
		if (this.remaining() != 0)
		{
			this._resItemID = this.readInt();
			this._resCount = this.readInt();
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (player.canRevive())
			{
				if (player.isFakeDeath())
				{
					player.stopFakeDeath(true);
				}
				else if (player.isDead())
				{
					if (player.isOnEvent())
					{
						for (AbstractEventListener listener : player.getListeners(EventType.ON_CREATURE_DEATH))
						{
							if (listener.getOwner() instanceof Event)
							{
								((Event) listener.getOwner()).notifyEvent("ResurrectPlayer", null, player);
								return;
							}
						}
					}

					Castle castle = CastleManager.getInstance().getCastle(player.getX(), player.getY(), player.getZ());
					if (castle != null && castle.getSiege().isInProgress() && player.getClan() != null && castle.getSiege().checkIsAttacker(player.getClan()))
					{
						ThreadPool.schedule(new RequestRestartPoint.DeathTask(player), castle.getSiege().getAttackerRespawnDelay());
						if (castle.getSiege().getAttackerRespawnDelay() > 0)
						{
							player.sendMessage("You will be re-spawned in " + castle.getSiege().getAttackerRespawnDelay() / 1000 + " seconds");
						}
					}
					else
					{
						this.portPlayer(player);
					}
				}
			}
		}
	}

	private void portPlayer(Player player)
	{
		Location loc = null;
		Instance instance = null;
		if (player.isJailed())
		{
			this._requestedPointType = 27;
		}

		switch (this._requestedPointType)
		{
			case 1:
				if (player.getClan() == null || player.getClan().getHideoutId() == 0)
				{
					PacketLogger.warning("Player [" + player.getName() + "] called RestartPointPacket - To Clanhall and he doesn't have Clanhall!");
					return;
				}

				loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.CLANHALL);
				ClanHall residense = ClanHallData.getInstance().getClanHallByClan(player.getClan());
				if (residense != null && residense.hasFunction(ResidenceFunctionType.EXP_RESTORE))
				{
					player.restoreExp(residense.getFunction(ResidenceFunctionType.EXP_RESTORE).getValue());
				}
				break;
			case 2:
				Clan clanx = player.getClan();
				Castle castlex = CastleManager.getInstance().getCastle(player);
				if (castlex != null && castlex.getSiege().isInProgress())
				{
					if (castlex.getSiege().checkIsDefender(clanx))
					{
						loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.CASTLE);
					}
					else
					{
						if (!castlex.getSiege().checkIsAttacker(clanx))
						{
							PacketLogger.warning("Player [" + player.getName() + "] called RestartPointPacket - To Castle and he doesn't have Castle!");
							return;
						}

						loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.TOWN);
					}
				}
				else
				{
					if (clanx == null || clanx.getCastleId() == 0)
					{
						return;
					}

					loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.CASTLE);
				}

				if (clanx != null)
				{
					castlex = CastleManager.getInstance().getCastleByOwner(clanx);
					if (castlex != null)
					{
						Castle.CastleFunction castleFunction = castlex.getCastleFunction(4);
						if (castleFunction != null)
						{
							player.restoreExp(castleFunction.getLvl());
						}
					}
				}
				break;
			case 3:
				Clan clan = player.getClan();
				if (clan == null || clan.getFortId() == 0)
				{
					PacketLogger.warning("Player [" + player.getName() + "] called RestartPointPacket - To Fortress and he doesn't have Fortress!");
					return;
				}

				loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.FORTRESS);
				Fort fort = FortManager.getInstance().getFortByOwner(clan);
				if (fort != null)
				{
					Fort.FortFunction fortFunction = fort.getFortFunction(4);
					if (fortFunction != null)
					{
						player.restoreExp(fortFunction.getLevel());
					}
				}
				break;
			case 4:
				SiegeClan siegeClan = null;
				Castle castle = CastleManager.getInstance().getCastle(player);
				Fort fortSiege = FortManager.getInstance().getFort(player);
				if (castle != null && castle.getSiege().isInProgress())
				{
					siegeClan = castle.getSiege().getAttackerClan(player.getClan());
				}
				else if (fortSiege != null && fortSiege.getSiege().isInProgress())
				{
					siegeClan = fortSiege.getSiege().getAttackerClan(player.getClan());
				}

				if (siegeClan == null || siegeClan.getFlag().isEmpty())
				{
					PacketLogger.warning("Player [" + player.getName() + "] called RestartPointPacket - To Siege HQ and he doesn't have Siege HQ!");
					return;
				}

				loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.SIEGEFLAG);
				break;
			case 5:
				if (!player.isGM() && !player.getInventory().haveItemForSelfResurrection())
				{
					PacketLogger.warning("Player [" + player.getName() + "] called RestartPointPacket - Fixed and he isn't festival participant!");
					return;
				}

				if (player.isGM())
				{
					player.doRevive(100.0);
				}
				else if (player.destroyItemByItemId(ItemProcessType.FEE, 10649, 1L, player, false))
				{
					player.doRevive(100.0);
					CommonSkill.FEATHER_OF_BLESSING.getSkill().applyEffects(player, player);
				}
				else
				{
					instance = player.getInstanceWorld();
					loc = new Location(player);
				}
			case 6:
			case 7:
				break;
			case 8:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
			case 18:
			case 19:
			case 20:
			case 21:
			case 22:
			case 23:
			case 24:
			case 26:
			default:
				if (player.isInTimedHuntingZone())
				{
					instance = player.getInstanceWorld();
					loc = player.getTimedHuntingZone().getEnterLocation();
				}
				else
				{
					loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.TOWN);
				}
				break;
			case 9:
				if (PlayerConfig.RESURRECT_BY_PAYMENT_ENABLED)
				{
					if (!player.isDead())
					{
						break;
					}

					int originalValue = player.getVariables().getInt("RESURRECT_BY_PAYMENT_COUNT", 0);
					if (originalValue < PlayerConfig.RESURRECT_BY_PAYMENT_MAX_FREE_TIMES)
					{
						player.getVariables().set("RESURRECT_BY_PAYMENT_COUNT", originalValue + 1);
						player.doRevive(100.0);
						loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.TOWN);
						player.teleToLocation(loc, true, instance);
						break;
					}
					int firstID = PlayerConfig.RESURRECT_BY_PAYMENT_ENABLED ? PlayerConfig.RESURRECT_BY_PAYMENT_FIRST_RESURRECT_ITEM : 91663;
					int secondID = PlayerConfig.RESURRECT_BY_PAYMENT_ENABLED ? PlayerConfig.RESURRECT_BY_PAYMENT_SECOND_RESURRECT_ITEM : 57;
					Map<Integer, Map<Integer, ResurrectByPaymentHolder>> resMAP = null;
					Item item = null;
					if (this._resItemID == firstID)
					{
						resMAP = PlayerConfig.RESURRECT_BY_PAYMENT_FIRST_RESURRECT_VALUES;
						item = player.getInventory().getItemByItemId(PlayerConfig.RESURRECT_BY_PAYMENT_FIRST_RESURRECT_ITEM);
					}
					else if (this._resItemID == secondID)
					{
						resMAP = PlayerConfig.RESURRECT_BY_PAYMENT_SECOND_RESURRECT_VALUES;
						item = player.getInventory().getItemByItemId(PlayerConfig.RESURRECT_BY_PAYMENT_SECOND_RESURRECT_ITEM);
					}

					if (resMAP == null || item == null)
					{
						break;
					}

					List<Integer> levelList = new ArrayList<>(resMAP.keySet());

					for (int level : levelList)
					{
						if (player.getLevel() < level || levelList.lastIndexOf(level) == levelList.size() - 1)
						{
							int maxResTime;
							try
							{
								maxResTime = resMAP.get(level).keySet().stream().max(Integer::compareTo).get();
							}
							catch (Exception var17)
							{
								player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
								return;
							}

							int getValue = maxResTime <= originalValue ? maxResTime : originalValue + 1;
							ResurrectByPaymentHolder rbph = resMAP.get(level).get(getValue);
							long fee = (int) (rbph.getAmount() * player.getStat().getValue(Stat.RESURRECTION_FEE_MODIFIER, 1.0));
							if (item.getCount() < fee)
							{
								return;
							}

							player.getVariables().set("RESURRECT_BY_PAYMENT_COUNT", originalValue + 1);
							player.destroyItem(ItemProcessType.FEE, item, fee, player, true);
							player.doRevive(rbph.getResurrectPercent());
							loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.TOWN);
							player.teleToLocation(loc, true, instance);
							break;
						}
					}
				}
			case 25:
				if (player.isInTimedHuntingZone())
				{
					loc = player.getTimedHuntingZone().getExitLocation();
				}

				if (loc == null)
				{
					loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.TOWN);
				}
				break;
			case 27:
				if (!player.isJailed())
				{
					return;
				}

				loc = new Location(-114356, -249645, -2984);
		}

		if (loc != null)
		{
			player.setIsPendingRevive(true);
			player.teleToLocation(loc, true, instance);
		}
	}

	class DeathTask implements Runnable
	{
		final Player _player;

		DeathTask(Player player)
		{
			Objects.requireNonNull(RequestRestartPoint.this);
			super();
			this._player = player;
		}

		@Override
		public void run()
		{
			RequestRestartPoint.this.portPlayer(this._player);
		}
	}
}
