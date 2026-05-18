package net.sf.l2jdev.gameserver.model.olympiad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.gameserver.config.OlympiadConfig;
import net.sf.l2jdev.gameserver.config.custom.DualboxCheckConfig;
import net.sf.l2jdev.gameserver.data.enums.CategoryType;
import net.sf.l2jdev.gameserver.managers.AntiFeedManager;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class OlympiadManager
{
	private final Set<Integer> _nonClassBasedRegisters = ConcurrentHashMap.newKeySet();
	private final Map<Integer, Set<Integer>> _classBasedRegisters = new ConcurrentHashMap<>();

	protected OlympiadManager()
	{
	}

	public static OlympiadManager getInstance()
	{
		return OlympiadManager.SingletonHolder.INSTANCE;
	}

	public Set<Integer> getRegisteredNonClassBased()
	{
		return this._nonClassBasedRegisters;
	}

	public Map<Integer, Set<Integer>> getRegisteredClassBased()
	{
		return this._classBasedRegisters;
	}

	protected final List<Set<Integer>> hasEnoughRegisteredClassed()
	{
		List<Set<Integer>> result = null;

		for (Entry<Integer, Set<Integer>> classList : this._classBasedRegisters.entrySet())
		{
			if (classList.getValue() != null && classList.getValue().size() >= OlympiadConfig.OLYMPIAD_CLASSED)
			{
				if (result == null)
				{
					result = new ArrayList<>();
				}

				result.add(classList.getValue());
			}
		}

		return result;
	}

	protected final boolean hasEnoughRegisteredNonClassed()
	{
		return this._nonClassBasedRegisters.size() >= OlympiadConfig.OLYMPIAD_NONCLASSED;
	}

	protected void clearRegistered()
	{
		this._nonClassBasedRegisters.clear();
		this._classBasedRegisters.clear();
		AntiFeedManager.getInstance().clear(1);
	}

	public boolean isRegistered(Player noble)
	{
		return this.isRegistered(noble, noble, false);
	}

	private boolean isRegistered(Player noble, Player player, boolean showMessage)
	{
		Integer objId = noble.getObjectId();
		if (this._nonClassBasedRegisters.contains(objId))
		{
			if (showMessage)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_ALREADY_REGISTERED_FOR_ALL_CLASS_BATTLES);
				sm.addPcName(noble);
				player.sendPacket(sm);
			}

			return true;
		}
		Set<Integer> classed = this._classBasedRegisters.get(this.getClassGroup(noble));
		if (classed != null && classed.contains(objId))
		{
			if (showMessage)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_ALREADY_REGISTERED_ON_THE_CLASS_MATCH_WAITING_LIST);
				sm.addPcName(noble);
				player.sendPacket(sm);
			}

			return true;
		}
		return false;
	}

	public boolean isRegisteredInComp(Player noble)
	{
		return this.isRegistered(noble, noble, false) || this.isInCompetition(noble, noble, false);
	}

	public boolean isInCompetition(Player noble, Player player, boolean showMessage)
	{
		if (!Olympiad._inCompPeriod)
		{
			return false;
		}
		int i = OlympiadGameManager.getInstance().getNumberOfStadiums();

		while (--i >= 0)
		{
			AbstractOlympiadGame game = OlympiadGameManager.getInstance().getOlympiadTask(i).getGame();
			if (game != null && game.containsParticipant(noble.getObjectId()))
			{
				if (!showMessage)
				{
					return true;
				}

				switch (game.getType())
				{
					case CLASSED:
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_ALREADY_REGISTERED_ON_THE_CLASS_MATCH_WAITING_LIST);
						sm.addPcName(noble);
						player.sendPacket(sm);
						break;
					}
					case NON_CLASSED:
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_ALREADY_REGISTERED_FOR_ALL_CLASS_BATTLES);
						sm.addPcName(noble);
						player.sendPacket(sm);
					}
				}

				return true;
			}
		}

		return false;
	}

	public boolean registerNoble(Player player, CompetitionType type)
	{
		if (!Olympiad._inCompPeriod)
		{
			player.sendPacket(SystemMessageId.THE_OLYMPIAD_IS_NOT_HELD_RIGHT_NOW);
			return false;
		}
		else if (Olympiad.getInstance().getMillisToCompEnd() < 1200000L)
		{
			player.sendPacket(SystemMessageId.GAME_PARTICIPATION_REQUEST_MUST_BE_FILED_NOT_EARLIER_THAN_10_MIN_AFTER_THE_GAME_ENDS);
			return false;
		}
		else
		{
			int charId = player.getObjectId();
			if (Olympiad.getInstance().getRemainingWeeklyMatches(charId) < 1)
			{
				player.sendPacket(SystemMessageId.THE_MAXIMUM_NUMBER_OF_MATCHES_YOU_CAN_PARTICIPATE_IN_1_WEEK_IS_25);
				return false;
			}
			else if (!this.isRegistered(player, player, true) && !this.isInCompetition(player, player, true))
			{
				StatSet statDat = Olympiad.getNobleStats(charId);
				if (statDat == null)
				{
					statDat = new StatSet();
					statDat.set("class_id", player.getBaseClass());
					statDat.set("char_name", player.getName());
					statDat.set("olympiad_points", Olympiad.DEFAULT_POINTS);
					statDat.set("competitions_done", 0);
					statDat.set("competitions_won", 0);
					statDat.set("competitions_lost", 0);
					statDat.set("competitions_drawn", 0);
					statDat.set("competitions_done_week", 0);
					statDat.set("to_save", true);
					Olympiad.addNobleStats(charId, statDat);
				}

				switch (type)
				{
					case CLASSED:
						if (player.isRegisteredOnEvent())
						{
							player.sendMessage("You can't join olympiad while participating on an event.");
							return false;
						}

						if (DualboxCheckConfig.DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP > 0 && !AntiFeedManager.getInstance().tryAddPlayer(1, player, DualboxCheckConfig.DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP))
						{
							NpcHtmlMessage message = new NpcHtmlMessage(player.getLastHtmlActionOriginId());
							message.setFile(player, "data/html/mods/OlympiadIPRestriction.htm");
							message.replace("%max%", String.valueOf(AntiFeedManager.getInstance().getLimit(player, DualboxCheckConfig.DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP)));
							player.sendPacket(message);
							return false;
						}

						this._classBasedRegisters.computeIfAbsent(this.getClassGroup(player), _ -> ConcurrentHashMap.newKeySet()).add(charId);
						player.sendPacket(SystemMessageId.YOU_VE_BEEN_REGISTERED_FOR_THE_OLYMPIAD_CLASS_MATCHES);
						break;
					case NON_CLASSED:
						if (player.isRegisteredOnEvent())
						{
							player.sendMessage("You can't join olympiad while participating on an event.");
							return false;
						}

						if (DualboxCheckConfig.DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP > 0 && !AntiFeedManager.getInstance().tryAddPlayer(1, player, DualboxCheckConfig.DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP))
						{
							NpcHtmlMessage message = new NpcHtmlMessage(player.getLastHtmlActionOriginId());
							message.setFile(player, "data/html/mods/OlympiadIPRestriction.htm");
							message.replace("%max%", String.valueOf(AntiFeedManager.getInstance().getLimit(player, DualboxCheckConfig.DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP)));
							player.sendPacket(message);
							return false;
						}

						this._nonClassBasedRegisters.add(charId);
						player.sendPacket(SystemMessageId.YOU_HAVE_REGISTERED_IN_THE_WORLD_OLYMPIAD);
				}

				return true;
			}
			else
			{
				return false;
			}
		}
	}

	public boolean unRegisterNoble(Player noble)
	{
		if (!Olympiad._inCompPeriod)
		{
			noble.sendPacket(SystemMessageId.THE_OLYMPIAD_IS_NOT_HELD_RIGHT_NOW);
			return false;
		}
		else if ((noble.isInCategory(CategoryType.THIRD_CLASS_GROUP) || noble.isInCategory(CategoryType.FOURTH_CLASS_GROUP)) && noble.getLevel() >= 55)
		{
			if (!this.isRegistered(noble, noble, false))
			{
				noble.sendPacket(SystemMessageId.YOU_ARE_NOT_CURRENTLY_REGISTERED_FOR_THE_OLYMPIAD);
				return false;
			}
			else if (this.isInCompetition(noble, noble, false))
			{
				return false;
			}
			else
			{
				Integer objId = noble.getObjectId();
				if (this._nonClassBasedRegisters.remove(objId))
				{
					if (DualboxCheckConfig.DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP > 0)
					{
						AntiFeedManager.getInstance().removePlayer(1, noble);
					}

					noble.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REMOVED_FROM_THE_OLYMPIAD_WAITING_LIST);
					return true;
				}
				Set<Integer> classed = this._classBasedRegisters.get(this.getClassGroup(noble));
				if (classed != null && classed.remove(objId))
				{
					if (DualboxCheckConfig.DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP > 0)
					{
						AntiFeedManager.getInstance().removePlayer(1, noble);
					}

					noble.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REMOVED_FROM_THE_OLYMPIAD_WAITING_LIST);
					return true;
				}
				return false;
			}
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_DOES_NOT_MEET_THE_REQUIREMENTS_ONLY_CHARACTERS_WITH_THE_2ND_CLASS_CHANGE_COMPLETED_CAN_TAKE_PART_IN_THE_OLYMPIAD);
			sm.addString(noble.getName());
			noble.sendPacket(sm);
			return false;
		}
	}

	public void removeDisconnectedCompetitor(Player player)
	{
		OlympiadGameTask task = OlympiadGameManager.getInstance().getOlympiadTask(player.getOlympiadGameId());
		if (task != null && task.isGameStarted())
		{
			task.getGame().handleDisconnect(player);
		}

		Integer objId = player.getObjectId();
		if (!this._nonClassBasedRegisters.remove(objId))
		{
			this._classBasedRegisters.getOrDefault(this.getClassGroup(player), Collections.emptySet()).remove(objId);
		}
	}

	public int getCountOpponents()
	{
		return this._nonClassBasedRegisters.size() + this._classBasedRegisters.size();
	}

	public int getClassGroup(Player player)
	{
		return player.getBaseClass();
	}

	private static class SingletonHolder
	{
		protected static final OlympiadManager INSTANCE = new OlympiadManager();
	}
}
