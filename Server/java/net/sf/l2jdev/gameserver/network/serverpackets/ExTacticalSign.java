package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExTacticalSign extends ServerPacket
{
	private final Creature _target;
	private final int _tokenId;

	public ExTacticalSign(Creature target, int tokenId)
	{
		this._target = target;
		this._tokenId = tokenId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_TACTICAL_SIGN.writeId(this, buffer);
		buffer.writeInt(this._target.getObjectId());
		buffer.writeInt(this._tokenId);
	}
}
