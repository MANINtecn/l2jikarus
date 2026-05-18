package net.sf.l2jdev.gameserver.data.holders;

public class RandomCraftRewardItemHolder
{
	private final int _id;
	private final long _count;
	private boolean _locked;
	private int _lockLeft;

	public RandomCraftRewardItemHolder(int id, long count, boolean locked, int lockLeft)
	{
		this._id = id;
		this._count = count;
		this._locked = locked;
		this._lockLeft = lockLeft;
	}

	public int getItemId()
	{
		return this._id;
	}

	public long getItemCount()
	{
		return this._count;
	}

	public boolean isLocked()
	{
		return this._locked;
	}

	public int getLockLeft()
	{
		return this._lockLeft;
	}

	public void lock()
	{
		this._locked = true;
	}

	public void decLock()
	{
		this._lockLeft--;
		if (this._lockLeft <= 0)
		{
			this._locked = false;
		}
	}
}
