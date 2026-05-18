package net.sf.l2jdev.gameserver.network.clientpackets.newhenna;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.data.xml.HennaData;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.henna.Henna;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.UserInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.newhenna.NewHennaEquip;

public class RequestNewHennaEquip extends ClientPacket
{
	private int _slotId;
	private int _symbolId;
	private int _otherItemId;

	@Override
	protected void readImpl()
	{
		this._slotId = this.readByte();
		this._symbolId = this.readInt();
		this._otherItemId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this.getClient().getFloodProtectors().canPerformTransaction())
			{
				if (player.getHennaEmptySlots() == 0)
				{
					PacketLogger.warning(player + ": Invalid Henna error 0 Id " + this._symbolId + " " + this._slotId);
					player.sendPacket(SystemMessageId.YOU_CANNOT_MAKE_A_PATTERN);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					Item item = player.getInventory().getItemByObjectId(this._symbolId);
					if (item == null)
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
						player.sendPacket(new NewHennaEquip(this._slotId, 0, false));
					}
					else
					{
						Henna henna = HennaData.getInstance().getHennaByItemId(item.getId());
						if (henna == null)
						{
							PacketLogger.warning(player + ": Invalid Henna SymbolId " + this._symbolId + " " + this._slotId + " " + item.getTemplate());
							player.sendPacket(ActionFailed.STATIC_PACKET);
							player.sendPacket(SystemMessageId.YOU_CANNOT_MAKE_A_PATTERN);
						}
						else
						{
							long _count = player.getInventory().getInventoryItemCount(henna.getDyeItemId(), -1);
							if (henna.isAllowedClass(player) && _count >= henna.getWearCount() && (player.getAdena() >= henna.getWearFee() || player.getInventory().getItemByItemId(91663).getCount() >= henna.getL2CoinFee()) && player.addHenna(this._slotId, henna))
							{
								int feeType = 0;
								if (this._otherItemId == 57)
								{
									feeType = henna.getWearFee();
								}

								if (this._otherItemId == 91663)
								{
									feeType = henna.getL2CoinFee();
								}

								player.destroyItemByItemId(ItemProcessType.FEE, henna.getDyeItemId(), henna.getWearCount(), player, true);
								player.destroyItemByItemId(ItemProcessType.FEE, this._otherItemId, feeType, player, true);
								if (player.getAdena() > 0L)
								{
									InventoryUpdate iu = new InventoryUpdate();
									iu.addModifiedItem(player.getInventory().getAdenaInstance());
									player.sendInventoryUpdate(iu);
								}

								player.sendPacket(new NewHennaEquip(this._slotId, henna.getDyeId(), true));
								player.getStat().recalculateStats(true);
								player.sendPacket(new UserInfo(player));
							}
							else
							{
								player.sendPacket(SystemMessageId.YOU_CANNOT_MAKE_A_PATTERN);
								if (!player.isGM() && !henna.isAllowedClass(player))
								{
									PunishmentManager.handleIllegalPlayerAction(player, "Exploit attempt: Character " + player.getName() + " of account " + player.getAccountName() + " tryed to add a forbidden henna.", GeneralConfig.DEFAULT_PUNISH);
								}

								player.sendPacket(ActionFailed.STATIC_PACKET);
								player.sendPacket(new NewHennaEquip(this._slotId, henna.getDyeId(), false));
							}
						}
					}
				}
			}
		}
	}
}
