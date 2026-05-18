package net.sf.l2jdev.gameserver.model;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.siege.SiegeClanType;

public class SiegeClan
{
	private int _clanId = 0;
	private final Set<Npc> _flags = ConcurrentHashMap.newKeySet();
	private SiegeClanType _type;

	public SiegeClan(int clanId, SiegeClanType type)
	{
		this._clanId = clanId;
		this._type = type;
	}

	public int getNumFlags()
	{
		return this._flags.size();
	}

	public void addFlag(Npc flag)
	{
		this._flags.add(flag);
	}

	public boolean removeFlag(Npc flag)
	{
		if (flag == null)
		{
			return false;
		}
		flag.deleteMe();
		return this._flags.remove(flag);
	}

	public void removeFlags()
	{
		for (Npc flag : this._flags)
		{
			this.removeFlag(flag);
		}
	}

	public int getClanId()
	{
		return this._clanId;
	}

	public Set<Npc> getFlag()
	{
		return this._flags;
	}

	public SiegeClanType getType()
	{
		return this._type;
	}

	public void setType(SiegeClanType setType)
	{
		this._type = setType;
	}
}
