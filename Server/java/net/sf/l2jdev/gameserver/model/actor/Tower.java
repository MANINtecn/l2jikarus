package net.sf.l2jdev.gameserver.model.actor;

import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;

public abstract class Tower extends Npc
{
	public Tower(NpcTemplate template)
	{
		super(template);
		this.setInvul(false);
	}

	@Override
	public boolean canBeAttacked()
	{
		return this.getCastle() != null && this.getCastle().getResidenceId() > 0 && this.getCastle().getSiege().isInProgress();
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return attacker != null && attacker.isPlayer() && this.getCastle() != null && this.getCastle().getResidenceId() > 0 && this.getCastle().getSiege().isInProgress() && this.getCastle().getSiege().checkIsAttacker(attacker.asPlayer().getClan());
	}

	@Override
	public void onAction(Player player, boolean interact)
	{
		if (this.canTarget(player))
		{
			if (this != player.getTarget())
			{
				player.setTarget(this);
			}
			else if (interact && this.isAutoAttackable(player) && Math.abs(player.getZ() - this.getZ()) < 100 && GeoEngine.getInstance().canSeeTarget(player, this))
			{
				player.getAI().setIntention(Intention.ATTACK, this);
			}

			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	@Override
	public void onForcedAttack(Player player)
	{
		this.onAction(player);
	}
}
