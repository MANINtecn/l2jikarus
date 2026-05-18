package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExConfirmAddingContact extends ServerPacket
{
	private final String _charName;
	private final boolean _added;

	public ExConfirmAddingContact(String charName, boolean added)
	{
		this._charName = charName;
		this._added = added;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_AGITAUCTION_CMD.writeId(this, buffer);
		buffer.writeString(this._charName);
		buffer.writeInt(this._added);
	}
}
