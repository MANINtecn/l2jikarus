package net.sf.l2jdev.gameserver.model.skill;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.effects.EffectTaskInfo;
import net.sf.l2jdev.gameserver.model.effects.EffectTickTask;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.options.Options;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillFinishType;
import net.sf.l2jdev.gameserver.model.stats.Formulas;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.taskmanagers.GameTimeTaskManager;

public class BuffInfo
{
	private final int _effectorObjectId;
	private final Creature _effector;
	private final Creature _effected;
	private final Skill _skill;
	private final List<AbstractEffect> _effects = new ArrayList<>(1);
	private Map<AbstractEffect, EffectTaskInfo> _tasks;
	private int _abnormalTime;
	private int _periodStartTicks;
	private volatile SkillFinishType _finishType = SkillFinishType.NORMAL;
	private volatile boolean _isInUse = true;
	private final boolean _hideStartMessage;
	private final Item _item;
	private final Options _option;

	public BuffInfo(Creature effector, Creature effected, Skill skill, boolean hideStartMessage, Item item, Options option)
	{
		this._effectorObjectId = effector != null ? effector.getObjectId() : 0;
		this._effector = effector;
		this._effected = effected;
		this._skill = skill;
		this._abnormalTime = Formulas.calcEffectAbnormalTime(effector, effected, skill);
		this._periodStartTicks = GameTimeTaskManager.getInstance().getGameTicks();
		this._hideStartMessage = hideStartMessage;
		this._item = item;
		this._option = option;
	}

	public List<AbstractEffect> getEffects()
	{
		return this._effects;
	}

	public void addEffect(AbstractEffect effect)
	{
		this._effects.add(effect);
	}

	private void addTask(AbstractEffect effect, EffectTaskInfo effectTaskInfo)
	{
		if (this._tasks == null)
		{
			synchronized (this)
			{
				if (this._tasks == null)
				{
					this._tasks = new ConcurrentHashMap<>();
				}
			}
		}

		this._tasks.put(effect, effectTaskInfo);
	}

	private EffectTaskInfo getEffectTask(AbstractEffect effect)
	{
		return this._tasks == null ? null : this._tasks.get(effect);
	}

	public Skill getSkill()
	{
		return this._skill;
	}

	public int getAbnormalTime()
	{
		return this._abnormalTime;
	}

	public void setAbnormalTime(int abnormalTime)
	{
		this._abnormalTime = abnormalTime;
	}

	public int getPeriodStartTicks()
	{
		return this._periodStartTicks;
	}

	public Item getItem()
	{
		return this._item;
	}

	public Options getOption()
	{
		return this._option;
	}

	public int getTime()
	{
		return this._abnormalTime - (GameTimeTaskManager.getInstance().getGameTicks() - this._periodStartTicks) / 10;
	}

	public boolean isRemoved()
	{
		return this._finishType == SkillFinishType.REMOVED;
	}

	public void setFinishType(SkillFinishType type)
	{
		this._finishType = type;
	}

	public boolean isInUse()
	{
		return this._isInUse;
	}

