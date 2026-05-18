package net.sf.l2jdev.gameserver.network.clientpackets.commission;

import net.sf.l2jdev.gameserver.managers.ItemCommissionManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.commission.ExCloseCommission;

public class RequestCommissionRegister extends ClientPacket
{
	private int _itemObjectId;
	private long _pricePerUnit;
	private long _itemCount;
	private int _durationType;
	private int _feeDiscountType;

	@Override
	protected void readImpl()
	{
		this._itemObjectId = this.readInt();
		this.readString();
		this._pricePerUnit = this.readLong();
		this._itemCount = this.readLong();
		this._durationType = this.readInt();
		this._feeDiscountType = this.readShort();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._feeDiscountType >= 0 && this._feeDiscountType <= 2)
			{
				if (this._feeDiscountType == 1 && player.getInventory().getItemByItemId(22351) == null)
				{
					PacketLogger.warning(player + ": Auction House Fee 30% Voucher not found in inventory.");
				}
				else if (this._feeDiscountType == 2 && player.getInventory().getItemByItemId(22352) == null)
				{
					PacketLogger.warning(player + ": Auction House Fee 100% Voucher not found in inventory.");
				}
				else if (this._durationType >= 0 && this._durationType <= 5)
				{
					if (this._durationType == 4 && player.getInventory().getItemByItemId(22353) == null)
					{
						PacketLogger.warning(player + ": Auction House (15-day) Extension not found in inventory.");
					}
					else if (this._durationType == 5 && player.getInventory().getItemByItemId(22354) == null)
					{
						PacketLogger.warning(player + ": Auction House (30-day) Extension not found in inventory.");
					}
					else if (!ItemCommissionManager.isPlayerAllowedToInteract(player))
					{
						player.sendPacket(ExCloseCommission.STATIC_PACKET);
					}
					else
					{
						ItemCommissionManager.getInstance().registerItem(player, this._itemObjectId, this._itemCount, this._pricePerUnit, this._durationType, (byte) Math.min(this._feeDiscountType * 30 * this._feeDiscountType, 100));
					}
				}
				else
				{
					PacketLogger.warning(player + " sent incorrect commission duration type: " + this._durationType + ".");
				}
			}
			else
			{
				PacketLogger.warning(player + " sent incorrect commission discount type: " + this._feeDiscountType + ".");
			}
		}
	}
}
