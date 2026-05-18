package net.sf.l2jdev.gameserver.managers;

import java.io.File;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.config.RatesConfig;
import net.sf.l2jdev.gameserver.data.holders.InstanceReenterTimeHolder;
import net.sf.l2jdev.gameserver.data.holders.StringStringHolder;
import net.sf.l2jdev.gameserver.data.xml.DoorData;
import net.sf.l2jdev.gameserver.data.xml.SpawnData;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.npc.MinionHolder;
import net.sf.l2jdev.gameserver.model.actor.templates.DoorTemplate;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.instancezone.InstanceReenterType;
import net.sf.l2jdev.gameserver.model.instancezone.InstanceRemoveBuffType;
import net.sf.l2jdev.gameserver.model.instancezone.InstanceTeleportType;
import net.sf.l2jdev.gameserver.model.instancezone.InstanceTemplate;
import net.sf.l2jdev.gameserver.model.instancezone.conditions.Condition;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.model.spawns.SpawnTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class InstanceManager implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(InstanceManager.class.getName());
	public static final String DELETE_INSTANCE_TIME = "DELETE FROM character_instance_time WHERE charId=? AND instanceId=?";
	private final Map<Integer, String> _instanceNames = new HashMap<>();
	private final List<StringStringHolder> _instanceTemplateNames = new LinkedList<>();
	private final Map<Integer, InstanceTemplate> _instanceTemplates = new ConcurrentHashMap<>();
	private int _currentInstanceId = 0;
	private final Map<Integer, Instance> _instanceWorlds = new ConcurrentHashMap<>();
	private final Map<Integer, Map<Integer, Long>> _playerTimes = new ConcurrentHashMap<>();

	protected InstanceManager()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._instanceNames.clear();
		this.parseDatapackFile("data/InstanceNames.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._instanceNames.size() + " instance names.");
		this._instanceTemplates.clear();
		this.parseDatapackDirectory("data/instances", true);
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._instanceTemplates.size() + " instance templates.");
		this._playerTimes.clear();
		this.restoreInstanceTimes();
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded instance reenter times for " + this._playerTimes.size() + " players.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, IXmlReader::isNode, listNode -> {
			String s0$ = listNode.getNodeName();
			switch (s0$)
			{
				case "list":
					this.parseInstanceName(listNode);
					break;
				case "instance":
					this.parseInstanceTemplate(listNode, file);
			}
		});
	}

	private void parseInstanceName(Node n)
	{
		this.forEach(n, "instance", instanceNode -> {
			NamedNodeMap attrs = instanceNode.getAttributes();
			int id = this.parseInteger(attrs, "id");
			String name = this.parseString(attrs, "name");
			this._instanceNames.put(id, name);
			this._instanceTemplateNames.add(new StringStringHolder(String.valueOf(id), name));
		});
	}

	private void parseInstanceTemplate(Node instanceNode, File file)
	{
		int id = this.parseInteger(instanceNode.getAttributes(), "id");
		if (this._instanceTemplates.containsKey(id))
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Instance template with ID " + id + " already exists");
		}
		else
		{
			InstanceTemplate template = new InstanceTemplate(new StatSet(this.parseAttributes(instanceNode)));
			if (template.getName() == null)
			{
				template.setName(this._instanceNames.get(id));
			}

			this.forEach(instanceNode, IXmlReader::isNode, innerNode -> {
				String s0$ = innerNode.getNodeName();
				switch (s0$)
				{
					case "time":
					{
						NamedNodeMap attrs = innerNode.getAttributes();
						template.setDuration(this.parseInteger(attrs, "duration", -1));
						template.setEmptyDestroyTime(this.parseInteger(attrs, "empty", -1).intValue());
						template.setEjectTime(this.parseInteger(attrs, "eject", -1));
						break;
					}
					case "misc":
					{
						NamedNodeMap attrs = innerNode.getAttributes();
						template.allowPlayerSummon(this.parseBoolean(attrs, "allowPlayerSummon", false));
						template.setPvP(this.parseBoolean(attrs, "isPvP", false));
						break;
					}
					case "rates":
					{
						NamedNodeMap attrs = innerNode.getAttributes();
						template.setExpRate(this.parseFloat(attrs, "exp", RatesConfig.RATE_INSTANCE_XP));
						template.setSPRate(this.parseFloat(attrs, "sp", RatesConfig.RATE_INSTANCE_SP));
						template.setExpPartyRate(this.parseFloat(attrs, "partyExp", RatesConfig.RATE_INSTANCE_PARTY_XP));
						template.setSPPartyRate(this.parseFloat(attrs, "partySp", RatesConfig.RATE_INSTANCE_PARTY_SP));
						break;
					}
					case "locations":
						this.forEach(innerNode, IXmlReader::isNode, locationsNode -> {
							String s0$x = locationsNode.getNodeName();
							switch (s0$x)
							{
								case "enter":
								{
									InstanceTeleportType typex = this.parseEnum(locationsNode.getAttributes(), InstanceTeleportType.class, "type");
									List<Location> locations = new ArrayList<>();
									this.forEach(locationsNode, "location", locationNode -> locations.add(this.parseLocation(locationNode)));
									template.setEnterLocation(typex, locations);
									break;
								}
								case "exit":
								{
									InstanceTeleportType typexx = this.parseEnum(locationsNode.getAttributes(), InstanceTeleportType.class, "type");
									if (typexx.equals(InstanceTeleportType.ORIGIN))
									{
										template.setExitLocation(typexx, null);
									}
									else if (typexx.equals(InstanceTeleportType.TOWN))
									{
										template.setExitLocation(typexx, null);
									}
									else
									{
										List<Location> locationsx = new ArrayList<>();
										this.forEach(locationsNode, "location", locationNode -> locationsx.add(this.parseLocation(locationNode)));
										if (locationsx.isEmpty())
										{
											LOGGER.warning(this.getClass().getSimpleName() + ": Missing exit location data for instance " + template.getName() + " (" + template.getId() + ")!");
										}
										else
										{
											template.setExitLocation(typexx, locationsx);
										}
									}
								}
							}
						});
						break;
					case "spawnlist":
						List<SpawnTemplate> spawns = new ArrayList<>();
						SpawnData.getInstance().parseSpawn(innerNode, file, spawns);
						template.addSpawns(spawns);
						break;
					case "doorlist":
						for (Node doorNode = innerNode.getFirstChild(); doorNode != null; doorNode = doorNode.getNextSibling())
						{
							if (doorNode.getNodeName().equals("door"))
							{
								StatSet parsedSet = DoorData.getInstance().parseDoor(doorNode);
								StatSet mergedSet = new StatSet();
								int doorId = parsedSet.getInt("id");
								StatSet templateSet = DoorData.getInstance().getDoorTemplate(doorId);
								if (templateSet != null)
								{
									mergedSet.merge(templateSet);
								}
								else
								{
									LOGGER.warning(this.getClass().getSimpleName() + ": Cannot find template for door: " + doorId + ", instance: " + template.getName() + " (" + template.getId() + ")");
								}

								mergedSet.merge(parsedSet);

								try
								{
									template.addDoor(doorId, new DoorTemplate(mergedSet));
								}
								catch (Exception var17)
								{
									LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Cannot initialize template for door: " + doorId + ", instance: " + template.getName() + " (" + template.getId() + ")", var17);
								}
							}
						}
						break;
					case "removeBuffs":
						InstanceRemoveBuffType removeBuffType = this.parseEnum(innerNode.getAttributes(), InstanceRemoveBuffType.class, "type");
						List<Integer> exceptionBuffList = new ArrayList<>();

						for (Node ex = innerNode.getFirstChild(); ex != null; ex = ex.getNextSibling())
						{
							if (ex.getNodeName().equals("skill"))
							{
								exceptionBuffList.add(this.parseInteger(ex.getAttributes(), "id"));
							}
						}

						template.setRemoveBuff(removeBuffType, exceptionBuffList);
						break;
					case "reenter":
						InstanceReenterType type = this.parseEnum(innerNode.getAttributes(), InstanceReenterType.class, "apply", InstanceReenterType.NONE);
						List<InstanceReenterTimeHolder> data = new ArrayList<>();

						for (Node e = innerNode.getFirstChild(); e != null; e = e.getNextSibling())
						{
							if (e.getNodeName().equals("reset"))
							{
								NamedNodeMap attrsx = e.getAttributes();
								int time = this.parseInteger(attrsx, "time", -1);
								if (time > 0)
								{
									data.add(new InstanceReenterTimeHolder(time));
								}
								else
								{
									DayOfWeek day = this.parseEnum(attrsx, DayOfWeek.class, "day");
									int hour = this.parseInteger(attrsx, "hour", -1);
									int minute = this.parseInteger(attrsx, "minute", -1);
									data.add(new InstanceReenterTimeHolder(day, hour, minute));
								}
							}
						}

						template.setReenterData(type, data);
						break;
					case "parameters":
						Map<String, Object> parameters = new HashMap<>();

						for (Node parameterNode = innerNode.getFirstChild(); parameterNode != null; parameterNode = parameterNode.getNextSibling())
						{
							NamedNodeMap attributes = parameterNode.getAttributes();
							String s0$x = parameterNode.getNodeName().toLowerCase();
							switch (s0$x)
							{
								case "param":
									parameters.put(this.parseString(attributes, "name"), this.parseString(attributes, "value"));
									break;
								case "skill":
									parameters.put(this.parseString(attributes, "name"), new SkillHolder(this.parseInteger(attributes, "id"), this.parseInteger(attributes, "level")));
									break;
								case "location":
									parameters.put(this.parseString(attributes, "name"), new Location(this.parseInteger(attributes, "x"), this.parseInteger(attributes, "y"), this.parseInteger(attributes, "z"), this.parseInteger(attributes, "heading", 0)));
									break;
								case "minions":
									List<MinionHolder> minions = new ArrayList<>(1);
									Node minionNode = parameterNode.getFirstChild();

									for (; minionNode != null; minionNode = minionNode.getNextSibling())
									{
										if (minionNode.getNodeName().equalsIgnoreCase("npc"))
										{
											attributes = minionNode.getAttributes();
											minions.add(new MinionHolder(this.parseInteger(attributes, "id"), this.parseInteger(attributes, "count"), this.parseInteger(attributes, "max", 0), this.parseInteger(attributes, "respawnTime").intValue(), this.parseInteger(attributes, "weightPoint", 0)));
										}
									}

									if (!minions.isEmpty())
									{
										parameters.put(this.parseString(parameterNode.getAttributes(), "name"), minions);
									}
							}
						}

						template.setParameters(parameters);
						break;
					case "conditions":
						List<Condition> conditions = new ArrayList<>();

						for (Node conditionNode = innerNode.getFirstChild(); conditionNode != null; conditionNode = conditionNode.getNextSibling())
						{
							if (conditionNode.getNodeName().equals("condition"))
							{
								NamedNodeMap attrsx = conditionNode.getAttributes();
								String conditionType = this.parseString(attrsx, "type");
								boolean onlyLeader = this.parseBoolean(attrsx, "onlyLeader", false);
								boolean showMessageAndHtml = this.parseBoolean(attrsx, "showMessageAndHtml", false);
								StatSet params = null;

								for (Node f = conditionNode.getFirstChild(); f != null; f = f.getNextSibling())
								{
									if (f.getNodeName().equals("param"))
									{
										if (params == null)
										{
											params = new StatSet();
										}

										params.set(this.parseString(f.getAttributes(), "name"), this.parseString(f.getAttributes(), "value"));
									}
								}

								if (params == null)
								{
									params = StatSet.EMPTY_STATSET;
								}

								try
								{
									Class<?> clazz = Class.forName("net.sf.l2jdev.gameserver.model.instancezone.conditions.Condition" + conditionType);
									Constructor<?> constructor = clazz.getConstructor(InstanceTemplate.class, StatSet.class, boolean.class, boolean.class);
									conditions.add((Condition) constructor.newInstance(template, params, onlyLeader, showMessageAndHtml));
								}
								catch (Exception var16)
								{
									LOGGER.warning(this.getClass().getSimpleName() + ": Unknown condition type " + conditionType + " for instance " + template.getName() + " (" + id + ")!");
								}
							}
						}

						template.setConditions(conditions);
				}
			});
			this._instanceTemplates.put(id, template);
		}
	}

	private Location parseLocation(Node node)
	{
		NamedNodeMap attributes = node.getAttributes();
		int x = this.parseInteger(attributes, "x");
		int y = this.parseInteger(attributes, "y");
		int z = this.parseInteger(attributes, "z");
		int heading = this.parseInteger(attributes, "heading", 0);
		return new Location(x, y, z, heading);
	}

	public Instance createInstance()
	{
		return new Instance(this.getNewInstanceId(), new InstanceTemplate(StatSet.EMPTY_STATSET), null);
	}

	public Instance createInstance(InstanceTemplate template, Player player)
	{
		return template != null ? new Instance(this.getNewInstanceId(), template, player) : null;
	}

	public Instance createInstance(int id, Player player)
	{
		if (!this._instanceTemplates.containsKey(id))
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Missing template for instance with id " + id + "!");
			return null;
		}
		return new Instance(this.getNewInstanceId(), this._instanceTemplates.get(id), player);
	}

	public Instance getInstance(int instanceId)
	{
		return this._instanceWorlds.get(instanceId);
	}

	public Collection<Instance> getInstances()
	{
		return this._instanceWorlds.values();
	}

	public Instance getPlayerInstance(Player player, boolean isInside)
	{
		for (Instance instance : this._instanceWorlds.values())
		{
			if (isInside)
			{
				if (instance.containsPlayer(player))
				{
					return instance;
				}
			}
			else if (instance.isAllowed(player))
			{
				return instance;
			}
		}

		return null;
	}

	private synchronized int getNewInstanceId()
	{
		do
		{
			if (this._currentInstanceId == Integer.MAX_VALUE)
			{
				this._currentInstanceId = 0;
			}

			this._currentInstanceId++;
		}
		while (this._instanceWorlds.containsKey(this._currentInstanceId));

		return this._currentInstanceId;
	}

	public void register(Instance instance)
	{
		int instanceId = instance.getId();
		if (!this._instanceWorlds.containsKey(instanceId))
		{
			this._instanceWorlds.put(instanceId, instance);
		}
	}

	public void unregister(int instanceId)
	{
		if (this._instanceWorlds.containsKey(instanceId))
		{
			this._instanceWorlds.remove(instanceId);
		}
	}

	public String getInstanceName(int templateId)
	{
		return this._instanceNames.get(templateId);
	}

	public Collection<StringStringHolder> getInstanceTemplateNames()
	{
		return this._instanceTemplateNames;
	}

	private void restoreInstanceTimes()
	{
		try (Connection con = DatabaseFactory.getConnection(); Statement ps = con.createStatement(); ResultSet rs = ps.executeQuery("SELECT * FROM character_instance_time ORDER BY charId");)
		{
			while (rs.next())
			{
				long time = rs.getLong("time");
				if (time > System.currentTimeMillis())
				{
					int charId = rs.getInt("charId");
					int instanceId = rs.getInt("instanceId");
					this.setReenterPenalty(charId, instanceId, time);
				}
			}
		}
		catch (Exception var14)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Cannot restore players instance reenter data: ", var14);
		}
	}

	public Map<Integer, Long> getAllInstanceTimes(Player player)
	{
		Map<Integer, Long> instanceTimes = this._playerTimes.get(player.getObjectId());
		if (instanceTimes != null && !instanceTimes.isEmpty())
		{
			List<Integer> invalidPenalty = new ArrayList<>(instanceTimes.size());

			for (Entry<Integer, Long> entry : instanceTimes.entrySet())
			{
				if (entry.getValue() <= System.currentTimeMillis())
				{
					invalidPenalty.add(entry.getKey());
				}
			}

			if (!invalidPenalty.isEmpty())
			{
				try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM character_instance_time WHERE charId=? AND instanceId=?");)
				{
					for (Integer id : invalidPenalty)
					{
						ps.setInt(1, player.getObjectId());
						ps.setInt(2, id);
						ps.addBatch();
					}

					ps.executeBatch();
					invalidPenalty.forEach(instanceTimes::remove);
				}
				catch (Exception var12)
				{
					LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Cannot delete instance character reenter data: ", var12);
				}
			}

			return instanceTimes;
		}
		return Collections.emptyMap();
	}

	public void setReenterPenalty(int objectId, int id, long time)
	{
		this._playerTimes.computeIfAbsent(objectId, _ -> new ConcurrentHashMap<>()).put(id, time);
	}

	public long getInstanceTime(Player player, int id)
	{
		Map<Integer, Long> playerData = this._playerTimes.get(player.getObjectId());
		if (playerData != null && playerData.containsKey(id))
		{
			long time = playerData.get(id);
			if (time <= System.currentTimeMillis())
			{
				this.deleteInstanceTime(player, id);
				return -1L;
			}
			return time;
		}
		return -1L;
	}

	public void deleteInstanceTime(Player player, int id)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM character_instance_time WHERE charId=? AND instanceId=?");)
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, id);
			ps.execute();
			if (this._playerTimes.get(player.getObjectId()) != null)
			{
				this._playerTimes.get(player.getObjectId()).remove(id);
			}
		}
		catch (Exception var11)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not delete character instance reenter data: ", var11);
		}
	}

	public InstanceTemplate getInstanceTemplate(int id)
	{
		return this._instanceTemplates.get(id);
	}

	public Collection<InstanceTemplate> getInstanceTemplates()
	{
		return this._instanceTemplates.values();
	}

	public long getWorldCount(int templateId)
	{
		long count = 0L;

		for (Instance i : this._instanceWorlds.values())
		{
			if (i.getTemplateId() == templateId)
			{
				count++;
			}
		}

		return count;
	}

	public static InstanceManager getInstance()
	{
		return InstanceManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final InstanceManager INSTANCE = new InstanceManager();
	}
}
