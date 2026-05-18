package net.sf.l2jdev.gameserver.model.actor.stat;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Position;
import net.sf.l2jdev.gameserver.model.actor.holders.creature.DelayedPumpHolder;
import net.sf.l2jdev.gameserver.model.actor.instance.Guardian;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.SkillConditionScope;
import net.sf.l2jdev.gameserver.model.stats.Formulas;
import net.sf.l2jdev.gameserver.model.stats.MoveType;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.model.stats.TraitType;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.util.MathUtil;

public class CreatureStat
{
	private final Creature _creature;
	private long _exp = 0L;
	private long _sp = 0L;
	private int _level = 1;
	private int _maxBuffCount = PlayerConfig.BUFFS_MAX_AMOUNT;
	private double _vampiricSum = 0.0;
	private double _mpVampiricSum = 0.0;
	private final Map<Stat, Double> _statsAdd = new EnumMap<>(Stat.class);
	private final Map<Stat, Double> _statsMul = new EnumMap<>(Stat.class);
	private final Map<Stat, Map<MoveType, Double>> _moveTypeStats = new ConcurrentHashMap<>();
	private final Map<Integer, Double> _reuseStat = new ConcurrentHashMap<>();
	private final Map<Integer, Double> _mpConsumeStat = new ConcurrentHashMap<>();
	private final Map<Integer, LinkedList<Double>> _skillEvasionStat = new ConcurrentHashMap<>();
	private final Map<Stat, Map<Position, Double>> _positionStats = new ConcurrentHashMap<>();
	private final Map<Stat, Double> _fixedValue = new ConcurrentHashMap<>();
	private final float[] _attackTraitValues = new float[TraitType.values().length];
	private final float[] _defenceTraitValues = new float[TraitType.values().length];
	private final Set<TraitType> _attackTraits = EnumSet.noneOf(TraitType.class);
	private final Set<TraitType> _defenceTraits = EnumSet.noneOf(TraitType.class);
	private final Set<TraitType> _invulnerableTraits = EnumSet.noneOf(TraitType.class);
	private double _attackSpeedMultiplier = 1.0;
	private double _mAttackSpeedMultiplier = 1.0;
	private ScheduledFuture<?> _recalculateStatsTask = null;
	private final AtomicBoolean _broadcast = new AtomicBoolean();
	private final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();

	public CreatureStat(Creature creature)
	{
		this._creature = creature;

		for (int i = 0; i < TraitType.values().length; i++)
		{
			this._attackTraitValues[i] = 1.0F;
			this._defenceTraitValues[i] = 0.0F;
		}
	}

	public int getAccuracy()
	{
		return (int) this.getValue(Stat.ACCURACY_COMBAT);
	}

	public int getCpRegen()
	{
		return (int) this.getValue(Stat.REGENERATE_CP_RATE);
	}

	public int getHpRegen()
	{
		return (int) this.getValue(Stat.REGENERATE_HP_RATE);
	}

	public int getMpRegen()
	{
		return (int) this.getValue(Stat.REGENERATE_MP_RATE);
	}

	public int getMagicAccuracy()
	{
		return (int) this.getValue(Stat.ACCURACY_MAGIC);
	}

	public Creature getActiveChar()
	{
		return this._creature;
	}

	public double getAttackSpeedMultiplier()
	{
		return this._attackSpeedMultiplier;
	}

	public double getMAttackSpeedMultiplier()
	{
		return this._mAttackSpeedMultiplier;
	}

	public int getCON()
	{
		return (int) this.getValue(Stat.STAT_CON);
	}

	public double getCriticalDmg(double init)
	{
		return this.getValue(Stat.CRITICAL_DAMAGE, init);
	}

	public int getCriticalHit()
	{
		return (int) this.getValue(Stat.CRITICAL_RATE);
	}

	public int getPSkillCriticalRate()
	{
		return (int) this.getValue(Stat.CRITICAL_RATE_SKILL);
	}

	public int getDEX()
	{
		return (int) this.getValue(Stat.STAT_DEX);
	}

