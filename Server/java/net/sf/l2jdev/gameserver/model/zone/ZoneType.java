package net.sf.l2jdev.gameserver.model.zone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.TeleportWhereType;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.ListenersContainer;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureZoneEnter;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureZoneExit;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public abstract class ZoneType extends ListenersContainer
{
	protected static final Logger LOGGER = Logger.getLogger(ZoneType.class.getName());
	private final int _id;
	protected ZoneForm _zone;
	protected List<ZoneForm> _blockedZones;
	private final Map<Integer, Creature> _characterList = new ConcurrentHashMap<>();
	private boolean _checkAffected = false;
	private String _name = null;
	private int _minLevel;
	private int _maxLevel;
	private int[] _race;
	private int[] _class;
	private char _classType;
	private InstanceType _target = InstanceType.Creature;
	private boolean _allowStore;
	private boolean _enabled;
	private AbstractZoneSettings _settings;
	private int _instanceTemplateId;
	private Map<Integer, Boolean> _enabledInInstance;

	protected ZoneType(int id)
	{
		this._id = id;
		this._minLevel = 0;
		this._maxLevel = 255;
		this._classType = 0;
		this._race = null;
		this._class = null;
		this._allowStore = true;
		this._enabled = true;
	}

	public int getId()
	{
		return this._id;
	}

	public void setParameter(String name, String value)
	{
		this._checkAffected = true;
		if (name.equals("name"))
		{
			this._name = value;
		}
		else if (name.equals("affectedLvlMin"))
		{
			this._minLevel = Integer.parseInt(value);
		}
		else if (name.equals("affectedLvlMax"))
		{
			this._maxLevel = Integer.parseInt(value);
		}
		else if (name.equals("affectedRace"))
		{
			if (this._race == null)
			{
				this._race = new int[1];
				this._race[0] = Integer.parseInt(value);
			}
			else
			{
				int[] temp = new int[this._race.length + 1];

				int i;
				for (i = 0; i < this._race.length; i++)
				{
					temp[i] = this._race[i];
				}

				temp[i] = Integer.parseInt(value);
				this._race = temp;
			}
		}
		else if (name.equals("affectedClassId"))
		{
			if (this._class == null)
			{
				this._class = new int[1];
				this._class[0] = Integer.parseInt(value);
			}
			else
			{
				int[] temp = new int[this._class.length + 1];

				int i;
				for (i = 0; i < this._class.length; i++)
				{
					temp[i] = this._class[i];
				}

				temp[i] = Integer.parseInt(value);
				this._class = temp;
			}
		}
		else if (name.equals("affectedClassType"))
		{
			if (value.equals("Fighter"))
			{
				this._classType = 1;
			}
			else
			{
				this._classType = 2;
			}
		}
		else if (name.equals("targetClass"))
		{
			this._target = Enum.valueOf(InstanceType.class, value);
		}
		else if (name.equals("allowStore"))
		{
			this._allowStore = Boolean.parseBoolean(value);
		}
		else if (name.equals("default_enabled"))
		{
			this._enabled = Boolean.parseBoolean(value);
		}
		else if (name.equals("instanceId"))
		{
			this._instanceTemplateId = Integer.parseInt(value);
		}
		else
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Unknown parameter - " + name + " in zone: " + this._id);
		}
	}

	private boolean isAffected(Creature creature)
	{
		Instance world = creature.getInstanceWorld();
		if (world != null)
		{
			if ((world.getTemplateId() != this._instanceTemplateId) || !this.isEnabled(creature.getInstanceId()))
			{
				return false;
			}
		}
		else
		{
			if ((this._instanceTemplateId > 0) || !this.isEnabled())
			{
				return false;
			}
		}

		if (creature.getLevel() >= this._minLevel && creature.getLevel() <= this._maxLevel)
		{
			if (!creature.isInstanceTypes(this._target))
			{
				return false;
			}
			if (creature.isPlayer())
			{
				if (this._classType != 0)
				{
					if (creature.asPlayer().isMageClass())
					{
						if (this._classType == 1)
						{
							return false;
						}
					}
					else if (this._classType == 2)
					{
						return false;
					}
				}

				if (this._race != null)
				{
					boolean ok = false;

					for (int element : this._race)
					{
						if (creature.getRace().ordinal() == element)
						{
							ok = true;
							break;
						}
					}

					if (!ok)
					{
						return false;
					}
				}

				if (this._class != null)
				{
					boolean ok = false;

					for (int _clas : this._class)
					{
						if (creature.asPlayer().getPlayerClass().getId() == _clas)
						{
							ok = true;
							break;
						}
					}

					if (!ok)
					{
						return false;
					}
				}
			}

			return true;
		}
		if (creature.isPlayer())
		{
			creature.asPlayer().sendPacket(new ExShowScreenMessage(SystemMessageId.YOU_CANNOT_ENTER_AS_YOUR_LEVEL_DOES_NOT_MEET_THE_REQUIREMENTS, 2, 10000));
		}

		return false;
	}

	public void setZone(ZoneForm zone)
	{
		if (this._zone != null)
		{
			throw new IllegalStateException("Zone already set");
		}
		this._zone = zone;
	}

	public ZoneForm getZone()
	{
		return this._zone;
	}

	public void setBlockedZones(List<ZoneForm> blockedZones)
	{
		if (this._blockedZones != null)
		{
			throw new IllegalStateException("Blocked zone already set");
		}
		this._blockedZones = blockedZones;
	}

	public List<ZoneForm> getBlockedZones()
	{
		return this._blockedZones;
	}

	public void setName(String name)
	{
		this._name = name;
	}

	public String getName()
	{
		return this._name;
	}

	public boolean isInsideZone(int x, int y, int z)
	{
		return this._zone != null && this._zone.isInsideZone(x, y, z) && !this.isInsideBlockedZone(x, y, z);
	}

	public boolean isInsideBlockedZone(int x, int y, int z)
	{
		if (this._blockedZones != null && !this._blockedZones.isEmpty())
		{
			for (ZoneForm zone : this._blockedZones)
			{
				if (zone.isInsideZone(x, y, z))
				{
					return true;
				}
			}

			return false;
		}
		return false;
	}

	public boolean isInsideZone(int x, int y)
	{
		return this.isInsideZone(x, y, this._zone.getHighZ());
	}

	public boolean isInsideZone(ILocational loc)
	{
		return this.isInsideZone(loc.getX(), loc.getY(), loc.getZ());
	}

	public boolean isInsideZone(WorldObject object)
	{
		return this.isInsideZone(object.getX(), object.getY(), object.getZ());
	}

	public double getDistanceToZone(int x, int y)
	{
		return this._zone.getDistanceToZone(x, y);
	}

	public double getDistanceToZone(WorldObject object)
	{
		return this._zone.getDistanceToZone(object.getX(), object.getY());
	}

	public void revalidateInZone(Creature creature)
	{
		if (this.isInsideZone(creature))
		{
			if (this._checkAffected && !this.isAffected(creature))
			{
				return;
			}

			if (this._characterList.putIfAbsent(creature.getObjectId(), creature) == null)
			{
				if (EventDispatcher.getInstance().hasListener(EventType.ON_CREATURE_ZONE_ENTER, this))
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnCreatureZoneEnter(creature, this), this);
				}

				this.onEnter(creature);
			}
		}
		else
		{
			this.removeCharacter(creature);
		}
	}

	public void removeCharacter(Creature creature)
	{
		if (this._characterList.containsKey(creature.getObjectId()))
		{
			if (EventDispatcher.getInstance().hasListener(EventType.ON_CREATURE_ZONE_EXIT, this))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnCreatureZoneExit(creature, this), this);
			}

			this._characterList.remove(creature.getObjectId());
			this.onExit(creature);
		}
	}

	public boolean isCharacterInZone(Creature creature)
	{
		return this._characterList.containsKey(creature.getObjectId());
	}

	public AbstractZoneSettings getSettings()
	{
		return this._settings;
	}

	public void setSettings(AbstractZoneSettings settings)
	{
		if (this._settings != null)
		{
			this._settings.clear();
		}

		this._settings = settings;
	}

	protected abstract void onEnter(Creature var1);

	protected abstract void onExit(Creature var1);

	public void onDieInside(Creature creature)
	{
	}

	public void onReviveInside(Creature creature)
	{
	}

	public void onPlayerLoginInside(Player player)
	{
	}

	public void onPlayerLogoutInside(Player player)
	{
	}

	public Map<Integer, Creature> getCharacters()
	{
		return this._characterList;
	}

	public Collection<Creature> getCharactersInside()
	{
		return this._characterList.values();
	}

	public List<Player> getPlayersInside()
	{
		List<Player> players = new ArrayList<>();

		for (Creature ch : this._characterList.values())
		{
			if (ch != null && ch.isPlayer())
			{
				players.add(ch.asPlayer());
			}
		}

		return players;
	}

	public void broadcastPacket(ServerPacket packet)
	{
		if (!this._characterList.isEmpty())
		{
			for (Creature creature : this._characterList.values())
			{
				if (creature != null && creature.isPlayer())
				{
					creature.sendPacket(packet);
				}
			}
		}
	}

	public InstanceType getTargetType()
	{
		return this._target;
	}

	public void setTargetType(InstanceType type)
	{
		this._target = type;
		this._checkAffected = true;
	}

	public boolean getAllowStore()
	{
		return this._allowStore;
	}

	public int getInstanceTemplateId()
	{
		return this._instanceTemplateId;
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + "[" + this._id + "]";
	}

	public void visualizeZone(int z)
	{
		this._zone.visualizeZone(z);
	}

	public void setEnabled(boolean value)
	{
		this._enabled = value;
	}

	public boolean isEnabled()
	{
		return this._enabled;
	}

	public void setEnabled(boolean state, int instanceId)
	{
		if (this._enabledInInstance == null)
		{
			synchronized (this)
			{
				if (this._enabledInInstance == null)
				{
					this._enabledInInstance = new ConcurrentHashMap<>();
				}
			}
		}

		this._enabledInInstance.put(instanceId, state);
	}

	public boolean isEnabled(int instanceId)
	{
		return this._enabledInInstance != null ? this._enabledInInstance.getOrDefault(instanceId, this._enabled) : this._enabled;
	}

	public void oustAllPlayers()
	{
		for (Creature obj : this._characterList.values())
		{
			if (obj != null && obj.isPlayer())
			{
				Player player = obj.asPlayer();
				if (player.isOnline())
				{
					player.teleToLocation(TeleportWhereType.TOWN);
				}
			}
		}
	}

	public void movePlayersTo(Location loc)
	{
		if (!this._characterList.isEmpty())
		{
			for (Creature creature : this._characterList.values())
			{
				if (creature != null && creature.isPlayer())
				{
					Player player = creature.asPlayer();
					if (player.isOnline())
					{
						player.teleToLocation(loc);
					}
				}
			}
		}
	}
}
