package net.sf.l2jdev.gameserver.network.serverpackets.chatbackground;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.variables.PlayerVariables;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExChatBackGroundSettingNotification extends ServerPacket
{
	private final int _activeBackground;
	private final boolean _enabled;

	public ExChatBackGroundSettingNotification(PlayerVariables variables)
	{
		this._activeBackground = variables.getInt("ACTIVE_CHAT_BACKGROUND", 0);
		this._enabled = variables.getBoolean("ENABLE_CHAT_BACKGROUND", false);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CHARACTER_STYLE_USE_ITEM.writeId(this, buffer);
		buffer.writeInt(this._activeBackground);
		buffer.writeByte(this._enabled);
	}
}
