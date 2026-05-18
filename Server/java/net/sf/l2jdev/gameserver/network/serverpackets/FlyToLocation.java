package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.model.skill.enums.FlyType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class FlyToLocation extends ServerPacket
{
	private final int _destX;
	private final int _destY;
	private final int _destZ;
	private final int _chaObjId;
	private final int _orgX;
	private final int _orgY;
	private final int _orgZ;
	private final FlyType _type;
	private int _flySpeed;
	private int _flyDelay;
	private int _animationSpeed;

	public FlyToLocation(Creature creature, int destX, int destY, int destZ, FlyType type)
	{
		this._chaObjId = creature.getObjectId();
		this._orgX = creature.getX();
		this._orgY = creature.getY();
		this._orgZ = creature.getZ();
		this._destX = destX;
		this._destY = destY;
		this._destZ = destZ;
		this._type = type;
		if (creature.isPlayer())
		{
			creature.asPlayer().setBlinkActive(true);
		}
	}

	public FlyToLocation(Creature creature, int destX, int destY, int destZ, FlyType type, int flySpeed, int flyDelay, int animationSpeed)
	{
		this._chaObjId = creature.getObjectId();
		this._orgX = creature.getX();
		this._orgY = creature.getY();
		this._orgZ = creature.getZ();
		this._destX = destX;
		this._destY = destY;
		this._destZ = destZ;
		this._type = type;
		this._flySpeed = flySpeed;
		this._flyDelay = flyDelay;
		this._animationSpeed = animationSpeed;
		if (creature.isPlayer())
		{
			creature.asPlayer().setBlinkActive(true);
		}
	}

	public FlyToLocation(Creature creature, ILocational dest, FlyType type)
	{
		this(creature, dest.getX(), dest.getY(), dest.getZ(), type);
	}

	public FlyToLocation(Creature creature, ILocational dest, FlyType type, int flySpeed, int flyDelay, int animationSpeed)
	{
		this(creature, dest.getX(), dest.getY(), dest.getZ(), type, flySpeed, flyDelay, animationSpeed);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.FLY_TO_LOCATION.writeId(this, buffer);
		buffer.writeInt(this._chaObjId);
		buffer.writeInt(this._destX);
		buffer.writeInt(this._destY);
		buffer.writeInt(this._destZ);
		buffer.writeInt(this._orgX);
		buffer.writeInt(this._orgY);
		buffer.writeInt(this._orgZ);
		buffer.writeInt(this._type.ordinal());
		buffer.writeInt(this._flySpeed);
		buffer.writeInt(this._flyDelay);
		buffer.writeInt(this._animationSpeed);
	}
}
