package net.sf.l2jdev.gameserver.network.serverpackets.newhenna;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class NewHennaEquip extends ServerPacket
{
	private final int _slotId;
	private final int _hennaId;
	private final boolean _success;

	public NewHennaEquip(int slotId, int hennaId, boolean success)
	{
		this._slotId = slotId;
		this._hennaId = hennaId;
		this._success = success;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_NEW_HENNA_EQUIP.writeId(this, buffer);
		buffer.writeByte(this._slotId);
		buffer.writeInt(this._hennaId);
		buffer.writeByte(this._success);
	}
}
