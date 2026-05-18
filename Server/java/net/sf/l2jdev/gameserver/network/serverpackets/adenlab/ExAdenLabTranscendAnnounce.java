package net.sf.l2jdev.gameserver.network.serverpackets.adenlab;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExAdenLabTranscendAnnounce extends ServerPacket
{
	private final String _name;
	private final int _bossId;
	private final byte _enchant;

	public ExAdenLabTranscendAnnounce(String name, int bossId, byte enchant)
	{
		this._name = name;
		this._bossId = bossId;
		this._enchant = enchant;
	}

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ADENLAB_TRANSCEND_ANNOUNCE.writeId(this, buffer);
		buffer.writeSizedString(this._name);
		buffer.writeInt(this._bossId);
		buffer.writeByte(this._enchant);
	}
}
