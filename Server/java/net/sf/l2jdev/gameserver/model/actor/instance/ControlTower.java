package net.sf.l2jdev.gameserver.model.actor.instance;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Tower;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;

public class ControlTower extends Tower
{
	private Set<Spawn> _guards;

	public ControlTower(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.ControlTower);
	}

	@Override
	public boolean doDie(Creature killer)
	{
		if (this.getCastle().getSiege().isInProgress())
		{
			this.getCastle().getSiege().killedCT(this);
			if (this._guards != null && !this._guards.isEmpty())
			{
				for (Spawn spawn : this._guards)
				{
					if (spawn != null)
					{
						try
						{
							spawn.stopRespawn();
						}
						catch (Exception var5)
						{
							LOGGER.log(Level.WARNING, "Error at ControlTower", var5);
						}
					}
				}

				this._guards.clear();
			}
		}

		return super.doDie(killer);
	}

	public void registerGuard(Spawn guard)
	{
		this.getGuards().add(guard);
	}

	private Set<Spawn> getGuards()
	{
		if (this._guards == null)
		{
			synchronized (this)
			{
				if (this._guards == null)
				{
					this._guards = ConcurrentHashMap.newKeySet();
				}
			}
		}

		return this._guards;
	}
}
