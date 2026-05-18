package net.sf.l2jdev.gameserver.model.actor.status;

import net.sf.l2jdev.gameserver.ai.Action;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class PetStatus extends SummonStatus
{
	private int _currentFed = 0;

	public PetStatus(Pet activeChar)
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
		Pet pet = this.getActiveChar();
		if (!pet.isDead())
		{
			super.reduceHp(value, attacker, awake, isDOT, isHpConsumption);
			if (attacker != null)
			{
				if (!isDOT && pet.getOwner() != null)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_DEALT_S2_DAMAGE_TO_YOUR_GUARDIAN);
					sm.addString(attacker.getName());
					sm.addInt((int) value);
					pet.sendPacket(sm);
				}

				pet.getAI().notifyAction(Action.ATTACKED, attacker);
			}
		}
	}

	public int getCurrentFed()
	{
		return this._currentFed;
	}

	public void setCurrentFed(int value)
	{
		this._currentFed = value;
	}

	@Override
	public Pet getActiveChar()
	{
		return super.getActiveChar().asPet();
	}
}