	public int getEvasionRate()
	{
		return (int) this.getValue(Stat.EVASION_RATE);
	}

	public int getMagicEvasionRate()
	{
		return (int) this.getValue(Stat.MAGIC_EVASION_RATE);
	}

	public long getExp()
	{
		return this._exp;
	}

	public void setExp(long value)
	{
		this._exp = value;
	}

	public int getINT()
	{
		return (int) this.getValue(Stat.STAT_INT);
	}

	public int getLevel()
	{
		return this._level;
	}

	public void setLevel(int value)
	{
		this._level = value;
	}

	public int getMagicalAttackRange(Skill skill)
	{
		if (skill != null)
		{
			int range = skill.getCastRange();
			return range > 0 ? range + (int) this.getValue(Stat.MAGIC_ATTACK_RANGE, 0.0) : range;
		}
		return this._creature.getTemplate().getBaseAttackRange();
	}

	public int getMaxCp()
	{
		return (int) this.getValue(Stat.MAX_CP);
	}

	public int getMaxRecoverableCp()
	{
		return (int) this.getValue(Stat.MAX_RECOVERABLE_CP, this.getMaxCp());
	}

	public long getMaxHp()
	{
		return (long) this.getValue(Stat.MAX_HP);
	}

	public long getMaxRecoverableHp()
	{
		return (long) this.getValue(Stat.MAX_RECOVERABLE_HP, this.getMaxHp());
	}

	public int getMaxMp()
	{
		return (int) this.getValue(Stat.MAX_MP);
	}

	public int getMaxRecoverableMp()
	{
		return (int) this.getValue(Stat.MAX_RECOVERABLE_MP, this.getMaxMp());
	}

	public int getMAtk()
	{
		return (int) this.getValue(Stat.MAGIC_ATTACK);
	}

	public int getWeaponBonusMAtk()
	{
		return (int) this.getValue(Stat.WEAPON_BONUS_MAGIC_ATTACK);
	}

	public int getMAtkSpd()
	{
		return (int) this.getValue(Stat.MAGIC_ATTACK_SPEED);
	}

	public int getMCriticalHit()
	{
		return (int) this.getValue(Stat.MAGIC_CRITICAL_RATE);
	}

	public int getMDef()
	{
		return (int) this.getValue(Stat.MAGICAL_DEFENCE);
	}

	public int getMEN()
	{
		return (int) this.getValue(Stat.STAT_MEN);
	}

	public double getMovementSpeedMultiplier()
	{
		double baseSpeed;
		if (this._creature.isInsideZone(ZoneId.WATER))
		{
			baseSpeed = this._creature.getTemplate().getBaseValue(this._creature.isRunning() ? Stat.SWIM_RUN_SPEED : Stat.SWIM_WALK_SPEED, 0.0);
		}
		else
		{
			baseSpeed = this._creature.getTemplate().getBaseValue(this._creature.isRunning() ? Stat.RUN_SPEED : Stat.WALK_SPEED, 0.0);
		}

		return this.getMoveSpeed() * (1.0 / baseSpeed);
	}

	public double getRunSpeed()
	{
		return this.getValue(this._creature.isInsideZone(ZoneId.WATER) ? Stat.SWIM_RUN_SPEED : Stat.RUN_SPEED);
	}

	public double getWalkSpeed()
	{
		return this.getValue(this._creature.isInsideZone(ZoneId.WATER) ? Stat.SWIM_WALK_SPEED : Stat.WALK_SPEED);
	}

	public double getSwimRunSpeed()
	{
		return this.getValue(Stat.SWIM_RUN_SPEED);
	}

	public double getSwimWalkSpeed()
	{
		return this.getValue(Stat.SWIM_WALK_SPEED);
	}

	public double getMoveSpeed()
	{
		if (this._creature.isInsideZone(ZoneId.WATER))
		{
			return this._creature.isRunning() ? this.getSwimRunSpeed() : this.getSwimWalkSpeed();
		}
		return this._creature.isRunning() ? this.getRunSpeed() : this.getWalkSpeed();
	}

