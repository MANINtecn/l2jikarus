package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ServerObjectInfo extends ServerPacket
{
	private final Npc _activeChar;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	private final int _displayId;
	private final boolean _isAttackable;
	private final double _collisionHeight;
	private final double _collisionRadius;
	private final String _name;

	public ServerObjectInfo(Npc activeChar, Creature actor)
	{
		this._activeChar = activeChar;
		this._displayId = this._activeChar.getTemplate().getDisplayId();
		this._isAttackable = this._activeChar.isAutoAttackable(actor);
		this._collisionHeight = this._activeChar.getCollisionHeight();
		this._collisionRadius = this._activeChar.getCollisionRadius();
		this._x = this._activeChar.getX();
		this._y = this._activeChar.getY();
		this._z = this._activeChar.getZ();
		this._heading = this._activeChar.getHeading();
		this._name = this._activeChar.getTemplate().isUsingServerSideName() ? this._activeChar.getTemplate().getName() : "";
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SERVER_OBJECT_INFO.writeId(this, buffer);
		buffer.writeInt(this._activeChar.getObjectId());
		buffer.writeInt(this._displayId + 1000000);
		buffer.writeString(this._name);
		buffer.writeInt(this._isAttackable);
		buffer.writeInt(this._x);
		buffer.writeInt(this._y);
		buffer.writeInt(this._z);
		buffer.writeInt(this._heading);
		buffer.writeDouble(1.0);
		buffer.writeDouble(1.0);
		buffer.writeDouble(this._collisionRadius);
		buffer.writeDouble(this._collisionHeight);
		buffer.writeInt((int) (this._isAttackable ? this._activeChar.getCurrentHp() : 0.0));
		buffer.writeInt((int) (this._isAttackable ? this._activeChar.getMaxHp() : 0L));
		buffer.writeInt(1);
		buffer.writeInt(0);
	}
}
