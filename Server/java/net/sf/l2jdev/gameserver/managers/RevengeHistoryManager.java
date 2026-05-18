package net.sf.l2jdev.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.RevengeType;
import net.sf.l2jdev.gameserver.network.holders.RevengeHistoryHolder;
import net.sf.l2jdev.gameserver.network.serverpackets.revenge.ExPvpBookShareRevengeKillerLocation;
import net.sf.l2jdev.gameserver.network.serverpackets.revenge.ExPvpBookShareRevengeList;
import net.sf.l2jdev.gameserver.network.serverpackets.revenge.ExPvpBookShareRevengeNewRevengeInfo;

public class RevengeHistoryManager
{
	private static final Logger LOGGER = Logger.getLogger(RevengeHistoryManager.class.getName());
	private static final Map<Integer, List<RevengeHistoryHolder>> REVENGE_HISTORY = new ConcurrentHashMap<>();
	public static final String DELETE_REVENGE_HISTORY = "TRUNCATE TABLE character_revenge_history";
	public static final String INSERT_REVENGE_HISTORY = "INSERT INTO character_revenge_history (charId, type, killer_name, killer_clan, killer_level, killer_race, killer_class, victim_name, victim_clan, victim_level, victim_race, victim_class, shared, show_location_remaining, teleport_remaining, shared_teleport_remaining, kill_time, share_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final SkillHolder HIDE_SKILL = new SkillHolder(922, 1);
	public static final long REVENGE_DURATION = 21600000L;
	private static final int[] LOCATION_PRICE = new int[]
	{
		0,
		50000,
		100000,
		100000,
		200000
	};
	private static final int[] TELEPORT_PRICE = new int[]
	{
		10,
		50,
		100,
		100,
		200
	};

