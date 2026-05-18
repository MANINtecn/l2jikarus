package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExStorageMaxCount extends ServerPacket
{
	private Player _player;
	private int _inventory;
	private int _warehouse;
	private int _clan;
	private int _privateSell;
	private int _privateBuy;
	private int _receipeD;
	private int _recipe;
	private int _inventoryExtraSlots;
	private int _inventoryQuestItems;

	public ExStorageMaxCount(Player player)
	{
		if (!player.isSubclassLocked())
		{
			this._player = player;
			this._inventory = player.getInventoryLimit();
			this._warehouse = player.getWareHouseLimit();
			this._privateSell = player.getPrivateSellStoreLimit();
			this._privateBuy = player.getPrivateBuyStoreLimit();
			this._clan = PlayerConfig.WAREHOUSE_SLOTS_CLAN;
			this._receipeD = player.getDwarfRecipeLimit();
			this._recipe = player.getCommonRecipeLimit();
			this._inventoryExtraSlots = (int) player.getStat().getValue(Stat.INVENTORY_NORMAL, 0.0);
			this._inventoryQuestItems = PlayerConfig.INVENTORY_MAXIMUM_QUEST_ITEMS;
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if (this._player != null)
		{
			ServerPackets.EX_STORAGE_MAX_COUNT.writeId(this, buffer);
			buffer.writeInt(this._inventory);
			buffer.writeInt(this._warehouse);
			buffer.writeInt(this._clan);
			buffer.writeInt(this._privateSell);
			buffer.writeInt(this._privateBuy);
			buffer.writeInt(this._receipeD);
			buffer.writeInt(this._recipe);
			buffer.writeInt(this._inventoryExtraSlots);
			buffer.writeInt(this._inventoryQuestItems);
			buffer.writeInt(40);
			buffer.writeInt(40);
			buffer.writeInt(100);
		}
	}
}
