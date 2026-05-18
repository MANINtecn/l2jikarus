package net.sf.l2jdev.gameserver.model.itemcontainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.custom.TransmogConfig;
import net.sf.l2jdev.gameserver.data.xml.AgathionData;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.model.TradeItem;
import net.sf.l2jdev.gameserver.model.TradeList;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerItemAdd;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerItemDestroy;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerItemDrop;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerItemTransfer;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.AgathionSkillHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.EtcItemType;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.SkillConditionScope;
import net.sf.l2jdev.gameserver.model.variables.ItemVariables;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.StatusUpdateType;
import net.sf.l2jdev.gameserver.network.serverpackets.ExAdenaInvenCount;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.limitshop.ExBloodyCoinCount;

public class PlayerInventory extends Inventory
{
	private static final Logger LOGGER = Logger.getLogger(PlayerInventory.class.getName());
	private final Player _owner;
	private Item _adena;
	private Item _ancientAdena;
	private Item _beautyTickets;
	private Collection<Integer> _blockItems = null;
	private InventoryBlockType _blockMode = InventoryBlockType.NONE;
	private final AtomicInteger _questItemSize = new AtomicInteger();

	public PlayerInventory(Player owner)
	{
		this._owner = owner;
	}

	@Override
	public Player getOwner()
	{
		return this._owner;
	}

	@Override
	protected ItemLocation getBaseLocation()
	{
		return ItemLocation.INVENTORY;
	}

	@Override
	protected ItemLocation getEquipLocation()
	{
		return ItemLocation.PAPERDOLL;
	}

	public Item getAdenaInstance()
	{
		return this._adena;
	}

	@Override
	public long getAdena()
	{
		return this._adena != null ? this._adena.getCount() : 0L;
	}

	public Item getAncientAdenaInstance()
	{
		return this._ancientAdena;
	}

	public long getAncientAdena()
	{
		return this._ancientAdena != null ? this._ancientAdena.getCount() : 0L;
	}

	public Item getBeautyTicketsInstance()
	{
		return this._beautyTickets;
	}

	@Override
	public long getBeautyTickets()
	{
		return this._beautyTickets != null ? this._beautyTickets.getCount() : 0L;
	}

	public Collection<Item> getUniqueItems(boolean allowAdena, boolean allowAncientAdena, boolean onlyAvailable)
	{
		List<Item> result = new LinkedList<>();

		for (Item item : this._items)
		{
			if ((allowAdena || item.getId() != 57) && (allowAncientAdena || item.getId() != 5575))
			{
				boolean isDuplicate = false;

				for (Item addedItem : result)
				{
					if (addedItem.getId() == item.getId())
					{
						isDuplicate = true;
						break;
					}
				}

				if (!isDuplicate && (!onlyAvailable || item.isAvailable(this._owner, false, false)))
				{
					result.add(item);
				}
			}
		}

		return result;
	}

	public Collection<Item> getAllItemsByItemId(int itemId, boolean includeEquipped)
	{
		List<Item> result = new LinkedList<>();

		for (Item item : this._items)
		{
			if (itemId == item.getId() && (includeEquipped || !item.isEquipped()))
			{
				result.add(item);
			}
		}

		return result;
	}

	public Collection<Item> getAllItemsByItemId(int itemId, int enchantment)
	{
		return this.getAllItemsByItemId(itemId, enchantment, true);
	}

	public Collection<Item> getAllItemsByItemId(int itemId, int enchantment, boolean includeEquipped)
	{
		List<Item> result = new LinkedList<>();

		for (Item item : this._items)
		{
			if (itemId == item.getId() && item.getEnchantLevel() == enchantment && (includeEquipped || !item.isEquipped()))
			{
				result.add(item);
			}
		}

		return result;
	}

	public Collection<Item> getAvailableItems(boolean allowAdena, boolean allowNonTradeable, boolean feightable)
	{
		List<Item> result = new LinkedList<>();

		for (Item item : this._items)
		{
			if (item.isAvailable(this._owner, allowAdena, allowNonTradeable) && this.canManipulateWithItemId(item.getId()))
			{
				if (feightable)
				{
					if (item.getItemLocation() == ItemLocation.INVENTORY && item.isFreightable())
					{
						result.add(item);
					}
				}
				else
				{
					result.add(item);
				}
			}
		}

		return result;
	}

