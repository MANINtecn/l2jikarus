package net.sf.l2jdev.gameserver.ai;

import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.instance.Shuttle;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.serverpackets.shuttle.ExShuttleMove;

public class ShuttleAI extends CreatureAI
{
	public ShuttleAI(Shuttle shuttle)
	{
		super(shuttle);
	}

	@Override
	public void moveTo(int x, int y, int z)
	{
		if (!this._actor.isMovementDisabled())
		{
			this._actor.moveToLocation(x, y, z, 0);
			this._actor.broadcastPacket(new ExShuttleMove(this.getActor(), x, y, z));
		}
	}

	@Override
	protected void onIntentionAttack(Creature target)
	{
	}

	@Override
	protected void onIntentionCast(Skill skill, WorldObject target, Item item, boolean forceUse, boolean dontMove)
	{
	}

	@Override
	protected void onIntentionFollow(Creature target)
	{
	}

	@Override
	protected void onIntentionPickUp(WorldObject item)
	{
	}

	@Override
	protected void onIntentionInteract(WorldObject object)
	{
	}

	@Override
	protected void onActionAttacked(Creature attacker)
	{
	}

	@Override
	protected void onActionAggression(Creature target, int aggro)
	{
	}

	@Override
	protected void onActionBlocked(Creature attacker)
	{
	}

	@Override
	protected void onActionRooted(Creature attacker)
	{
	}

	@Override
	protected void onActionForgetObject(WorldObject object)
	{
	}

	@Override
	protected void onActionCancel()
	{
	}

	@Override
	protected void onActionDeath()
	{
	}

	@Override
	protected void onActionFakeDeath()
	{
	}

	@Override
	protected void onActionFinishCasting()
	{
	}

	@Override
	protected void clientActionFailed()
	{
	}

	@Override
	public void moveToPawn(WorldObject pawn, int offset)
	{
	}

	@Override
	protected void clientStoppedMoving()
	{
	}

	@Override
	public Shuttle getActor()
	{
		return (Shuttle) this._actor;
	}
}
