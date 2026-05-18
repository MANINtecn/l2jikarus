package net.sf.l2jdev.gameserver.network.serverpackets.attributechange;

import java.util.EnumMap;
import java.util.Map;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExChangeAttributeInfo extends ServerPacket
{
	private static final Map<AttributeType, Byte> ATTRIBUTE_MASKS = new EnumMap<>(AttributeType.class);
	private final int _crystalItemId;
	private int _attributes;
	private int _itemObjId;

	public ExChangeAttributeInfo(int crystalItemId, Item item)
	{
		this._crystalItemId = crystalItemId;
		this._attributes = 0;

		for (AttributeType e : AttributeType.ATTRIBUTE_TYPES)
		{
			if (e != item.getAttackAttributeType())
			{
				this._attributes = this._attributes | ATTRIBUTE_MASKS.get(e);
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CHANGE_ATTRIBUTE_INFO.writeId(this, buffer);
		buffer.writeInt(this._crystalItemId);
		buffer.writeInt(this._attributes);
		buffer.writeInt(this._itemObjId);
	}

	static
	{
		ATTRIBUTE_MASKS.put(AttributeType.FIRE, (byte) 1);
		ATTRIBUTE_MASKS.put(AttributeType.WATER, (byte) 2);
		ATTRIBUTE_MASKS.put(AttributeType.WIND, (byte) 4);
		ATTRIBUTE_MASKS.put(AttributeType.EARTH, (byte) 8);
		ATTRIBUTE_MASKS.put(AttributeType.HOLY, (byte) 16);
		ATTRIBUTE_MASKS.put(AttributeType.DARK, (byte) 32);
	}
}
