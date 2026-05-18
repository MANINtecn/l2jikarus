package net.sf.l2jdev.gameserver.network.serverpackets.penaltyitemdrop;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.FeatureConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.holders.ItemPenaltyHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPenaltyItemList extends ServerPacket
{
	private final Player _player;

	public ExPenaltyItemList(Player player)
	{
		this._player = player;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PENALTY_ITEM_LIST.writeId(this, buffer);
		if (this._player.getItemPenaltyList().isEmpty())
		{
			buffer.writeInt(0);
		}
		else
		{
			buffer.writeInt(this._player.getItemPenaltyList().size());
			List<ItemPenaltyHolder> listItems = new ArrayList<>(this._player.getItemPenaltyList());

			for (ItemPenaltyHolder holder : listItems.reversed())
			{
				Item item = this._player.getItemPenalty().getItemByObjectId(holder.getItemObjectId());
				buffer.writeInt(item.getObjectId());
				buffer.writeInt((int) (holder.getDateLost().getTime() / 1000L));
				buffer.writeLong(FeatureConfig.ITEM_PENALTY_RESTORE_ADENA);
				buffer.writeInt(FeatureConfig.ITEM_PENALTY_RESTORE_LCOIN);
				buffer.writeInt(49);
				buffer.writeInt(0);
				buffer.writeShort(0);
				buffer.writeInt(item.getId());
				buffer.writeByte(0);
				buffer.writeLong(item.getCount());
				buffer.writeInt(1);
				buffer.writeInt(64);
				buffer.writeInt(0);
				buffer.writeInt(item.getEnchantLevel());
				buffer.writeShort(0);
				buffer.writeByte(0);
				buffer.writeInt(-9999);
				buffer.writeShort(1);
				buffer.writeByte(0);
				buffer.writeInt(item.getObjectId());
			}
		}
	}
}
