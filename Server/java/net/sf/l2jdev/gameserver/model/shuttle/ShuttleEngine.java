package net.sf.l2jdev.gameserver.model.shuttle;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.data.xml.DoorData;
import net.sf.l2jdev.gameserver.model.actor.instance.Door;
import net.sf.l2jdev.gameserver.model.actor.instance.Shuttle;

public class ShuttleEngine implements Runnable
{
	private static final Logger LOGGER = Logger.getLogger(ShuttleEngine.class.getName());
	private final Shuttle _shuttle;
	private int _cycle = 0;
	private final Door _door1;
	private final Door _door2;

	public ShuttleEngine(ShuttleDataHolder data, Shuttle shuttle)
	{
		this._shuttle = shuttle;
		this._door1 = DoorData.getInstance().getDoor(data.getDoors().get(0));
		this._door2 = DoorData.getInstance().getDoor(data.getDoors().get(1));
	}

	@Override
	public void run()
	{
		try
		{
			if (!this._shuttle.isSpawned())
			{
				return;
			}

			switch (this._cycle)
			{
				case 0:
					this._door1.openMe();
					this._door2.closeMe();
					this._shuttle.openDoor(0);
					this._shuttle.closeDoor(1);
					this._shuttle.broadcastShuttleInfo();
					ThreadPool.schedule(this, 15000L);
					break;
				case 1:
					this._door1.closeMe();
					this._door2.closeMe();
					this._shuttle.closeDoor(0);
					this._shuttle.closeDoor(1);
					this._shuttle.broadcastShuttleInfo();
					ThreadPool.schedule(this, 1000L);
					break;
				case 2:
					this._shuttle.executePath(this._shuttle.getShuttleData().getRoutes().get(0));
					break;
				case 3:
					this._door1.closeMe();
					this._door2.openMe();
					this._shuttle.openDoor(1);
					this._shuttle.closeDoor(0);
					this._shuttle.broadcastShuttleInfo();
					ThreadPool.schedule(this, 15000L);
					break;
				case 4:
					this._door1.closeMe();
					this._door2.closeMe();
					this._shuttle.closeDoor(0);
					this._shuttle.closeDoor(1);
					this._shuttle.broadcastShuttleInfo();
					ThreadPool.schedule(this, 1000L);
					break;
				case 5:
					this._shuttle.executePath(this._shuttle.getShuttleData().getRoutes().get(1));
			}

			this._cycle++;
			if (this._cycle > 5)
			{
				this._cycle = 0;
			}
		}
		catch (Exception var2)
		{
			LOGGER.log(Level.INFO, var2.getMessage(), var2);
		}
	}
}
