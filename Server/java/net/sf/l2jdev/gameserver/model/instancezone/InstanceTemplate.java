package net.sf.l2jdev.gameserver.model.instancezone;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.RatesConfig;
import net.sf.l2jdev.gameserver.data.holders.InstanceReenterTimeHolder;
import net.sf.l2jdev.gameserver.managers.InstanceManager;
import net.sf.l2jdev.gameserver.managers.MapRegionManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Playable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.GroupType;
import net.sf.l2jdev.gameserver.model.actor.templates.DoorTemplate;
import net.sf.l2jdev.gameserver.model.events.ListenersContainer;
import net.sf.l2jdev.gameserver.model.groups.AbstractPlayerGroup;
import net.sf.l2jdev.gameserver.model.instancezone.conditions.Condition;
import net.sf.l2jdev.gameserver.model.instancezone.conditions.ConditionCommandChannel;
import net.sf.l2jdev.gameserver.model.instancezone.conditions.ConditionGroupMax;
import net.sf.l2jdev.gameserver.model.instancezone.conditions.ConditionGroupMin;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.spawns.SpawnTemplate;
import net.sf.l2jdev.gameserver.model.variables.PlayerVariables;

public class InstanceTemplate extends ListenersContainer
{
	private int _templateId = -1;
	private String _name = "UnknownInstance";
	private int _duration = -1;
	private long _emptyDestroyTime = -1L;
	private int _ejectTime = GeneralConfig.EJECT_DEAD_PLAYER_TIME;
	private int _maxWorldCount = -1;
	private boolean _isPvP = false;
	private boolean _allowPlayerSummon = false;
	private float _expRate = RatesConfig.RATE_INSTANCE_XP;
	private float _spRate = RatesConfig.RATE_INSTANCE_SP;
	private float _expPartyRate = RatesConfig.RATE_INSTANCE_PARTY_XP;
	private float _spPartyRate = RatesConfig.RATE_INSTANCE_PARTY_SP;
	private StatSet _parameters = StatSet.EMPTY_STATSET;
	private final Map<Integer, DoorTemplate> _doors = new HashMap<>();
	private final List<SpawnTemplate> _spawns = new ArrayList<>();
	private InstanceTeleportType _enterLocationType = InstanceTeleportType.NONE;
	private List<Location> _enterLocations = null;
	private InstanceTeleportType _exitLocationType = InstanceTeleportType.NONE;
	private List<Location> _exitLocations = null;
	private InstanceReenterType _reenterType = InstanceReenterType.NONE;
	private List<InstanceReenterTimeHolder> _reenterData = Collections.emptyList();
	private InstanceRemoveBuffType _removeBuffType = InstanceRemoveBuffType.NONE;
	private List<Integer> _removeBuffExceptions = Collections.emptyList();
	private List<Condition> _conditions = Collections.emptyList();
	private int _groupMask = GroupType.NONE.getMask();

	public InstanceTemplate(StatSet set)
	{
		this._templateId = set.getInt("id", 0);
		this._name = set.getString("name", null);
		this._maxWorldCount = set.getInt("maxWorlds", -1);
	}

	public void setName(String name)
	{
		if (name != null && !name.isEmpty())
		{
			this._name = name;
		}
	}

	public void setDuration(int duration)
	{
		if (duration > 0)
		{
			this._duration = duration;
		}
	}

	public void setEmptyDestroyTime(long emptyDestroyTime)
	{
		if (emptyDestroyTime >= 0L)
		{
			this._emptyDestroyTime = TimeUnit.MINUTES.toMillis(emptyDestroyTime);
		}
	}

	public void setEjectTime(int ejectTime)
	{
		if (ejectTime >= 0)
		{
			this._ejectTime = ejectTime;
		}
	}

	public void allowPlayerSummon(boolean value)
	{
		this._allowPlayerSummon = value;
	}

	public void setPvP(boolean value)
	{
		this._isPvP = value;
	}

	public void setParameters(Map<String, Object> set)
	{
		if (!set.isEmpty())
		{
			this._parameters = new StatSet(Collections.unmodifiableMap(set));
		}
	}

	public void addDoor(int templateId, DoorTemplate template)
	{
		this._doors.put(templateId, template);
	}

	public void addSpawns(List<SpawnTemplate> spawns)
	{
		this._spawns.addAll(spawns);
	}

	public void setEnterLocation(InstanceTeleportType type, List<Location> locations)
	{
		this._enterLocationType = type;
		this._enterLocations = locations;
	}

