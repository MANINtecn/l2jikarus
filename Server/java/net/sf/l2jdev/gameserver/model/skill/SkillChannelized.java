package net.sf.l2jdev.gameserver.model.skill;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.gameserver.model.actor.Creature;

public class SkillChannelized
{
	private final Map<Integer, Map<Integer, Creature>> _channelizers = new ConcurrentHashMap<>();

	public void addChannelizer(int skillId, Creature channelizer)
	{
		this._channelizers.computeIfAbsent(skillId, _ -> new ConcurrentHashMap<>()).put(channelizer.getObjectId(), channelizer);
	}

	public void removeChannelizer(int skillId, Creature channelizer)
	{
		this.getChannelizers(skillId).remove(channelizer.getObjectId());
	}

	public int getChannerlizersSize(int skillId)
	{
		return this.getChannelizers(skillId).size();
	}

	public Map<Integer, Creature> getChannelizers(int skillId)
	{
		return this._channelizers.getOrDefault(skillId, Collections.emptyMap());
	}

	public void abortChannelization()
	{
		for (Map<Integer, Creature> map : this._channelizers.values())
		{
			for (Creature channelizer : map.values())
			{
				channelizer.abortCast();
			}
		}

		this._channelizers.clear();
	}

	public boolean isChannelized()
	{
		for (Map<Integer, Creature> map : this._channelizers.values())
		{
			if (!map.isEmpty())
			{
				return true;
			}
		}

		return false;
	}
}
