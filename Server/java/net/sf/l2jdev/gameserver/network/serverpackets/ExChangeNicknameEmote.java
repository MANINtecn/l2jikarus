package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExChangeNicknameEmote extends ServerPacket
{
	private final int _itemId;

	public ExChangeNicknameEmote(int itemId)
	{
		this._itemId = itemId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CHANGE_NICKNAME_COLOR_ICON.writeId(this, buffer);
		buffer.writeInt(this._itemId);
	}
}