	public void setExitLocation(InstanceTeleportType type, List<Location> locations)
	{
		this._exitLocationType = type;
		this._exitLocations = locations;
	}

	public void setReenterData(InstanceReenterType type, List<InstanceReenterTimeHolder> holder)
	{
		this._reenterType = type;
		this._reenterData = holder;
	}

	public void setRemoveBuff(InstanceRemoveBuffType type, List<Integer> exceptionList)
	{
		this._removeBuffType = type;
		this._removeBuffExceptions = exceptionList;
	}

	public void setConditions(List<Condition> conditions)
	{
		this._conditions = conditions;
		boolean onlyCC = false;
		int min = 1;
		int max = 1;

		for (Condition cond : this._conditions)
		{
			if (cond instanceof ConditionCommandChannel)
			{
				onlyCC = true;
			}
			else if (cond instanceof ConditionGroupMin)
			{
				min = ((ConditionGroupMin) cond).getLimit();
			}
			else if (cond instanceof ConditionGroupMax)
			{
				max = ((ConditionGroupMax) cond).getLimit();
			}
		}

		this._groupMask = 0;
		if (!onlyCC)
		{
			if (min == 1)
			{
				this._groupMask = this._groupMask | GroupType.NONE.getMask();
			}

			int partySize = PlayerConfig.ALT_PARTY_MAX_MEMBERS;
			if (max > 1 && max <= partySize || min <= partySize && max > partySize)
			{
				this._groupMask = this._groupMask | GroupType.PARTY.getMask();
			}
		}

		if (onlyCC || max > 7)
		{
			this._groupMask = this._groupMask | GroupType.COMMAND_CHANNEL.getMask();
		}
	}

	public int getId()
	{
		return this._templateId;
	}

	public String getName()
	{
		return this._name;
	}

	public List<Location> getEnterLocations()
	{
		return this._enterLocations;
	}

	public Location getEnterLocation()
	{
		Location loc = null;
		switch (this._enterLocationType)
		{
			case RANDOM:
				loc = this._enterLocations.get(Rnd.get(this._enterLocations.size()));
				break;
			case FIXED:
				loc = this._enterLocations.get(0);
		}

		return loc;
	}

	public InstanceTeleportType getExitLocationType()
	{
		return this._exitLocationType;
	}

	public Location getExitLocation(Player player)
	{
		Location location = null;
		switch (this._exitLocationType)
		{
			case RANDOM:
				location = this._exitLocations.get(Rnd.get(this._exitLocations.size()));
				break;
			case FIXED:
				location = this._exitLocations.get(0);
				break;
			case ORIGIN:
				PlayerVariables vars = player.getVariables();
				if (vars.contains("INSTANCE_ORIGIN"))
				{
					int[] loc = vars.getIntArray("INSTANCE_ORIGIN", ";");
					if (loc != null && loc.length == 3)
					{
						location = new Location(loc[0], loc[1], loc[2]);
					}

					vars.remove("INSTANCE_ORIGIN");
				}
				break;
			case TOWN:
				if (player.getReputation() < 0)
				{
					location = MapRegionManager.getInstance().getNearestKarmaRespawn(player);
				}
				else
				{
					location = MapRegionManager.getInstance().getNearestTownRespawn(player);
				}
		}

		return location;
	}

	public long getEmptyDestroyTime()
	{
		return this._emptyDestroyTime;
	}

	public int getDuration()
	{
		return this._duration;
	}

	public int getEjectTime()
	{
		return this._ejectTime;
	}

	public boolean isPlayerSummonAllowed()
	{
		return this._allowPlayerSummon;
	}

	public boolean isPvP()
	{
		return this._isPvP;
	}

	public Map<Integer, DoorTemplate> getDoors()
	{
		return this._doors;
	}

	public List<SpawnTemplate> getSpawns()
	{
		return this._spawns;
	}

	public int getMaxWorlds()
	{
		return this._maxWorldCount;
	}

	public StatSet getParameters()
	{
		return this._parameters;
	}

	public boolean isRemoveBuffEnabled()
	{
		return this._removeBuffType != InstanceRemoveBuffType.NONE;
	}

