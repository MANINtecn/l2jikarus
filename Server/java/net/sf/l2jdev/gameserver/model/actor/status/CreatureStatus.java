package net.sf.l2jdev.gameserver.model.actor.status;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureHpChange;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.model.stats.Formulas;
import net.sf.l2jdev.gameserver.model.stats.Stat;

public class CreatureStatus
{
	protected static final Logger LOGGER = Logger.getLogger(CreatureStatus.class.getName());
	private final Creature _creature;
	private double _currentHp = 0.0;
	private double _currentMp = 0.0;
	private Set<Creature> _StatusListener;
	private Future<?> _regTask;
	protected byte _flagsRegenActive = 0;
	protected static final byte REGEN_FLAG_CP = 4;
	public CreatureStatus(Creature creature)
	{
		this._creature = creature;
	}

	public void addStatusListener(Creature object)
	{
		if (object != this._creature)
		{
			this.getStatusListener().add(object);
		}
	}

	public void removeStatusListener(Creature object)
	{
		this.getStatusListener().remove(object);
	}

	public Set<Creature> getStatusListener()
	{
		if (this._StatusListener == null)
		{
			this._StatusListener = ConcurrentHashMap.newKeySet();
		}

		return this._StatusListener;
	}

	public void reduceCp(int value)
	{
	}

	public void reduceHp(double value, Creature attacker)
	{
		this.reduceHp(value, attacker, true, false, false);
	}

	public void reduceHp(double value, Creature attacker, boolean isHpConsumption)
	{
		this.reduceHp(value, attacker, true, false, isHpConsumption);
	}

	public void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHPConsumption)
	{
		Creature creature = this._creature;
		if (!creature.isDead())
		{
			if (!creature.isHpBlocked() || isDOT || isHPConsumption)
			{
				if (attacker != null)
				{
					Player attackerPlayer = attacker.asPlayer();
					if (attackerPlayer != null && attackerPlayer.isGM() && !attackerPlayer.getAccessLevel().canGiveDamage())
					{
						return;
					}
				}

				if (!isDOT && !isHPConsumption)
				{
					if (awake)
					{
						creature.stopEffectsOnDamage();
					}

					if (Formulas.calcStunBreak(creature))
					{
						creature.stopStunning(true);
					}

					if (Formulas.calcRealTargetBreak())
					{
						this._creature.getEffectList().stopEffects(AbnormalType.REAL_TARGET);
					}
				}

				if (value > 0.0)
				{
					this.setCurrentHp(Math.max(this._currentHp - value, creature.isUndying() ? 1.0 : 0.0));
				}

				if (creature.getCurrentHp() < 0.5)
				{
					creature.doDie(attacker);
				}
			}
		}
	}

	public void reduceMp(double value)
	{
		this.setCurrentMp(Math.max(this._currentMp - value, 0.0));
	}

	public synchronized void startHpMpRegeneration()
	{
		if (this._regTask == null && !this._creature.isDead())
		{
			int period = Formulas.getRegeneratePeriod(this._creature);
			this._regTask = ThreadPool.scheduleAtFixedRate(this::doRegeneration, period, period);
		}
	}

	public synchronized void stopHpMpRegeneration()
	{
		if (this._regTask != null)
		{
			this._regTask.cancel(false);
			this._regTask = null;
			this._flagsRegenActive = 0;
		}
	}

	public double getCurrentCp()
	{
		return 0.0;
	}

	public void setCurrentCp(double newCp)
	{
	}

	public void setCurrentCp(double newCp, boolean broadcastPacket)
	{
	}

	public double getCurrentHp()
	{
		return this._currentHp;
	}

	public void setCurrentHp(double newHp)
	{
		this.setCurrentHp(newHp, true);
	}

	public boolean setCurrentHp(double newHp, boolean broadcastPacket)
	{
		long oldHp = (long) this._currentHp;
		double maxHp = this._creature.getStat().getMaxHp();
		synchronized (this)
		{
			if (this._creature.isDead())
			{
				return false;
			}

			if (newHp >= maxHp)
			{
				this._currentHp = maxHp;
				this._flagsRegenActive &= -2;
				if (this._flagsRegenActive == 0)
				{
					this.stopHpMpRegeneration();
				}
			}
			else
			{
				this._currentHp = newHp;
				this._flagsRegenActive = (byte) (this._flagsRegenActive | 1);
				this.startHpMpRegeneration();
			}
		}

		boolean hpWasChanged = oldHp != this._currentHp;
		if (hpWasChanged)
		{
			if (broadcastPacket)
			{
				this._creature.broadcastStatusUpdate();
			}

			Creature creature = this.getActiveChar();
			if (EventDispatcher.getInstance().hasListener(EventType.ON_CREATURE_HP_CHANGE, creature))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnCreatureHpChange(creature, oldHp, this._currentHp), creature);
			}
		}

		return hpWasChanged;
	}

	public void setCurrentHpMp(double newHp, double newMp)
	{
		boolean hpOrMpWasChanged = this.setCurrentHp(newHp, false);
		hpOrMpWasChanged |= this.setCurrentMp(newMp, false);
		if (hpOrMpWasChanged)
		{
			this._creature.broadcastStatusUpdate();
		}
	}

	public double getCurrentMp()
	{
		return this._currentMp;
	}

	public void setCurrentMp(double newMp)
	{
		this.setCurrentMp(newMp, true);
	}

	public boolean setCurrentMp(double newMp, boolean broadcastPacket)
	{
		int currentMp = (int) this._currentMp;
		int maxMp = this._creature.getStat().getMaxMp();
		synchronized (this)
		{
			if (this._creature.isDead())
			{
				return false;
			}

			if (newMp >= maxMp)
			{
				this._currentMp = maxMp;
				this._flagsRegenActive &= -3;
				if (this._flagsRegenActive == 0)
				{
					this.stopHpMpRegeneration();
				}
			}
			else
			{
				this._currentMp = newMp;
				this._flagsRegenActive = (byte) (this._flagsRegenActive | 2);
				this.startHpMpRegeneration();
			}
		}

		boolean mpWasChanged = currentMp != this._currentMp;
		if (mpWasChanged && broadcastPacket)
		{
			this._creature.broadcastStatusUpdate();
		}

		return mpWasChanged;
	}

	protected void doRegeneration()
	{
		if (this._creature.isDead() || !(this._currentHp < this._creature.getMaxRecoverableHp()) && !(this._currentMp < this._creature.getMaxRecoverableMp()))
		{
			this.stopHpMpRegeneration();
		}
		else
		{
			double newHp = this._currentHp + this._creature.getStat().getValue(Stat.REGENERATE_HP_RATE);
			double newMp = this._currentMp + this._creature.getStat().getValue(Stat.REGENERATE_MP_RATE);
			this.setCurrentHpMp(newHp, newMp);
		}
	}

	public Creature getActiveChar()
	{
		return this._creature;
	}
}