	public int getPAtk()
	{
		return (int) this.getValue(Stat.PHYSICAL_ATTACK);
	}

	public int getWeaponBonusPAtk()
	{
		return (int) this.getValue(Stat.WEAPON_BONUS_PHYSICAL_ATTACK);
	}

	public int getPAtkSpd()
	{
		return (int) this.getValue(Stat.PHYSICAL_ATTACK_SPEED);
	}

	public int getPDef()
	{
		return (int) this.getValue(Stat.PHYSICAL_DEFENCE);
	}

	public int getPhysicalAttackRange()
	{
		return (int) this.getValue(Stat.PHYSICAL_ATTACK_RANGE);
	}

	public int getPhysicalAttackRadius()
	{
		return 40;
	}

	public int getPhysicalAttackAngle()
	{
		return 0;
	}

	public double getWeaponReuseModifier()
	{
		return this.getValue(Stat.ATK_REUSE, 1.0);
	}

	public int getShldDef()
	{
		return (int) this.getValue(Stat.SHIELD_DEFENCE);
	}

	public long getSp()
	{
		return this._sp;
	}

	public void setSp(long value)
	{
		this._sp = value;
	}

	public int getSTR()
	{
		return (int) this.getValue(Stat.STAT_STR);
	}

	public int getWIT()
	{
		return (int) this.getValue(Stat.STAT_WIT);
	}

	public int getMpConsume(Skill skill)
	{
		if (skill == null)
		{
			return 1;
		}
		double mpConsume = skill.getMpConsume();
		double nextDanceMpCost = Math.ceil(skill.getMpConsume() / 2.0);
		if (skill.isDance() && PlayerConfig.DANCE_CONSUME_ADDITIONAL_MP && this._creature != null && this._creature.getDanceCount() > 0)
		{
			mpConsume += this._creature.getDanceCount() * nextDanceMpCost;
		}

		return (int) (mpConsume * this.getMpConsumeTypeValue(skill.getMagicType()));
	}

	public int getMpInitialConsume(Skill skill)
	{
		return skill == null ? 1 : skill.getMpInitialConsume();
	}

	public AttributeType getAttackElement()
	{
		Item weaponInstance = this._creature.getActiveWeaponInstance();
		if (weaponInstance != null && weaponInstance.getAttackAttributeType() != AttributeType.NONE)
		{
			return weaponInstance.getAttackAttributeType();
		}
		int tempVal = 0;
		int[] stats = new int[]
		{
			this.getAttackElementValue(AttributeType.FIRE),
			this.getAttackElementValue(AttributeType.WATER),
			this.getAttackElementValue(AttributeType.WIND),
			this.getAttackElementValue(AttributeType.EARTH),
			this.getAttackElementValue(AttributeType.HOLY),
			this.getAttackElementValue(AttributeType.DARK)
		};
		AttributeType returnVal = AttributeType.NONE;

		for (byte x = 0; x < stats.length; x++)
		{
			if (stats[x] > tempVal)
			{
				returnVal = AttributeType.findByClientId(x);
				tempVal = stats[x];
			}
		}

		return returnVal;
	}

	public int getAttackElementValue(AttributeType attackAttribute)
	{
		switch (attackAttribute)
		{
			case FIRE:
				return (int) this.getValue(Stat.FIRE_POWER);
			case WATER:
				return (int) this.getValue(Stat.WATER_POWER);
			case WIND:
				return (int) this.getValue(Stat.WIND_POWER);
			case EARTH:
				return (int) this.getValue(Stat.EARTH_POWER);
			case HOLY:
				return (int) this.getValue(Stat.HOLY_POWER);
			case DARK:
				return (int) this.getValue(Stat.DARK_POWER);
			default:
				return 0;
		}
	}

