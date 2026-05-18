package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExItemAnnounce extends ServerPacket
{
	public static final int ENCHANT = 0;
	public static final int LOOT_BOX = 1;
	public static final int RANDOM_CRAFT = 2;
	public static final int SPECIAL_CREATION = 3;
	public static final int WORKSHOP = 4;
	public static final int EVENT_PARTICIPATE = 5;
	public static final int LIMITED_CRAFT = 6;
	public static final int CONTAINER = 7;
	public static final int COMPOUND = 8;
	public static final int CRAFT_SYSTEM_FANCY = 9;
	public static final int UPGRADE = 10;
	private final Item _item;
	private final int _type;
	private final String _announceName;

	public ExItemAnnounce(Player player, Item item, int type)
	{
		this._item = item;
		this._type = type;
		if (!player.getClientSettings().isAnnounceDisabled())
		{
			this._announceName = player.getName();
		}
		else if ("ru".equals(player.getLang()))
		{
			this._announceName = "Некто";
		}
		else
		{
			this._announceName = "Someone";
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ITEM_ANNOUNCE.writeId(this, buffer);
		buffer.writeByte(this._type);
		buffer.writeSizedString(this._announceName);
		buffer.writeInt(this._item.getId());
		buffer.writeByte(this._item.getEnchantLevel());
		buffer.writeInt(0);
	}
}
