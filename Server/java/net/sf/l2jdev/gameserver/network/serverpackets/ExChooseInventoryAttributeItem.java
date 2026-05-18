package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.HashSet;
import java.util.Set;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.ElementalAttributeData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExChooseInventoryAttributeItem extends ServerPacket
{
	private final int _itemId;
	private final long _count;
	private final AttributeType _atribute;
	private final int _level;
	private final Set<Integer> _items = new HashSet<>();

	public ExChooseInventoryAttributeItem(Player player, Item stone)
	{
		this._itemId = stone.getDisplayId();
		this._count = stone.getCount();
		this._atribute = ElementalAttributeData.getInstance().getItemElement(this._itemId);
		if (this._atribute == AttributeType.NONE)
		{
			throw new IllegalArgumentException("Undefined Atribute item: " + stone);
		}
		this._level = ElementalAttributeData.getInstance().getMaxElementLevel(this._itemId);

		for (Item item : player.getInventory().getItems())
		{
			if (item.isElementable())
			{
				this._items.add(item.getObjectId());
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CHOOSE_INVENTORY_ATTRIBUTE_ITEM.writeId(this, buffer);
		buffer.writeInt(this._itemId);
		buffer.writeLong(this._count);
		buffer.writeInt(this._atribute == AttributeType.FIRE);
		buffer.writeInt(this._atribute == AttributeType.WATER);
		buffer.writeInt(this._atribute == AttributeType.WIND);
		buffer.writeInt(this._atribute == AttributeType.EARTH);
		buffer.writeInt(this._atribute == AttributeType.HOLY);
		buffer.writeInt(this._atribute == AttributeType.DARK);
		buffer.writeInt(this._level);
		buffer.writeInt(this._items.size());
		this._items.forEach(buffer::writeInt);
	}
}
