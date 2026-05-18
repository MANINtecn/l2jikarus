package net.sf.l2jdev.gameserver.ai;

import java.util.Objects;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.instance.Defender;
import net.sf.l2jdev.gameserver.model.actor.instance.Door;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class DoorAI extends CreatureAI
{
	public DoorAI(Door door)
	{
		super(door);
	}

	@Override
	protected void onIntentionIdle()
	{
	}

	@Override
	protected void onIntentionActive()
	{
	}

	@Override
	protected void onIntentionRest()
	{
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
	protected void onIntentionMoveTo(ILocational destination)
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
	public void onActionThink()
	{
	}

	@Override
	protected void onActionAttacked(Creature attacker)
	{
		ThreadPool.execute(new DoorAI.onEventAttackedDoorTask(this._actor.asDoor(), attacker));
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
	protected void onActionReadyToAct()
	{
	}

	@Override
	protected void onActionArrived()
	{
	}

	@Override
	protected void onActionArrivedRevalidate()
	{
	}

	@Override
	protected void onActionArrivedBlocked(Location location)
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

	private class onEventAttackedDoorTask implements Runnable
	{
		private final Door _door;
		private final Creature _attacker;

		public onEventAttackedDoorTask(Door door, Creature attacker)
		{
			Objects.requireNonNull(DoorAI.this);
			super();
			this._door = door;
			this._attacker = attacker;
		}

		@Override
		public void run()
		{
			World.getInstance().forEachVisibleObject(this._door, Defender.class, guard -> {
				if (DoorAI.this._actor.isInsideRadius3D(guard, guard.getTemplate().getClanHelpRange()))
				{
					guard.getAI().notifyAction(Action.AGGRESSION, this._attacker, 15);
				}
			});
		}
	}
}
