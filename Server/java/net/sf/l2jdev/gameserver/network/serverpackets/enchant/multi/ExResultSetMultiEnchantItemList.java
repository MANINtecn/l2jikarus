package net.sf.l2jdev.gameserver.network.serverpackets.enchant.multi;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.EnchantItemRequest;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExResultSetMultiEnchantItemList extends ServerPacket
{
	private final Player _player;
	private final int _resultType;

	public ExResultSetMultiEnchantItemList(Player player, int resultType)
	{
		this._player = player;
		this._resultType = resultType;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if (this._player.getRequest(EnchantItemRequest.class) != null)
		{
			ServerPackets.EX_RES_SET_MULTI_ENCHANT_ITEM_LIST.writeId(this, buffer);
			buffer.writeInt(this._resultType);
		}
	}
}
