package net.sf.l2jdev.gameserver.network.clientpackets.ensoul;

import java.util.Collection;

import net.sf.l2jdev.gameserver.data.xml.EnsoulData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.ensoul.EnsoulOption;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.ensoul.ExEnSoulExtractionResult;

public class RequestTryEnSoulExtraction extends ClientPacket
{
	private int _itemObjectId;
	private int _type;
	private int _position;

	@Override
	protected void readImpl()
	{
		this._itemObjectId = this.readInt();
		this._type = this.readByte();
		this._position = this.readByte() - 1;
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Item item = player.getInventory().getItemByObjectId(this._itemObjectId);
			if (item != null)
			{
				EnsoulOption option = null;
				if (this._type == 1)
				{
					option = item.getSpecialAbility(this._position);
					if (option == null && this._position == 0)
					{
						option = item.getSpecialAbility(1);
						if (option != null)
						{
							this._position = 1;
						}
					}
				}

				if (this._type == 2)
				{
					option = item.getAdditionalSpecialAbility(this._position);
				}

				if (option != null)
				{
					int runeId = EnsoulData.getInstance().getStone(this._type, option.getId());
					Collection<ItemHolder> removalFee = EnsoulData.getInstance().getRemovalFee(runeId);
					if (!removalFee.isEmpty())
					{
						for (ItemHolder itemHolder : removalFee)
						{
							if (player.getInventory().getInventoryItemCount(itemHolder.getId(), -1) < itemHolder.getCount())
							{
								player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT);
								player.sendPacket(new ExEnSoulExtractionResult(false, item));
								return;
							}
						}

						for (ItemHolder itemHolderx : removalFee)
						{
							player.destroyItemByItemId(ItemProcessType.FEE, itemHolderx.getId(), itemHolderx.getCount(), player, true);
						}

						item.removeSpecialAbility(this._position, this._type);
						InventoryUpdate iu = new InventoryUpdate();
						iu.addModifiedItem(item);
						if (runeId > 0)
						{
							iu.addItem(player.addItem(ItemProcessType.REWARD, runeId, 1L, player, true));
						}

						player.sendInventoryUpdate(iu);
						player.sendItemList();
						player.sendPacket(new ExEnSoulExtractionResult(true, item));
					}
				}
			}
		}
	}
}
