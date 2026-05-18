package net.sf.l2jdev.gameserver.model.actor.tasks.npc.trap;

import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.model.actor.instance.Trap;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.serverpackets.SocialAction;

public class TrapTask implements Runnable
{
	private static final Logger LOGGER = Logger.getLogger(TrapTask.class.getName());
	private final Trap _trap;

	public TrapTask(Trap trap)
	{
		this._trap = trap;
	}

	@Override
	public void run()
	{
		try
		{
			if (!this._trap.isTriggered())
			{
				if (this._trap.hasLifeTime())
				{
					this._trap.setRemainingTime(this._trap.getRemainingTime() - 1000);
					if (this._trap.getRemainingTime() < this._trap.getLifeTime() - 15000)
					{
						this._trap.broadcastPacket(new SocialAction(this._trap.getObjectId(), 2));
					}

					if (this._trap.getRemainingTime() <= 0)
					{
						this._trap.triggerTrap(this._trap);
						return;
					}
				}

				Skill skill = this._trap.getSkill();
				if (skill != null && !skill.getTargetsAffected(this._trap, this._trap).isEmpty())
				{
					this._trap.triggerTrap(this._trap);
				}
			}
		}
		catch (Exception var2)
		{
			LOGGER.severe(TrapTask.class.getSimpleName() + ": " + var2.getMessage());
			this._trap.unSummon();
		}
	}
}
