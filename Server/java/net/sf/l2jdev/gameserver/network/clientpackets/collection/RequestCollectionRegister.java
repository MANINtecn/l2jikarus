package net.sf.l2jdev.gameserver.network.clientpackets.collection;

import net.sf.l2jdev.gameserver.data.holders.CollectionDataHolder;
import net.sf.l2jdev.gameserver.data.xml.CollectionData;
import net.sf.l2jdev.gameserver.data.xml.OptionData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.PlayerCollectionData;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemEnchantHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.options.Options;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ConfirmDlg;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.collection.ExCollectionComplete;
import net.sf.l2jdev.gameserver.network.serverpackets.collection.ExCollectionRegister;

public class RequestCollectionRegister extends ClientPacket
{
	private int _collectionId;
	private int _index;
	private int _itemObjId;

	@Override
	protected void readImpl()
	{
		this._collectionId = this.readShort();
		this._index = this.readInt();
		this._itemObjId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Item item = player.getInventory().getItemByObjectId(this._itemObjId);
			if (item == null)
			{
				player.sendMessage("Item not found.");
			}
			else
			{
				CollectionDataHolder collection = CollectionData.getInstance().getCollection(this._collectionId);
				if (collection == null)
				{
					player.sendMessage("Could not find collection.");
				}
				else
				{
					long count = 0L;

					for (ItemEnchantHolder data : collection.getItems())
					{
						if (data.getId() == item.getId() && (data.getEnchantLevel() == 0 || data.getEnchantLevel() == item.getEnchantLevel()))
						{
							count = data.getCount();
							break;
						}
					}

					if (count != 0L && item.getCount() >= count && !item.isEquipped())
					{
						PlayerCollectionData currentColl = null;

						for (PlayerCollectionData coll : player.getCollections())
						{
							if (coll.getCollectionId() == this._collectionId)
							{
								currentColl = coll;
								break;
							}
						}

						if (currentColl != null && currentColl.getIndex() == this._index)
						{
							player.sendPacket(new ExCollectionRegister(false, this._collectionId, this._index, new ItemEnchantHolder(item.getId(), count, item.getEnchantLevel())));
							player.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_ADDED_TO_YOUR_COLLECTION);
							player.sendPacket(new ConfirmDlg("Collection already registered;"));
						}
						else
						{
							player.destroyItem(ItemProcessType.FEE, item, count, player, true);
							player.sendPacket(new ExCollectionRegister(true, this._collectionId, this._index, new ItemEnchantHolder(item.getId(), count, item.getEnchantLevel())));
							player.getCollections().add(new PlayerCollectionData(this._collectionId, item.getId(), this._index));
							int completeCount = 0;

							for (PlayerCollectionData collx : player.getCollections())
							{
								if (collx.getCollectionId() == this._collectionId)
								{
									completeCount++;
								}
							}

							if (completeCount == collection.getCompleteCount())
							{
								player.sendPacket(new ExCollectionComplete(this._collectionId));
								player.sendPacket(new SystemMessage(SystemMessageId.S1_COLLECTION_IS_COMPLETE).addString(""));
								Options options = OptionData.getInstance().getOptions(collection.getOptionId());
								if (options != null)
								{
									options.apply(player);
								}
							}
						}
					}
					else
					{
						player.sendMessage("Incorrect item count.");
					}
				}
			}
		}
	}
}