	public int getDefenseElementValue(AttributeType defenseAttribute)
	{
		switch (defenseAttribute)
		{
			case FIRE:
				return (int) this.getValue(Stat.FIRE_RES);
			case WATER:
				return (int) this.getValue(Stat.WATER_RES);
			case WIND:
				return (int) this.getValue(Stat.WIND_RES);
			case EARTH:
				return (int) this.getValue(Stat.EARTH_RES);
			case HOLY:
				return (int) this.getValue(Stat.HOLY_RES);
			case DARK:
				return (int) this.getValue(Stat.DARK_RES);
			default:
				return (int) this.getValue(Stat.BASE_ATTRIBUTE_RES);
		}
	}

	public void mergeAttackTrait(TraitType traitType, float value)
	{
		this._lock.readLock().lock();

		try
		{
			this._attackTraitValues[traitType.ordinal()] += value;
			this._attackTraits.add(traitType);
		}
		finally
		{
			this._lock.readLock().unlock();
		}
	}

	public void removeAttackTrait(TraitType traitType, float value)
	{
		this._lock.readLock().lock();

		try
		{
			this._attackTraitValues[traitType.ordinal()] -= value;
			if (this._attackTraitValues[traitType.ordinal()] == 1.0F)
			{
				this._attackTraits.remove(traitType);
			}
		}
		finally
		{
			this._lock.readLock().unlock();
		}
	}

	public float getAttackTrait(TraitType traitType)
	{
		this._lock.readLock().lock();

		float var2;
		try
		{
			var2 = this._attackTraitValues[traitType.ordinal()];
		}
		finally
		{
			this._lock.readLock().unlock();
		}

		return var2;
	}

	public boolean hasAttackTrait(TraitType traitType)
	{
		this._lock.readLock().lock();

		boolean var2;
		try
		{
			var2 = this._attackTraits.contains(traitType);
		}
		finally
		{
			this._lock.readLock().unlock();
		}

		return var2;
	}

	public void mergeDefenceTrait(TraitType traitType, float value)
	{
		this._lock.readLock().lock();

		try
		{
			this._defenceTraitValues[traitType.ordinal()] += value;
			this._defenceTraits.add(traitType);
		}
		finally
		{
			this._lock.readLock().unlock();
		}
	}

	public void removeDefenceTrait(TraitType traitType, float value)
	{
		this._lock.readLock().lock();

		try
		{
			this._defenceTraitValues[traitType.ordinal()] -= value;
			if (this._defenceTraitValues[traitType.ordinal()] == 0.0F)
			{
				this._defenceTraits.remove(traitType);
			}
		}
		finally
		{
			this._lock.readLock().unlock();
		}
	}

	public float getDefenceTrait(TraitType traitType)
	{
		this._lock.readLock().lock();

		float var2;
		try
		{
			var2 = this._defenceTraitValues[traitType.ordinal()];
		}
		finally
		{
			this._lock.readLock().unlock();
		}

		return var2;
	}

	public boolean hasDefenceTrait(TraitType traitType)
	{
		this._lock.readLock().lock();

		boolean var2;
		try
		{
			var2 = this._defenceTraits.contains(traitType);
		}
		finally
		{
			this._lock.readLock().unlock();
		}

		return var2;
	}

	public void mergeInvulnerableTrait(TraitType traitType)
	{
		this._lock.readLock().lock();

		try
		{
			this._invulnerableTraits.add(traitType);
		}
		finally
		{
			this._lock.readLock().unlock();
		}
	}

	public void removeInvulnerableTrait(TraitType traitType)
	{
		this._lock.readLock().lock();

		try
		{
			this._invulnerableTraits.remove(traitType);
		}
		finally
		{
			this._lock.readLock().unlock();
		}
	}

	public boolean isInvulnerableTrait(TraitType traitType)
	{
		this._lock.readLock().lock();

		boolean var2;
		try
		{
			var2 = this._invulnerableTraits.contains(traitType);
		}
		finally
		{
			this._lock.readLock().unlock();
		}

		return var2;
	}

	public int getMaxBuffCount()
	{
		return this._maxBuffCount;
	}

	public void setMaxBuffCount(int value)
	{
		this._maxBuffCount = value;
	}

	public void mergeAdd(Stat stat, Double value)
	{
		this._statsAdd.merge(stat, value, stat::functionAdd);
	}

