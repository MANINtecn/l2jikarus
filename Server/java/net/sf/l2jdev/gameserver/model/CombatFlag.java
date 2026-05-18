package net.sf.l2jdev.gameserver.model;

import net.sf.l2jdev.gameserver.managers.ItemManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class CombatFlag
{
	private Player _player = null;
	private int _playerId = 0;
	private Item _item = null;
	private Item _itemInstance;
	private final Location _location;
	private final int _itemId;
	public final int _fortId;

	public CombatFlag(int fortId, int x, int y, int z, int heading, int itemId)
	{
		this._fortId = fortId;
		this._location = new Location(x, y, z, heading);
		this._itemId = itemId;
	}

	public synchronized void spawnMe()
	{
		this._itemInstance = ItemManager.createItem(ItemProcessType.QUEST, this._itemId, 1L, null, null);
		this._itemInstance.dropMe(null, this._location.getX(), this._location.getY(), this._location.getZ());
	}

	public synchronized void unSpawnMe()
	{
		if (this._player != null)
		{
			this.dropIt();
		}

		if (this._itemInstance != null)
		{
			this._itemInstance.decayMe();
		}
	}

	public boolean activate(Player player, Item item)
	{
		if (player.isMounted())
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
			return false;
		}
		this._player = player;
		this._playerId = this._player.getObjectId();
		this._itemInstance = null;
		this._item = item;
		this._player.getInventory().equipItem(this._item);
		SystemMessage sm = new SystemMessage(SystemMessageId.S1_EQUIPPED);
		sm.addItemName(this._item);
		this._player.sendPacket(sm);
		InventoryUpdate iu = new InventoryUpdate();
		iu.addItem(this._item);
		this._player.sendInventoryUpdate(iu);
		this._player.broadcastUserInfo();
		this._player.setCombatFlagEquipped(true);
		return true;
	}

	public void dropIt()
	{
		this._player.setCombatFlagEquipped(false);
		BodyPart bodyPart = BodyPart.fromItem(this._item);
		this._player.getInventory().unEquipItemInBodySlot(bodyPart);
		this._player.destroyItem(ItemProcessType.DESTROY, this._item, null, true);
		this._item = null;
		this._player.broadcastUserInfo();
		this._player = null;
		this._playerId = 0;
	}

	public int getPlayerObjectId()
	{
		return this._playerId;
	}

	public Item getCombatFlagInstance()
	{
		return this._itemInstance;
	}
}
