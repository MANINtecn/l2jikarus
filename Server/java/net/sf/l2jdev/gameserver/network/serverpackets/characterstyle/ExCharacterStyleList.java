package net.sf.l2jdev.gameserver.network.serverpackets.characterstyle;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.enums.CharacterStyleCategoryType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCharacterStyleList extends ServerPacket
{
	private final CharacterStyleCategoryType _type;
	private final List<Integer> _unlockedStyles;
	private final List<Integer> _favoriteStyles;
	private final Map<Integer, Integer> _activeStyles;
	private final ItemHolder _swapCost;

	public ExCharacterStyleList(CharacterStyleCategoryType type, ItemHolder swapCost, List<Integer> styles, List<Integer> favoriteStyles, Map<Integer, Integer> activeStyles)
	{
		this._type = type;
		this._swapCost = swapCost;
		this._unlockedStyles = styles;
		this._favoriteStyles = favoriteStyles;
		this._activeStyles = activeStyles;
	}

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CHARACTER_STYLE_LIST.writeId(this, buffer);
		buffer.writeInt(this._type.getClientId());
		buffer.writeInt(this._swapCost.getId());
		buffer.writeLong(this._swapCost.getCount());
		buffer.writeInt(this._activeStyles.size());

		for (Entry<Integer, Integer> style : this._activeStyles.entrySet())
		{
			buffer.writeInt(style.getKey());
			buffer.writeInt(style.getValue());
		}

		buffer.writeInt(this._favoriteStyles.size());

		for (int style : this._favoriteStyles)
		{
			buffer.writeInt(style);
		}

		buffer.writeInt(this._unlockedStyles.size());

		for (int style : this._unlockedStyles)
		{
			buffer.writeInt(style);
			buffer.writeInt(0);
			buffer.writeLong(0L);
		}
	}
}
