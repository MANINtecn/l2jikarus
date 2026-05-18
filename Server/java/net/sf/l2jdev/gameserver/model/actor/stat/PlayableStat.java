package net.sf.l2jdev.gameserver.model.actor.stat;

import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.config.FeatureConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.ExperienceData;
import net.sf.l2jdev.gameserver.data.xml.PetDataTable;
import net.sf.l2jdev.gameserver.data.xml.SkillTreeData;
import net.sf.l2jdev.gameserver.model.actor.Playable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayableExpChanged;
import net.sf.l2jdev.gameserver.model.events.returns.TerminateReturn;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExNewSkillToLearnByLevelUp;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class PlayableStat extends CreatureStat
{
	protected static final Logger LOGGER = Logger.getLogger(PlayableStat.class.getName());

	public PlayableStat(Playable player)
	{
		super(player);
	}

	public boolean addExp(long amount)
	{
		Playable playable = this.getActiveChar();
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYABLE_EXP_CHANGED, playable))
		{
			TerminateReturn term = EventDispatcher.getInstance().notifyEvent(new OnPlayableExpChanged(playable, this.getExp(), this.getExp() + amount), playable, TerminateReturn.class);
			if (term != null && term.terminate())
			{
				return false;
			}
		}

		if (this.getExp() + amount >= 0L && (amount <= 0L || this.getExp() != this.getExpForLevel(this.getMaxLevel()) - 1L))
		{
			long value = amount;
			if (this.getExp() + amount >= this.getExpForLevel(this.getMaxLevel()))
			{
				value = this.getExpForLevel(this.getMaxLevel()) - 1L - this.getExp();
			}

			int oldLevel = this.getLevel();
			this.setExp(this.getExp() + value);
			int minimumLevel = 1;
			if (playable.isPet())
			{
				minimumLevel = PetDataTable.getInstance().getPetMinLevel(playable.asPet().getTemplate().getId());
			}

			int level = minimumLevel;

			for (int tmp = minimumLevel; tmp <= this.getMaxLevel(); tmp++)
			{
				if (this.getExp() < this.getExpForLevel(tmp))
				{
					level = --tmp;
					break;
				}
			}

			if (level != this.getLevel() && level >= minimumLevel)
			{
				this.addLevel(level - this.getLevel());
			}

			int newLevel = this.getLevel();
			if (newLevel > oldLevel && playable.isPlayer())
			{
				Player player = playable.asPlayer();
				if (SkillTreeData.getInstance().hasAvailableSkills(player, player.getPlayerClass()))
				{
					player.sendPacket(ExNewSkillToLearnByLevelUp.STATIC_PACKET);
				}

				int lastPledgedLevel = player.getVariables().getInt("LAST_PLEDGE_REPUTATION_LEVEL", 0);
				if (lastPledgedLevel < newLevel)
				{
					int leveledUpCount = newLevel - lastPledgedLevel;
					addReputationToClanBasedOnLevel(player, leveledUpCount);
					player.getVariables().set("LAST_PLEDGE_REPUTATION_LEVEL", newLevel);
				}

				if (!PlayerConfig.DISABLE_TUTORIAL)
				{
					player.sendQuestList();
				}
			}

			return true;
		}
		return true;
	}

	public boolean removeExp(long amount)
	{
		long value = amount;
		if (this.getExp() - amount < this.getExpForLevel(this.getLevel()) && (!PlayerConfig.PLAYER_DELEVEL || PlayerConfig.PLAYER_DELEVEL && this.getLevel() <= PlayerConfig.DELEVEL_MINIMUM))
		{
			value = this.getExp() - this.getExpForLevel(this.getLevel());
		}

		if (this.getExp() - value < 0L)
		{
			value = this.getExp() - 1L;
		}

		this.setExp(this.getExp() - value);
		int minimumLevel = 1;
		Playable playable = this.getActiveChar();
		if (playable.isPet())
		{
			minimumLevel = PetDataTable.getInstance().getPetMinLevel(playable.asPet().getTemplate().getId());
		}

		int level = minimumLevel;

		for (int tmp = minimumLevel; tmp <= this.getMaxLevel(); tmp++)
		{
			if (this.getExp() < this.getExpForLevel(tmp))
			{
				level = --tmp;
				break;
			}
		}

		if (level != this.getLevel() && level >= minimumLevel)
		{
			this.addLevel(level - this.getLevel());
		}

		return true;
	}

	public boolean removeExpAndSp(long removeExp, long removeSp)
	{
		boolean expRemoved = false;
		boolean spRemoved = false;
		if (removeExp > 0L)
		{
			expRemoved = this.removeExp(removeExp);
		}

		if (removeSp > 0L)
		{
			spRemoved = this.removeSp(removeSp);
		}

		return expRemoved || spRemoved;
	}

	public boolean addLevel(int amount)
	{
		int value = amount;
		if (this.getLevel() + amount > this.getMaxLevel() - 1)
		{
			if (this.getLevel() >= this.getMaxLevel() - 1)
			{
				return false;
			}

			value = this.getMaxLevel() - 1 - this.getLevel();
		}

		boolean levelIncreased = this.getLevel() + value > this.getLevel();
		value += this.getLevel();
		this.setLevel(value);
		if (this.getExp() >= this.getExpForLevel(this.getLevel() + 1) || this.getExpForLevel(this.getLevel()) > this.getExp())
		{
			this.setExp(this.getExpForLevel(this.getLevel()));
		}

		Playable playable = this.getActiveChar();
		if (!levelIncreased && playable.isPlayer() && !playable.isGM() && PlayerConfig.DECREASE_SKILL_LEVEL)
		{
			playable.asPlayer().checkPlayerSkills();
		}

		if (!levelIncreased)
		{
			return false;
		}
		playable.getStatus().setCurrentHp(playable.getStat().getMaxHp());
		playable.getStatus().setCurrentMp(playable.getStat().getMaxMp());
		return true;
	}

	public boolean addSp(long amount)
	{
		if (amount < 0L)
		{
			LOGGER.warning("wrong usage");
			return false;
		}
		long currentSp = this.getSp();
		if (currentSp >= PlayerConfig.MAX_SP)
		{
			return false;
		}
		long value = amount;
		if (currentSp > PlayerConfig.MAX_SP - amount)
		{
			value = PlayerConfig.MAX_SP - currentSp;
		}

		this.setSp(currentSp + value);
		return true;
	}

	public boolean removeSp(long amount)
	{
		long currentSp = this.getSp();
		if (currentSp < amount)
		{
			this.setSp(this.getSp() - currentSp);
			return true;
		}
		this.setSp(this.getSp() - amount);
		return true;
	}

	public long getExpForLevel(int level)
	{
		return ExperienceData.getInstance().getExpForLevel(level);
	}

	@Override
	public Playable getActiveChar()
	{
		return super.getActiveChar().asPlayable();
	}

	public int getMaxLevel()
	{
		return ExperienceData.getInstance().getMaxLevel();
	}

	@Override
	public int getPhysicalAttackRadius()
	{
		Weapon weapon = this.getActiveChar().getActiveWeaponItem();
		return weapon != null ? weapon.getBaseAttackRadius() : super.getPhysicalAttackRadius();
	}

	@Override
	public int getPhysicalAttackAngle()
	{
		Playable playable = this.getActiveChar();
		Weapon weapon = playable.getActiveWeaponItem();
		return weapon != null ? weapon.getBaseAttackAngle() + (int) playable.getStat().getValue(Stat.WEAPON_ATTACK_ANGLE_BONUS, 0.0) : super.getPhysicalAttackAngle();
	}

	private static void addReputationToClanBasedOnLevel(Player player, int leveledUpCount)
	{
		Clan clan = player.getClan();
		if (clan != null)
		{
			if (clan.getLevel() >= 3)
			{
				int reputation = 0;

				for (int i = 0; i < leveledUpCount; i++)
				{
					int level = player.getLevel() - i;
					if (level >= 20 && level <= 25)
					{
						reputation += FeatureConfig.LVL_UP_20_AND_25_REP_SCORE;
					}
					else if (level >= 26 && level <= 30)
					{
						reputation += FeatureConfig.LVL_UP_26_AND_30_REP_SCORE;
					}
					else if (level >= 31 && level <= 35)
					{
						reputation += FeatureConfig.LVL_UP_31_AND_35_REP_SCORE;
					}
					else if (level >= 36 && level <= 40)
					{
						reputation += FeatureConfig.LVL_UP_36_AND_40_REP_SCORE;
					}
					else if (level >= 41 && level <= 45)
					{
						reputation += FeatureConfig.LVL_UP_41_AND_45_REP_SCORE;
					}
					else if (level >= 46 && level <= 50)
					{
						reputation += FeatureConfig.LVL_UP_46_AND_50_REP_SCORE;
					}
					else if (level >= 51 && level <= 55)
					{
						reputation += FeatureConfig.LVL_UP_51_AND_55_REP_SCORE;
					}
					else if (level >= 56 && level <= 60)
					{
						reputation += FeatureConfig.LVL_UP_56_AND_60_REP_SCORE;
					}
					else if (level >= 61 && level <= 65)
					{
						reputation += FeatureConfig.LVL_UP_61_AND_65_REP_SCORE;
					}
					else if (level >= 66 && level <= 70)
					{
						reputation += FeatureConfig.LVL_UP_66_AND_70_REP_SCORE;
					}
					else if (level >= 71 && level <= 75)
					{
						reputation += FeatureConfig.LVL_UP_71_AND_75_REP_SCORE;
					}
					else if (level >= 76 && level <= 80)
					{
						reputation += FeatureConfig.LVL_UP_76_AND_80_REP_SCORE;
					}
					else if (level >= 81 && level <= 90)
					{
						reputation += FeatureConfig.LVL_UP_81_AND_90_REP_SCORE;
					}
					else if (level >= 91 && level <= 120)
					{
						reputation += FeatureConfig.LVL_UP_91_PLUS_REP_SCORE;
					}
				}

				if (reputation != 0)
				{
					reputation = (int) Math.ceil(reputation * FeatureConfig.LVL_OBTAINED_REP_SCORE_MULTIPLIER);
					clan.addReputationScore(reputation);

					for (ClanMember member : clan.getMembers())
					{
						if (member.isOnline())
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_REPUTATION_POINTS_S1);
							sm.addInt(reputation);
							member.getPlayer().sendPacket(sm);
						}
					}
				}
			}
		}
	}
}
