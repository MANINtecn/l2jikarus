package net.sf.l2jdev.gameserver.model.actor.holders.npc;

import java.time.Duration;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.model.StatSet;

public class MinionHolder
{
	private final int _id;
	private final int _count;
	private final int _max;
	private final long _respawnTime;
	private final int _weightPoint;

	public MinionHolder(StatSet set)
	{
		this._id = set.getInt("id");
		this._count = set.getInt("count", 1);
		this._max = set.getInt("max", 0);
		this._respawnTime = set.getDuration("respawnTime", Duration.ofSeconds(0L)).getSeconds() * 1000L;
		this._weightPoint = set.getInt("weightPoint", 0);
	}

	public MinionHolder(int id, int count, int max, long respawnTime, int weightPoint)
	{
		this._id = id;
		this._count = count;
		this._max = max;
		this._respawnTime = respawnTime;
		this._weightPoint = weightPoint;
	}

	public int getId()
	{
		return this._id;
	}

	public int getCount()
	{
		return this._max > this._count ? Rnd.get(this._count, this._max) : this._count;
	}

	public long getRespawnTime()
	{
		return this._respawnTime;
	}

	public int getWeightPoint()
	{
		return this._weightPoint;
	}
}
