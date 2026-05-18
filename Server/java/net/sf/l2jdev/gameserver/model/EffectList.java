package net.sf.l2jdev.gameserver.model;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.effects.EffectFlag;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadGameManager;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadGameTask;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.model.skill.AbnormalVisualEffect;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.model.skill.EffectScope;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.SkillBuffType;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillFinishType;
import net.sf.l2jdev.gameserver.network.serverpackets.AbnormalStatusUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.ExAbnormalStatusUpdateFromTarget;
import net.sf.l2jdev.gameserver.network.serverpackets.PartySpelled;
import net.sf.l2jdev.gameserver.network.serverpackets.ShortBuffStatusUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.olympiad.ExOlympiadSpelledInfo;

public class EffectList
{
	private static final Logger LOGGER = Logger.getLogger(EffectList.class.getName());
	private final Queue<BuffInfo> _actives = new ConcurrentLinkedQueue<>();
	private final Set<BuffInfo> _passives = ConcurrentHashMap.newKeySet();
	private final Set<BuffInfo> _options = ConcurrentHashMap.newKeySet();
	private Set<AbnormalType> _stackedEffects = EnumSet.noneOf(AbnormalType.class);
	private final Set<AbnormalType> _blockedAbnormalTypes = EnumSet.noneOf(AbnormalType.class);
	private Set<AbnormalVisualEffect> _abnormalVisualEffects = EnumSet.noneOf(AbnormalVisualEffect.class);
	private BuffInfo _shortBuff = null;
	private final AtomicInteger _buffCount = new AtomicInteger();
	private final AtomicInteger _triggerBuffCount = new AtomicInteger();
	private final AtomicInteger _danceCount = new AtomicInteger();
	private final AtomicInteger _toggleCount = new AtomicInteger();
	private final AtomicInteger _debuffCount = new AtomicInteger();
	private final AtomicInteger _hasBuffsRemovedOnAnyAction = new AtomicInteger();
	private final AtomicInteger _hasBuffsRemovedOnDamage = new AtomicInteger();
	private long _effectFlags;
	private final Creature _owner;
	private final AtomicInteger _hiddenBuffs = new AtomicInteger();
	private ScheduledFuture<?> _updateEffectIconTask;
	private final AtomicBoolean _updateAbnormalStatus = new AtomicBoolean();

	public EffectList(Creature owner)
	{
		this._owner = owner;
	}

	public Set<BuffInfo> getPassives()
	{
		return Collections.unmodifiableSet(this._passives);
	}

	public Set<BuffInfo> getOptions()
	{
		return Collections.unmodifiableSet(this._options);
	}

	public Collection<BuffInfo> getEffects()
	{
		return Collections.unmodifiableCollection(this._actives);
	}

	public List<BuffInfo> getBuffs()
	{
		List<BuffInfo> result = new LinkedList<>();

		for (BuffInfo info : this._actives)
		{
			if (info.getSkill().getBuffType().isBuff())
			{
				result.add(info);
			}
		}

		return result;
	}

	public List<BuffInfo> getDances()
	{
		List<BuffInfo> result = new LinkedList<>();

		for (BuffInfo info : this._actives)
		{
			if (info.getSkill().getBuffType().isDance())
			{
				result.add(info);
			}
		}

		return result;
	}

	public List<BuffInfo> getDebuffs()
	{
		List<BuffInfo> result = new LinkedList<>();

		for (BuffInfo info : this._actives)
		{
			if (info.getSkill().getBuffType().isDebuff())
			{
				result.add(info);
			}
		}

		return result;
	}

	public boolean isAffectedBySkill(int skillId)
	{
		for (BuffInfo info : this._actives)
		{
			if (info.getSkill().getId() == skillId)
			{
				return true;
			}
		}

		for (BuffInfo infox : this._passives)
		{
			if (infox.getSkill().getId() == skillId)
			{
				return true;
			}
		}

		return false;
	}

	public BuffInfo getBuffInfoBySkillId(int skillId)
	{
		for (BuffInfo info : this._actives)
		{
			if (info.getSkill().getId() == skillId)
			{
				return info;
			}
		}

		for (BuffInfo infox : this._passives)
		{
			if (infox.getSkill().getId() == skillId)
			{
				return infox;
			}
		}

		return null;
	}

