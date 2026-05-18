package net.sf.l2jdev.gameserver.model.actor.status;

import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Creature;

public class AttackableStatus extends NpcStatus
{
	public AttackableStatus(Attackable activeChar)
	{
		super(activeChar);
	}

	@Override
	public void reduceHp(double value, Creature attacker)
	{
		this.reduceHp(value, attacker, true, false, false);
	}

	@Override
	public void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHpConsumption)
	{
		Attackable attackable = this.getActiveChar();
		if (!attackable.isDead())
		{
			if (value > 0.0)
			{
				if (attackable.isOverhit())
				{
					attackable.setOverhitValues(attacker, value);
				}
				else
				{
					attackable.overhitEnabled(false);
				}
			}
			else
			{
				attackable.overhitEnabled(false);
			}

			super.reduceHp(value, attacker, awake, isDOT, isHpConsumption);
			if (!attackable.isDead())
			{
				attackable.overhitEnabled(false);
			}
		}
	}

	@Override
	public boolean setCurrentHp(double newHp, boolean broadcastPacket)
	{
		return super.setCurrentHp(newHp, true);
	}

	@Override
	public Attackable getActiveChar()
	{
		return super.getActiveChar().asAttackable();
	}
}
