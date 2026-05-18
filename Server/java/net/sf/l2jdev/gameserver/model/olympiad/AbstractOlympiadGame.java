package net.sf.l2jdev.gameserver.model.olympiad;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.config.custom.DualboxCheckConfig;
import net.sf.l2jdev.gameserver.managers.AntiFeedManager;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.groups.PartyMessageType;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SkillCoolTime;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.olympiad.ExOlympiadMode;

public abstract class AbstractOlympiadGame
{
	protected static final Logger LOGGER = Logger.getLogger(AbstractOlympiadGame.class.getName());
	protected static final Logger LOGGER_OLYMPIAD = Logger.getLogger("olympiad");
	protected static final String POINTS = "olympiad_points";
	protected static final String COMP_DONE = "competitions_done";
	protected static final String COMP_WON = "competitions_won";
	protected static final String COMP_LOST = "competitions_lost";
	protected static final String COMP_DRAWN = "competitions_drawn";
	protected static final String COMP_DONE_WEEK = "competitions_done_week";
	protected static final String COMP_DONE_WEEK_CLASSED = "competitions_done_week_classed";
	protected static final String COMP_DONE_WEEK_NON_CLASSED = "competitions_done_week_non_classed";
	protected static final String COMP_DONE_WEEK_TEAM = "competitions_done_week_team";
	protected long _startTime = 0L;
	protected boolean _aborted = false;
	protected final int _stadiumId;

	protected AbstractOlympiadGame(int id)
	{
		this._stadiumId = id;
	}

	public boolean isAborted()
	{
		return this._aborted;
	}

	public int getStadiumId()
	{
		return this._stadiumId;
	}

	protected boolean makeCompetitionStart()
	{
		this._startTime = System.currentTimeMillis();
		return !this._aborted;
	}