	public boolean hasAbnormalType(AbnormalType type)
	{
		return this._stackedEffects.contains(type);
	}

	public boolean hasAbnormalType(Collection<AbnormalType> types)
	{
		for (AbnormalType abnormalType : this._stackedEffects)
		{
			if (types.contains(abnormalType))
			{
				return true;
			}
		}

		return false;
	}

	public boolean hasAbnormalType(AbnormalType type, Predicate<BuffInfo> filter)
	{
		if (this.hasAbnormalType(type))
		{
			for (BuffInfo info : this._actives)
			{
				if (info.isAbnormalType(type) && filter.test(info))
				{
					return true;
				}
			}
		}

		return false;
	}

	public BuffInfo getFirstBuffInfoByAbnormalType(AbnormalType type)
	{
		if (this.hasAbnormalType(type))
		{
			for (BuffInfo info : this._actives)
			{
				if (info.isAbnormalType(type))
				{
					return info;
				}
			}
		}

		return null;
	}

	public void addBlockedAbnormalTypes(Set<AbnormalType> blockedAbnormalTypes)
	{
		this._blockedAbnormalTypes.addAll(blockedAbnormalTypes);
	}

	public boolean removeBlockedAbnormalTypes(Set<AbnormalType> blockedBuffSlots)
	{
		return this._blockedAbnormalTypes.removeAll(blockedBuffSlots);
	}

	public Set<AbnormalType> getBlockedAbnormalTypes()
	{
		return Collections.unmodifiableSet(this._blockedAbnormalTypes);
	}

	public void shortBuffStatusUpdate(BuffInfo info)
	{
		if (this._owner.isPlayer())
		{
			this._shortBuff = info;
			if (info == null)
			{
				this._owner.sendPacket(ShortBuffStatusUpdate.RESET_SHORT_BUFF);
			}
			else
			{
				this._owner.sendPacket(new ShortBuffStatusUpdate(info.getSkill().getId(), info.getSkill().getLevel(), info.getSkill().getSubLevel(), info.getTime()));
			}
		}
	}

	public int getBuffCount()
	{
		return !this._actives.isEmpty() ? this._buffCount.get() - this._hiddenBuffs.get() : 0;
	}

	public int getDanceCount()
	{
		return this._danceCount.get();
	}

	public int getTriggeredBuffCount()
	{
		return this._triggerBuffCount.get();
	}

	public int getToggleCount()
	{
		return this._toggleCount.get();
	}

	public int getDebuffCount()
	{
		return this._debuffCount.get();
	}

	public int getHiddenBuffsCount()
	{
		return this._hiddenBuffs.get();
	}

	public void stopAllEffects(boolean broadcast)
	{
		this.stopEffects(b -> !b.getSkill().isIrreplaceableBuff(), true, broadcast);
	}

