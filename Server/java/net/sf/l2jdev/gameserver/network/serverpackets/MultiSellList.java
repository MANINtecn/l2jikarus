package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.holders.MultisellEntryHolder;
import net.sf.l2jdev.gameserver.data.holders.PreparedMultisellListHolder;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.model.ItemInfo;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.holders.ItemChanceHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class MultiSellList extends AbstractItemPacket
{
	private final Player _player;
	private int _size;
	private int _index;
	private final PreparedMultisellListHolder _list;
	private final boolean _finished;
	private final int _type;

	public MultiSellList(Player player, PreparedMultisellListHolder list, int index, int type)
	{
		this._player = player;
		this._list = list;
		this._index = index;
		this._size = list.getEntries().size() - index;
		if (this._size > 40)
		{
			this._finished = false;
			this._size = 40;
		}
		else
		{
			this._finished = true;
		}

		this._type = type;
	}

	public MultiSellList(Player player, PreparedMultisellListHolder list, int index)
	{
		this(player, list, index, 0);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.MULTI_SELL_LIST.writeId(this, buffer);
		buffer.writeByte(0);
		buffer.writeInt(this._list.getId());
		buffer.writeByte(this._type);
		buffer.writeInt(1 + this._index / 40);
		buffer.writeInt(this._finished);
		buffer.writeInt(40);
		buffer.writeInt(this._size);
		buffer.writeByte(0);
		buffer.writeByte(this._list.isChanceMultisell());
		buffer.writeInt(32);

		while (this._size-- > 0)
		{
			ItemInfo itemEnchantment = this._list.getItemEnchantment(this._index);
			MultisellEntryHolder entry = this._list.getEntries().get(this._index++);
			if (itemEnchantment == null && this._list.isMaintainEnchantment())
			{
				for (ItemChanceHolder holder : entry.getIngredients())
				{
					Item item = this._player.getInventory().getItemByItemId(holder.getId());
					if (item != null && item.isEquipable() && !item.isEquipped())
					{
						itemEnchantment = new ItemInfo(item);
						break;
					}
				}
			}

			buffer.writeInt(this._index);
			buffer.writeByte(entry.isStackable());
			buffer.writeShort(itemEnchantment != null ? itemEnchantment.getEnchantLevel() : 0);
			this.writeItemAugment(itemEnchantment, buffer);
			this.writeItemElemental(itemEnchantment, buffer);
			this.writeItemEnsoulOptions(itemEnchantment, buffer);
			buffer.writeByte(0);
			buffer.writeShort(entry.getProducts().size());
			buffer.writeShort(entry.getIngredients().size());

			for (ItemChanceHolder product : entry.getProducts())
			{
				ItemTemplate template = ItemData.getInstance().getTemplate(product.getId());
				ItemInfo displayItemEnchantment = this._list.isMaintainEnchantment() && itemEnchantment != null && template != null && template.getClass().equals(itemEnchantment.getItem().getClass()) ? itemEnchantment : null;
				if (template != null)
				{
					buffer.writeInt(template.getDisplayId());
					buffer.writeLong(template.getBodyPart().getMask());
					buffer.writeShort(template.getType2());
				}
				else
				{
					buffer.writeInt(product.getId());
					buffer.writeLong(0L);
					buffer.writeShort(65535);
				}

				buffer.writeLong(this._list.getProductCount(product));
				buffer.writeShort(product.getEnchantmentLevel() > 0 ? product.getEnchantmentLevel() : (displayItemEnchantment != null ? displayItemEnchantment.getEnchantLevel() : 0));
				buffer.writeInt((int) (product.getChance() * 1000000.0));
				this.writeItemAugment(displayItemEnchantment, buffer);
				this.writeItemElemental(displayItemEnchantment, buffer);
				this.writeItemEnsoulOptions(displayItemEnchantment, buffer);
				buffer.writeByte(0);
			}

			for (ItemChanceHolder ingredient : entry.getIngredients())
			{
				ItemTemplate template = ItemData.getInstance().getTemplate(ingredient.getId());
				ItemInfo displayItemEnchantment = itemEnchantment != null && template != null && template.getClass().equals(itemEnchantment.getItem().getClass()) ? itemEnchantment : null;
				if (template != null)
				{
					buffer.writeInt(template.getDisplayId());
					buffer.writeShort(template.getType2());
				}
				else
				{
					buffer.writeInt(ingredient.getId());
					buffer.writeShort(65535);
				}

				buffer.writeLong(this._list.getIngredientCount(ingredient));
				buffer.writeShort(ingredient.getEnchantmentLevel() > 0 ? ingredient.getEnchantmentLevel() : (displayItemEnchantment != null ? displayItemEnchantment.getEnchantLevel() : 0));
				this.writeItemAugment(displayItemEnchantment, buffer);
				this.writeItemElemental(displayItemEnchantment, buffer);
				this.writeItemEnsoulOptions(displayItemEnchantment, buffer);
				buffer.writeByte(0);
			}
		}
	}
}
