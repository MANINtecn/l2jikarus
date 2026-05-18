package net.sf.l2jdev.gameserver.network.clientpackets.pet;

import net.sf.l2jdev.gameserver.data.holders.PetExtractionHolder;
import net.sf.l2jdev.gameserver.data.xml.NpcData;
import net.sf.l2jdev.gameserver.data.xml.PetDataTable;
import net.sf.l2jdev.gameserver.data.xml.PetExtractData;
import net.sf.l2jdev.gameserver.model.PetData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.PetInventory;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerInventory;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.pet.ResultPetExtractSystem;

public class ExTryPetExtractSystem extends ClientPacket
{
	private int _itemObjId;

	@Override
	protected void readImpl()
	{
		this._itemObjId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Item petItem = player.getInventory().getItemByObjectId(this._itemObjId);
			if (petItem != null && (player.getPet() == null || player.getPet().getControlItem() != petItem))
			{
				PetData petData = PetDataTable.getInstance().getPetDataByItemId(petItem.getId());
				NpcTemplate npcTemplate = NpcData.getInstance().getTemplate(petData.getNpcId());
				Pet pet = new Pet(npcTemplate, player, petItem);
				PetInventory petInventory = pet.getInventory();
				PlayerInventory playerInventory = player.getInventory();
				if (petInventory == null || playerInventory == null)
				{
					player.sendPacket(new ResultPetExtractSystem(false));
				}
				else if (playerInventory.validateWeight(petInventory.getTotalWeight()) && playerInventory.validateCapacity(petInventory.getSize()))
				{
					petInventory.transferItemsToOwner();
					Pet petInfo = Pet.restore(petItem, NpcData.getInstance().getTemplate(petData.getNpcId()), player);
					int petId = PetDataTable.getInstance().getPetDataByItemId(petItem.getId()).getType();
					int petLevel = petInfo.getLevel();
					PetExtractionHolder holder = PetExtractData.getInstance().getExtraction(petId, petLevel);
					if (holder == null)
					{
						player.sendPacket(new ResultPetExtractSystem(false));
					}
					else
					{
						int extractItemId = holder.getExtractItem();
						int extractItemCount = (int) (petInfo.getStat().getExp() / holder.getExtractExp());
						int extractCostId = holder.getExtractCost().getId();
						long extractCostCount = holder.getExtractCost().getCount() * extractItemCount;
						int defaultCostId = holder.getDefaultCost().getId();
						long defaultCostCount = holder.getDefaultCost().getCount();
						if (player.getInventory().getInventoryItemCount(extractCostId, -1) < extractCostCount || player.getInventory().getInventoryItemCount(defaultCostId, -1) < defaultCostCount)
						{
							player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT);
							player.sendPacket(new ResultPetExtractSystem(false));
						}
						else if (player.destroyItemByItemId(ItemProcessType.FEE, extractCostId, extractCostCount, player, true) && player.destroyItemByItemId(ItemProcessType.FEE, defaultCostId, defaultCostCount, player, true) && player.destroyItem(ItemProcessType.FEE, petItem, player, true))
						{
							player.addItem(ItemProcessType.REWARD, extractItemId, extractItemCount, player, true);
							player.sendPacket(new ResultPetExtractSystem(true));
						}
					}
				}
				else
				{
					player.sendPacket(SystemMessageId.CANNOT_BE_SENT_VIA_MAIL_SOLD_AT_A_SHOP_OR_VIA_THE_AUCTION_THE_GUARDIAN_S_INVENTORY_IS_NOT_EMPTY_PLEASE_TAKE_EVERYTHING_FROM_THERE_FIRST);
					player.sendPacket(new ResultPetExtractSystem(false));
				}
			}
			else
			{
				player.sendPacket(new ResultPetExtractSystem(false));
			}
		}
	}
}
