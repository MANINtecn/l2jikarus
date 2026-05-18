package net.sf.l2jdev.gameserver.network.serverpackets.friend;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class L2Friend extends ServerPacket
{
	private final boolean _action;
	private final boolean _online;
	private final int _objid;
	private final String _name;

	public L2Friend(boolean action, int objId)
	{
		this._action = action;
		this._objid = objId;
		this._name = CharInfoTable.getInstance().getNameById(objId);
		this._online = World.getInstance().getPlayer(objId) != null;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.L2_FRIEND.writeId(this, buffer);
		buffer.writeInt(this._action ? 1 : 3);
		buffer.writeInt(this._objid);
		buffer.writeString(this._name);
		buffer.writeInt(this._online);
		buffer.writeInt(this._online ? this._objid : 0);
	}
}
