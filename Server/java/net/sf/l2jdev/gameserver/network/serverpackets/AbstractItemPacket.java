package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.PetDataTable;
import net.sf.l2jdev.gameserver.data.xml.PetTypeData;
import net.sf.l2jdev.gameserver.model.ItemInfo;
import net.sf.l2jdev.gameserver.model.TradeItem;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.actor.holders.creature.PetEvolveHolder;
import net.sf.l2jdev.gameserver.model.buylist.Product;
import net.sf.l2jdev.gameserver.model.ensoul.EnsoulOption;
import net.sf.l2jdev.gameserver.model.item.WarehouseItem;
import net.sf.l2jdev.gameserver.model.item.enums.ItemListType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerInventory;

public abstract class AbstractItemPacket extends AbstractMaskPacket<ItemListType>
{
	private static final byte[] MASKS = new byte[]
	{
		0
	};

	@Override
	protected byte[] getMasks()
	{
		return MASKS;
	}

	protected void writeItem(TradeItem item, long count, WritableBuffer buffer)
	{
		this.writeItem(new ItemInfo(item), count, buffer);
	}

	protected void writeItem(TradeItem item, WritableBuffer buffer)
	{
		this.writeItem(new ItemInfo(item), buffer);
	}

	protected void writeItem(WarehouseItem item, WritableBuffer buffer)
	{
		this.writeItem(new ItemInfo(item), buffer);
	}

	protected void writeItem(Item item, WritableBuffer buffer)
	{
		this.writeItem(new ItemInfo(item), buffer);
	}

	protected void writeItem(Product item, WritableBuffer buffer)
	{
		this.writeItem(new ItemInfo(item), buffer);
	}

	protected void writeItem(ItemInfo item, WritableBuffer buffer)
	{
		int mask = calculateMask(item);
		buffer.writeShort(mask);
		buffer.writeInt(item.getObjectId());
		buffer.writeInt(item.getItem().getDisplayId());
		buffer.writeByte(!item.getItem().isQuestItem() && item.getEquipped() != 1 ? item.getLocation() : 255);
		buffer.writeLong(item.getCount());
		buffer.writeByte(item.getItem().getType2());
		buffer.writeByte(item.getCustomType1());
		buffer.writeShort(item.getEquipped());
		buffer.writeLong(item.isPetEquipped() ? item.getItem().getPetBodyPart().getMask() : item.getItem().getBodyPart().getMask());
		buffer.writeShort(item.getEnchantLevel());
		buffer.writeInt(Math.max(0, item.getMana()));
		buffer.writeByte(0);
		buffer.writeInt(item.getTime());
		buffer.writeByte(item.isAvailable());
		buffer.writeShort(0);
		if (this.containsMask(mask, ItemListType.AUGMENT_BONUS))
		{
			this.writeItemAugment(item, buffer);
		}

		if (this.containsMask(mask, ItemListType.ELEMENTAL_ATTRIBUTE))
		{
			this.writeItemElemental(item, buffer);
		}

		if (this.containsMask(mask, ItemListType.VISUAL_ID))
		{
			buffer.writeInt(item.getVisualId());
		}

		if (this.containsMask(mask, ItemListType.SOUL_CRYSTAL))
		{
			this.writeItemEnsoulOptions(item, buffer);
		}

		if (this.containsMask(mask, ItemListType.PET_EVOLVE))
		{
			PetEvolveHolder petData = item.getPetData();
			if (petData != null)
			{
				buffer.writeInt(petData.getEvolve().ordinal());
				buffer.writeInt(PetTypeData.getInstance().getIdByName(petData.getName()));
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeInt(PetDataTable.getInstance().getTypeByIndex(petData.getIndex()));
				buffer.writeLong(petData.getExp());
			}
			else
			{
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeLong(0L);
			}
		}

		if (this.containsMask(mask, ItemListType.BLESSED))
		{
			buffer.writeByte(1);
		}
	}

