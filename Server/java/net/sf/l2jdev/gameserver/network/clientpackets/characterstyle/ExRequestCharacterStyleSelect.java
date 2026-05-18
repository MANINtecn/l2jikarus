package net.sf.l2jdev.gameserver.network.clientpackets.characterstyle;

import net.sf.l2jdev.gameserver.data.enums.CharacterStyleCategoryType;
import net.sf.l2jdev.gameserver.data.xml.CharacterStylesData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUserInfoEquipSlot;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.characterstyle.ExCharacterStyleSelect;

public class ExRequestCharacterStyleSelect extends ClientPacket
{
	private int _styleType;
	private int _styleId;

	@Override
	protected void readImpl()
	{
		this._styleType = this.readInt();
		this._styleId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			CharacterStyleCategoryType category = CharacterStyleCategoryType.getByClientId(this._styleType);
			if (this._styleId != 0)
			{
				ItemHolder applyCostHolder = CharacterStylesData.getInstance().getSwapCostItemByCategory(category);
				if (applyCostHolder != null)
				{
					if ((player.getInventory().getInventoryItemCount(applyCostHolder.getId(), -1) < applyCostHolder.getCount()) || (player.getInventory().destroyItemByItemId(ItemProcessType.DESTROY, applyCostHolder.getId(), applyCostHolder.getCount(), player, null) == null))
					{
						player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS));
						return;
					}
				}
			}

			player.setActiveCharacterStyle(category, this._styleId);
			player.sendPacket(ExCharacterStyleSelect.STATIC_PACKET);
			if (category == CharacterStyleCategoryType.APPEARANCE_WEAPON)
			{
				player.sendPacket(new ExUserInfoEquipSlot(player));
				player.broadcastUserInfo();
			}
		}
	}
}
