package net.sf.l2jdev.gameserver.network.serverpackets.commission;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.commission.CommissionItem;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.AbstractItemPacket;

public class ExResponseCommissionBuyInfo extends AbstractItemPacket
{
	public static final ExResponseCommissionBuyInfo FAILED = new ExResponseCommissionBuyInfo(null);
	private final CommissionItem _commissionItem;

	public ExResponseCommissionBuyInfo(CommissionItem commissionItem)
	{
		this._commissionItem = commissionItem;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RESPONSE_COMMISSION_BUY_INFO.writeId(this, buffer);
		buffer.writeInt(this._commissionItem != null);
		if (this._commissionItem != null)
		{
			buffer.writeLong(this._commissionItem.getPricePerUnit());
			buffer.writeLong(this._commissionItem.getCommissionId());
			buffer.writeInt(0);
			this.writeItem(this._commissionItem.getItemInfo(), buffer);
		}
	}
}
