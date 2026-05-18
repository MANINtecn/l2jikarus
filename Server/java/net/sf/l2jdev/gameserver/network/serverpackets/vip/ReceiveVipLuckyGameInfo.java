package net.sf.l2jdev.gameserver.network.serverpackets.vip;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ReceiveVipLuckyGameInfo extends ServerPacket
{
	private final short _adenaAmount;
	private final short _lcoinCount;

	public ReceiveVipLuckyGameInfo(Player player)
	{
		this._adenaAmount = (short) player.getAdena();
		Item item = player.getInventory().getItemByItemId(91663);
		this._lcoinCount = item == null ? 0 : (short) item.getCount();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_LUCKY_GAME_INFO.writeId(this, buffer);
		buffer.writeByte(true);
		buffer.writeShort(this._adenaAmount);
		buffer.writeShort(this._lcoinCount);
	}
}
