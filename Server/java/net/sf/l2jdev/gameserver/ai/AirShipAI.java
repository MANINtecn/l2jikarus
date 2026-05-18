package net.sf.l2jdev.gameserver.ai;

import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.AirShip;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.serverpackets.ExMoveToLocationAirShip;
import net.sf.l2jdev.gameserver.network.serverpackets.ExStopMoveAirShip;

public class AirShipAI extends CreatureAI
{
	public AirShipAI(AirShip airShip)
	{
		super(airShip);
	}

	@Override
	protected void moveTo(int x, int y, int z)
	{
		if (!this._actor.isMovementDisabled())
		{
			this._actor.moveToLocation(x, y, z, 0);
			this._actor.broadcastPacket(new ExMoveToLocationAirShip(this.getActor()));
		}
	}

	@Override
	public void clientStopMoving(Location loc)
	{
		if (this._actor.isMoving())
		{
			this._actor.stopMove(loc);
			this._actor.broadcastPacket(new ExStopMoveAirShip(this.getActor()));
		}
		else
		{
			if (loc != null)
			{
				this._actor.broadcastPacket(new ExStopMoveAirShip(this.getActor()));
			}
		}
	}

	@Override
	public void describeStateToPlayer(Player player)
	{
		if (this._actor.isMoving())
		{
			player.sendPacket(new ExMoveToLocationAirShip(this.getActor()));
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
	public AirShip getActor()
	{
		return (AirShip) this._actor;
	}
}
