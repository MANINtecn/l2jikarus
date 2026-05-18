package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldRegion;
import net.sf.l2jdev.gameserver.model.actor.instance.Door;
import net.sf.l2jdev.gameserver.model.actor.templates.DoorTemplate;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DoorData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(DoorData.class.getName());
	private final Map<String, Set<Integer>> _groups = new HashMap<>();
	private final Map<Integer, Door> _doors = new HashMap<>();
	private final Map<Integer, StatSet> _templates = new HashMap<>();

	protected DoorData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._doors.clear();
		this._groups.clear();
		this.parseDatapackFile("data/DoorData.xml");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "door", doorNode -> this.spawnDoor(this.parseDoor(doorNode))));
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._doors.size() + " doors.");
	}

	public StatSet parseDoor(Node doorNode)
	{
		StatSet params = new StatSet(this.parseAttributes(doorNode));
		params.set("baseHpMax", 1);
		this.forEach(doorNode, IXmlReader::isNode, innerDoorNode -> {
			NamedNodeMap attrs = innerDoorNode.getAttributes();
			if (innerDoorNode.getNodeName().equals("nodes"))
			{
				params.set("nodeZ", this.parseInteger(attrs, "nodeZ"));
				AtomicInteger count = new AtomicInteger();
				this.forEach(innerDoorNode, IXmlReader::isNode, nodes -> {
					NamedNodeMap nodeAttrs = nodes.getAttributes();
					if ("node".equals(nodes.getNodeName()))
					{
						params.set("nodeX_" + count.get(), this.parseInteger(nodeAttrs, "x"));
						params.set("nodeY_" + count.getAndIncrement(), this.parseInteger(nodeAttrs, "y"));
					}
				});
			}
			else if (attrs != null)
			{
				for (int i = 0; i < attrs.getLength(); i++)
				{
					Node att = attrs.item(i);
					params.set(att.getNodeName(), att.getNodeValue());
				}
			}
		});
		applyCollisions(params);
		return params;
	}

	private static void applyCollisions(StatSet set)
	{
		if (set.contains("nodeX_0") && set.contains("nodeY_0") && set.contains("nodeX_1") && set.contains("nodeY_1"))
		{
			int height = set.getInt("height", 150);
			int nodeX = set.getInt("nodeX_0");
			int nodeY = set.getInt("nodeY_0");
			int posX = set.getInt("nodeX_1");
			int posY = set.getInt("nodeY_1");
			int collisionRadius = Math.min(Math.abs(nodeX - posX), Math.abs(nodeY - posY));
			if (collisionRadius < 20)
			{
				collisionRadius = 20;
			}

			set.set("collision_radius", collisionRadius);
			set.set("collision_height", height);
		}
	}

	public Door spawnDoor(StatSet set)
	{
		DoorTemplate template = new DoorTemplate(set);
		Door door = this.spawnDoor(template, null);
		this._templates.put(door.getId(), set);
		this._doors.put(door.getId(), door);
		return door;
	}

	public Door spawnDoor(DoorTemplate template, Instance instance)
	{
		Door door = new Door(template);
		door.setCurrentHp(door.getMaxHp());
		if (instance != null)
		{
			door.setInstance(instance);
		}

		door.spawnMe(template.getX(), template.getY(), template.getZ());
		if (template.getGroupName() != null)
		{
			this._groups.computeIfAbsent(door.getGroupName(), _ -> new HashSet<>()).add(door.getId());
		}

		return door;
	}

	public StatSet getDoorTemplate(int doorId)
	{
		return this._templates.get(doorId);
	}

	public Door getDoor(int doorId)
	{
		return this._doors.get(doorId);
	}

	public Set<Integer> getDoorsByGroup(String groupName)
	{
		return this._groups.getOrDefault(groupName, Collections.emptySet());
	}

	public Collection<Door> getDoors()
	{
		return this._doors.values();
	}

	public boolean checkIfDoorsBetween(Location start, Location end, Instance instance)
	{
		return this.checkIfDoorsBetween(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ(), instance);
	}

	public boolean checkIfDoorsBetween(int x, int y, int z, int tx, int ty, int tz, Instance instance)
	{
		return this.checkIfDoorsBetween(x, y, z, tx, ty, tz, instance, false);
	}

	public boolean checkIfDoorsBetween(int x, int y, int z, int tx, int ty, int tz, Instance instance, boolean doubleFaceCheck)
	{
		Collection<Door> doors;
		if (instance == null)
		{
			WorldRegion region = World.getInstance().getRegion(x, y, z);
			if (region != null)
			{
				doors = region.getDoors();
			}
			else
			{
				doors = null;
			}
		}
		else
		{
			doors = instance.getDoors();
		}

		if (doors != null && !doors.isEmpty())
		{
			for (Door doorInst : doors)
			{
				if (instance == doorInst.getInstanceWorld() && !doorInst.isDead() && !doorInst.isOpen() && doorInst.checkCollision() && doorInst.getX(0) != 0)
				{
					boolean intersectFace = false;

					for (int i = 0; i < 4; i++)
					{
						int j = i + 1 < 4 ? i + 1 : 0;
						int denominator = (ty - y) * (doorInst.getX(i) - doorInst.getX(j)) - (tx - x) * (doorInst.getY(i) - doorInst.getY(j));
						if (denominator != 0)
						{
							float multiplier1 = (float) ((doorInst.getX(j) - doorInst.getX(i)) * (y - doorInst.getY(i)) - (doorInst.getY(j) - doorInst.getY(i)) * (x - doorInst.getX(i))) / denominator;
							float multiplier2 = (float) ((tx - x) * (y - doorInst.getY(i)) - (ty - y) * (x - doorInst.getX(i))) / denominator;
							if (multiplier1 >= 0.0F && multiplier1 <= 1.0F && multiplier2 >= 0.0F && multiplier2 <= 1.0F)
							{
								int intersectZ = Math.round(z + multiplier1 * (tz - z));
								if (intersectZ > doorInst.getZMin() && intersectZ < doorInst.getZMax())
								{
									if (!doubleFaceCheck || intersectFace)
									{
										return true;
									}

									intersectFace = true;
								}
							}
						}
					}
				}
			}

			return false;
		}
		return false;
	}

	public static DoorData getInstance()
	{
		return DoorData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final DoorData INSTANCE = new DoorData();
	}
}
