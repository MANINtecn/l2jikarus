package net.sf.l2jdev.gameserver.network.serverpackets.newhenna;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class NewHennaPotenSelect extends ServerPacket
{
	private final int _slotId;
	private final int _potenId;
	private final int _activeStep;
	private final boolean _success;

	public NewHennaPotenSelect(int slotId, int potenId, int activeStep, boolean success)
	{
		this._slotId = slotId;
		this._potenId = potenId;
		this._activeStep = activeStep;
		this._success = success;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_NEW_HENNA_POTEN_SELECT.writeId(this, buffer);
		buffer.writeByte(this._slotId);
		buffer.writeInt(this._potenId);
		buffer.writeShort(this._activeStep);
		buffer.writeByte(this._success);
	}
}
