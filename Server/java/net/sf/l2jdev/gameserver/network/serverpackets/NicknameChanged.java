package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class NicknameChanged extends ServerPacket
{
	private final String _title;
	private final int _objectId;

	public NicknameChanged(Creature creature)
	{
		this._objectId = creature.getObjectId();
		this._title = creature.getTitle();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.NICKNAME_CHANGED.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeString(this._title);
	}
}
