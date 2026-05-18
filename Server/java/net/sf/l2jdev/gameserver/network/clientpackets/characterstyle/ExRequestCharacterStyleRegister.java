package net.sf.l2jdev.gameserver.network.clientpackets.characterstyle;

import net.sf.l2jdev.gameserver.data.enums.CharacterStyleCategoryType;
import net.sf.l2jdev.gameserver.data.holders.CharacterStyleDataHolder;
import net.sf.l2jdev.gameserver.data.xml.CharacterStylesData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.characterstyle.ExCharacterStyleRegister;

public class ExRequestCharacterStyleRegister extends ClientPacket
{
	private int _styleType;
	private int _styleId;

	@Override
	protected void readImpl()
	{
		this._styleType = this.readInt();
		this._styleId = this.readInt();
		this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			CharacterStyleCategoryType category = CharacterStyleCategoryType.getByClientId(this._styleType);
			CharacterStyleDataHolder style = CharacterStylesData.getInstance().getSpecificStyleByCategoryAndId(category, this._styleId);
			if (category != null && style != null)
			{
				for (ItemHolder price : style._cost)
				{
					if (player.getInventory().getInventoryItemCount(price.getId(), -1) < price.getCount())
					{
						player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS));
						player.sendPacket(ExCharacterStyleRegister.STATIC_PACKET_FAIL);
						return;
					}
				}

				for (ItemHolder pricex : style._cost)
				{
					if (player.getInventory().destroyItemByItemId(ItemProcessType.DESTROY, pricex.getId(), pricex.getCount(), player, null) == null)
					{
						player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS));
						player.sendPacket(ExCharacterStyleRegister.STATIC_PACKET_FAIL);
						return;
					}
				}

				player.modifyCharacterStyle(category, this._styleId, false, true);
				player.sendPacket(ExCharacterStyleRegister.STATIC_PACKET_SUCCESS);
			}
			else
			{
				player.sendPacket(ExCharacterStyleRegister.STATIC_PACKET_FAIL);
			}
		}
	}
}