	public void setInUse(boolean value)
	{
		this._isInUse = value;
		if (this._skill != null && !this._skill.isHidingMessages() && this._effected.isPlayer())
		{
			if (value)
			{
				if (!this._hideStartMessage && !this._skill.isAura() && this.isDisplayedForEffected())
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_USED_S1);
					sm.addSkillName(this._skill);
					this._effected.sendPacket(sm);
				}
			}
			else
			{
				SystemMessage sm = new SystemMessage(this._skill.isToggle() ? SystemMessageId.S1_HAS_BEEN_ABORTED : SystemMessageId.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED);
				sm.addSkillName(this._skill);
				this._effected.sendPacket(sm);
			}
		}
	}

	public int getEffectorObjectId()
	{
		return this._effectorObjectId;
	}

	public Creature getEffector()
	{
		return this._effector;
	}

	public Creature getEffected()
	{
		return this._effected;
	}

	public void stopAllEffects(SkillFinishType type)
	{
		this.setFinishType(type);
		this._effected.removeBuffInfoTime(this);
		this.finishEffects();
	}

	public void initializeEffects()
	{
		if (this._effected != null && this._skill != null)
		{
			if (!this._hideStartMessage && this._effected.isPlayer() && (this._effected.asPlayer().hasEnteredWorld() || PlayerConfig.SHOW_EFFECT_MESSAGES_ON_LOGIN) && !this._skill.isHidingMessages() && !this._skill.isAura() && this.isDisplayedForEffected())
			{
				SystemMessage sm = new SystemMessage(this._skill.isToggle() ? SystemMessageId.YOU_HAVE_USED_S1 : SystemMessageId.YOU_FEEL_THE_S1_EFFECT);
				sm.addSkillName(this._skill);
				this._effected.sendPacket(sm);
			}

			if (this._abnormalTime > 0)
			{
				this._effected.addBuffInfoTime(this);
			}

			for (AbstractEffect effect : this._effects)
			{
				if (!effect.isInstant() && (!this._effected.isDead() || this._skill.isPassive() || this._skill.isStayAfterDeath()))
				{
					effect.onStart(this._effector, this._effected, this._skill, this._item);
					if ((!this._effected.isDead() || this._skill.isPassive()) && effect.getTicks() > 0)
					{
						EffectTickTask effectTask = new EffectTickTask(this, effect);
						ScheduledFuture<?> scheduledFuture = ThreadPool.scheduleAtFixedRate(effectTask, effect.getTicks() * PlayerConfig.EFFECT_TICK_RATIO, effect.getTicks() * PlayerConfig.EFFECT_TICK_RATIO);
						this.addTask(effect, new EffectTaskInfo(effectTask, scheduledFuture));
					}
				}
			}
		}
	}

	public void onTick(AbstractEffect effect)
	{
		boolean continueForever = false;
		if (this._isInUse)
		{
			continueForever = effect.onActionTime(this._effector, this._effected, this._skill, this._item);
		}

		if (!continueForever && this._skill.isToggle())
		{
			EffectTaskInfo task = this.getEffectTask(effect);
			if (task != null)
			{
				ScheduledFuture<?> schedule = task.getScheduledFuture();
				if (schedule != null && !schedule.isCancelled() && !schedule.isDone())
				{
					schedule.cancel(true);
				}

				this._effected.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, this._skill);
			}
		}
	}

	public void finishEffects()
	{
		if (this._tasks != null)
		{
			for (EffectTaskInfo effectTask : this._tasks.values())
			{
				ScheduledFuture<?> schedule = effectTask.getScheduledFuture();
				if (schedule != null && !schedule.isCancelled() && !schedule.isDone())
				{
					schedule.cancel(true);
				}
			}
		}

		for (AbstractEffect effect : this._effects)
		{
			effect.onExit(this._effector, this._effected, this._skill);
		}

		if (this._skill != null && (!this._effected.isSummon() || this._effected.asSummon().getOwner().hasSummon()) && !this._skill.isHidingMessages())
		{
			SystemMessageId smId = null;
			if (this._finishType != SkillFinishType.SILENT && this.isDisplayedForEffected())
			{
				if (this._skill.isToggle())
				{
					smId = SystemMessageId.S1_HAS_BEEN_ABORTED;
				}
				else if (this._finishType == SkillFinishType.REMOVED)
				{
					smId = SystemMessageId.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED;
				}
				else if (!this._skill.isPassive())
				{
					smId = SystemMessageId.S1_HAS_WORN_OFF;
				}
			}

			if (smId != null)
			{
				Player player = this._effected.asPlayer();
				if (player != null && player.isOnline())
				{
					SystemMessage sm = new SystemMessage(smId);
					sm.addSkillName(this._skill);
					this._effected.sendPacket(sm);
				}
			}
		}
	}

	public void resetAbnormalTime(int abnormalTime)
	{
		if (this._abnormalTime > 0)
		{
			this._periodStartTicks = GameTimeTaskManager.getInstance().getGameTicks();
			this._abnormalTime = abnormalTime;
			this._effected.removeBuffInfoTime(this);
			this._effected.addBuffInfoTime(this);
		}
	}

	public boolean isAbnormalType(AbnormalType type)
	{
		return this._skill.getAbnormalType() == type;
	}

	public boolean isDisplayedForEffected()
	{
		return !this._skill.isSelfContinuous() || this._effected == this._effector || !this._skill.hasEffects(EffectScope.SELF);
	}
}
