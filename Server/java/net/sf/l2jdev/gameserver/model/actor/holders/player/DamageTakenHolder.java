package net.sf.l2jdev.gameserver.model.actor.holders.player;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.enums.player.DamageTakenType;

public class DamageTakenHolder
{
	private final Creature _creature;
	private final int _skillId;
	private final double _damage;
	private final DamageTakenType _damageTakenType;

	public DamageTakenHolder(Creature creature, int skillId, double damage, boolean isDOT, boolean reflect)
	{
		this._creature = creature;
		this._skillId = skillId;
		if (isDOT)
		{
			this._damageTakenType = DamageTakenType.POISON_FIELD;
		}
		else if (reflect)
		{
			this._damageTakenType = DamageTakenType.REFLECTED_DAMAGE;
		}
		else if (skillId > 0)
		{
			this._damageTakenType = DamageTakenType.OTHER_DAMAGE;
		}
		else
		{
			this._damageTakenType = DamageTakenType.NORMAL_DAMAGE;
		}

		this._damage = damage;
	}

	public Creature getCreature()
	{
		return this._creature;
	}

	public int getSkillId()
	{
		return this._skillId;
	}

	public double getDamage()
	{
		return this._damage;
	}

	public int getClientId()
	{
		return this._damageTakenType.getClientId();
	}
}
