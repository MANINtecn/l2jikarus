package net.sf.l2jdev.gameserver.model.actor.enums.creature;

public enum InstanceType
{
	WorldObject(null),
	Item(WorldObject),
	Creature(WorldObject),
	Npc(Creature),
	Playable(Creature),
	Summon(Playable),
	Player(Playable),
	Folk(Npc),
	Merchant(Folk),
	Warehouse(Folk),
	StaticObject(Creature),
	Door(Creature),
	TerrainObject(Npc),
	EffectPoint(Npc),
	CommissionManager(Npc),
	Servitor(Summon),
	Pet(Summon),
	Shadow(Summon),
	Guardian(Summon),
	Cubic(Creature),
	Decoy(Creature),
	Trap(Npc),
	Attackable(Npc),
	Guard(Attackable),
	Monster(Attackable),
	Chest(Monster),
	ControllableMob(Monster),
	FeedableBeast(Monster),
	TamedBeast(FeedableBeast),
	FriendlyMob(Attackable),
	RaidBoss(Monster),
	GrandBoss(RaidBoss),
	FriendlyNpc(Attackable),
	FlyTerrainObject(Npc),
	Vehicle(Creature),
	Boat(Vehicle),
	AirShip(Vehicle),
	Shuttle(Vehicle),
	ControllableAirShip(AirShip),
	Defender(Attackable),
	Artefact(Folk),
	ControlTower(Npc),
	FlameTower(Npc),
	SiegeFlag(Npc),
	FortCommander(Defender),
	FortLogistics(Merchant),
	FortManager(Merchant),
	BroadcastingTower(Npc),
	Fisherman(Merchant),
	OlympiadManager(Npc),
	PetManager(Merchant),
	Teleporter(Npc),
	VillageMaster(Folk),
	Doorman(Folk),
	FortDoorman(Doorman),
	ClassMaster(Folk),
	SchemeBuffer(Npc),
	EventMob(Npc);

	private final InstanceType _parent;
	private final long _typeL;
	private final long _typeH;
	private final long _maskL;
	private final long _maskH;

	private InstanceType(InstanceType parent)
	{
		this._parent = parent;
		int high = this.ordinal() - 63;
		if (high < 0)
		{
			this._typeL = 1L << this.ordinal();
			this._typeH = 0L;
		}
		else
		{
			this._typeL = 0L;
			this._typeH = 1L << high;
		}

		if (this._typeL >= 0L && this._typeH >= 0L)
		{
			if (parent != null)
			{
				this._maskL = this._typeL | parent._maskL;
				this._maskH = this._typeH | parent._maskH;
			}
			else
			{
				this._maskL = this._typeL;
				this._maskH = this._typeH;
			}
		}
		else
		{
			throw new Error("Too many instance types, failed to load " + this.name());
		}
	}

	public InstanceType getParent()
	{
		return this._parent;
	}

	public boolean isType(InstanceType it)
	{
		return (this._maskL & it._typeL) > 0L || (this._maskH & it._typeH) > 0L;
	}

	public boolean isTypes(InstanceType... it)
	{
		for (InstanceType i : it)
		{
			if (this.isType(i))
			{
				return true;
			}
		}

		return false;
	}
}
