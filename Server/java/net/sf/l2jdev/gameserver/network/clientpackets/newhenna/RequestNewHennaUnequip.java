package net.sf.l2jdev.gameserver.network.clientpackets.newhenna;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.henna.Henna;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.UserInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.newhenna.NewHennaUnequip;

public class RequestNewHennaUnequip extends ClientPacket
{
	private int _slotId;
	private int _itemId;

	@Override
	protected void readImpl()
	{
		this._slotId = this.readByte();
		this._itemId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (!this.getClient().getFloodProtectors().canPerformTransaction())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				player.sendPacket(new NewHennaUnequip(this._slotId, 0));
			}
			else if (this._slotId <= player.getHennaPotenList().length)
			{
				Henna henna = player.getHenna(this._slotId);
				if (henna == null)
				{
					PacketLogger.warning(this.getClass().getSimpleName() + ": " + player + " requested Henna Draw remove without any henna.");
					player.sendPacket(ActionFailed.STATIC_PACKET);
					player.sendPacket(new NewHennaUnequip(this._slotId, 0));
				}
				else
				{
					int feeType = 0;
					if (this._itemId == 57)
					{
						feeType = henna.getCancelFee();
					}
					else if (this._itemId == 91663)
					{
						feeType = henna.getCancelL2CoinFee();
					}

					if (player.destroyItemByItemId(ItemProcessType.FEE, this._itemId, feeType, player, false))
					{
						player.removeHenna(this._slotId);
						player.getStat().recalculateStats(true);
						player.sendPacket(new NewHennaUnequip(this._slotId, 1));
						player.sendPacket(new UserInfo(player));
					}
					else
					{
						if (this._itemId == 57)
						{
							player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA_TO_REGISTER_THE_ITEM);
						}
						else if (this._itemId == 91663)
						{
							player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_L2_COINS_ADD_MORE_L2_COINS_AND_TRY_AGAIN);
						}

						player.sendPacket(ActionFailed.STATIC_PACKET);
						player.sendPacket(new NewHennaUnequip(this._slotId, 0));
					}
				}
			}
		}
	}
}