	public Collection<TradeItem> getAvailableItems(TradeList tradeList)
	{
		List<TradeItem> result = new LinkedList<>();

		for (Item item : this._items)
		{
			if (item != null && item.isAvailable(this._owner, false, false))
			{
				TradeItem adjItem = tradeList.adjustAvailableItem(item);
				if (adjItem != null)
				{
					result.add(adjItem);
				}
			}
		}

		return result;
	}

	public void adjustAvailableItem(TradeItem item)
	{
		boolean notAllEquipped = false;

		for (Item adjItem : this.getAllItemsByItemId(item.getItem().getId()))
		{
			if (!adjItem.isEquipable())
			{
				notAllEquipped |= true;
				break;
			}

			if (!adjItem.isEquipped())
			{
				notAllEquipped |= true;
			}
		}

		if (notAllEquipped)
		{
			Item adjItem = this.getItemByItemId(item.getItem().getId());
			item.setObjectId(adjItem.getObjectId());
			item.setEnchant(adjItem.getEnchantLevel());
			if (adjItem.getCount() < item.getCount())
			{
				item.setCount(adjItem.getCount());
			}
		}
		else
		{
			item.setCount(0L);
		}
	}

	public void addAdena(ItemProcessType process, long count, Player actor, Object reference)
	{
		if (count > 0L)
		{
			this.addItem(process, 57, count, actor, reference);
		}
	}

	public void addBeautyTickets(ItemProcessType process, long count, Player actor, Object reference)
	{
		if (count > 0L)
		{
			this.addItem(process, 36308, count, actor, reference);
		}
	}

	public boolean reduceAdena(ItemProcessType process, long count, Player actor, Object reference)
	{
		return count > 0L ? this.destroyItemByItemId(process, 57, count, actor, reference) != null : false;
	}

	public boolean reduceBeautyTickets(ItemProcessType process, long count, Player actor, Object reference)
	{
		return count > 0L ? this.destroyItemByItemId(process, 36308, count, actor, reference) != null : false;
	}

	public void addAncientAdena(ItemProcessType process, long count, Player actor, Object reference)
	{
		if (count > 0L)
		{
			this.addItem(process, 5575, count, actor, reference);
		}
	}

	public boolean reduceAncientAdena(ItemProcessType process, long count, Player actor, Object reference)
	{
		return count > 0L && this.destroyItemByItemId(process, 5575, count, actor, reference) != null;
	}