	protected void addPointsToParticipant(Participant par, int points)
	{
		par.updateStat("olympiad_points", points);
		SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_EARNED_OLYMPIAD_POINTS_X_S2);
		sm.addString(par.getName());
		sm.addInt(points);
		this.broadcastPacket(sm);
	}

	protected void removePointsFromParticipant(Participant par, int points)
	{
		par.updateStat("olympiad_points", -points);
		SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_LOST_OLYMPIAD_POINTS_X_S2);
		sm.addString(par.getName());
		sm.addInt(points);
		this.broadcastPacket(sm);
	}

	protected static SystemMessage checkDefaulted(Player player)
	{
		if (player == null || !player.isOnline())
		{
			return new SystemMessage(SystemMessageId.YOUR_OPPONENT_MADE_HASTE_WITH_THEIR_TAIL_BETWEEN_THEIR_LEGS_THE_MATCH_HAS_BEEN_CANCELLED);
		}
		else if (player.getClient() == null || player.getClient().isDetached())
		{
			return new SystemMessage(SystemMessageId.YOUR_OPPONENT_MADE_HASTE_WITH_THEIR_TAIL_BETWEEN_THEIR_LEGS_THE_MATCH_HAS_BEEN_CANCELLED);
		}
		else if (player.inObserverMode())
		{
			return new SystemMessage(SystemMessageId.YOUR_OPPONENT_DOES_NOT_MEET_THE_REQUIREMENTS_TO_DO_BATTLE_THE_MATCH_HAS_BEEN_CANCELLED);
		}
		else if (player.isDead())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_DEAD_AND_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD);
			sm.addPcName(player);
			player.sendPacket(sm);
			return new SystemMessage(SystemMessageId.YOUR_OPPONENT_DOES_NOT_MEET_THE_REQUIREMENTS_TO_DO_BATTLE_THE_MATCH_HAS_BEEN_CANCELLED);
		}
		else if (player.isSubClassActive())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD_BECAUSE_YOU_HAVE_CHANGED_YOUR_CLASS_TO_SUBCLASS);
			sm.addPcName(player);
			player.sendPacket(sm);
			return new SystemMessage(SystemMessageId.YOUR_OPPONENT_DOES_NOT_MEET_THE_REQUIREMENTS_TO_DO_BATTLE_THE_MATCH_HAS_BEEN_CANCELLED);
		}
		else if (player.isCursedWeaponEquipped())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_THE_OWNER_OF_S2_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD);
			sm.addPcName(player);
			sm.addItemName(player.getCursedWeaponEquippedId());
			player.sendPacket(sm);
			return new SystemMessage(SystemMessageId.YOUR_OPPONENT_DOES_NOT_MEET_THE_REQUIREMENTS_TO_DO_BATTLE_THE_MATCH_HAS_BEEN_CANCELLED);
		}
		else if (!player.isInventoryUnder90(true))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD_AS_THEIR_INVENTORY_IS_FILLED_FOR_MORE_THAN_95);
			sm.addPcName(player);
			player.sendPacket(sm);
			return new SystemMessage(SystemMessageId.YOUR_OPPONENT_DOES_NOT_MEET_THE_REQUIREMENTS_TO_DO_BATTLE_THE_MATCH_HAS_BEEN_CANCELLED);
		}
		else
		{
			return null;
		}
	}

	protected static boolean portPlayerToArena(Participant par, Location loc, int id, Instance instance, OlympiadMode mode)
	{
		Player player = par.getPlayer();
		if (player != null && player.isOnline())
		{
			try
			{
				player.setPvpFlag(0);
				player.setLastLocation();
				if (player.isSitting())
				{
					player.standUp();
				}

				player.setTarget(null);
				player.setOlympiadGameId(id);
				player.setInOlympiadMode(true);
				player.setOlympiadStart(false);
				player.setOlympiadSide(par.getSide());
				player.teleToLocation(loc, instance);
				player.sendPacket(new ExOlympiadMode(mode));
				return true;
			}
			catch (Exception var7)
			{
				LOGGER.log(Level.WARNING, var7.getMessage(), var7);
				return false;
			}
		}
		return false;
	}

	protected void removals(Player player, boolean removeParty)
	{
		try
		{
			if (player == null)
			{
				return;
			}

			player.stopAllEffectsExceptThoseThatLastThroughDeath();
			player.getEffectList().stopEffects(info -> info.getSkill().isBlockedInOlympiad(), true, true);
			Clan clan = player.getClan();
			if (clan != null)
			{
				clan.removeSkillEffects(player);
				if (clan.getCastleId() > 0)
				{
					Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
					if (castle != null)
					{
						castle.removeResidentialSkills(player);
					}
				}

				if (clan.getFortId() > 0)
				{
					Fort fort = FortManager.getInstance().getFortByOwner(clan);
					if (fort != null)
					{
						fort.removeResidentialSkills(player);
					}
				}
			}

			player.abortAttack();
			player.abortCast();
			player.setInvisible(false);
			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());
			if (player.hasSummon())
			{
				Summon pet = player.getPet();
				if (pet != null)
				{
					pet.unSummon(player);
				}

				player.getServitors().values().forEach(s -> {
					s.stopAllEffectsExceptThoseThatLastThroughDeath();
					s.getEffectList().stopEffects(info -> info.getSkill().isBlockedInOlympiad(), true, true);
					s.abortAttack();
					s.abortCast();
				});
			}

			player.stopCubicsByOthers();
			if (removeParty)
			{
				Party party = player.getParty();
				if (party != null)
				{
					party.removePartyMember(player, PartyMessageType.EXPELLED);
				}
			}

			if (player.getAgathionId() > 0)
			{
				player.setAgathionId(0);
				player.broadcastUserInfo();
			}

			player.checkItemRestriction();

			for (Skill skill : player.getAllSkills())
			{
				if (skill.getReuseDelay() <= 900000)
				{
					player.enableSkill(skill);
				}
			}

			player.sendSkillList();
			player.sendPacket(new SkillCoolTime(player));
		}
		catch (Exception var6)
		{
			LOGGER.log(Level.WARNING, var6.getMessage(), var6);
		}
	}

	protected void cleanEffects(Player player)
	{
		try
		{
			player.setOlympiadStart(false);
			player.setTarget(null);
			player.abortAttack();
			player.abortCast();
			player.getAI().setIntention(Intention.IDLE);
			if (player.isDead())
			{
				player.setDead(false);
			}

			player.stopAllEffectsExceptThoseThatLastThroughDeath();
			player.getEffectList().stopEffects(info -> info.getSkill().isBlockedInOlympiad(), true, true);
			player.clearSouls();
			player.clearCharges();
			if (player.getAgathionId() > 0)
			{
				player.setAgathionId(0);
			}

			Summon pet = player.getPet();
			if (pet != null && !pet.isDead())
			{
				pet.setTarget(null);
				pet.abortAttack();
				pet.abortCast();
				pet.getAI().setIntention(Intention.IDLE);
				pet.stopAllEffectsExceptThoseThatLastThroughDeath();
				pet.getEffectList().stopEffects(info -> info.getSkill().isBlockedInOlympiad(), true, true);
			}

			for (Summon s : player.getServitors().values())
			{
				if (!s.isDead())
				{
					s.setTarget(null);
					s.abortAttack();
					s.abortCast();
					s.getAI().setIntention(Intention.IDLE);
					s.stopAllEffectsExceptThoseThatLastThroughDeath();
					s.getEffectList().stopEffects(info -> info.getSkill().isBlockedInOlympiad(), true, true);
				}
			}

			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());
			player.getStatus().startHpMpRegeneration();
		}
		catch (Exception var5)
		{
			LOGGER.log(Level.WARNING, var5.getMessage(), var5);
		}
	}

	protected void playerStatusBack(Player player)
	{
		try
		{
			if (player.isTransformed())
			{
				player.untransform();
			}

			player.setInOlympiadMode(false);
			player.setOlympiadStart(false);
			player.setOlympiadSide(-1);
			player.setOlympiadGameId(-1);
			player.sendPacket(new ExOlympiadMode(OlympiadMode.NONE));
			Clan clan = player.getClan();
			if (clan != null)
			{
				clan.addSkillEffects(player);
				if (clan.getCastleId() > 0)
				{
					Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
					if (castle != null)
					{
						castle.giveResidentialSkills(player);
					}
				}

				if (clan.getFortId() > 0)
				{
					Fort fort = FortManager.getInstance().getFortByOwner(clan);
					if (fort != null)
					{
						fort.giveResidentialSkills(player);
					}
				}

				player.sendSkillList();
			}

			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());
			player.getStatus().startHpMpRegeneration();
			if (DualboxCheckConfig.DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP > 0)
			{
				AntiFeedManager.getInstance().removePlayer(1, player);
			}
		}
		catch (Exception var4)
		{
			LOGGER.log(Level.WARNING, "playerStatusBack()", var4);
		}
	}

	protected void portPlayerBack(Player player)
	{
		if (player != null)
		{
			Location loc = player.getLastLocation();
			if (loc != null)
			{
				player.setIsPendingRevive(false);
				player.teleToLocation(loc, null);
				player.unsetLastLocation();
			}
		}
	}

	public static void rewardParticipant(Player player, List<ItemHolder> list)
	{
		if (player != null && player.isOnline() && !list.isEmpty())
		{
			try
			{
				InventoryUpdate iu = new InventoryUpdate();
				list.forEach(holder -> {
					Item item = player.getInventory().addItem(ItemProcessType.REWARD, holder.getId(), holder.getCount(), player, null);
					if (item != null)
					{
						iu.addModifiedItem(item);
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_X_S2);
						sm.addItemName(item);
						sm.addLong(holder.getCount());
						player.sendPacket(sm);
					}
				});
				player.sendInventoryUpdate(iu);
			}
			catch (Exception var3)
			{
				LOGGER.log(Level.WARNING, var3.getMessage(), var3);
			}
		}
	}

	public abstract CompetitionType getType();

	public abstract String[] getPlayerNames();

	public abstract boolean containsParticipant(int var1);

	public abstract void sendOlympiadInfo(Creature var1);

	public abstract void broadcastOlympiadInfo(OlympiadStadium var1);

	protected abstract void broadcastPacket(ServerPacket var1);

	protected abstract boolean needBuffers();

	protected abstract boolean checkDefaulted();

	protected abstract void removals();

	protected abstract boolean portPlayersToArena(List<Location> var1, Instance var2);

	protected abstract void cleanEffects();

	protected abstract void portPlayersBack();

	protected abstract void playersStatusBack();

	protected abstract void clearPlayers();

	protected abstract void handleDisconnect(Player var1);

	protected abstract void resetDamage();

	protected abstract void addDamage(Player var1, int var2);

	protected abstract boolean checkBattleStatus();

	protected abstract boolean haveWinner();

	protected abstract void validateWinner(OlympiadStadium var1);

	protected abstract int getDivider();

	protected abstract void healPlayers();

	protected abstract void untransformPlayers();

	protected abstract void makePlayersInvul();

	protected abstract void removePlayersInvul();
}