	public void mergeMul(Stat stat, Double value)
	{
		this._statsMul.merge(stat, value, stat::functionMul);
	}

	public double getAdd(Stat stat)
	{
		return this.getAdd(stat, 0.0);
	}

	public double getAdd(Stat stat, double defaultValue)
	{
		this._lock.readLock().lock();

		double var5;
		try
		{
			Double val = this._statsAdd.get(stat);
			var5 = val != null ? val : defaultValue;
		}
		finally
		{
			this._lock.readLock().unlock();
		}

		return var5;
	}

	public double getAddValue(Stat stat)
	{
		return this.getAddValue(stat, 0.0);
	}

	public double getAddValue(Stat stat, double defaultValue)
	{
		Double val = this._statsAdd.get(stat);
		return val != null ? val : defaultValue;
	}

	public double getMul(Stat stat)
	{
		return this.getMul(stat, 1.0);
	}

	public double getMul(Stat stat, double defaultValue)
	{
		this._lock.readLock().lock();

		double var5;
		try
		{
			Double val = this._statsMul.get(stat);
			var5 = val != null ? val : defaultValue;
		}
		finally
		{
			this._lock.readLock().unlock();
		}

		return var5;
	}

	public double getMulValue(Stat stat)
	{
		return this.getMulValue(stat, 1.0);
	}

	public double getMulValue(Stat stat, double defaultValue)
	{
		Double val = this._statsMul.get(stat);
		return val != null ? val : defaultValue;
	}

	public double getValue(Stat stat, double baseValue)
	{
		Double val = this._fixedValue.get(stat);
		return val != null ? val : stat.finalize(this._creature, OptionalDouble.of(baseValue));
	}

	public double getValue(Stat stat)
	{
		Double val = this._fixedValue.get(stat);
		return val != null ? val : stat.finalize(this._creature, OptionalDouble.empty());
	}

