package net.sf.l2jdev.gameserver.network.serverpackets.newhenna;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class NewHennaPotenEnchantReset extends ServerPacket
{
	private final boolean _success;

	public NewHennaPotenEnchantReset(boolean success)
	{
		this._success = success;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_NEW_HENNA_POTEN_ENCHANT_RESET.writeId(this, buffer);
		buffer.writeByte(this._success);
	}
}
