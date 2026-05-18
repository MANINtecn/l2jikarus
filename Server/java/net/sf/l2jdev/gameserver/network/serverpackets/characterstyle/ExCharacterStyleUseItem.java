package net.sf.l2jdev.gameserver.network.serverpackets.characterstyle;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.enums.CharacterStyleCategoryType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCharacterStyleUseItem extends ServerPacket
{
	final CharacterStyleCategoryType _type;
	final int _styleId;

	public ExCharacterStyleUseItem(CharacterStyleCategoryType type, int styleId)
	{
		this._type = type;
		this._styleId = styleId;
	}

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CHARACTER_STYLE_USE_ITEM.writeId(this, buffer);
		buffer.writeInt(this._type.getClientId());
		buffer.writeInt(this._styleId);
	}
}