	public void removePlayerBuff(Player player)
	{
		List<Playable> affected = new ArrayList<>();
		affected.add(player);
		player.getServitors().values().forEach(affected::add);
		if (player.hasPet())
		{
			affected.add(player.getPet());
		}

		if (this._removeBuffType == InstanceRemoveBuffType.ALL)
		{
			for (Playable playable : affected)
			{
				playable.stopAllEffectsExceptThoseThatLastThroughDeath();
			}
		}
		else
		{
			for (Playable playable : affected)
			{
				playable.getEffectList().stopEffects(info -> !info.getSkill().isIrreplaceableBuff() && info.getSkill().getBuffType().isBuff() && this.hasRemoveBuffException(info.getSkill()), true, true);
			}
		}
	}

	private boolean hasRemoveBuffException(Skill skill)
	{
		boolean containsSkill = this._removeBuffExceptions.contains(skill.getId());
		return this._removeBuffType == InstanceRemoveBuffType.BLACKLIST ? containsSkill : !containsSkill;
	}

	public InstanceReenterType getReenterType()
	{
		return this._reenterType;
	}

	public long calculateReenterTime()
	{
		long time = -1L;

		for (InstanceReenterTimeHolder data : this._reenterData)
		{
			if (data.getTime() > 0L)
			{
				time = System.currentTimeMillis() + data.getTime();
				break;
			}

			Calendar calendar = Calendar.getInstance();
			calendar.set(11, data.getHour());
			calendar.set(12, data.getMinute());
			calendar.set(13, 0);
			if (calendar.getTimeInMillis() <= System.currentTimeMillis())
			{
				calendar.add(5, 1);
			}

			if (data.getDay() != null)
			{
				int day = data.getDay().getValue() + 1;
				if (day > 7)
				{
					day = 1;
				}

				calendar.set(7, day);
				if (calendar.getTimeInMillis() <= System.currentTimeMillis())
				{
					calendar.add(4, 1);
				}
			}

			if (time == -1L || calendar.getTimeInMillis() < time)
			{
				time = calendar.getTimeInMillis();
			}
		}

		return time;
	}

	private final boolean groupMaskContains(GroupType type)
	{
		int flag = type.getMask();
		return (this._groupMask & flag) == flag;
	}

	private final GroupType getEnterGroupType(Player player)
	{
		if (this._groupMask == 0)
		{
			return null;
		}
		else if (player.isGM())
		{
			return GroupType.NONE;
		}
		else
		{
			GroupType playerGroup = player.getGroupType();
			if (this.groupMaskContains(playerGroup))
			{
				return playerGroup;
			}
			GroupType type = GroupType.getByMask(this._groupMask);
			if (type != null)
			{
				return type;
			}
			for (GroupType t : GroupType.values())
			{
				if (t != playerGroup && this.groupMaskContains(t))
				{
					return t;
				}
			}

			return null;
		}
	}

	public List<Player> getEnterGroup(Player player)
	{
		GroupType type = this.getEnterGroupType(player);
		if (type == null)
		{
			return null;
		}
		List<Player> group = new ArrayList<>();
		group.add(player);
		AbstractPlayerGroup pGroup = null;
		if (type == GroupType.PARTY)
		{
			pGroup = player.getParty();
		}
		else if (type == GroupType.COMMAND_CHANNEL)
		{
			pGroup = player.getCommandChannel();
		}

		if (pGroup != null)
		{
			for (Player member : pGroup.getMembers())
			{
				if (!member.equals(player))
				{
					group.add(member);
				}
			}
		}

		return group;
	}

	public boolean validateConditions(List<Player> group, Npc npc, BiConsumer<Player, String> htmlCallback)
	{
		for (Condition cond : this._conditions)
		{
			if (!cond.validate(npc, group, htmlCallback))
			{
				return false;
			}
		}

		return true;
	}

	public void applyConditionEffects(List<Player> group)
	{
		this._conditions.forEach(c -> c.applyEffect(group));
	}

	public float getExpRate()
	{
		return this._expRate;
	}

	public void setExpRate(float expRate)
	{
		this._expRate = expRate;
	}

	public float getSPRate()
	{
		return this._spRate;
	}

	public void setSPRate(float spRate)
	{
		this._spRate = spRate;
	}

	public float getExpPartyRate()
	{
		return this._expPartyRate;
	}

	public void setExpPartyRate(float expRate)
	{
		this._expPartyRate = expRate;
	}

	public float getSPPartyRate()
	{
		return this._spPartyRate;
	}

	public void setSPPartyRate(float spRate)
	{
		this._spPartyRate = spRate;
	}

	public long getWorldCount()
	{
		return InstanceManager.getInstance().getWorldCount(this.getId());
	}

	@Override
	public String toString()
	{
		return "ID: " + this._templateId + " Name: " + this._name;
	}
}
