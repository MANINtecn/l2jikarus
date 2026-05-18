package net.sf.l2jdev.gameserver.model;

import java.util.concurrent.ScheduledFuture;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.managers.WalkingManager;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcMoveRouteFinished;

public class WalkInfo
{
	private final String _routeName;
	private ScheduledFuture<?> _walkCheckTask;
	private boolean _blocked = false;
	private boolean _suspended = false;
	private boolean _stoppedByAttack = false;
	private int _currentNode = 0;
	private boolean _forward = true;
	private long _lastActionTime;

	public WalkInfo(String routeName)
	{
		this._routeName = routeName;
	}

	public WalkRoute getRoute()
	{
		return WalkingManager.getInstance().getRoute(this._routeName);
	}

	public NpcWalkerNode getCurrentNode()
	{
		return this.getRoute().getNodeList().get(Math.min(Math.max(0, this._currentNode), this.getRoute().getNodeList().size() - 1));
	}

	public synchronized void calculateNextNode(Npc npc)
	{
		if (this.getRoute().getRepeatType() == 3)
		{
			int newNode = this._currentNode;

			while (newNode == this._currentNode)
			{
				newNode = Rnd.get(this.getRoute().getNodesCount());
			}

			this._currentNode = newNode;
		}
		else
		{
			if (this._forward)
			{
				this._currentNode++;
			}
			else
			{
				this._currentNode--;
			}

			if (this._currentNode == this.getRoute().getNodesCount())
			{
				if (EventDispatcher.getInstance().hasListener(EventType.ON_NPC_MOVE_ROUTE_FINISHED, npc))
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnNpcMoveRouteFinished(npc), npc);
				}

				if (!this.getRoute().repeatWalk())
				{
					WalkingManager.getInstance().cancelMoving(npc);
					return;
				}

				switch (this.getRoute().getRepeatType())
				{
					case 0:
						this._forward = false;
						this._currentNode -= 2;
						break;
					case 1:
						this._currentNode = 0;
						break;
					case 2:
						npc.teleToLocation(npc.getSpawn().getLocation());
						this._currentNode = 0;
				}
			}
			else if (this._currentNode == -1)
			{
				this._currentNode = 1;
				this._forward = true;
			}
		}
	}

	public boolean isBlocked()
	{
		return this._blocked;
	}

	public void setBlocked(boolean value)
	{
		this._blocked = value;
	}

	public boolean isSuspended()
	{
		return this._suspended;
	}

	public void setSuspended(boolean value)
	{
		this._suspended = value;
	}

	public boolean isStoppedByAttack()
	{
		return this._stoppedByAttack;
	}

	public void setStoppedByAttack(boolean value)
	{
		this._stoppedByAttack = value;
	}

	public int getCurrentNodeId()
	{
		return this._currentNode;
	}

	public long getLastAction()
	{
		return this._lastActionTime;
	}

	public void setLastAction(long value)
	{
		this._lastActionTime = value;
	}

	public ScheduledFuture<?> getWalkCheckTask()
	{
		return this._walkCheckTask;
	}

	public void setWalkCheckTask(ScheduledFuture<?> task)
	{
		this._walkCheckTask = task;
	}

	@Override
	public String toString()
	{
		return "WalkInfo [_routeName=" + this._routeName + ", _walkCheckTask=" + this._walkCheckTask + ", _blocked=" + this._blocked + ", _suspended=" + this._suspended + ", _stoppedByAttack=" + this._stoppedByAttack + ", _currentNode=" + this._currentNode + ", _forward=" + this._forward + ", _lastActionTime=" + this._lastActionTime + "]";
	}
}
