package net.sf.l2jdev.gameserver.model;

import java.util.concurrent.ScheduledFuture;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;

public class DropProtection implements Runnable
{
	private volatile boolean _isProtected = false;
	private Creature _owner = null;
	private ScheduledFuture<?> _task = null;
	public static final long PROTECTED_MILLIS_TIME = 15000L;

	@Override
	public synchronized void run()
	{
		this._isProtected = false;
		this._owner = null;
		this._task = null;
	}

	public boolean isProtected()
	{
		return this._isProtected;
	}

	public Creature getOwner()
	{
		return this._owner;
	}

	public synchronized boolean tryPickUp(Player actor)
	{
		if (!this._isProtected)
		{
			return true;
		}
		return this._owner == actor ? true : this._owner.getParty() != null && this._owner.getParty() == actor.getParty();
	}

	public boolean tryPickUp(Pet pet)
	{
		return this.tryPickUp(pet.getOwner());
	}

	public synchronized void unprotect()
	{
		if (this._task != null)
		{
			this._task.cancel(false);
		}

		this._isProtected = false;
		this._owner = null;
		this._task = null;
	}

	public synchronized void protect(Creature creature)
	{
		this.unprotect();
		this._isProtected = true;
		this._owner = creature;
		if (this._owner == null)
		{
			throw new NullPointerException("Trying to protect dropped item to null owner");
		}
		this._task = ThreadPool.schedule(this, 15000L);
	}
}