	protected void writeItem(ItemInfo item, long count, WritableBuffer buffer)
	{
		int mask = calculateMask(item);
		buffer.writeShort(mask);
		buffer.writeInt(item.getObjectId());
		buffer.writeInt(item.getItem().getDisplayId());
		buffer.writeByte(!item.getItem().isQuestItem() && item.getEquipped() != 1 ? item.getLocation() : 255);
		buffer.writeLong(count);
		buffer.writeByte(item.getItem().getType2());
		buffer.writeByte(item.getCustomType1());
		buffer.writeShort(item.getEquipped());
		buffer.writeLong(item.getItem().getBodyPart().getMask());
		buffer.writeShort(item.getEnchantLevel());
		buffer.writeInt(Math.max(0, item.getMana()));
		buffer.writeByte(0);
		buffer.writeInt(item.getTime());
		buffer.writeByte(item.isAvailable());
		buffer.writeShort(0);
		if (this.containsMask(mask, ItemListType.AUGMENT_BONUS))
		{
			this.writeItemAugment(item, buffer);
		}

		if (this.containsMask(mask, ItemListType.ELEMENTAL_ATTRIBUTE))
		{
			this.writeItemElemental(item, buffer);
		}

		if (this.containsMask(mask, ItemListType.VISUAL_ID))
		{
			buffer.writeInt(item.getVisualId());
		}

		if (this.containsMask(mask, ItemListType.SOUL_CRYSTAL))
		{
			this.writeItemEnsoulOptions(item, buffer);
		}

		if (this.containsMask(mask, ItemListType.PET_EVOLVE))
		{
			PetEvolveHolder petData = item.getPetData();
			if (petData != null)
			{
				buffer.writeInt(petData.getEvolve().ordinal());
				buffer.writeInt(PetTypeData.getInstance().getIdByName(petData.getName()));
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeInt(PetDataTable.getInstance().getTypeByIndex(petData.getIndex()));
				buffer.writeLong(petData.getExp());
			}
			else
			{
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeLong(0L);
			}
		}

		if (this.containsMask(mask, ItemListType.BLESSED))
		{
			buffer.writeByte(1);
		}
	}

	protected static int calculateMask(ItemInfo item)
	{
		int mask = 0;
		if (item.getAugmentation() != null)
		{
			mask |= ItemListType.AUGMENT_BONUS.getMask();
		}

		if (item.getAttackElementType() >= 0 || item.getAttributeDefence(AttributeType.FIRE) > 0 || item.getAttributeDefence(AttributeType.WATER) > 0 || item.getAttributeDefence(AttributeType.WIND) > 0 || item.getAttributeDefence(AttributeType.EARTH) > 0 || item.getAttributeDefence(AttributeType.HOLY) > 0 || item.getAttributeDefence(AttributeType.DARK) > 0)
		{
			mask |= ItemListType.ELEMENTAL_ATTRIBUTE.getMask();
		}

		if (item.getVisualId() > 0)
		{
			mask |= ItemListType.VISUAL_ID.getMask();
		}

		if (item.getSoulCrystalOptions() != null && !item.getSoulCrystalOptions().isEmpty() || item.getSoulCrystalSpecialOptions() != null && !item.getSoulCrystalSpecialOptions().isEmpty())
		{
			mask |= ItemListType.SOUL_CRYSTAL.getMask();
		}

		if (item.getItem().isPetItem() && item.getPetData() != null)
		{
			mask |= ItemListType.PET_EVOLVE.getMask();
		}

		if (item.isBlessed())
		{
			mask |= ItemListType.BLESSED.getMask();
		}

		return mask;
	}

	protected void writeItemAugment(ItemInfo item, WritableBuffer buffer)
	{
		if (item != null && item.getAugmentation() != null)
		{
			buffer.writeInt(item.getAugmentation().getOption1Id());
			buffer.writeInt(item.getAugmentation().getOption2Id());
			buffer.writeInt(item.getAugmentation().getOption3Id());
		}
		else
		{
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
		}
	}

