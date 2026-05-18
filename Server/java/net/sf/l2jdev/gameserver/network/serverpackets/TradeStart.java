package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.managers.MentorManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class TradeStart extends AbstractItemPacket
{
	private final int _sendType;
	private final Player _player;
	private final Player _partner;
	private final Collection<Item> _itemList;
	private int _mask = 0;

	public TradeStart(int sendType, Player player)
	{
		this._sendType = sendType;
		this._player = player;
		this._partner = player.getActiveTradeList().getPartner();
		this._itemList = this._player.getInventory().getAvailableItems(true, this._player.isGM() && GeneralConfig.GM_TRADE_RESTRICTED_ITEMS, false);
		if (this._partner != null)
		{
			if (player.getFriendList().contains(this._partner.getObjectId()))
			{
				this._mask |= 1;
			}

			if (player.getClanId() > 0 && player.getClanId() == this._partner.getClanId())
			{
				this._mask |= 2;
			}

			if (MentorManager.getInstance().getMentee(player.getObjectId(), this._partner.getObjectId()) != null || MentorManager.getInstance().getMentee(this._partner.getObjectId(), player.getObjectId()) != null)
			{
				this._mask |= 4;
			}

			if (player.getAllyId() > 0 && player.getAllyId() == this._partner.getAllyId())
			{
				this._mask |= 8;
			}

			if (this._partner.isGM())
			{
				this._mask |= 16;
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if (this._player.getActiveTradeList() != null && this._partner != null)
		{
			ServerPackets.TRADE_START.writeId(this, buffer);
			buffer.writeByte(this._sendType);
			if (this._sendType == 2)
			{
				buffer.writeInt(this._itemList.size());
				buffer.writeInt(this._itemList.size());

				for (Item item : this._itemList)
				{
					this.writeItem(item, buffer);
				}
			}
			else
			{
				buffer.writeInt(this._partner.getObjectId());
				buffer.writeByte(this._mask);
				if ((this._mask & 16) == 0)
				{
					buffer.writeByte(this._partner.getLevel());
				}
			}
		}
	}
}
