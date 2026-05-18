package net.sf.l2jdev.gameserver.network.serverpackets.enchant.multi;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.EnchantItemRequest;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExResetSelectMultiEnchantScroll extends ServerPacket
{
	private final Player _player;
	private final int _scrollObjectId;
	private final int _resultType;

	public ExResetSelectMultiEnchantScroll(Player player, int scrollObjectId, int resultType)
	{
		this._player = player;
		this._scrollObjectId = scrollObjectId;
		this._resultType = resultType;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		EnchantItemRequest request = this._player.getRequest(EnchantItemRequest.class);
		if (request != null)
		{
			if (request.getEnchantingScroll() == null)
			{
				request.setEnchantingScroll(this._scrollObjectId);
			}

			ServerPackets.EX_RES_SELECT_MULTI_ENCHANT_SCROLL.writeId(this, buffer);
			buffer.writeInt(this._scrollObjectId);
			buffer.writeInt(this._resultType);
		}
	}
}
