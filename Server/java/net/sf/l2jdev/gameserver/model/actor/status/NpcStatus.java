package net.sf.l2jdev.gameserver.model.actor.status;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class NpcStatus extends CreatureStatus
{
	public NpcStatus(Npc activeChar)
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
		Npc npc = this.getActiveChar();
		if (!npc.isDead())
		{
			if (attacker != null)
			{
				Player attackerPlayer = attacker.asPlayer();
				if (attackerPlayer != null && attackerPlayer.isInDuel())
				{
					attackerPlayer.setDuelState(4);
				}

				npc.addAttackerToAttackByList(attacker);
			}

			super.reduceHp(value, attacker, awake, isDOT, isHpConsumption);
		}
	}

	@Override
	public Npc getActiveChar()
	{
		return super.getActiveChar().asNpc();
	}
}