	protected void writeItemElementalAndEnchant(ItemInfo item, WritableBuffer buffer)
	{
		this.writeItemElemental(item, buffer);
		this.writeItemEnchantEffect(item, buffer);
	}

	protected void writeItemElemental(ItemInfo item, WritableBuffer buffer)
	{
		if (item != null)
		{
			buffer.writeShort(item.getAttackElementType());
			buffer.writeShort(item.getAttackElementPower());
			buffer.writeShort(item.getAttributeDefence(AttributeType.FIRE));
			buffer.writeShort(item.getAttributeDefence(AttributeType.WATER));
			buffer.writeShort(item.getAttributeDefence(AttributeType.WIND));
			buffer.writeShort(item.getAttributeDefence(AttributeType.EARTH));
			buffer.writeShort(item.getAttributeDefence(AttributeType.HOLY));
			buffer.writeShort(item.getAttributeDefence(AttributeType.DARK));
		}
		else
		{
			buffer.writeShort(0);
			buffer.writeShort(0);
			buffer.writeShort(0);
			buffer.writeShort(0);
			buffer.writeShort(0);
			buffer.writeShort(0);
			buffer.writeShort(0);
			buffer.writeShort(0);
		}
	}

	protected void writeItemEnchantEffect(ItemInfo item, WritableBuffer buffer)
	{
		for (int op : item.getEnchantOptions())
		{
			buffer.writeInt(op);
		}
	}

	protected void writeItemEnsoulOptions(ItemInfo item, WritableBuffer buffer)
	{
		if (item != null)
		{
			buffer.writeByte(item.getSoulCrystalOptions().size());

			for (EnsoulOption option : item.getSoulCrystalOptions())
			{
				buffer.writeInt(option.getId());
			}

			buffer.writeByte(item.getSoulCrystalSpecialOptions().size());

			for (EnsoulOption option : item.getSoulCrystalSpecialOptions())
			{
				buffer.writeInt(option.getId());
			}
		}
		else
		{
			buffer.writeByte(0);
			buffer.writeByte(0);
		}
	}

	protected void writeInventoryBlock(PlayerInventory inventory, WritableBuffer buffer)
	{
		if (inventory.hasInventoryBlock())
		{
			buffer.writeShort(inventory.getBlockItems().size());
			buffer.writeByte(inventory.getBlockMode().getClientId());

			for (int id : inventory.getBlockItems())
			{
				buffer.writeInt(id);
			}
		}
		else
		{
			buffer.writeShort(0);
		}
	}

	protected int calculatePacketSize(ItemInfo item)
	{
		int mask = calculateMask(item);
		int size = 0;
		size += 2;
		size += 4;
		size += 4;
		size = ++size + 8;
		size++;
		size = ++size + 2;
		size += 8;
		size += 2;
		size += 4;
		size = ++size + 4;
		size = ++size + 2;
		if (this.containsMask(mask, ItemListType.AUGMENT_BONUS))
		{
			size += 8;
		}

		if (this.containsMask(mask, ItemListType.ELEMENTAL_ATTRIBUTE))
		{
			size += 16;
		}

		if (this.containsMask(mask, ItemListType.VISUAL_ID))
		{
			size += 4;
		}

		if (this.containsMask(mask, ItemListType.SOUL_CRYSTAL))
		{
			size = ++size + item.getSoulCrystalOptions().size() * 4;
			size = ++size + item.getSoulCrystalSpecialOptions().size() * 4;
		}

		if (this.containsMask(mask, ItemListType.PET_EVOLVE))
		{
			size += 4;
			size += 4;
			size += 4;
			size += 4;
			size += 4;
			size += 8;
		}

		if (this.containsMask(mask, ItemListType.BLESSED))
		{
			size++;
		}

		return size;
	}
}
