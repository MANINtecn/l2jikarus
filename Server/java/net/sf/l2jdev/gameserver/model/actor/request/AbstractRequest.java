package net.sf.l2jdev.gameserver.model.actor.request;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.model.actor.Player;

public abstract class AbstractRequest
{
	private final Player _player;
	private volatile long _timestamp = 0L;
	private volatile boolean _isProcessing;
	private ScheduledFuture<?> _timeOutTask;

	public AbstractRequest(Player player)
	{
		Objects.requireNonNull(player);
		this._player = player;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public long getTimestamp()
	{
		return this._timestamp;
	}

	public void setTimestamp(long timestamp)
	{
		this._timestamp = timestamp;
	}

	public void scheduleTimeout(long delay)
	{
		this._timeOutTask = ThreadPool.schedule(this::onTimeout, delay);
	}

	public boolean isTimeout()
	{
		return this._timeOutTask != null && !this._timeOutTask.isDone();
	}

	public void cancelTimeout()
	{
		if (this._timeOutTask != null)
		{
			this._timeOutTask.cancel(false);
		}
	}

	public boolean isProcessing()
	{
		return this._isProcessing;
	}

	public boolean setProcessing(boolean isProcessing)
	{
		return this._isProcessing = isProcessing;
	}

	public boolean canWorkWith(AbstractRequest request)
	{
		return true;
	}

	public boolean isItemRequest()
	{
		return false;
	}

	public abstract boolean isUsing(int var1);

	public void onTimeout()
	{
	}
}