	@SuppressWarnings("unchecked")
	protected RevengeHistoryManager()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM character_revenge_history"); ResultSet rs = ps.executeQuery();)
		{
			while (rs.next())
			{
				int charId = rs.getInt("charId");
				List<RevengeHistoryHolder> history = (List<RevengeHistoryHolder>) (REVENGE_HISTORY.containsKey(charId) ? REVENGE_HISTORY.get(charId) : new CopyOnWriteArrayList<>());
				StatSet killer = new StatSet();
				killer.set("name", rs.getString("killer_name"));
				killer.set("clan", rs.getString("killer_clan"));
				killer.set("level", rs.getInt("killer_level"));
				killer.set("race", rs.getInt("killer_race"));
				killer.set("class", rs.getInt("killer_class"));
				StatSet victim = new StatSet();
				victim.set("name", rs.getString("victim_name"));
				victim.set("clan", rs.getString("victim_clan"));
				victim.set("level", rs.getInt("victim_level"));
				victim.set("race", rs.getInt("victim_race"));
				victim.set("class", rs.getInt("victim_class"));
				history.add(new RevengeHistoryHolder(killer, victim, RevengeType.values()[rs.getInt("type")], rs.getBoolean("shared"), rs.getInt("show_location_remaining"), rs.getInt("teleport_remaining"), rs.getInt("shared_teleport_remaining"), rs.getLong("kill_time"), rs.getLong("share_time")));
				REVENGE_HISTORY.put(charId, history);
			}
		}
		catch (Exception var14)
		{
			LOGGER.warning("Failed loading revenge history! " + var14);
		}
	}

	public void storeMe()
	{
		for (Entry<Integer, List<RevengeHistoryHolder>> entry : REVENGE_HISTORY.entrySet())
		{
			List<RevengeHistoryHolder> history = entry.getValue();
			if (history != null)
			{
				long currentTime = System.currentTimeMillis();
				List<RevengeHistoryHolder> removals = new ArrayList<>();

				for (RevengeHistoryHolder holder : history)
				{
					if (holder.getKillTime() != 0L && holder.getKillTime() + 21600000L < currentTime || holder.getShareTime() != 0L && holder.getShareTime() + 21600000L < currentTime)
					{
						removals.add(holder);
					}
				}

				for (RevengeHistoryHolder holderx : removals)
				{
					history.remove(holderx);
				}
			}
		}

		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps1 = con.prepareStatement("TRUNCATE TABLE character_revenge_history");
			PreparedStatement ps2 = con.prepareStatement("INSERT INTO character_revenge_history (charId, type, killer_name, killer_clan, killer_level, killer_race, killer_class, victim_name, victim_clan, victim_level, victim_race, victim_class, shared, show_location_remaining, teleport_remaining, shared_teleport_remaining, kill_time, share_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");)
		{
			ps1.execute();

			for (Entry<Integer, List<RevengeHistoryHolder>> entryx : REVENGE_HISTORY.entrySet())
			{
				List<RevengeHistoryHolder> history = entryx.getValue();
				if (history != null && !history.isEmpty())
				{
					for (RevengeHistoryHolder holderx : history)
					{
						ps2.clearParameters();
						ps2.setInt(1, entryx.getKey());
						ps2.setInt(2, holderx.getType().ordinal());
						ps2.setString(3, holderx.getKillerName());
						ps2.setString(4, holderx.getKillerClanName());
						ps2.setInt(5, holderx.getKillerLevel());
						ps2.setInt(6, holderx.getKillerRaceId());
						ps2.setInt(7, holderx.getKillerClassId());
						ps2.setString(8, holderx.getVictimName());
						ps2.setString(9, holderx.getVictimClanName());
						ps2.setInt(10, holderx.getVictimLevel());
						ps2.setInt(11, holderx.getVictimRaceId());
						ps2.setInt(12, holderx.getVictimClassId());
						ps2.setBoolean(13, holderx.wasShared());
						ps2.setInt(14, holderx.getShowLocationRemaining());
						ps2.setInt(15, holderx.getTeleportRemaining());
						ps2.setInt(16, holderx.getSharedTeleportRemaining());
						ps2.setLong(17, holderx.getKillTime());
						ps2.setLong(18, holderx.getShareTime());
						ps2.addBatch();
					}
				}
			}

			ps2.executeBatch();
		}
		catch (Exception var15)
		{
			LOGGER.warning(this.getClass().getSimpleName() + " Error while saving revenge history. " + var15);
		}
	}

	@SuppressWarnings("unchecked")
	public void addNewKill(Player victim, Player killer)
	{
		try
		{
			boolean found = false;
			int victimObjectId = victim.getObjectId();
			long currentTime = System.currentTimeMillis();
			List<RevengeHistoryHolder> removals = new ArrayList<>();
			List<RevengeHistoryHolder> history = (List<RevengeHistoryHolder>) (REVENGE_HISTORY.containsKey(victimObjectId) ? REVENGE_HISTORY.get(victimObjectId) : new CopyOnWriteArrayList<>());

			for (RevengeHistoryHolder holder : history)
			{
				if ((holder.getKillTime() == 0L || holder.getKillTime() + 21600000L >= currentTime) && (holder.getShareTime() == 0L || holder.getShareTime() + 21600000L >= currentTime))
				{
					if (holder.getKillerName().equals(killer.getName()))
					{
						found = true;
					}
				}
				else
				{
					removals.add(holder);
				}
			}

			history.removeAll(removals);
			if (!found)
			{
				history.add(new RevengeHistoryHolder(killer, victim, RevengeType.REVENGE));
				REVENGE_HISTORY.put(victimObjectId, history);
				victim.sendPacket(new ExPvpBookShareRevengeNewRevengeInfo(victim.getName(), killer.getName(), RevengeType.REVENGE));
				victim.sendPacket(new ExPvpBookShareRevengeList(victim));
			}
		}
		catch (Exception var11)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Failed adding revenge history! " + var11);
		}
	}

	public void locateKiller(Player player, String killerName)
	{
		List<RevengeHistoryHolder> history = REVENGE_HISTORY.get(player.getObjectId());
		if (history != null)
		{
			RevengeHistoryHolder revenge = null;

			for (RevengeHistoryHolder holder : history)
			{
				if (holder.getKillerName().equals(killerName))
				{
					revenge = holder;
					break;
				}
			}

			if (revenge != null)
			{
				Player killer = World.getInstance().getPlayer(killerName);
				if (killer == null || !killer.isOnline())
				{
					player.sendPacket(SystemMessageId.THE_ENEMY_IS_OFFLINE_AND_CANNOT_BE_FOUND_RIGHT_NOW);
				}
				else if (!killer.isInsideZone(ZoneId.PEACE) && !killer.isInInstance() && !killer.isInTimedHuntingZone() && !killer.isInsideZone(ZoneId.SIEGE) && !player.isDead() && !player.isInInstance() && !player.isInTimedHuntingZone() && !player.isInsideZone(ZoneId.SIEGE))
				{
					if (revenge.getShowLocationRemaining() > 0)
					{
						int price = LOCATION_PRICE[Math.min(LOCATION_PRICE.length - revenge.getShowLocationRemaining(), LOCATION_PRICE.length - 1)];
						if (player.reduceAdena(ItemProcessType.FEE, price, player, true))
						{
							revenge.setShowLocationRemaining(revenge.getShowLocationRemaining() - 1);
							player.sendPacket(new ExPvpBookShareRevengeKillerLocation(killer));
							player.sendPacket(new ExPvpBookShareRevengeList(player));
						}
					}
				}
				else
				{
					player.sendPacket(SystemMessageId.THE_CHARACTER_IS_IN_A_LOCATION_WHERE_IT_IS_IMPOSSIBLE_TO_USE_THIS_FUNCTION);
				}
			}
		}
	}

	public boolean checkTeleportConditions(Player player, Player killer)
	{
		if (killer == null || !killer.isOnline())
		{
			player.sendPacket(SystemMessageId.THE_ENEMY_IS_OFFLINE_AND_CANNOT_BE_FOUND_RIGHT_NOW);
			return false;
		}
		else if (killer.isTeleporting() || killer.isInsideZone(ZoneId.PEACE) || killer.isInInstance() || killer.isInTimedHuntingZone() || killer.isInsideZone(ZoneId.SIEGE) || killer.isInsideZone(ZoneId.NO_BOOKMARK))
		{
			player.sendPacket(SystemMessageId.THE_CHARACTER_IS_IN_A_LOCATION_WHERE_IT_IS_IMPOSSIBLE_TO_USE_THIS_FUNCTION);
			return false;
		}
		else if (killer.isDead())
		{
			player.sendPacket(SystemMessageId.THE_CHARACTER_IS_IN_A_LOCATION_WHERE_IT_IS_IMPOSSIBLE_TO_USE_THIS_FUNCTION);
			return false;
		}
		else if (player.isInInstance() || player.isInTimedHuntingZone() || player.isInsideZone(ZoneId.SIEGE))
		{
			player.sendPacket(SystemMessageId.THE_CHARACTER_IS_IN_A_LOCATION_WHERE_IT_IS_IMPOSSIBLE_TO_USE_THIS_FUNCTION);
			return false;
		}
		else if (player.isDead())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_TELEPORT_WHILE_YOU_ARE_DEAD);
			return false;
		}
		else if (!player.isInCombat() && !player.isDisabled())
		{
			return true;
		}
		else
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_WHILE_IN_COMBAT);
			return false;
		}
	}

	public void teleportToKiller(Player player, String killerName)
	{
		List<RevengeHistoryHolder> history = REVENGE_HISTORY.get(player.getObjectId());
		if (history != null)
		{
			RevengeHistoryHolder revenge = null;

			for (RevengeHistoryHolder holder : history)
			{
				if (holder.getKillerName().equals(killerName))
				{
					revenge = holder;
					break;
				}
			}

			if (revenge != null)
			{
				if (!revenge.wasShared())
				{
					Player killer = World.getInstance().getPlayer(killerName);
					if (this.checkTeleportConditions(player, killer))
					{
						if (revenge.getTeleportRemaining() > 0)
						{
							int price = TELEPORT_PRICE[Math.min(TELEPORT_PRICE.length - revenge.getTeleportRemaining(), TELEPORT_PRICE.length - 1)];
							if (player.destroyItemByItemId(ItemProcessType.FEE, 91663, price, player, true))
							{
								revenge.setTeleportRemaining(revenge.getTeleportRemaining() - 1);
								HIDE_SKILL.getSkill().applyEffects(player, player);

								for (Summon summon : player.getServitorsAndPets())
								{
									HIDE_SKILL.getSkill().applyEffects(summon, summon);
								}

								player.teleToLocation(killer.getLocation());
							}
						}
					}
				}
			}
		}
	}

	public void teleportToSharedKiller(Player player, String victimName, String killerName)
	{
		if (!player.getName().equals(killerName))
		{
			List<RevengeHistoryHolder> history = REVENGE_HISTORY.get(player.getObjectId());
			if (history != null)
			{
				RevengeHistoryHolder revenge = null;

				for (RevengeHistoryHolder holder : history)
				{
					if (holder.getVictimName().equals(victimName) && holder.getKillerName().equals(killerName))
					{
						revenge = holder;
						break;
					}
				}

				if (revenge != null)
				{
					if (revenge.wasShared())
					{
						Player killer = World.getInstance().getPlayer(killerName);
						if (this.checkTeleportConditions(player, killer))
						{
							if (revenge.getSharedTeleportRemaining() > 0 && player.destroyItemByItemId(ItemProcessType.FEE, 91663, 100L, player, true))
							{
								revenge.setSharedTeleportRemaining(revenge.getSharedTeleportRemaining() - 1);
								HIDE_SKILL.getSkill().applyEffects(player, player);

								for (Summon summon : player.getServitorsAndPets())
								{
									HIDE_SKILL.getSkill().applyEffects(summon, summon);
								}

								player.teleToLocation(killer.getLocation());
							}
						}
					}
				}
			}
		}
	}

	public void requestHelp(Player player, Player killer, int type)
	{
		List<RevengeHistoryHolder> history = REVENGE_HISTORY.get(player.getObjectId());
		if (history != null)
		{
			RevengeHistoryHolder revenge = null;

			for (RevengeHistoryHolder holder : history)
			{
				if (holder.getKillerName().equals(killer.getName()))
				{
					revenge = holder;
					break;
				}
			}

			if (revenge != null)
			{
				if (!revenge.wasShared())
				{
					if (player.reduceAdena(ItemProcessType.FEE, 100000L, player, true))
					{
						long currentTime = System.currentTimeMillis();
						revenge.setShared(true);
						revenge.setType(RevengeType.OWN_HELP_REQUEST);
						revenge.setShareTime(currentTime);
						List<Player> targets = new LinkedList<>();
						if (type == 1)
						{
							Clan clan = player.getClan();
							if (clan != null)
							{
								for (ClanMember member : clan.getMembers())
								{
									if (member.isOnline())
									{
										targets.add(member.getPlayer());
									}
									else
									{
										this.saveToRevengeHistory(player, killer, revenge, currentTime, member.getObjectId());
									}
								}
							}
						}
						else if (type == 2)
						{
							for (Integer playerObjectId : RankManager.getInstance().getTop50())
							{
								Player plr = World.getInstance().getPlayer(playerObjectId);
								if (plr != null)
								{
									targets.add(plr);
								}
								else
								{
									this.saveToRevengeHistory(player, killer, revenge, currentTime, playerObjectId);
								}
							}
						}

						for (Player target : targets)
						{
							if (target != killer)
							{
								int targetObjectId = target.getObjectId();
								this.saveToRevengeHistory(player, killer, revenge, currentTime, targetObjectId);
								target.sendPacket(new ExPvpBookShareRevengeNewRevengeInfo(player.getName(), killer.getName(), RevengeType.HELP_REQUEST));
								target.sendPacket(new ExPvpBookShareRevengeList(target));
							}
						}
					}

					player.sendPacket(new ExPvpBookShareRevengeList(player));
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void saveToRevengeHistory(Player player, Player killer, RevengeHistoryHolder revenge, long currentTime, int objectId)
	{
		List<RevengeHistoryHolder> targetHistory = (List<RevengeHistoryHolder>) (REVENGE_HISTORY.containsKey(objectId) ? REVENGE_HISTORY.get(objectId) : new CopyOnWriteArrayList<>());

		for (RevengeHistoryHolder holder : targetHistory)
		{
			if (holder.getVictimName().equals(player.getName()) && holder.getKillerName().equals(killer.getName()) && holder != revenge)
			{
				targetHistory.remove(holder);
				break;
			}
		}

		targetHistory.add(new RevengeHistoryHolder(killer, player, RevengeType.HELP_REQUEST, 1, revenge.getKillTime(), currentTime));
		REVENGE_HISTORY.put(objectId, targetHistory);
	}

	public Collection<RevengeHistoryHolder> getHistory(Player player)
	{
		return REVENGE_HISTORY.get(player.getObjectId());
	}

	public static RevengeHistoryManager getInstance()
	{
		return RevengeHistoryManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final RevengeHistoryManager INSTANCE = new RevengeHistoryManager();
	}
}
