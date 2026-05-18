package net.sf.l2jdev.gameserver.model.actor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.gameserver.ai.Action;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.holders.player.AutoUseSettingsHolder;
import net.sf.l2jdev.gameserver.model.actor.stat.PlayableStat;
import net.sf.l2jdev.gameserver.model.actor.status.PlayableStatus;
import net.sf.l2jdev.gameserver.model.actor.templates.CreatureTemplate;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanWar;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanWarState;
import net.sf.l2jdev.gameserver.model.effects.EffectFlag;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureDeath;
import net.sf.l2jdev.gameserver.model.events.returns.TerminateReturn;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.script.QuestState;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.serverpackets.AbnormalStatusUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.EtcStatusUpdate;

public abstract class Playable extends Creature
{
	private Creature _lockedTarget = null;
	private Player transferDmgTo = null;
	private final Map<Integer, Integer> _replacedSkills = new ConcurrentHashMap<>(1);
	private final Map<Integer, Integer> _originalSkills = new ConcurrentHashMap<>(1);

	public Playable(int objectId, CreatureTemplate template)
	{
		super(objectId, template);
		this.setInstanceType(InstanceType.Playable);
		this.setInvul(false);
	}

	public Playable(CreatureTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.Playable);
		this.setInvul(false);
	}

	@Override
	public PlayableStat getStat()
	{
		return (PlayableStat) super.getStat();
	}

	@Override
	public void initCharStat()
	{
		this.setStat(new PlayableStat(this));
	}

	@Override
	public PlayableStatus getStatus()
	{
		return (PlayableStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		this.setStatus(new PlayableStatus(this));
	}

	@Override
	public boolean doDie(Creature killer)
	{
		if (EventDispatcher.getInstance().hasListener(EventType.ON_CREATURE_DEATH, this))
		{
			TerminateReturn returnBack = EventDispatcher.getInstance().notifyEvent(new OnCreatureDeath(killer, this), this, TerminateReturn.class);
			if (returnBack != null && returnBack.terminate())
			{
				return false;
			}
		}

		synchronized (this)
		{
			if (this.isDead())
			{
				return false;
			}

			this.setCurrentHp(0.0);
			this.setDead(true);
		}

		this.setTarget(null);
		this.abortAttack();
		this.abortCast();
		this.stopMove(null);
		this.getStatus().stopHpMpRegeneration();
		boolean deleteBuffs = true;
		if (this.isNoblesseBlessedAffected())
		{
			this.stopEffects(EffectFlag.NOBLESS_BLESSING);
			deleteBuffs = false;
		}

		if (this.isResurrectSpecialAffected())
		{
			this.stopEffects(EffectFlag.RESURRECTION_SPECIAL);
			deleteBuffs = false;
		}

		Player player = this.asPlayer();
		if (this.isPlayer() && player.hasCharmOfCourage())
		{
			if (player.isInSiege())
			{
				player.reviveRequest(player, false, 0, 0, 0, 0);
			}

			player.setCharmOfCourage(false);
			player.sendPacket(new EtcStatusUpdate(player));
		}

		if (deleteBuffs)
		{
			this.stopAllEffectsExceptThoseThatLastThroughDeath();
		}

		this.broadcastStatusUpdate();
		ZoneManager.getInstance().getRegion(this).onDeath(this);
		if (!player.isNotifyQuestOfDeathEmpty())
		{
			for (QuestState qs : player.getNotifyQuestOfDeath())
			{
				qs.getQuest().onDeath((Creature) (killer == null ? this : killer), this, qs);
			}
		}

		if (this.isPlayer())
		{
			Instance instance = this.getInstanceWorld();
			if (instance != null)
			{
				instance.onDeath(player);
			}
		}

		if (killer != null)
		{
			Player killerPlayer = killer.asPlayer();
			if (killerPlayer != null)
			{
				killerPlayer.onPlayerKill(this);
			}
		}

		this.getAI().notifyAction(Action.DEATH);
		return true;
	}

	public boolean checkIfPvP(Player target)
	{
		Player player = this.asPlayer();
		if (player != null && target != null && player != target && target.getReputation() >= 0 && target.getPvpFlag() <= 0 && !target.isOnDarkSide())
		{
			if (player.isInParty() && player.getParty().containsPlayer(target))
			{
				return false;
			}
			Clan playerClan = player.getClan();
			if (playerClan != null && !player.isAcademyMember() && !target.isAcademyMember())
			{
				ClanWar war = playerClan.getWarWith(target.getClanId());
				return war != null && war.getState() == ClanWarState.MUTUAL;
			}
			return false;
		}
		return true;
	}

	@Override
	public boolean canBeAttacked()
	{
		return true;
	}

	public boolean isNoblesseBlessedAffected()
	{
		return this.isAffected(EffectFlag.NOBLESS_BLESSING);
	}

	public boolean isResurrectSpecialAffected()
	{
		return this.isAffected(EffectFlag.RESURRECTION_SPECIAL);
	}

	public boolean isSilentMovingAffected()
	{
		return this.isAffected(EffectFlag.SILENT_MOVE);
	}

	public boolean isProtectionBlessingAffected()
	{
		return this.isAffected(EffectFlag.PROTECTION_BLESSING);
	}

	@Override
	public void updateEffectIcons(boolean partyOnly)
	{
		this.getEffectList().updateEffectIcons(partyOnly);
	}

	public boolean isLockedTarget()
	{
		return this._lockedTarget != null;
	}

	public Creature getLockedTarget()
	{
		return this._lockedTarget;
	}

	public void setLockedTarget(Creature creature)
	{
		this._lockedTarget = creature;
	}

	public void setTransferDamageTo(Player val)
	{
		this.transferDmgTo = val;
	}

	public Player getTransferingDamageTo()
	{
		return this.transferDmgTo;
	}

	public void addReplacedSkill(int originalId, int replacementId)
	{
		this._replacedSkills.put(originalId, replacementId);
		this._originalSkills.put(replacementId, originalId);
		Skill knownSkill = this.getKnownSkill(originalId);
		if (knownSkill != null)
		{
			Player player = this.asPlayer();
			AutoUseSettingsHolder autoUseSettings = player.getAutoUseSettings();
			if (knownSkill.hasNegativeEffect())
			{
				List<Integer> autoSkills = autoUseSettings.getAutoSkills();
				if (autoSkills.contains(originalId))
				{
					autoSkills.add(replacementId);
					autoSkills.remove(Integer.valueOf(originalId));
				}
			}
			else
			{
				Collection<Integer> autoBuffs = autoUseSettings.getAutoBuffs();
				if (autoBuffs.contains(originalId))
				{
					autoBuffs.add(replacementId);
					autoBuffs.remove(originalId);
				}
			}

			if (knownSkill.isContinuous() && this.isAffectedBySkill(originalId))
			{
				int abnormalTime = 0;

				for (BuffInfo info : this.getEffectList().getEffects())
				{
					if (info.getSkill().getId() == originalId)
					{
						abnormalTime = info.getAbnormalTime();
						break;
					}
				}

				if (abnormalTime > 2000)
				{
					Skill replacementkill = this.getKnownSkill(replacementId);
					if (replacementkill != null)
					{
						replacementkill.applyEffects(this, this);
						AbnormalStatusUpdate asu = new AbnormalStatusUpdate();

						for (BuffInfo infox : this.getEffectList().getEffects())
						{
							if (infox.getSkill().getId() == replacementId)
							{
								infox.resetAbnormalTime(abnormalTime);
								asu.addSkill(infox);
							}
						}

						player.sendPacket(asu);
					}
				}
			}

			this.removeSkill(knownSkill, false);
			player.sendSkillList();
		}
	}

	public void removeReplacedSkill(int originalId)
	{
		Integer replacementId = this._replacedSkills.remove(originalId);
		if (replacementId != null)
		{
			this._originalSkills.remove(replacementId);
			Skill knownSkill = this.getKnownSkill(replacementId);
			if (knownSkill != null)
			{
				Player player = this.asPlayer();
				AutoUseSettingsHolder autoUseSettings = player.getAutoUseSettings();
				if (knownSkill.hasNegativeEffect())
				{
					List<Integer> autoSkills = autoUseSettings.getAutoSkills();
					if (autoSkills.contains(replacementId))
					{
						autoSkills.add(originalId);
						autoSkills.remove(Integer.valueOf(replacementId));
					}
				}
				else
				{
					Collection<Integer> autoBuffs = autoUseSettings.getAutoBuffs();
					if (autoBuffs.contains(replacementId))
					{
						autoBuffs.add(originalId);
						autoBuffs.remove(replacementId);
					}
				}

				if (knownSkill.isContinuous() && this.isAffectedBySkill(replacementId))
				{
					int abnormalTime = 0;

					for (BuffInfo info : this.getEffectList().getEffects())
					{
						if (info.getSkill().getId() == replacementId)
						{
							abnormalTime = info.getAbnormalTime();
							break;
						}
					}

					if (abnormalTime > 2000)
					{
						Skill originalskill = this.getKnownSkill(originalId);
						if (originalskill != null)
						{
							originalskill.applyEffects(this, this);
							AbnormalStatusUpdate asu = new AbnormalStatusUpdate();

							for (BuffInfo infox : this.getEffectList().getEffects())
							{
								if (infox.getSkill().getId() == originalId)
								{
									infox.resetAbnormalTime(abnormalTime);
									asu.addSkill(infox);
								}
							}

							player.sendPacket(asu);
						}
					}
				}

				this.removeSkill(knownSkill, false);
				player.sendSkillList();
			}
		}
	}

	public int getReplacementSkill(int originalId)
	{
		if (this._replacedSkills == null)
		{
			return originalId;
		}
		int replacedSkillId = originalId;

		while (true)
		{
			Integer nextId = this._replacedSkills.get(replacedSkillId);
			if (nextId == null || nextId == replacedSkillId)
			{
				return replacedSkillId;
			}

			replacedSkillId = nextId;
		}
	}

	public int getOriginalSkill(int replacementId)
	{
		if (this._originalSkills == null)
		{
			return replacementId;
		}
		int originalSkillId = replacementId;

		while (true)
		{
			Integer nextId = this._originalSkills.get(originalSkillId);
			if (nextId == null || nextId == originalSkillId)
			{
				return originalSkillId;
			}

			originalSkillId = nextId;
		}
	}

	public Collection<Integer> getReplacedSkills()
	{
		return this._replacedSkills.keySet();
	}

	public abstract void doPickupItem(WorldObject var1);

	public abstract boolean useMagic(Skill var1, Item var2, boolean var3, boolean var4);

	public abstract void storeMe();

	public abstract void storeEffect(boolean var1);

	public abstract void restoreEffects();

	public boolean isOnEvent()
	{
		return false;
	}

	@Override
	public boolean isPlayable()
	{
		return true;
	}

	@Override
	public Playable asPlayable()
	{
		return this;
	}
}
