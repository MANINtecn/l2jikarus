package net.sf.l2jdev.gameserver.model;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.gameserver.model.actor.instance.ControllableMob;

public class MobGroupTable
{
	private final Map<Integer, MobGroup> _groupMap = new ConcurrentHashMap<>();
	public static final int FOLLOW_RANGE = 300;
	public static final int RANDOM_RANGE = 300;

	protected MobGroupTable()
	{
	}

	public static MobGroupTable getInstance()
	{
		return MobGroupTable.SingletonHolder.INSTANCE;
	}

	public void addGroup(int groupKey, MobGroup group)
	{
		this._groupMap.put(groupKey, group);
	}

	public MobGroup getGroup(int groupKey)
	{
		return this._groupMap.get(groupKey);
	}

	public int getGroupCount()
	{
		return this._groupMap.size();
	}

	public MobGroup getGroupForMob(ControllableMob mobInst)
	{
		for (MobGroup mobGroup : this._groupMap.values())
		{
			if (mobGroup.isGroupMember(mobInst))
			{
				return mobGroup;
			}
		}

		return null;
	}

	public Collection<MobGroup> getGroups()
	{
		return this._groupMap.values();
	}

	public boolean removeGroup(int groupKey)
	{
		return this._groupMap.remove(groupKey) != null;
	}

	private static class SingletonHolder
	{
		protected static final MobGroupTable INSTANCE = new MobGroupTable();
	}
}
