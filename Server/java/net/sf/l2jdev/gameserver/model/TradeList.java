package net.sf.l2jdev.gameserver.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.managers.PrivateStoreHistoryManager;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PrivateStoreType;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerInventory;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPrivateStoreBuyingResult;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPrivateStoreSellingResult;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class TradeList
{
	private static final Logger LOGGER = Logger.getLogger(TradeList.class.getName());
	private final Player _owner;
	private Player _partner;
	private final Set<TradeItem> _items = ConcurrentHashMap.newKeySet();
	private String _title;
	private boolean _packaged;
	private boolean _confirmed = false;
	private boolean _locked = false;

	public TradeList(Player owner)
	{
		this._owner = owner;
	}

	public Player getOwner()
	{
		return this._owner;
	}

	public void setPartner(Player partner)
	{
		this._partner = partner;
	}

	public Player getPartner()
	{
		return this._partner;
	}

	public void setTitle(String title)
	{
		this._title = title;
	}

	public String getTitle()
	{
		return this._title;
	}

	public boolean isLocked()
	{
		return this._locked;
	}

	public boolean isConfirmed()
	{
		return this._confirmed;
	}

	public boolean isPackaged()
	{
		return this._packaged;
	}

	public void setPackaged(boolean value)
	{
		this._packaged = value;
	}

	public Collection<TradeItem> getItems()
	{
		return this._items;
	}

	public Collection<TradeItem> getAvailableItems(PlayerInventory inventory)
	{
		List<TradeItem> list = new LinkedList<>();

		for (TradeItem item : this._items)
		{
			item = new TradeItem(item, item.getCount(), item.getPrice());
			inventory.adjustAvailableItem(item);
			list.add(item);
		}

		return list;
	}

	public int getItemCount()
	{
		return this._items.size();
	}

	public TradeItem adjustAvailableItem(Item item)
	{
		if (item.isStackable())
		{
			for (TradeItem exclItem : this._items)
			{
				if (exclItem.getItem().getId() == item.getId())
				{
					return item.getCount() <= exclItem.getCount() ? null : new TradeItem(item, item.getCount() - exclItem.getCount(), item.getReferencePrice());
				}
			}
		}

		return new TradeItem(item, item.getCount(), item.getReferencePrice());
	}

	public TradeItem addItem(int objectId, long count)
	{
		return this.addItem(objectId, count, 0L);
	}

	public synchronized TradeItem addItem(int objectId, long count, long price)
	{
		if (this._locked)
		{
			return null;
		}
		else if (!(World.getInstance().findObject(objectId) instanceof Item item))
		{
			return null;
		}
		else if ((item.isTradeable() || this._owner.isGM() && GeneralConfig.GM_TRADE_RESTRICTED_ITEMS) && !item.isQuestItem())
		{
			if (!this._owner.getInventory().canManipulateWithItemId(item.getId()))
			{
				return null;
			}
			else if (count > 0L && count <= item.getCount())
			{
				if (!item.isStackable() && count > 1L)
				{
					return null;
				}
				else if (Inventory.MAX_ADENA / count < price)
				{
					return null;
				}
				else
				{
					for (TradeItem checkitem : this._items)
					{
						if (checkitem.getObjectId() == objectId)
						{
							return null;
						}
					}

					TradeItem titem = new TradeItem(item, count, price);
					this._items.add(titem);
					this.invalidateConfirmation();
					return titem;
				}
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}

	public synchronized TradeItem addItemByItemId(int itemId, long count, long price)
	{
		if (this._locked)
		{
			LOGGER.warning(this._owner.getName() + ": Attempt to modify locked TradeList!");
			return null;
		}
		ItemTemplate item = ItemData.getInstance().getTemplate(itemId);
		if (item == null)
		{
			LOGGER.warning(this._owner.getName() + ": Attempt to add invalid item to TradeList!");
			return null;
		}
		else if (!item.isTradeable() || item.isQuestItem())
		{
			return null;
		}
		else if (!item.isStackable() && count > 1L)
		{
			LOGGER.warning(this._owner.getName() + ": Attempt to add non-stackable item to TradeList with count > 1!");
			return null;
		}
		else if (Inventory.MAX_ADENA / count < price)
		{
			LOGGER.warning(this._owner.getName() + ": Attempt to overflow adena !");
			return null;
		}
		else
		{
			TradeItem titem = new TradeItem(item, count, price);
			this._items.add(titem);
			this.invalidateConfirmation();
			return titem;
		}
	}

	private synchronized TradeItem removeItem(int objectId, int itemId, long count)
	{
		if (this._locked)
		{
			LOGGER.warning(this._owner.getName() + ": Attempt to modify locked TradeList!");
			return null;
		}
		else if (count < 0L)
		{
			LOGGER.warning(this._owner.getName() + ": Attempt to remove " + count + " items from TradeList!");
			return null;
		}
		else
		{
			for (TradeItem titem : this._items)
			{
				if (titem.getObjectId() == objectId || titem.getItem().getId() == itemId)
				{
					if (this._partner != null)
					{
						TradeList partnerList = this._partner.getActiveTradeList();
						if (partnerList == null)
						{
							LOGGER.warning(this._partner.getName() + ": Trading partner (" + this._partner.getName() + ") is invalid in this trade!");
							return null;
						}

						partnerList.invalidateConfirmation();
					}

					if (count != -1L && titem.getCount() > count)
					{
						titem.setCount(titem.getCount() - count);
					}
					else
					{
						this._items.remove(titem);
					}

					return titem;
				}
			}

			return null;
		}
	}

	public synchronized void updateItems()
	{
		for (TradeItem titem : this._items)
		{
			Item item = this._owner.getInventory().getItemByObjectId(titem.getObjectId());
			if (item == null || titem.getCount() < 1L)
			{
				this.removeItem(titem.getObjectId(), -1, -1L);
			}
			else if (item.getCount() < titem.getCount())
			{
				titem.setCount(item.getCount());
			}
		}
	}

	public void lock()
	{
		this._locked = true;
	}

	public synchronized void clear()
	{
		this._items.clear();
		this._locked = false;
	}

	public boolean confirm()
	{
		if (this._confirmed)
		{
			return true;
		}
		else if (this._partner != null)
		{
			TradeList partnerList = this._partner.getActiveTradeList();
			if (partnerList == null)
			{
				LOGGER.warning(this._partner.getName() + ": Trading partner (" + this._partner.getName() + ") is invalid in this trade!");
				return false;
			}
			TradeList sync1;
			TradeList sync2;
			if (this.getOwner().getObjectId() > partnerList.getOwner().getObjectId())
			{
				sync1 = partnerList;
				sync2 = this;
			}
			else
			{
				sync1 = this;
				sync2 = partnerList;
			}

			synchronized (sync1)
			{
				boolean var10000;
				synchronized (sync2)
				{
					this._confirmed = true;
					if (partnerList.isConfirmed())
					{
						partnerList.lock();
						this.lock();
						if (!partnerList.validate() || !this.validate())
						{
							var10000 = false;
						}
						else
						{
							this.doExchange(partnerList);
							return this._confirmed;
						}
					}
					else
					{
						this._partner.onTradeConfirm(this._owner);
						return this._confirmed;
					}
				}

				return var10000;
			}
		}
		else
		{
			this._confirmed = true;
			return this._confirmed;
		}
	}

	private void invalidateConfirmation()
	{
		this._confirmed = false;
	}

	private boolean validate()
	{
		if (this._owner != null && World.getInstance().getPlayer(this._owner.getObjectId()) != null)
		{
			for (TradeItem titem : this._items)
			{
				Item item = this._owner.checkItemManipulation(titem.getObjectId(), titem.getCount(), "transfer");
				if (item == null || item.getCount() < 1L)
				{
					LOGGER.warning(this._owner.getName() + ": Invalid Item in TradeList");
					return false;
				}
			}

			return true;
		}
		LOGGER.warning("Invalid owner of TradeList");
		return false;
	}

	private boolean TransferItems(Player partner, InventoryUpdate ownerIU, InventoryUpdate partnerIU)
	{
		for (TradeItem titem : this._items)
		{
			Item oldItem = this._owner.getInventory().getItemByObjectId(titem.getObjectId());
			if (oldItem == null)
			{
				return false;
			}

			Item newItem = this._owner.getInventory().transferItem(ItemProcessType.TRANSFER, titem.getObjectId(), titem.getCount(), partner.getInventory(), this._owner, this._partner);
			if (newItem == null)
			{
				return false;
			}

			if (ownerIU != null)
			{
				if (oldItem.getCount() > 0L && oldItem != newItem)
				{
					ownerIU.addModifiedItem(oldItem);
				}
				else
				{
					ownerIU.addRemovedItem(oldItem);
				}
			}

			if (partnerIU != null)
			{
				if (newItem.getCount() > titem.getCount())
				{
					partnerIU.addModifiedItem(newItem);
				}
				else
				{
					partnerIU.addNewItem(newItem);
				}
			}
		}

		return true;
	}

	private int countItemsSlots(Player partner)
	{
		int slots = 0;

		for (TradeItem item : this._items)
		{
			if (item != null)
			{
				ItemTemplate template = ItemData.getInstance().getTemplate(item.getItem().getId());
				if (template != null)
				{
					if (!template.isStackable())
					{
						slots = (int) (slots + item.getCount());
					}
					else if (partner.getInventory().getItemByItemId(item.getItem().getId()) == null)
					{
						slots++;
					}
				}
			}
		}

		return slots;
	}

	private int calcItemsWeight()
	{
		long weight = 0L;

		for (TradeItem item : this._items)
		{
			if (item != null)
			{
				ItemTemplate template = ItemData.getInstance().getTemplate(item.getItem().getId());
				if (template != null)
				{
					weight += item.getCount() * template.getWeight();
				}
			}
		}

		return (int) Math.min(weight, 2147483647L);
	}

	private void doExchange(TradeList partnerList)
	{
		boolean success = false;
		if (!this._owner.getInventory().validateWeight(partnerList.calcItemsWeight()) || !partnerList.getOwner().getInventory().validateWeight(this.calcItemsWeight()))
		{
			partnerList.getOwner().sendPacket(SystemMessageId.WEIGHT_LIMIT_IS_EXCEEDED);
			this._owner.sendPacket(SystemMessageId.WEIGHT_LIMIT_IS_EXCEEDED);
		}
		else if (this._owner.getInventory().validateCapacity(partnerList.countItemsSlots(this.getOwner())) && partnerList.getOwner().getInventory().validateCapacity(this.countItemsSlots(partnerList.getOwner())))
		{
			InventoryUpdate ownerIU = new InventoryUpdate();
			InventoryUpdate partnerIU = new InventoryUpdate();
			partnerList.TransferItems(this._owner, partnerIU, ownerIU);
			this.TransferItems(partnerList.getOwner(), ownerIU, partnerIU);
			this._owner.sendInventoryUpdate(ownerIU);
			this._partner.sendInventoryUpdate(partnerIU);
			success = true;
		}
		else
		{
			partnerList.getOwner().sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
			this._owner.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
		}

		this._owner.sendItemList();
		this._partner.sendItemList();
		partnerList.getOwner().onTradeFinish(success);
		this._owner.onTradeFinish(success);
	}

	public synchronized int privateStoreBuy(Player player, Set<ItemRequest> items)
	{
		if (this._locked)
		{
			return 1;
		}
		else if (!this.validate())
		{
			this.lock();
			return 1;
		}
		else if (this._owner.isOnline() && player.isOnline())
		{
			int slots = 0;
			int weight = 0;
			long totalPrice = 0L;
			PlayerInventory ownerInventory = this._owner.getInventory();
			PlayerInventory playerInventory = player.getInventory();

			for (ItemRequest item : items)
			{
				boolean found = false;

				for (TradeItem ti : this._items)
				{
					if (ti.getObjectId() == item.getObjectId())
					{
						if (ti.getPrice() == item.getPrice())
						{
							if (ti.getCount() < item.getCount())
							{
								item.setCount(ti.getCount());
							}

							found = true;
						}
						break;
					}
				}

				if (found)
				{
					if (Inventory.MAX_ADENA / item.getCount() < item.getPrice())
					{
						this.lock();
						return 1;
					}

					totalPrice += item.getCount() * item.getPrice();
					if (Inventory.MAX_ADENA >= totalPrice && totalPrice >= 0L)
					{
						Item oldItem = this._owner.checkItemManipulation(item.getObjectId(), item.getCount(), "sell");
						if (oldItem != null && oldItem.isTradeable())
						{
							ItemTemplate template = ItemData.getInstance().getTemplate(item.getItemId());
							if (template != null)
							{
								weight = (int) (weight + item.getCount() * template.getWeight());
								if (!template.isStackable())
								{
									slots = (int) (slots + item.getCount());
								}
								else if (playerInventory.getItemByItemId(item.getItemId()) == null)
								{
									slots++;
								}
							}
							continue;
						}

						this.lock();
						return 2;
					}

					this.lock();
					return 1;
				}
				if (this._packaged)
				{
					PunishmentManager.handleIllegalPlayerAction(player, "[TradeList.privateStoreBuy()] " + player + " tried to cheat the package sell and buy only a part of the package! Ban this player for bot usage!", GeneralConfig.DEFAULT_PUNISH);
					return 2;
				}

				item.setCount(0L);
			}

			if (totalPrice > playerInventory.getAdena())
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
				return 1;
			}
			else if (!playerInventory.validateWeight(weight))
			{
				player.sendPacket(SystemMessageId.WEIGHT_LIMIT_IS_EXCEEDED);
				return 1;
			}
			else if (!playerInventory.validateCapacity(slots))
			{
				player.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
				return 1;
			}
			else
			{
				InventoryUpdate ownerIU = new InventoryUpdate();
				InventoryUpdate playerIU = new InventoryUpdate();
				Item adenaItem = playerInventory.getAdenaInstance();
				if (!playerInventory.reduceAdena(ItemProcessType.BUY, totalPrice, player, this._owner))
				{
					player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
					return 1;
				}
				playerIU.addItem(adenaItem);
				ownerInventory.addAdena(ItemProcessType.SELL, totalPrice, this._owner, player);
				boolean ok = true;

				for (ItemRequest item : items)
				{
					if (item.getCount() != 0L)
					{
						Item oldItem = this._owner.checkItemManipulation(item.getObjectId(), item.getCount(), "sell");
						if (oldItem == null)
						{
							this.lock();
							ok = false;
							break;
						}

						Item newItem = ownerInventory.transferItem(ItemProcessType.TRANSFER, item.getObjectId(), item.getCount(), playerInventory, this._owner, player);
						if (newItem == null)
						{
							ok = false;
							break;
						}

						this.removeItem(item.getObjectId(), -1, item.getCount());
						PrivateStoreHistoryManager.getInstance().registerTransaction(PrivateStoreType.SELL, newItem, item.getCount(), item.getCount() * item.getPrice());
						if (oldItem.getCount() > 0L && oldItem != newItem)
						{
							ownerIU.addModifiedItem(oldItem);
						}
						else
						{
							ownerIU.addRemovedItem(oldItem);
						}

						if (newItem.getCount() > item.getCount())
						{
							playerIU.addModifiedItem(newItem);
						}
						else
						{
							playerIU.addNewItem(newItem);
						}

						if (newItem.isStackable())
						{
							SystemMessage msg = new SystemMessage(SystemMessageId.C1_PURCHASED_S3_S2_S);
							msg.addString(player.getName());
							msg.addItemName(newItem);
							msg.addLong(item.getCount());
							this._owner.sendPacket(msg);
							msg = new SystemMessage(SystemMessageId.YOU_HAVE_PURCHASED_S3_S2_S_FROM_C1);
							msg.addString(this._owner.getName());
							msg.addItemName(newItem);
							msg.addLong(item.getCount());
							player.sendPacket(msg);
						}
						else
						{
							SystemMessage msg = new SystemMessage(SystemMessageId.C1_PURCHASED_S2);
							msg.addString(player.getName());
							msg.addItemName(newItem);
							this._owner.sendPacket(msg);
							msg = new SystemMessage(SystemMessageId.YOU_HAVE_PURCHASED_S2_FROM_C1);
							msg.addString(this._owner.getName());
							msg.addItemName(newItem);
							player.sendPacket(msg);
						}

						this._owner.sendPacket(new ExPrivateStoreSellingResult(item.getObjectId(), item.getCount(), player.getAppearance().getVisibleName()));
					}
				}

				this._owner.sendInventoryUpdate(ownerIU);
				player.sendInventoryUpdate(playerIU);
				this._owner.sendItemList();
				player.sendItemList();
				return ok ? 0 : 2;
			}
		}
		else
		{
			return 1;
		}
	}

	public synchronized boolean privateStoreSell(Player player, ItemRequest[] requestedItems)
	{
		if (!this._locked && this._owner.isOnline() && player.isOnline())
		{
			boolean ok = false;
			PlayerInventory ownerInventory = this._owner.getInventory();
			PlayerInventory playerInventory = player.getInventory();
			InventoryUpdate ownerIU = new InventoryUpdate();
			InventoryUpdate playerIU = new InventoryUpdate();
			long totalPrice = 0L;
			TradeItem[] sellerItems = this._items.toArray(new TradeItem[this._items.size()]);

			for (ItemRequest item : requestedItems)
			{
				boolean found = false;

				for (TradeItem ti : sellerItems)
				{
					if (ti.getItem().getId() == item.getItemId())
					{
						if (ti.getPrice() == item.getPrice())
						{
							if (ti.getCount() < item.getCount())
							{
								item.setCount(ti.getCount());
							}

							found = item.getCount() > 0L;
						}
						break;
					}
				}

				if (found)
				{
					if (Inventory.MAX_ADENA / item.getCount() < item.getPrice())
					{
						this.lock();
						break;
					}

					long _totalPrice = totalPrice + item.getCount() * item.getPrice();
					if (Inventory.MAX_ADENA < _totalPrice || _totalPrice < 0L)
					{
						this.lock();
						break;
					}

					if (ownerInventory.getAdena() >= _totalPrice && item.getObjectId() >= 1 && item.getObjectId() <= sellerItems.length)
					{
						TradeItem tradeItem = sellerItems[item.getObjectId() - 1];
						if (tradeItem != null && tradeItem.getItem().getId() == item.getItemId())
						{
							int objectId = tradeItem.getObjectId();
							Item oldItem = player.checkItemManipulation(objectId, item.getCount(), "sell");
							if (oldItem == null)
							{
								oldItem = playerInventory.getItemByItemId(item.getItemId());
								if (oldItem == null)
								{
									continue;
								}

								objectId = oldItem.getObjectId();
								oldItem = player.checkItemManipulation(objectId, item.getCount(), "sell");
								if (oldItem == null)
								{
									continue;
								}
							}

							if (oldItem.getId() != item.getItemId())
							{
								PunishmentManager.handleIllegalPlayerAction(player, player + " is cheating with sell items", GeneralConfig.DEFAULT_PUNISH);
								return false;
							}

							if (oldItem.isTradeable())
							{
								Item newItem = playerInventory.transferItem(ItemProcessType.TRANSFER, objectId, item.getCount(), ownerInventory, player, this._owner);
								if (newItem != null)
								{
									this.removeItem(-1, item.getItemId(), item.getCount());
									ok = true;
									PrivateStoreHistoryManager.getInstance().registerTransaction(PrivateStoreType.BUY, newItem, item.getCount(), item.getCount() * item.getPrice());
									totalPrice = _totalPrice;
									if (oldItem.getCount() > 0L && oldItem != newItem)
									{
										playerIU.addModifiedItem(oldItem);
									}
									else
									{
										playerIU.addRemovedItem(oldItem);
									}

									if (newItem.getCount() > item.getCount())
									{
										ownerIU.addModifiedItem(newItem);
									}
									else
									{
										ownerIU.addNewItem(newItem);
									}

									if (newItem.isStackable())
									{
										SystemMessage msg = new SystemMessage(SystemMessageId.YOU_HAVE_PURCHASED_S3_S2_S_FROM_C1);
										msg.addString(player.getName());
										msg.addItemName(newItem);
										msg.addLong(item.getCount());
										this._owner.sendPacket(msg);
										msg = new SystemMessage(SystemMessageId.C1_PURCHASED_S3_S2_S);
										msg.addString(this._owner.getName());
										msg.addItemName(newItem);
										msg.addLong(item.getCount());
										player.sendPacket(msg);
									}
									else
									{
										SystemMessage msg = new SystemMessage(SystemMessageId.YOU_HAVE_PURCHASED_S2_FROM_C1);
										msg.addString(player.getName());
										msg.addItemName(newItem);
										this._owner.sendPacket(msg);
										msg = new SystemMessage(SystemMessageId.C1_PURCHASED_S2);
										msg.addString(this._owner.getName());
										msg.addItemName(newItem);
										player.sendPacket(msg);
									}

									this._owner.sendPacket(new ExPrivateStoreBuyingResult(item.getObjectId(), item.getCount(), player.getAppearance().getVisibleName()));
								}
							}
						}
					}
				}
			}

			if (totalPrice > 0L)
			{
				if (totalPrice > ownerInventory.getAdena())
				{
					return false;
				}

				Item adenaItem = ownerInventory.getAdenaInstance();
				ownerInventory.reduceAdena(ItemProcessType.BUY, totalPrice, this._owner, player);
				ownerIU.addItem(adenaItem);
				playerInventory.addAdena(ItemProcessType.SELL, totalPrice, player, this._owner);
				playerIU.addItem(playerInventory.getAdenaInstance());
			}

			if (ok)
			{
				this._owner.sendInventoryUpdate(ownerIU);
				player.sendInventoryUpdate(playerIU);
				this._owner.sendItemList();
				player.sendItemList();
			}

			return ok;
		}
		return false;
	}
}
