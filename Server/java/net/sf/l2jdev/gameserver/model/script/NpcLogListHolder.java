package net.sf.l2jdev.gameserver.model.script;

import net.sf.l2jdev.gameserver.network.NpcStringId;

public class NpcLogListHolder
{
	private final int _id;
	private final boolean _isNpcString;
	private final int _count;

	public NpcLogListHolder(NpcStringId npcStringId, int count)
	{
		this._id = npcStringId.getId();
		this._isNpcString = true;
		this._count = count;
	}

	public NpcLogListHolder(int id, boolean isNpcString, int count)
	{
		this._id = id;
		this._isNpcString = isNpcString;
		this._count = count;
	}

	public int getId()
	{
		return this._id;
	}

	public boolean isNpcString()
	{
		return this._isNpcString;
	}

	public int getCount()
	{
		return this._count;
	}
}