	@Override
	public Item addItem(ItemProcessType process, Item item, Player actor, Object reference)
	{
		Item addedItem = super.addItem(process, item, actor, reference);
		if (addedItem != null)
		{
			if (addedItem.getId() == 57 && !addedItem.equals(this._adena))
			{
				this._adena = addedItem;
			}
			else if (addedItem.getId() == 5575 && !addedItem.equals(this._ancientAdena))
			{
				this._ancientAdena = addedItem;
			}
			else if (addedItem.getId() == 36308 && !addedItem.equals(this._beautyTickets))
			{
				this._beautyTickets = addedItem;
			}

			if (actor != null)
			{
				InventoryUpdate playerIU = new InventoryUpdate();
				playerIU.addItem(addedItem);
				actor.sendInventoryUpdate(playerIU);
				if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_ITEM_ADD, actor, addedItem.getTemplate()))
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnPlayerItemAdd(actor, addedItem), actor, addedItem.getTemplate());
				}
			}
		}

		return addedItem;
	}

	@Override
	public Item addItem(ItemProcessType process, int itemId, long count, Player actor, Object reference)
	{
		return this.addItem(process, itemId, count, actor, reference, true);
	}

	public Item addItem(ItemProcessType process, int itemId, long count, Player actor, Object reference, boolean update)
	{
		Item item = super.addItem(process, itemId, count, actor, reference);
		if (item != null)
		{
			if (item.getId() == 57 && !item.equals(this._adena))
			{
				this._adena = item;
			}
			else if (item.getId() == 5575 && !item.equals(this._ancientAdena))
			{
				this._ancientAdena = item;
			}
			else if (item.getId() == 36308 && !item.equals(this._beautyTickets))
			{
				this._beautyTickets = item;
			}
			else if (item.getId() == 94481)
			{
				long xpCount = item.getCount();
				Clan clan = actor.getClan();
				if (clan != null)
				{
					clan.addExp(actor.getObjectId(), (int) xpCount);
				}

				ThreadPool.schedule(() -> actor.destroyItemByItemId(ItemProcessType.FEE, 94481, xpCount, actor, false), 100L);
				return item;
			}

			if (actor != null)
			{
				if (update)
				{
					InventoryUpdate playerIU = new InventoryUpdate();
					if (item.isStackable() && item.getCount() > count)
					{
						playerIU.addModifiedItem(item);
					}
					else
					{
						playerIU.addNewItem(item);
					}

					actor.sendInventoryUpdate(playerIU);
					if (item.getId() == 57)
					{
						actor.sendPacket(new ExAdenaInvenCount(actor));
					}
					else if (item.getId() == 91663)
					{
						actor.sendPacket(new ExBloodyCoinCount(actor));
					}
				}

				if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_ITEM_ADD, actor, item.getTemplate()))
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnPlayerItemAdd(actor, item), actor, item.getTemplate());
				}
			}
		}

		return item;
	}

	@Override
	public Item transferItem(ItemProcessType process, int objectId, long count, ItemContainer target, Player actor, Object reference)
	{
		Item item = super.transferItem(process, objectId, count, target, actor, reference);
		if (this._adena != null && (this._adena.getCount() <= 0L || this._adena.getOwnerId() != this.getOwnerId()))
		{
			this._adena = null;
		}

		if (this._ancientAdena != null && (this._ancientAdena.getCount() <= 0L || this._ancientAdena.getOwnerId() != this.getOwnerId()))
		{
			this._ancientAdena = null;
		}

		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_ITEM_TRANSFER, item.getTemplate()))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerItemTransfer(actor, item, target), item.getTemplate());
		}

		return item;
	}

	@Override
	public Item detachItem(ItemProcessType process, Item item, long count, ItemLocation newLocation, Player actor, Object reference)
	{
		Item detachedItem = super.detachItem(process, item, count, newLocation, actor, reference);
		if (detachedItem != null && actor != null)
		{
			actor.sendItemList();
		}

		return detachedItem;
	}

	@Override
	public Item destroyItem(ItemProcessType process, Item item, Player actor, Object reference)
	{
		return this.destroyItem(process, item, item.getCount(), actor, reference);
	}

	@Override
	public Item destroyItem(ItemProcessType process, Item item, long count, Player actor, Object reference)
	{
		Item destroyedItem = super.destroyItem(process, item, count, actor, reference);
		if (this._adena != null && this._adena.getCount() <= 0L)
		{
			this._adena = null;
		}

		if (this._ancientAdena != null && this._ancientAdena.getCount() <= 0L)
		{
			this._ancientAdena = null;
		}

		if (destroyedItem != null)
		{
			if (destroyedItem.getId() == 57)
			{
				actor.sendPacket(new ExAdenaInvenCount(actor));
			}
			else if (destroyedItem.getId() == 91663)
			{
				actor.sendPacket(new ExBloodyCoinCount(actor));
			}

			if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_ITEM_DESTROY, destroyedItem.getTemplate()))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerItemDestroy(actor, destroyedItem), destroyedItem.getTemplate());
			}
		}

		return destroyedItem;
	}

	@Override
	public Item destroyItem(ItemProcessType process, int objectId, long count, Player actor, Object reference)
	{
		Item item = this.getItemByObjectId(objectId);
		return item == null ? null : this.destroyItem(process, item, count, actor, reference);
	}

	@Override
	public Item destroyItemByItemId(ItemProcessType process, int itemId, long count, Player actor, Object reference)
	{
		Item destroyItem = null;
		Collection<Item> items = this.getAllItemsByItemId(itemId);

		for (Item item : items)
		{
			destroyItem = item;
			if (!item.isEquipped())
			{
				break;
			}
		}

		if (destroyItem == null)
		{
			return null;
		}
		if (!destroyItem.isStackable() && count > 1L)
		{
			if (this.getInventoryItemCount(itemId, -1, false) < count)
			{
				return null;
			}

			InventoryUpdate iu = new InventoryUpdate();
			long destroyed = 0L;

			for (Item itemx : items)
			{
				if (!itemx.isEquipped() && this.destroyItem(process, itemx, 1L, actor, reference) != null)
				{
					iu.addRemovedItem(itemx);
					if (++destroyed == count)
					{
						this._owner.sendInventoryUpdate(iu);
						this.refreshWeight();
						return itemx;
					}
				}
			}
		}

		return this.destroyItem(process, destroyItem, count, actor, reference);
	}

	@Override
	public Item dropItem(ItemProcessType process, Item item, Player actor, Object reference)
	{
		Item droppedItem = super.dropItem(process, item, actor, reference);
		if (this._adena != null && (this._adena.getCount() <= 0L || this._adena.getOwnerId() != this.getOwnerId()))
		{
			this._adena = null;
		}

		if (this._ancientAdena != null && (this._ancientAdena.getCount() <= 0L || this._ancientAdena.getOwnerId() != this.getOwnerId()))
		{
			this._ancientAdena = null;
		}

		if (droppedItem != null && EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_ITEM_DROP, droppedItem.getTemplate()))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerItemDrop(actor, droppedItem, droppedItem.getLocation()), droppedItem.getTemplate());
		}

		return droppedItem;
	}

	@Override
	public Item dropItem(ItemProcessType process, int objectId, long count, Player actor, Object reference)
	{
		Item item = super.dropItem(process, objectId, count, actor, reference);
		if (this._adena != null && (this._adena.getCount() <= 0L || this._adena.getOwnerId() != this.getOwnerId()))
		{
			this._adena = null;
		}

		if (this._ancientAdena != null && (this._ancientAdena.getCount() <= 0L || this._ancientAdena.getOwnerId() != this.getOwnerId()))
		{
			this._ancientAdena = null;
		}

		if (item != null && EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_ITEM_DROP, item.getTemplate()))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerItemDrop(actor, item, item.getLocation()), item.getTemplate());
		}

		return item;
	}

	@Override
	protected void addItem(Item item)
	{
		if (item.isQuestItem())
		{
			this._questItemSize.incrementAndGet();
		}

		super.addItem(item);
	}

	@Override
	protected boolean removeItem(Item item)
	{
		this._owner.removeItemFromShortcut(item.getObjectId());
		if (this._owner.isProcessingItem(item.getObjectId()))
		{
			this._owner.removeRequestsThatProcessesItem(item.getObjectId());
		}

		if (item.getId() == 57)
		{
			this._adena = null;
		}
		else if (item.getId() == 5575)
		{
			this._ancientAdena = null;
		}
		else if (item.getId() == 36308)
		{
			this._beautyTickets = null;
		}

		if (item.isQuestItem())
		{
			this._questItemSize.decrementAndGet();
		}

		return super.removeItem(item);
	}

	public int getQuestSize()
	{
		return this._questItemSize.get();
	}

	public int getNonQuestSize()
	{
		return this._items.size() - this._questItemSize.get();
	}

	@Override
	public void refreshWeight()
	{
		super.refreshWeight();
		this._owner.refreshOverloaded(true);
		StatusUpdate su = new StatusUpdate(this._owner);
		su.addUpdate(StatusUpdateType.CUR_LOAD, this._owner.getCurrentLoad());
		this._owner.sendPacket(su);
	}

	@Override
	public void restore()
	{
		super.restore();
		this._adena = this.getItemByItemId(57);
		this._ancientAdena = this.getItemByItemId(5575);
		this._beautyTickets = this.getItemByItemId(36308);
	}

	public static int[][] restoreVisibleInventory(int objectId)
	{
		int[][] paperdoll = new int[59][4];

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT object_id,item_id,loc_data,enchant_level FROM items WHERE owner_id=? AND loc='PAPERDOLL'");)
		{
			ps.setInt(1, objectId);

			try (ResultSet invdata = ps.executeQuery())
			{
				while (invdata.next())
				{
					int slot = invdata.getInt("loc_data");
					ItemVariables vars = new ItemVariables(invdata.getInt("object_id"));
					paperdoll[slot][0] = invdata.getInt("object_id");
					int itemId = invdata.getInt("item_id");
					ItemTemplate template = ItemData.getInstance().getTemplate(itemId);
					paperdoll[slot][1] = template == null ? itemId : template.getDisplayId();
					paperdoll[slot][2] = invdata.getInt("enchant_level");
					paperdoll[slot][3] = vars.getInt("visualId", TransmogConfig.ENABLE_TRANSMOG ? vars.getInt("transmogId", 0) : 0);
					if (paperdoll[slot][3] > 0)
					{
						paperdoll[slot][1] = paperdoll[slot][3];
					}
				}
			}
		}
		catch (Exception var15)
		{
			LOGGER.log(Level.WARNING, "Could not restore inventory: " + var15.getMessage(), var15);
		}

		return paperdoll;
	}

	public boolean checkInventorySlotsAndWeight(List<ItemTemplate> itemList, boolean sendMessage, boolean sendSkillMessage)
	{
		int lootWeight = 0;
		int requiredSlots = 0;
		if (itemList != null)
		{
			for (ItemTemplate item : itemList)
			{
				if (!item.isStackable() || this.getInventoryItemCount(item.getId(), -1) <= 0L)
				{
					requiredSlots++;
				}

				lootWeight += item.getWeight();
			}
		}

		boolean inventoryStatusOK = this.validateCapacity(requiredSlots) && this.validateWeight(lootWeight);
		if (!inventoryStatusOK && sendMessage)
		{
			this._owner.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
			if (sendSkillMessage)
			{
				this._owner.sendPacket(SystemMessageId.WEIGHT_AND_VOLUME_LIMIT_HAVE_BEEN_EXCEEDED_THAT_SKILL_IS_CURRENTLY_UNAVAILABLE);
			}
		}

		return inventoryStatusOK;
	}

	public boolean validateCapacity(Item item)
	{
		int slots = 0;
		if (!item.isStackable() || this.getInventoryItemCount(item.getId(), -1) <= 0L && !item.getTemplate().hasExImmediateEffect())
		{
			slots++;
		}

		return this.validateCapacity(slots, item.isQuestItem());
	}

	public boolean validateCapacityByItemId(int itemId)
	{
		int slots = 0;
		Item invItem = this.getItemByItemId(itemId);
		if (invItem == null || !invItem.isStackable())
		{
			slots++;
		}

		return this.validateCapacity(slots, ItemData.getInstance().getTemplate(itemId).isQuestItem());
	}

	@Override
	public boolean validateCapacity(long slots)
	{
		return this.validateCapacity(slots, false);
	}

	public boolean validateCapacity(long slots, boolean questItem)
	{
		return (slots != 0L || PlayerConfig.AUTO_LOOT_SLOT_LIMIT) && !questItem ? this.getNonQuestSize() + slots <= this._owner.getInventoryLimit() : this.getQuestSize() + slots <= this._owner.getQuestInventoryLimit();
	}

	@Override
	public boolean validateWeight(long weight)
	{
		return this._owner.isGM() && this._owner.getDietMode() && this._owner.getAccessLevel().allowTransaction() ? true : this._totalWeight + weight <= this._owner.getMaxLoad();
	}

	public void setInventoryBlock(Collection<Integer> items, InventoryBlockType mode)
	{
		this._blockMode = mode;
		this._blockItems = items;
		this._owner.sendItemList();
	}

	public void unblock()
	{
		this._blockMode = InventoryBlockType.NONE;
		this._blockItems = null;
		this._owner.sendItemList();
	}

	public boolean hasInventoryBlock()
	{
		return this._blockMode != InventoryBlockType.NONE && this._blockItems != null && !this._blockItems.isEmpty();
	}

	public void blockAllItems()
	{
		this.setInventoryBlock(Arrays.asList(57), InventoryBlockType.WHITELIST);
	}

	public InventoryBlockType getBlockMode()
	{
		return this._blockMode;
	}

	public Collection<Integer> getBlockItems()
	{
		return this._blockItems;
	}

	public boolean canManipulateWithItemId(int itemId)
	{
		Collection<Integer> blockedItems = this._blockItems;
		if (blockedItems != null)
		{
			switch (this._blockMode)
			{
				case NONE:
					return true;
				case WHITELIST:
					for (int idx : blockedItems)
					{
						if (idx == itemId)
						{
							return true;
						}
					}

					return false;
				case BLACKLIST:
					for (int id : blockedItems)
					{
						if (id == itemId)
						{
							return false;
						}
					}

					return true;
			}
		}

		return true;
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + "[" + this._owner + "]";
	}

	public void applyItemSkills()
	{
		for (Item item : this._items)
		{
			item.giveSkillsToOwner();
			item.applyEnchantStats();
			if (item.isEquipped())
			{
				item.applySpecialAbilities();
				if (item.getLocationSlot() >= 17 && item.getLocationSlot() <= 21)
				{
					AgathionSkillHolder agathionSkills = AgathionData.getInstance().getSkills(item.getId());
					if (agathionSkills != null)
					{
						for (Skill skill : agathionSkills.getMainSkills(item.getEnchantLevel()))
						{
							this._owner.removeSkill(skill, false, skill.isPassive());
						}

						for (Skill skill : agathionSkills.getSubSkills(item.getEnchantLevel()))
						{
							this._owner.removeSkill(skill, false, skill.isPassive());
						}

						if (item.getLocationSlot() == 17)
						{
							for (Skill skill : agathionSkills.getMainSkills(item.getEnchantLevel()))
							{
								if (!skill.isPassive() || skill.checkConditions(SkillConditionScope.PASSIVE, this._owner, this._owner))
								{
									this._owner.addSkill(skill, false);
								}
							}
						}

						for (Skill skillx : agathionSkills.getSubSkills(item.getEnchantLevel()))
						{
							if (!skillx.isPassive() || skillx.checkConditions(SkillConditionScope.PASSIVE, this._owner, this._owner))
							{
								this._owner.addSkill(skillx, false);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void reduceAmmunitionCount(EtcItemType type)
	{
		if (type != EtcItemType.ARROW && type != EtcItemType.BOLT)
		{
			LOGGER.log(Level.WARNING, type.toString(), " which is not ammo type.");
		}
		else
		{
			Weapon weapon = this._owner.getActiveWeaponItem();
			if (weapon != null)
			{
				Item ammunition = null;
				switch (weapon.getItemType())
				{
					case BOW:
						ammunition = this.findArrowForBow(weapon);
						break;
					case CROSSBOW:
					case TWOHANDCROSSBOW:
						ammunition = this.findBoltForCrossBow(weapon);
						break;
					default:
						return;
				}

				if (ammunition != null && ammunition.getItemType() == type)
				{
					if (!ammunition.getEtcItem().isInfinite())
					{
						this.updateItemCountNoDbUpdate(ItemProcessType.NONE, ammunition, -1L, this._owner, null);
					}
				}
			}
		}
	}

	public boolean updateItemCountNoDbUpdate(ItemProcessType process, Item item, long countDelta, Player creator, Object reference)
	{
		InventoryUpdate iu = new InventoryUpdate();
		long left = item.getCount() + countDelta;

		boolean var11;
		try
		{
			if (left <= 0L)
			{
				if (left != 0L)
				{
					return false;
				}

				iu.addRemovedItem(item);
				this.destroyItem(process, item, this._owner, null);
				return true;
			}

			synchronized (item)
			{
				if (process != null && process != ItemProcessType.NONE)
				{
					item.changeCount(process, countDelta, creator, reference);
				}
				else
				{
					item.changeCount(ItemProcessType.NONE, -1L, creator, reference);
				}

				item.setLastChange(2);
				this.refreshWeight();
				iu.addModifiedItem(item);
				var11 = true;
			}
		}
		finally
		{
			this._owner.sendInventoryUpdate(iu);
		}

		return var11;
	}

	public boolean updateItemCount(ItemProcessType process, Item item, long countDelta, Player creator, Object reference)
	{
		if (item != null)
		{
			boolean var7;
			try
			{
				var7 = this.updateItemCountNoDbUpdate(process, item, countDelta, creator, reference);
			}
			finally
			{
				if (item.getCount() > 0L)
				{
					item.updateDatabase();
				}
			}

			return var7;
		}
		return false;
	}
}
