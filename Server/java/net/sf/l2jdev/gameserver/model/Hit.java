package net.sf.l2jdev.gameserver.model;

import java.lang.ref.WeakReference;

import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttackType;

public class Hit
{
	private final WeakReference<WorldObject> _target;
	private final int _targetId;
	private final int _damage;
	private final int _ssGrade;
	private int _flags = 0;

	public Hit(WorldObject target, int damage, boolean miss, boolean crit, byte shld, boolean soulshot, int ssGrade)
	{
		this._target = new WeakReference<>(target);
		this._targetId = target.getObjectId();
		this._damage = damage;
		this._ssGrade = ssGrade;
		if (miss)
		{
			this.addMask(AttackType.MISSED);
		}
		else
		{
			if (crit)
			{
				this.addMask(AttackType.CRITICAL);
			}

			if (soulshot)
			{
				this.addMask(AttackType.SHOT_USED);
			}

			if (target.isCreature() && target.asCreature().isHpBlocked() || shld > 0)
			{
				this.addMask(AttackType.BLOCKED);
			}
		}
	}

	private void addMask(AttackType type)
	{
		this._flags = this._flags | type.getMask();
	}

	public WorldObject getTarget()
	{
		return this._target.get();
	}

	public int getTargetId()
	{
		return this._targetId;
	}

	public int getDamage()
	{
		return this._damage;
	}

	public int getFlags()
	{
		return this._flags;
	}

	public int getGrade()
	{
		return this._ssGrade;
	}

	public boolean isMiss()
	{
		return (AttackType.MISSED.getMask() & this._flags) != 0;
	}

	public boolean isCritical()
	{
		return (AttackType.CRITICAL.getMask() & this._flags) != 0;
	}

	public boolean isShotUsed()
	{
		return (AttackType.SHOT_USED.getMask() & this._flags) != 0;
	}

	public boolean isBlocked()
	{
		return (AttackType.BLOCKED.getMask() & this._flags) != 0;
	}
}
