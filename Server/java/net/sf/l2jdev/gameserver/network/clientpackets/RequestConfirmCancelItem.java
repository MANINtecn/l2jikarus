package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.VariationData;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPutItemResultForVariationCancel;

public class RequestConfirmCancelItem extends ClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Item item = player.getInventory().getItemByObjectId(this._objectId);
			if (item != null)
			{
				if (item.getOwnerId() != player.getObjectId())
				{
					PunishmentManager.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tryied to destroy augment on item that doesn't own.", GeneralConfig.DEFAULT_PUNISH);
				}
				else if (!item.isAugmented())
				{
					player.sendPacket(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
				}
				else if (item.isPvp() && !PlayerConfig.ALT_ALLOW_AUGMENT_PVP_ITEMS)
				{
					player.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
				}
				else
				{
					long price = VariationData.getInstance().getCancelFee(item.getId(), item.getAugmentation().getMineralId());
					if (price < 0L)
					{
						player.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
					}
					else
					{
						player.sendPacket(new ExPutItemResultForVariationCancel(item, price));
					}
				}
			}
		}
	}
}
