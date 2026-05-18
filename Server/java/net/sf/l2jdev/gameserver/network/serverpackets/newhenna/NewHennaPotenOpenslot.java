package net.sf.l2jdev.gameserver.network.serverpackets.newhenna;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class NewHennaPotenOpenslot extends ServerPacket
{
	private final boolean _success;
	private final int _dye;
	private final int _slot;
	private final int _stage;

	public NewHennaPotenOpenslot(boolean success, int dye, int slot, int stage)
	{
		this._dye = dye;
		this._slot = slot;
		this._success = success;
		this._stage = stage;
	}

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_NEW_HENNA_POTEN_OPENSLOT.writeId(this, buffer);
		buffer.writeInt(this._success);
		buffer.writeInt(this._dye);
		buffer.writeInt(this._slot);
		buffer.writeInt(this._stage);
	}
}