	public void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		this.stopEffects(info -> !info.getSkill().isStayAfterDeath(), true, true);
	}

	public void stopAllEffectsWithoutExclusions(boolean update, boolean broadcast)
	{
		for (BuffInfo info : this._actives)
		{
			this.remove(info);
		}

		for (BuffInfo info : this._passives)
		{
			this.remove(info);
		}

		for (BuffInfo info : this._options)
		{
			this.remove(info);
		}

		if (update)
		{
			this.updateEffectList(broadcast);
		}
	}

	public void stopAllToggles()
	{
		if (this._toggleCount.get() > 0)
		{
			this.stopEffects(b -> b.getSkill().isToggle() && !b.getSkill().isIrreplaceableBuff(), true, true);
		}
	}

	public void stopAllTogglesOfGroup(int toggleGroup)
	{
		if (this._toggleCount.get() > 0)
		{
			this.stopEffects(b -> b.getSkill().isToggle() && b.getSkill().getToggleGroupId() == toggleGroup, true, true);
		}
	}

	public void stopAllPassives(boolean update, boolean broadcast)
	{
		if (!this._passives.isEmpty())
		{
			this._passives.forEach(this::remove);
			if (update)
			{
				this.updateEffectList(broadcast);
			}
		}
	}

	public void stopAllOptions(boolean update, boolean broadcast)
	{
		if (!this._options.isEmpty())
		{
			this._options.forEach(this::remove);
			if (update)
			{
				this.updateEffectList(broadcast);
			}
		}
	}

	public void stopEffects(EffectFlag effectFlag)
	{
		if (this.isAffected(effectFlag) && !this._actives.isEmpty())
		{
			boolean update = false;

			for (BuffInfo info : this._actives)
			{
				for (AbstractEffect effect : info.getEffects())
				{
					if (effect != null && (effect.getEffectFlags() & effectFlag.getMask()) != 0L)
					{
						this.remove(info);
						update = true;
					}
				}
			}

			if (update)
			{
				this.updateEffectList(true);
			}
		}
	}

	public void stopSkillEffects(SkillFinishType type, int skillId)
	{
		BuffInfo info = this.getBuffInfoBySkillId(skillId);
		if (info != null)
		{
			this.remove(info, type, true, true);
		}
	}

	public void stopSkillEffects(SkillFinishType type, Skill skill)
	{
		this.stopSkillEffects(type, skill.getId());
	}

	public boolean stopEffects(AbnormalType type)
	{
		if (this.hasAbnormalType(type))
		{
			this.stopEffects(i -> i.isAbnormalType(type), true, true);
			return true;
		}
		return false;
	}

	public boolean stopEffects(Collection<AbnormalType> types)
	{
		if (this.hasAbnormalType(types))
		{
			this.stopEffects(i -> types.contains(i.getSkill().getAbnormalType()), true, true);
			return true;
		}
		return false;
	}

	public void stopEffects(Predicate<BuffInfo> filter, boolean update, boolean broadcast)
	{
		if (!this._actives.isEmpty())
		{
			for (BuffInfo info : this._actives)
			{
				if (filter.test(info))
				{
					this.remove(info);
				}
			}

			if (update)
			{
				this.updateEffectList(broadcast);
			}
		}
	}

	public void stopEffectsOnAction()
	{
		if (this._hasBuffsRemovedOnAnyAction.get() > 0)
		{
			this.stopEffects(info -> info.getSkill().isRemovedOnAnyActionExceptMove(), true, true);
		}
	}

	public void stopEffectsOnDamage()
	{
		if (this._hasBuffsRemovedOnDamage.get() > 0)
		{
			this.stopEffects(info -> info.getSkill().isRemovedOnDamage(), true, true);
		}
	}

	private boolean isLimitExceeded(SkillBuffType... buffTypes)
	{
		for (SkillBuffType buffType : buffTypes)
		{
			switch (buffType)
			{
				case TRIGGER:
					if (this._triggerBuffCount.get() > PlayerConfig.TRIGGERED_BUFFS_MAX_AMOUNT)
					{
						return true;
					}
					break;
				case DANCE:
					if (this._danceCount.get() > PlayerConfig.DANCES_MAX_AMOUNT)
					{
						return true;
					}
					break;
				case DEBUFF:
					if (this._debuffCount.get() > 24)
					{
						return true;
					}
					break;
				case BUFF:
					if (this.getBuffCount() > this._owner.getStat().getMaxBuffCount())
					{
						return true;
					}
			}
		}

		return false;
	}

	private int increaseDecreaseCount(BuffInfo info, boolean increase)
	{
		if (!info.isInUse())
		{
			if (increase)
			{
				this._hiddenBuffs.incrementAndGet();
			}
			else
			{
				this._hiddenBuffs.decrementAndGet();
			}
		}

		if (info.getSkill().isRemovedOnAnyActionExceptMove())
		{
			if (increase)
			{
				this._hasBuffsRemovedOnAnyAction.incrementAndGet();
			}
			else
			{
				this._hasBuffsRemovedOnAnyAction.decrementAndGet();
			}
		}

		if (info.getSkill().isRemovedOnDamage())
		{
			if (increase)
			{
				this._hasBuffsRemovedOnDamage.incrementAndGet();
			}
			else
			{
				this._hasBuffsRemovedOnDamage.decrementAndGet();
			}
		}

		switch (info.getSkill().getBuffType())
		{
			case TRIGGER:
				return increase ? this._triggerBuffCount.incrementAndGet() : this._triggerBuffCount.decrementAndGet();
			case DANCE:
				return increase ? this._danceCount.incrementAndGet() : this._danceCount.decrementAndGet();
			case DEBUFF:
				return increase ? this._debuffCount.incrementAndGet() : this._debuffCount.decrementAndGet();
			case BUFF:
				return increase ? this._buffCount.incrementAndGet() : this._buffCount.decrementAndGet();
			case TOGGLE:
				return increase ? this._toggleCount.incrementAndGet() : this._toggleCount.decrementAndGet();
			default:
				return 0;
		}
	}

	private void remove(BuffInfo info)
	{
		this.remove(info, SkillFinishType.REMOVED, false, false);
	}

	public void remove(BuffInfo info, SkillFinishType type, boolean update, boolean broadcast)
	{
		if (info != null)
		{
			if (info.getOption() != null)
			{
				this.removeOption(info, type);
			}
			else if (info.getSkill().isPassive())
			{
				this.removePassive(info, type);
			}
			else
			{
				this.removeActive(info, type);
				if (this._owner.isNpc())
				{
					this.updateEffectList(broadcast);
				}
			}

			if (update)
			{
				this.updateEffectList(broadcast);
			}
		}
	}

	private void removeActive(BuffInfo info, SkillFinishType type)
	{
		if (!this._actives.isEmpty())
		{
			this._actives.remove(info);
			if (info == this._shortBuff)
			{
				this.shortBuffStatusUpdate(null);
			}

			info.stopAllEffects(type);
			this.increaseDecreaseCount(info, false);
			info.getSkill().applyEffectScope(EffectScope.END, info, true, false);
		}
	}

	private void removePassive(BuffInfo info, SkillFinishType type)
	{
		if (!this._passives.isEmpty())
		{
			this._passives.remove(info);
			info.stopAllEffects(type);
		}
	}

	private void removeOption(BuffInfo info, SkillFinishType type)
	{
		if (!this._options.isEmpty())
		{
			this._options.remove(info);
			info.stopAllEffects(type);
		}
	}

	public void add(BuffInfo info)
	{
		if (info != null)
		{
			Skill skill = info.getSkill();
			if (!info.getEffected().isDead() || skill == null || skill.isPassive() || skill.isStayAfterDeath())
			{
				if (skill == null)
				{
					this.addOption(info);
				}
				else if (skill.isPassive())
				{
					this.addPassive(info);
				}
				else
				{
					this.addActive(info);
				}

				this.updateEffectList(true);
			}
		}
	}

	private void addActive(BuffInfo info)
	{
		Skill skill = info.getSkill();
		if (!info.getEffected().isDead() || skill.isStayAfterDeath())
		{
			if (this._blockedAbnormalTypes == null || !this._blockedAbnormalTypes.contains(skill.getAbnormalType()))
			{
				if (skill.isTriggeredSkill())
				{
					BuffInfo triggerInfo = info.getEffected().getEffectList().getBuffInfoBySkillId(skill.getId());
					if (triggerInfo != null && triggerInfo.getSkill().getLevel() >= skill.getLevel())
					{
						return;
					}
				}

				if (info.getEffector() != null)
				{
					if (info.getEffector() != info.getEffected() && skill.hasNegativeEffect())
					{
						if (info.getEffected().isDebuffBlocked() || info.getEffector().isGM() && !info.getEffector().getAccessLevel().canGiveDamage())
						{
							return;
						}

						if (info.getEffector().isPlayer() && info.getEffected().isPlayer() && info.getEffected().isAffected(EffectFlag.DUELIST_FURY) && !info.getEffector().isAffected(EffectFlag.DUELIST_FURY))
						{
							return;
						}
					}

					if (info.getEffected().isBuffBlocked() && !skill.hasNegativeEffect())
					{
						return;
					}
				}

				if (this.hasAbnormalType(skill.getAbnormalType()))
				{
					for (BuffInfo existingInfo : this._actives)
					{
						Skill existingSkill = existingInfo.getSkill();
						if ((skill.getAbnormalType().isNone() && existingSkill.getId() == skill.getId() || !skill.getAbnormalType().isNone() && existingSkill.getAbnormalType() == skill.getAbnormalType()) && (skill.getSubordinationAbnormalType().isNone() || skill.getSubordinationAbnormalType() != existingSkill.getSubordinationAbnormalType() || info.getEffectorObjectId() != 0 && existingInfo.getEffectorObjectId() != 0 && info.getEffectorObjectId() == existingInfo.getEffectorObjectId()))
						{
							if (skill.getAbnormalLevel() >= existingSkill.getAbnormalLevel())
							{
								if ((skill.isAbnormalInstant() || existingSkill.isIrreplaceableBuff()) && skill.getId() != existingSkill.getId())
								{
									existingInfo.setInUse(false);
									this._hiddenBuffs.incrementAndGet();
								}
								else
								{
									this.remove(existingInfo);
								}
							}
							else
							{
								if (!skill.isIrreplaceableBuff())
								{
									return;
								}

								info.setInUse(false);
							}
						}
					}
				}

				this.increaseDecreaseCount(info, true);
				if (this.isLimitExceeded(SkillBuffType.values()))
				{
					for (BuffInfo existingInfox : this._actives)
					{
						if (existingInfox.isInUse() && !skill.is7Signs() && this.isLimitExceeded(existingInfox.getSkill().getBuffType()))
						{
							this.remove(existingInfox);
						}

						if (!this.isLimitExceeded(SkillBuffType.values()))
						{
							break;
						}
					}
				}

				this._actives.add(info);
				info.initializeEffects();
			}
		}
	}

	private void addPassive(BuffInfo info)
	{
		Skill skill = info.getSkill();
		if (!skill.getAbnormalType().isNone())
		{
			LOGGER.warning("Passive " + skill + " with abnormal type: " + skill.getAbnormalType() + "!");
		}

		for (BuffInfo b : this._passives)
		{
			if (b != null && b.getSkill().getId() == skill.getId())
			{
				b.setInUse(false);
				this._passives.remove(b);
			}
		}

		this._passives.add(info);
		info.initializeEffects();
	}

	private void addOption(BuffInfo info)
	{
		if (info.getOption() != null)
		{
			for (BuffInfo b : this._options)
			{
				if (b != null && b.getOption().getId() == info.getOption().getId())
				{
					b.setInUse(false);
					this._options.remove(b);
				}
			}

			this._options.add(info);
			info.initializeEffects();
		}
	}

	public void updateEffectIcons(boolean partyOnly)
	{
		if (!partyOnly)
		{
			this._updateAbnormalStatus.compareAndSet(false, true);
		}

		if (this._updateEffectIconTask == null)
		{
			this._updateEffectIconTask = ThreadPool.schedule(() -> {
				Player player = this._owner.asPlayer();
				if (player != null)
				{
					Party party = player.getParty();
					Optional<AbnormalStatusUpdate> asu = this._owner.isPlayer() && this._updateAbnormalStatus.get() ? Optional.of(new AbnormalStatusUpdate()) : Optional.empty();
					Optional<PartySpelled> ps = party == null && !this._owner.isSummon() ? Optional.empty() : Optional.of(new PartySpelled(this._owner));
					Optional<ExOlympiadSpelledInfo> os = player.isInOlympiadMode() && player.isOlympiadStart() ? Optional.of(new ExOlympiadSpelledInfo(player)) : Optional.empty();
					if (!this._actives.isEmpty())
					{
						for (BuffInfo info : this._actives)
						{
							if (info != null && info.isInUse())
							{
								if (info.getSkill().isHealingPotionSkill())
								{
									this.shortBuffStatusUpdate(info);
								}
								else if (info.isDisplayedForEffected())
								{
									asu.ifPresent(a -> a.addSkill(info));
									ps.filter(_ -> !info.getSkill().isToggle()).ifPresent(p -> p.addSkill(info));
									os.ifPresent(o -> o.addSkill(info));
								}
							}
						}
					}

					asu.ifPresent(this._owner::sendPacket);
					if (party != null)
					{
						ps.ifPresent(party::broadcastPacket);
					}
					else
					{
						ps.ifPresent(player::sendPacket);
					}

					if (os.isPresent())
					{
						OlympiadGameTask game = OlympiadGameManager.getInstance().getOlympiadTask(player.getOlympiadGameId());
						if (game != null && game.isBattleStarted())
						{
							os.ifPresent(game.getStadium()::broadcastPacketToObservers);
						}
					}
				}

				ExAbnormalStatusUpdateFromTarget upd = new ExAbnormalStatusUpdateFromTarget(this._owner);

				for (Creature creature : this._owner.getStatus().getStatusListener())
				{
					if (creature != null && creature.isPlayer())
					{
						creature.sendPacket(upd);
					}
				}

				if (this._owner.isPlayer() && this._owner.getTarget() == this._owner)
				{
					this._owner.sendPacket(upd);
				}

				this._updateAbnormalStatus.set(false);
				this._updateEffectIconTask = null;
			}, 300L);
		}
	}

	public Set<AbnormalVisualEffect> getCurrentAbnormalVisualEffects()
	{
		return this._abnormalVisualEffects;
	}

	public boolean hasAbnormalVisualEffect(AbnormalVisualEffect ave)
	{
		return this._abnormalVisualEffects.contains(ave);
	}

	public void startAbnormalVisualEffect(AbnormalVisualEffect... aves)
	{
		for (AbnormalVisualEffect ave : aves)
		{
			this._abnormalVisualEffects.add(ave);
		}

		this._owner.updateAbnormalVisualEffects();
	}

	public void stopAbnormalVisualEffect(AbnormalVisualEffect... aves)
	{
		for (AbnormalVisualEffect ave : aves)
		{
			this._abnormalVisualEffects.remove(ave);
		}

		this._owner.updateAbnormalVisualEffects();
	}

	public void updateEffectList()
	{
		this.updateEffectList(true);
	}

	private void updateEffectList(boolean broadcast)
	{
		long flags = 0L;
		Set<AbnormalType> abnormalTypeFlags = EnumSet.noneOf(AbnormalType.class);
		Set<AbnormalVisualEffect> abnormalVisualEffectFlags = EnumSet.noneOf(AbnormalVisualEffect.class);
		Set<BuffInfo> unhideBuffs = new HashSet<>();

		for (BuffInfo info : this._actives)
		{
			if (info != null && info.isDisplayedForEffected())
			{
				Skill skill = info.getSkill();
				if (this._hiddenBuffs.get() > 0 && this._stackedEffects.contains(skill.getAbnormalType()))
				{
					if (info.isInUse())
					{
						unhideBuffs.removeIf(b -> b.isAbnormalType(skill.getAbnormalType()));
					}
					else if (!abnormalTypeFlags.contains(skill.getAbnormalType()) || unhideBuffs.removeIf(b -> b.isAbnormalType(skill.getAbnormalType()) && b.getSkill().getAbnormalLevel() <= skill.getAbnormalLevel()))
					{
						unhideBuffs.add(info);
					}
				}

				for (AbstractEffect e : info.getEffects())
				{
					flags |= e.getEffectFlags();
				}

				abnormalTypeFlags.add(skill.getAbnormalType());
				if (skill.hasAbnormalVisualEffects())
				{
					for (AbnormalVisualEffect ave : skill.getAbnormalVisualEffects())
					{
						abnormalVisualEffectFlags.add(ave);
						this._abnormalVisualEffects.add(ave);
					}

					if (broadcast)
					{
						this._owner.updateAbnormalVisualEffects();
					}
				}
			}
		}

		for (BuffInfo infox : this._passives)
		{
			if (infox != null)
			{
				for (AbstractEffect e : infox.getEffects())
				{
					flags |= e.getEffectFlags();
				}

				Skill skillx = infox.getSkill();
				if (skillx.hasAbnormalVisualEffects())
				{
					for (AbnormalVisualEffect ave : skillx.getAbnormalVisualEffects())
					{
						abnormalVisualEffectFlags.add(ave);
						this._abnormalVisualEffects.add(ave);
					}

					if (broadcast)
					{
						this._owner.updateAbnormalVisualEffects();
					}
				}
			}
		}

		this._effectFlags = flags;
		this._stackedEffects = abnormalTypeFlags;
		unhideBuffs.forEach(b -> {
			b.setInUse(true);
			this._hiddenBuffs.decrementAndGet();
		});
		this._owner.getStat().recalculateStats(broadcast);
		if (broadcast)
		{
			if (!abnormalVisualEffectFlags.equals(this._abnormalVisualEffects))
			{
				this._abnormalVisualEffects = abnormalVisualEffectFlags;
				this._owner.updateAbnormalVisualEffects();
			}

			this.updateEffectIcons(false);
		}
	}

	public boolean isAffected(EffectFlag flag)
	{
		return (this._effectFlags & flag.getMask()) != 0L;
	}
}
