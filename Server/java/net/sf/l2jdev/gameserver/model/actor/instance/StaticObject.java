package net.sf.l2jdev.gameserver.model.actor.instance;

import net.sf.l2jdev.gameserver.ai.CreatureAI;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.stat.StaticObjectStat;
import net.sf.l2jdev.gameserver.model.actor.status.StaticObjectStatus;
import net.sf.l2jdev.gameserver.model.actor.templates.CreatureTemplate;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.serverpackets.ShowTownMap;
import net.sf.l2jdev.gameserver.network.serverpackets.StaticObjectInfo;

public class StaticObject extends Creature
{
	public static final int INTERACTION_DISTANCE = 150;
	private final int _staticObjectId;
	private int _meshIndex = 0;
	private int _type = -1;
	private ShowTownMap _map;

	@Override
	protected CreatureAI initAI()
	{
		return null;
	}

	@Override
	public int getId()
	{
		return this._staticObjectId;
	}

	public StaticObject(CreatureTemplate template, int staticId)
	{
		super(template);
		this.setInstanceType(InstanceType.StaticObject);
		this._staticObjectId = staticId;
	}

	@Override
	public StaticObjectStat getStat()
	{
		return (StaticObjectStat) super.getStat();
	}

	@Override
	public void initCharStat()
	{
		this.setStat(new StaticObjectStat(this));
	}

	@Override
	public StaticObjectStatus getStatus()
	{
		return (StaticObjectStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		this.setStatus(new StaticObjectStatus(this));
	}

	public int getType()
	{
		return this._type;
	}

	public void setType(int type)
	{
		this._type = type;
	}

	public void setMap(String texture, int x, int y)
	{
		this._map = new ShowTownMap("town_map." + texture, x, y);
	}

	public ShowTownMap getMap()
	{
		return this._map;
	}

	@Override
	public int getLevel()
	{
		return 1;
	}

	@Override
	public Item getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public Item getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}

	public void setMeshIndex(int meshIndex)
	{
		this._meshIndex = meshIndex;
		this.broadcastPacket(new StaticObjectInfo(this));
	}

	public int getMeshIndex()
	{
		return this._meshIndex;
	}

	@Override
	public void sendInfo(Player player)
	{
		player.sendPacket(new StaticObjectInfo(this));
	}

	@Override
	public void moveToLocation(int x, int y, int z, int offset)
	{
	}

	@Override
	public void stopMove(Location loc)
	{
	}

	@Override
	public void doAutoAttack(Creature target)
	{
	}

	@Override
	public void doCast(Skill skill)
	{
	}
}
