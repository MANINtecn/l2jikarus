package net.sf.l2jdev.gameserver.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import net.sf.l2jdev.gameserver.handler.ActionHandler;
import net.sf.l2jdev.gameserver.handler.ActionShiftHandler;
import net.sf.l2jdev.gameserver.handler.IActionHandler;
import net.sf.l2jdev.gameserver.handler.IActionShiftHandler;
import net.sf.l2jdev.gameserver.managers.IdManager;
import net.sf.l2jdev.gameserver.managers.InstanceManager;
import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Playable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.instance.Door;
import net.sf.l2jdev.gameserver.model.actor.instance.Monster;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.actor.instance.Servitor;
import net.sf.l2jdev.gameserver.model.events.ListenersContainer;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.model.interfaces.IPositionable;
import net.sf.l2jdev.gameserver.model.skill.SkillCaster;
import net.sf.l2jdev.gameserver.model.skill.TriggerCastInfo;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.DeleteObject;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public abstract class WorldObject extends ListenersContainer implements IPositionable
{
	protected String _name;
	private int _objectId;
	private WorldRegion _worldRegion;
	private final Location _location = new Location(0, 0, -10000);
	private Instance _instance;
	private InstanceType _instanceType;
	private boolean _isSpawned;
	private boolean _isInvisible;
	private boolean _isTargetable = true;
	private Map<String, Object> _scripts;
	private final ConcurrentLinkedQueue<TriggerCastInfo> _triggerCastQueue = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean _triggerCasting = new AtomicBoolean();

	public WorldObject(int objectId)
	{
		this.setInstanceType(InstanceType.WorldObject);
		this._objectId = objectId;
	}

	public InstanceType getInstanceType()
	{
		return this._instanceType;
	}

	protected void setInstanceType(InstanceType newInstanceType)
	{
		this._instanceType = newInstanceType;
	}

	public boolean isInstanceTypes(InstanceType... instanceTypes)
	{
		return this._instanceType.isTypes(instanceTypes);
	}

	public void onAction(Player player)
	{
		this.onAction(player, true);
	}

	public void onAction(Player player, boolean interact)
	{
		IActionHandler handler = ActionHandler.getInstance().getHandler(this.getInstanceType());
		if (handler != null)
		{
			handler.onAction(player, this, interact);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void onActionShift(Player player)
	{
		IActionShiftHandler handler = ActionShiftHandler.getInstance().getHandler(this.getInstanceType());
		if (handler != null)
		{
			handler.onAction(player, this, true);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void onForcedAttack(Player player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void onSpawn()
	{
	}

	public boolean decayMe()
	{
		this._isSpawned = false;
		World.getInstance().removeVisibleObject(this, this._worldRegion);
		World.getInstance().removeObject(this);
		return true;
	}

	public void refreshId()
	{
		World.getInstance().removeObject(this);
		IdManager.getInstance().releaseId(this.getObjectId());
		this._objectId = IdManager.getInstance().getNextId();
	}

	public boolean spawnMe()
	{
		synchronized (this)
		{
			this._isSpawned = true;
			this.setWorldRegion(World.getInstance().getRegion(this));
			World.getInstance().addObject(this);
			this._worldRegion.addVisibleObject(this);
		}

		World.getInstance().addVisibleObject(this, this.getWorldRegion());
		this.onSpawn();
		return true;
	}

	public void spawnMe(int x, int y, int z)
	{
		synchronized (this)
		{
			int spawnX = x;
			if (x > 294912)
			{
				spawnX = 289912;
			}

			if (spawnX < -294912)
			{
				spawnX = -289912;
			}

			int spawnY = y;
			if (y > 294912)
			{
				spawnY = 289912;
			}

			if (spawnY < -262144)
			{
				spawnY = -257144;
			}

			this.setXYZ(spawnX, spawnY, z);
		}

		this.spawnMe();
	}

	public boolean canBeAttacked()
	{
		return false;
	}

	public abstract boolean isAutoAttackable(Creature var1);

	public boolean isSpawned()
	{
		return this._isSpawned;
	}

	public void setSpawned(boolean value)
	{
		this._isSpawned = value;
	}

	public String getName()
	{
		return this._name;
	}

	public void setName(String value)
	{
		this._name = value;
	}

	public int getId()
	{
		return 0;
	}

	public int getObjectId()
	{
		return this._objectId;
	}

	public abstract void sendInfo(Player var1);

	public void sendPacket(ServerPacket packet)
	{
	}

	public void sendPacket(SystemMessageId id)
	{
	}

	public Attackable asAttackable()
	{
		return null;
	}

	public Creature asCreature()
	{
		return null;
	}

	public Door asDoor()
	{
		return null;
	}

	public Monster asMonster()
	{
		return null;
	}

	public Npc asNpc()
	{
		return null;
	}

	public Pet asPet()
	{
		return null;
	}

	public Playable asPlayable()
	{
		return null;
	}

	public Player asPlayer()
	{
		return null;
	}

	public Servitor asServitor()
	{
		return null;
	}

	public Summon asSummon()
	{
		return null;
	}

	public boolean isArtefact()
	{
		return false;
	}

	public boolean isAttackable()
	{
		return false;
	}

	public boolean isCreature()
	{
		return false;
	}

	public boolean isCubic()
	{
		return false;
	}

	public boolean isDoor()
	{
		return false;
	}

	public boolean isFakePlayer()
	{
		return false;
	}

	public boolean isFence()
	{
		return false;
	}

	public boolean isItem()
	{
		return false;
	}

	public boolean isMonster()
	{
		return false;
	}

	public boolean isNpc()
	{
		return false;
	}

	public boolean isPet()
	{
		return false;
	}

	public boolean isPlayable()
	{
		return false;
	}

	public boolean isPlayer()
	{
		return false;
	}

	public boolean isServitor()
	{
		return false;
	}

	public boolean isSummon()
	{
		return false;
	}

	public boolean isTrap()
	{
		return false;
	}

	public boolean isVehicle()
	{
		return false;
	}

	public boolean isWalker()
	{
		return false;
	}

	public void setTargetable(boolean targetable)
	{
		if (this._isTargetable != targetable)
		{
			this._isTargetable = targetable;
			if (!targetable)
			{
				World.getInstance().forEachVisibleObject(this, Creature.class, creature -> {
					if (creature.getTarget() == this)
					{
						creature.setTarget(null);
						creature.abortAttack();
						creature.abortCast();
					}
				});
			}
		}
	}

	public boolean isTargetable()
	{
		return this._isTargetable;
	}

	public boolean isInsideZone(ZoneId zone)
	{
		return false;
	}

	public <T> T addScript(T script)
	{
		if (this._scripts == null)
		{
			synchronized (this)
			{
				if (this._scripts == null)
				{
					this._scripts = new ConcurrentHashMap<>();
				}
			}
		}

		this._scripts.put(script.getClass().getName(), script);
		return script;
	}

	@SuppressWarnings("unchecked")
	public <T> T removeScript(Class<T> script)
	{
		return (T) (this._scripts == null ? null : this._scripts.remove(script.getName()));
	}

	@SuppressWarnings("unchecked")
	public <T> T getScript(Class<T> script)
	{
		return (T) (this._scripts == null ? null : this._scripts.get(script.getName()));
	}

	public void removeStatusListener(Creature object)
	{
	}

	public void setXYZInvisible(int x, int y, int z)
	{
		int correctX = x;
		if (x > 294912)
		{
			correctX = 289912;
		}

		if (correctX < -294912)
		{
			correctX = -289912;
		}

		int correctY = y;
		if (y > 294912)
		{
			correctY = 289912;
		}

		if (correctY < -262144)
		{
			correctY = -257144;
		}

		this.setXYZ(correctX, correctY, z);
		this.setSpawned(false);
	}

	public void setLocationInvisible(ILocational loc)
	{
		this.setXYZInvisible(loc.getX(), loc.getY(), loc.getZ());
	}

	public WorldRegion getWorldRegion()
	{
		return this._worldRegion;
	}

	public void setWorldRegion(WorldRegion region)
	{
		if (region == null && this._worldRegion != null)
		{
			this._worldRegion.removeVisibleObject(this);
		}

		this._worldRegion = region;
	}

	@Override
	public int getX()
	{
		return this._location.getX();
	}

	@Override
	public int getY()
	{
		return this._location.getY();
	}

	@Override
	public int getZ()
	{
		return this._location.getZ();
	}

	@Override
	public int getHeading()
	{
		return this._location.getHeading();
	}

	public int getInstanceId()
	{
		Instance instance = this._instance;
		return instance != null ? instance.getId() : 0;
	}

	public boolean isInInstance()
	{
		return this._instance != null;
	}

	public Instance getInstanceWorld()
	{
		return this._instance;
	}

	@Override
	public Location getLocation()
	{
		return this._location;
	}

	@Override
	public void setXYZ(int newX, int newY, int newZ)
	{
		this._location.setXYZ(newX, newY, newZ);
		if (this._isSpawned)
		{
			WorldRegion newRegion = World.getInstance().getRegion(this);
			if (newRegion != null && newRegion != this._worldRegion)
			{
				if (this._worldRegion != null)
				{
					this._worldRegion.removeVisibleObject(this);
				}

				newRegion.addVisibleObject(this);
				World.getInstance().switchRegion(this, newRegion);
				this.setWorldRegion(newRegion);
			}
		}
	}

	@Override
	public void setXYZ(ILocational loc)
	{
		this.setXYZ(loc.getX(), loc.getY(), loc.getZ());
	}

	@Override
	public void setHeading(int newHeading)
	{
		this._location.setHeading(newHeading);
	}

	public void setInstanceById(int id)
	{
		Instance instance = InstanceManager.getInstance().getInstance(id);
		if (id == 0 || instance != null)
		{
			this.setInstance(instance);
		}
	}

	public synchronized void setInstance(Instance newInstance)
	{
		if (this._instance != newInstance)
		{
			if (this._instance != null)
			{
				this._instance.onInstanceChange(this, false);
			}

			this._instance = newInstance;
			if (newInstance != null)
			{
				newInstance.onInstanceChange(this, true);
			}
		}
	}

	@Override
	public void setLocation(Location loc)
	{
		this._location.setXYZ(loc.getX(), loc.getY(), loc.getZ());
		this._location.setHeading(loc.getHeading());
	}

	public double calculateDistance2D(int x, int y, int z)
	{
		return Math.sqrt(Math.pow(x - this.getX(), 2.0) + Math.pow(y - this.getY(), 2.0));
	}

	public double calculateDistance2D(ILocational loc)
	{
		return loc == null ? Double.MAX_VALUE : this.calculateDistance2D(loc.getX(), loc.getY(), loc.getZ());
	}

	public double calculateDistance3D(int x, int y, int z)
	{
		return Math.sqrt(Math.pow(x - this.getX(), 2.0) + Math.pow(y - this.getY(), 2.0) + Math.pow(z - this.getZ(), 2.0));
	}

	public double calculateDistance3D(ILocational loc)
	{
		return loc == null ? Double.MAX_VALUE : this.calculateDistance3D(loc.getX(), loc.getY(), loc.getZ());
	}

	public double calculateDirectionTo(ILocational target)
	{
		return LocationUtil.calculateAngleFrom(this, target);
	}

	public boolean isInvisible()
	{
		return this._isInvisible;
	}

	public void setInvisible(boolean invisible)
	{
		this._isInvisible = invisible;
		if (invisible)
		{
			DeleteObject deletePacket = new DeleteObject(this);
			World.getInstance().forEachVisibleObject(this, Player.class, player -> {
				if (!this.isVisibleFor(player))
				{
					player.sendPacket(deletePacket);
				}
			});
		}

		this.broadcastInfo();
	}

	public boolean isVisibleFor(Player player)
	{
		return !this._isInvisible || player.isGM();
	}

	public void broadcastInfo()
	{
		World.getInstance().forEachVisibleObject(this, Player.class, player -> {
			if (this.isVisibleFor(player))
			{
				this.sendInfo(player);
			}
		});
	}

	public boolean isInvul()
	{
		return false;
	}

	public boolean isInSurroundingRegion(WorldObject worldObject)
	{
		if (worldObject == null)
		{
			return false;
		}
		WorldRegion worldRegion = worldObject.getWorldRegion();
		if (worldRegion == null)
		{
			return false;
		}
		return this._worldRegion == null ? false : worldRegion.isSurroundingRegion(this._worldRegion);
	}

	public void addTriggerCast(TriggerCastInfo info)
	{
		this._triggerCastQueue.offer(info);
		if (this._triggerCasting.compareAndSet(false, true))
		{
			this.processTriggerCastQueue();
		}
	}

	private void processTriggerCastQueue()
	{
		try
		{
			while (!this._triggerCastQueue.isEmpty())
			{
				TriggerCastInfo info = this._triggerCastQueue.poll();
				if (info != null)
				{
					SkillCaster.triggerCast(info);
				}
			}
		}
		finally
		{
			this._triggerCasting.set(false);
			if (!this._triggerCastQueue.isEmpty() && this._triggerCasting.compareAndSet(false, true))
			{
				this.processTriggerCastQueue();
			}
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof WorldObject && ((WorldObject) obj).getObjectId() == this.getObjectId();
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append(":");
		sb.append(this._name);
		sb.append("[");
		sb.append(this._objectId);
		sb.append("]");
		return sb.toString();
	}
}
