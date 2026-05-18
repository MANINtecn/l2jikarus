package net.sf.l2jdev.gameserver.network.serverpackets.settings;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExUISetting extends ServerPacket
{
	public static final String SPLIT_VAR = "\t";
	private final byte[] _uiKeyMapping;

	public ExUISetting(Player player)
	{
		if (player.getVariables().hasVariable("UI_KEY_MAPPING"))
		{
			this._uiKeyMapping = player.getVariables().getByteArray("UI_KEY_MAPPING", "\t");
		}
		else
		{
			this._uiKeyMapping = null;
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_UI_SETTING.writeId(this, buffer);
		if (this._uiKeyMapping != null)
		{
			buffer.writeInt(this._uiKeyMapping.length);
			buffer.writeBytes(this._uiKeyMapping);
		}
		else
		{
			buffer.writeInt(0);
		}
	}
}
