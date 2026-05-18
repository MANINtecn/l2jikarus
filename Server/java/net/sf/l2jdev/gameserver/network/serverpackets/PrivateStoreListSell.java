package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.SellBuffsManager;
import net.sf.l2jdev.gameserver.model.TradeItem;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PrivateStoreListSell extends AbstractItemPacket
{
	private final Player _player;
	private final Player _seller;

	public PrivateStoreListSell(Player player, Player seller)
	{
		this._player = player;
		this._seller = seller;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if (this._seller.isSellingBuffs())
		{
			SellBuffsManager.getInstance().sendBuffMenu(this._player, this._seller, 0);
		}
		else
		{
			ServerPackets.PRIVATE_STORE_LIST.writeId(this, buffer);
			buffer.writeInt(this._seller.getObjectId());
			buffer.writeInt(this._seller.getSellList().isPackaged());
			buffer.writeLong(this._player.getAdena());
			buffer.writeInt(0);
			buffer.writeInt(this._seller.getSellList().getItems().size());

			for (TradeItem item : this._seller.getSellList().getItems())
			{
				this.writeItem(item, buffer);
				buffer.writeLong(item.getPrice());
				buffer.writeLong(item.getItem().getReferencePrice() * 2);
			}
		}
	}
}