	protected void resetStats()
	{
		this._statsAdd.clear();
		this._statsMul.clear();
		this._vampiricSum = 0.0;
		this._mpVampiricSum = 0.0;

		for (Stat stat : Stat.values())
		{
			if (stat.getResetAddValue() != 0.0)
			{
				this._statsAdd.put(stat, stat.getResetAddValue());
			}

			if (stat.getResetMulValue() != 0.0)
			{
				this._statsMul.put(stat, stat.getResetMulValue());
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void recalculateStats(boolean broadcast)
	{
		if (broadcast)
		{
			synchronized (this._broadcast)
			{
				this._broadcast.compareAndSet(false, true);
			}
		}

		synchronized (this)
		{
			if (this._recalculateStatsTask == null)
			{
				this._recalculateStatsTask = ThreadPool.schedule(() -> {
					synchronized (this._broadcast)
					{
						boolean broadcastChanges = this._broadcast.get();
						this._broadcast.compareAndSet(true, false);
						this._lock.writeLock().lock();
						Map<Stat, Double> adds = (Map<Stat, Double>) (!broadcastChanges ? Collections.emptyMap() : new EnumMap<>(this._statsAdd));
						Map<Stat, Double> muls = (Map<Stat, Double>) (!broadcastChanges ? Collections.emptyMap() : new EnumMap<>(this._statsMul));

						try
						{
							this.resetStats();
							List<DelayedPumpHolder> delayedPumps = new LinkedList<>();

							for (BuffInfo info : this._creature.getEffectList().getPassives())
							{
								if (info.isInUse() && info.getSkill().checkConditions(SkillConditionScope.PASSIVE, this._creature, this._creature.getTarget()))
								{
									for (AbstractEffect effect : info.getEffects())
									{
										if (effect.canStart(info.getEffector(), info.getEffected(), info.getSkill()) && effect.canPump(info.getEffector(), info.getEffected(), info.getSkill()))
										{
											if (effect.delayPump())
											{
												delayedPumps.add(new DelayedPumpHolder(effect, info.getEffected(), info.getSkill()));
											}
											else
											{
												effect.pump(info.getEffected(), info.getSkill());
											}
										}
									}
								}
							}

							for (BuffInfo infox : this._creature.getEffectList().getOptions())
							{
								if (infox.isInUse())
								{
									for (AbstractEffect effectx : infox.getEffects())
									{
										if (effectx.canStart(infox.getEffector(), infox.getEffected(), infox.getSkill()) && effectx.canPump(infox.getEffector(), infox.getEffected(), infox.getSkill()))
										{
											if (effectx.delayPump())
											{
												delayedPumps.add(new DelayedPumpHolder(effectx, infox.getEffected(), infox.getSkill()));
											}
											else
											{
												effectx.pump(infox.getEffected(), infox.getSkill());
											}
										}
									}
								}
							}

							for (BuffInfo infoxx : this._creature.getEffectList().getEffects())
							{
								if (infoxx.isInUse())
								{
									for (AbstractEffect effectxx : infoxx.getEffects())
									{
										if (effectxx.canStart(infoxx.getEffector(), infoxx.getEffected(), infoxx.getSkill()) && effectxx.canPump(infoxx.getEffector(), infoxx.getEffected(), infoxx.getSkill()))
										{
											if (effectxx.delayPump())
											{
												delayedPumps.add(new DelayedPumpHolder(effectxx, infoxx.getEffected(), infoxx.getSkill()));
											}
											else
											{
												effectxx.pump(infoxx.getEffected(), infoxx.getSkill());
											}
										}
									}
								}
							}

							if (!delayedPumps.isEmpty())
							{
								for (DelayedPumpHolder holder : delayedPumps)
								{
									holder.getEffect().pump(holder.getEffected(), holder.getSkill());
								}
							}

							if (this._creature.isSummon() || this._creature instanceof Guardian)
							{
								Player player = this._creature.asPlayer();
								if (player != null && player.hasAbnormalType(AbnormalType.ABILITY_CHANGE))
								{
									for (BuffInfo infoxxx : player.getEffectList().getEffects())
									{
										if (infoxxx.isInUse() && infoxxx.isAbnormalType(AbnormalType.ABILITY_CHANGE))
										{
											for (AbstractEffect effectxxx : infoxxx.getEffects())
											{
												if (effectxxx.canStart(infoxxx.getEffector(), infoxxx.getEffected(), infoxxx.getSkill()) && effectxxx.canPump(this._creature, this._creature, infoxxx.getSkill()))
												{
													effectxxx.pump(this._creature, infoxxx.getSkill());
												}
											}
										}
									}
								}
							}

							this._attackSpeedMultiplier = Formulas.calcAtkSpdMultiplier(this._creature);
							this._mAttackSpeedMultiplier = Formulas.calcMAtkSpdMultiplier(this._creature);
						}
						finally
						{
							this._lock.writeLock().unlock();
						}

						this.onRecalculateStats(broadcastChanges);
						if (broadcastChanges)
						{
							Set<Stat> changed = EnumSet.noneOf(Stat.class);

							for (Stat stat : Stat.values())
							{
								Double statAddResetValue = stat.getResetAddValue();
								Double statMulResetValue = stat.getResetMulValue();
								Double addsValue = adds.getOrDefault(stat, statAddResetValue);
								Double mulsValue = muls.getOrDefault(stat, statMulResetValue);
								Double statAddValue = this._statsAdd.getOrDefault(stat, statAddResetValue);
								Double statMulValue = this._statsMul.getOrDefault(stat, statMulResetValue);
								if (addsValue.equals(statAddResetValue) || mulsValue.equals(statMulResetValue) || !addsValue.equals(statAddValue) || !mulsValue.equals(statMulValue))
								{
									changed.add(stat);
								}
							}

							this._creature.broadcastModifiedStats(changed);
						}
					}

					this._recalculateStatsTask = null;
				}, 50L);
			}
		}
	}

	protected void onRecalculateStats(boolean broadcast)
	{
		if (this._creature.getCurrentCp() > this.getMaxCp())
		{
			this._creature.setCurrentCp(this.getMaxCp());
		}

		if (this._creature.getCurrentHp() > this.getMaxHp())
		{
			this._creature.setCurrentHp(this.getMaxHp());
		}

		if (this._creature.getCurrentMp() > this.getMaxMp())
		{
			this._creature.setCurrentMp(this.getMaxMp());
		}
	}

	public double getPositionTypeValue(Stat stat, Position position)
	{
		Map<Position, Double> map = this._positionStats.get(stat);
		if (map != null)
		{
			Double val = map.get(position);
			if (val != null)
			{
				return val;
			}
		}

		return 1.0;
	}

	public void mergePositionTypeValue(Stat stat, Position position, double value, BiFunction<? super Double, ? super Double, ? extends Double> func)
	{
		this._positionStats.computeIfAbsent(stat, _ -> new ConcurrentHashMap<>()).merge(position, value, func);
	}

	public double getMoveTypeValue(Stat stat, MoveType type)
	{
		Map<MoveType, Double> map = this._moveTypeStats.get(stat);
		if (map != null)
		{
			Double val = map.get(type);
			if (val != null)
			{
				return val;
			}
		}

		return 0.0;
	}

	public void mergeMoveTypeValue(Stat stat, MoveType type, double value)
	{
		this._moveTypeStats.computeIfAbsent(stat, _ -> new ConcurrentHashMap<>()).merge(type, value, MathUtil::add);
	}

	public double getReuseTypeValue(int magicType)
	{
		Double val = this._reuseStat.get(magicType);
		return val != null ? val : 1.0;
	}

	public void mergeReuseTypeValue(int magicType, double value, BiFunction<? super Double, ? super Double, ? extends Double> func)
	{
		this._reuseStat.merge(magicType, value, func);
	}

	public double getMpConsumeTypeValue(int magicType)
	{
		Double val = this._mpConsumeStat.get(magicType);
		return val != null ? val : 1.0;
	}

	public void mergeMpConsumeTypeValue(int magicType, double value, BiFunction<? super Double, ? super Double, ? extends Double> func)
	{
		this._mpConsumeStat.merge(magicType, value, func);
	}

	public double getSkillEvasionTypeValue(int magicType)
	{
		LinkedList<Double> skillEvasions = this._skillEvasionStat.get(magicType);
		return skillEvasions != null && !skillEvasions.isEmpty() ? skillEvasions.peekLast() : 0.0;
	}

	public void addSkillEvasionTypeValue(int magicType, double value)
	{
		this._skillEvasionStat.computeIfAbsent(magicType, _ -> new LinkedList<>()).add(value);
	}

	public void removeSkillEvasionTypeValue(int magicType, double value)
	{
		this._skillEvasionStat.computeIfPresent(magicType, (_, v) -> {
			v.remove(value);
			return !v.isEmpty() ? v : null;
		});
	}

	public void addToVampiricSum(double sum)
	{
		this._vampiricSum += sum;
	}

	public double getVampiricSum()
	{
		this._lock.readLock().lock();

		double var1;
		try
		{
			var1 = this._vampiricSum;
		}
		finally
		{
			this._lock.readLock().unlock();
		}

		return var1;
	}

	public void addToMpVampiricSum(double sum)
	{
		this._mpVampiricSum += sum;
	}

	public double getMpVampiricSum()
	{
		this._lock.readLock().lock();

		double var1;
		try
		{
			var1 = this._mpVampiricSum;
		}
		finally
		{
			this._lock.readLock().unlock();
		}

		return var1;
	}

	public int getReuseTime(Skill skill)
	{
		return !skill.isStaticReuse() && !skill.isStatic() ? (int) (skill.getReuseDelay() * this.getReuseTypeValue(skill.getMagicType())) : skill.getReuseDelay();
	}

	public boolean addFixedValue(Stat stat, Double value)
	{
		return this._fixedValue.put(stat, value) == null;
	}

	public boolean removeFixedValue(Stat stat)
	{
		return this._fixedValue.remove(stat) != null;
	}
}
