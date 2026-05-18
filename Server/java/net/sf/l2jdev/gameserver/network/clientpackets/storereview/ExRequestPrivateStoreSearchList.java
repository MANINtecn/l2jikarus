package net.sf.l2jdev.gameserver.network.clientpackets.storereview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.l2jdev.gameserver.data.holders.CollectionDataHolder;
import net.sf.l2jdev.gameserver.data.xml.CollectionData;
import net.sf.l2jdev.gameserver.data.xml.EnsoulData;
import net.sf.l2jdev.gameserver.data.xml.HennaData;
import net.sf.l2jdev.gameserver.data.xml.VariationData;
import net.sf.l2jdev.gameserver.handler.IItemHandler;
import net.sf.l2jdev.gameserver.handler.ItemHandler;
import net.sf.l2jdev.gameserver.managers.PrivateStoreHistoryManager;
import net.sf.l2jdev.gameserver.model.ItemInfo;
import net.sf.l2jdev.gameserver.model.TradeItem;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PrivateStoreType;
import net.sf.l2jdev.gameserver.model.item.EtcItem;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.holders.ItemEnchantHolder;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.storereview.ExPrivateStoreSearchHistory;
import net.sf.l2jdev.gameserver.network.serverpackets.storereview.ExPrivateStoreSearchItem;

public class ExRequestPrivateStoreSearchList extends ClientPacket
{
	public static final int MAX_ITEM_PER_PAGE = 120;
	private String _searchWord;
	private ExRequestPrivateStoreSearchList.StoreType _storeType;
	private ExRequestPrivateStoreSearchList.StoreItemType _itemType;
	private ExRequestPrivateStoreSearchList.StoreSubItemType _itemSubtype;
	private int _searchCollection;

	@Override
	protected void readImpl()
	{
		this._searchWord = this.readSizedString();
		this._storeType = ExRequestPrivateStoreSearchList.StoreType.findById(this.readByte());
		this._itemType = ExRequestPrivateStoreSearchList.StoreItemType.findById(this.readByte());
		this._itemSubtype = ExRequestPrivateStoreSearchList.StoreSubItemType.findById(this.readByte());
		this._searchCollection = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Collection<Player> stores = World.getInstance().getSellingOrBuyingPlayers();
			List<ExRequestPrivateStoreSearchList.ShopItem> items = new ArrayList<>();
			List<Integer> itemIds = new ArrayList<>();
			stores.forEach(vendor -> {
				for (TradeItem item : vendor.getPrivateStoreType() == PrivateStoreType.BUY ? vendor.getBuyList().getItems() : vendor.getSellList().getItems())
				{
					if ((this._storeType == ExRequestPrivateStoreSearchList.StoreType.ALL || this._storeType == ExRequestPrivateStoreSearchList.StoreType.SELL && (vendor.getPrivateStoreType() == PrivateStoreType.SELL || vendor.getPrivateStoreType() == PrivateStoreType.PACKAGE_SELL) || this._storeType == ExRequestPrivateStoreSearchList.StoreType.BUY && vendor.getPrivateStoreType() == PrivateStoreType.BUY) && this.isItemVisibleForShop(item) && (this._searchWord.equals("") || !this._searchWord.equals("") && item.getItem().getName().toLowerCase().contains(this._searchWord.toLowerCase())))
					{
						items.add(new ExRequestPrivateStoreSearchList.ShopItem(item, vendor, vendor.getPrivateStoreType()));
						itemIds.add(item.getItem().getId());
					}
				}
			});
			int nSize = items.size();
			int maxPage = Math.max(1, nSize / 120.0F > nSize / 120 ? nSize / 120 + 1 : nSize / 120);

			for (int page = 1; page <= maxPage; page++)
			{
				int nsize = page == maxPage ? (nSize % 120 <= 0 && nSize != 0 ? 120 : nSize % 120) : 120;
				player.sendPacket(new ExPrivateStoreSearchItem(page, maxPage, nsize, items));
			}

			List<PrivateStoreHistoryManager.ItemHistoryTransaction> history = new ArrayList<>();
			List<PrivateStoreHistoryManager.ItemHistoryTransaction> historyTemp = new ArrayList<>();
			PrivateStoreHistoryManager.getInstance().getHistory().forEach(transaction -> {
				if (itemIds.contains(transaction.getItemId()))
				{
					history.add(transaction);
				}
			});
			int page = 1;
			maxPage = Math.max(1, history.size() / 120.0F > history.size() / 120 ? history.size() / 120 + 1 : history.size() / 120);

			for (int index = 0; index < history.size(); index++)
			{
				historyTemp.add(history.get(index));
				if (index == history.size() - 1 || index == 119 || index > 0 && index % 119 == 0)
				{
					player.sendPacket(new ExPrivateStoreSearchHistory(page, maxPage, historyTemp));
					page++;
					historyTemp.clear();
				}
			}

			if (page == 1)
			{
				player.sendPacket(new ExPrivateStoreSearchHistory(1, 1, historyTemp));
			}
		}
	}

	private boolean isItemVisibleForShop(TradeItem item)
	{
		if (this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.EQUIPMENT && this._itemSubtype == ExRequestPrivateStoreSearchList.StoreSubItemType.ALL && this._searchCollection == 0)
		{
			return item.getItem().isEquipable();
		}
		else if (this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.EQUIPMENT && this._itemSubtype == ExRequestPrivateStoreSearchList.StoreSubItemType.WEAPON && this._searchCollection == 0)
		{
			return item.getItem().isEquipable() && item.getItem().isWeapon();
		}
		else if (this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.EQUIPMENT && this._itemSubtype == ExRequestPrivateStoreSearchList.StoreSubItemType.ARMOR && this._searchCollection == 0)
		{
			return item.getItem().isEquipable() && this.isEquipmentArmor(item.getItem());
		}
		else if (this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.EQUIPMENT && this._itemSubtype == ExRequestPrivateStoreSearchList.StoreSubItemType.ACCESSORY && this._searchCollection == 0)
		{
			return item.getItem().isEquipable() && this.isAccessory(item.getItem());
		}
		else if (this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.EQUIPMENT && this._itemSubtype == ExRequestPrivateStoreSearchList.StoreSubItemType.EQUIPMENT_MISC && this._searchCollection == 0)
		{
			return item.getItem().isEquipable() && !item.getItem().isWeapon() && !this.isEquipmentArmor(item.getItem()) && !this.isAccessory(item.getItem());
		}
		else if (this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.ENHANCEMENT_OR_EXPING && this._itemSubtype == ExRequestPrivateStoreSearchList.StoreSubItemType.ALL && this._searchCollection == 0)
		{
			return this.isEnhancementItem(item.getItem());
		}
		else if (this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.ENHANCEMENT_OR_EXPING && this._itemSubtype == ExRequestPrivateStoreSearchList.StoreSubItemType.ENCHANT_SCROLL && this._searchCollection == 0)
		{
			return this.isEnchantScroll(item.getItem());
		}
		else if (this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.ENHANCEMENT_OR_EXPING && this._itemSubtype == ExRequestPrivateStoreSearchList.StoreSubItemType.CRYSTAL && this._searchCollection == 0)
		{
			return this.isCrystal(item.getItem());
		}
		else if (this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.ENHANCEMENT_OR_EXPING && this._itemSubtype == ExRequestPrivateStoreSearchList.StoreSubItemType.LIFE_STONE && this._searchCollection == 0)
		{
			return this.isLifeStone(item.getItem());
		}
		else if (this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.ENHANCEMENT_OR_EXPING && this._itemSubtype == ExRequestPrivateStoreSearchList.StoreSubItemType.DYES && this._searchCollection == 0)
		{
			return this.isDye(item.getItem());
		}
		else if (this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.ENHANCEMENT_OR_EXPING && this._itemSubtype == ExRequestPrivateStoreSearchList.StoreSubItemType.SPELLBOOK && this._searchCollection == 0)
		{
			return this.isSpellBook(item.getItem());
		}
		else if (this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.ENHANCEMENT_OR_EXPING && this._itemSubtype == ExRequestPrivateStoreSearchList.StoreSubItemType.ENHANCEMENT_MISC && this._searchCollection == 0)
		{
			return this.isEnhancementMisc(item.getItem());
		}
		else if (this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.GROCERY_OR_COLLECTION_MISC && this._itemSubtype == ExRequestPrivateStoreSearchList.StoreSubItemType.ALL && this._searchCollection == 0)
		{
			return item.getItem().isPotion() || item.getItem().isScroll() || this.isTicket(item.getItem()) || this.isPackOrCraft(item.getItem()) || this.isGroceryMisc(item.getItem());
		}
		else if (this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.GROCERY_OR_COLLECTION_MISC && this._itemSubtype == ExRequestPrivateStoreSearchList.StoreSubItemType.POTION_SCROLL && this._searchCollection == 0)
		{
			return item.getItem().isPotion() || item.getItem().isScroll();
		}
		else if (this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.GROCERY_OR_COLLECTION_MISC && this._itemSubtype == ExRequestPrivateStoreSearchList.StoreSubItemType.TICKET && this._searchCollection == 0)
		{
			return this.isTicket(item.getItem());
		}
		else if (this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.GROCERY_OR_COLLECTION_MISC && this._itemSubtype == ExRequestPrivateStoreSearchList.StoreSubItemType.PACK_CRAFT && this._searchCollection == 0)
		{
			return this.isPackOrCraft(item.getItem());
		}
		else if (this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.GROCERY_OR_COLLECTION_MISC && this._itemSubtype == ExRequestPrivateStoreSearchList.StoreSubItemType.GROCERY_MISC && this._searchCollection == 0)
		{
			return this.isGroceryMisc(item.getItem());
		}
		else if (this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.ALL && this._searchCollection == 1)
		{
			return this.isCollection(item.getItem());
		}
		else if (this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.EQUIPMENT && this._searchCollection == 1)
		{
			return this.isCollectionEquipement(item.getItem());
		}
		else if (this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.ENHANCEMENT_OR_EXPING && this._searchCollection == 1)
		{
			return this.isCollectionEnchanted(item.getItem());
		}
		else
		{
			return this._itemType == ExRequestPrivateStoreSearchList.StoreItemType.GROCERY_OR_COLLECTION_MISC && this._searchCollection == 1 ? this.isCollectionMisc(item.getItem()) : true;
		}
	}

	protected boolean isEquipmentArmor(ItemTemplate item)
	{
		if (!item.isArmor())
		{
			return false;
		}
		BodyPart bodyPart = item.getBodyPart();
		return bodyPart == BodyPart.CHEST || bodyPart == BodyPart.FULL_ARMOR || bodyPart == BodyPart.HEAD || bodyPart == BodyPart.LEGS || bodyPart == BodyPart.FEET || bodyPart == BodyPart.GLOVES;
	}

	protected boolean isAccessory(ItemTemplate item)
	{
		if (!item.isArmor())
		{
			return false;
		}
		BodyPart bodyPart = item.getBodyPart();
		return bodyPart == BodyPart.L_BRACELET || bodyPart == BodyPart.R_BRACELET || bodyPart == BodyPart.BROOCH || bodyPart == BodyPart.R_FINGER || bodyPart == BodyPart.L_FINGER || bodyPart == BodyPart.LR_FINGER || bodyPart == BodyPart.NECK || bodyPart == BodyPart.R_EAR || bodyPart == BodyPart.L_EAR || bodyPart == BodyPart.LR_EAR;
	}

	protected boolean isEnchantScroll(ItemTemplate item)
	{
		if (!(item instanceof EtcItem))
		{
			return false;
		}
		IItemHandler ih = ItemHandler.getInstance().getHandler((EtcItem) item);
		return ih != null && ih.getClass().getSimpleName().equals("EnchantScrolls");
	}

	protected boolean isCrystal(ItemTemplate item)
	{
		return EnsoulData.getInstance().getStone(item.getId()) != null;
	}

	protected boolean isLifeStone(ItemTemplate item)
	{
		return VariationData.getInstance().hasVariation(item.getId());
	}

	protected boolean isDye(ItemTemplate item)
	{
		return HennaData.getInstance().getHennaByItemId(item.getId()) != null;
	}

	protected boolean isSpellBook(ItemTemplate item)
	{
		return item.getName().contains("Spellbook: ");
	}

	protected boolean isEnhancementMisc(ItemTemplate item)
	{
		return item.getId() >= 91031 && item.getId() <= 91038;
	}

	protected boolean isEnhancementItem(ItemTemplate item)
	{
		return this.isEnchantScroll(item) || this.isCrystal(item) || this.isLifeStone(item) || this.isDye(item) || this.isSpellBook(item) || this.isEnhancementMisc(item);
	}

	protected boolean isTicket(ItemTemplate item)
	{
		return item.getId() == 90045 || item.getId() == 91462 || item.getId() == 91463 || item.getId() == 91972 || item.getId() == 93903;
	}

	protected boolean isPackOrCraft(ItemTemplate item)
	{
		if (item.getId() == 92477 || item.getId() == 91462 || item.getId() == 92478 || item.getId() == 92479 || item.getId() == 92480 || item.getId() == 92481 || item.getId() == 49756 || item.getId() == 93906 || item.getId() == 93907 || item.getId() == 93908 || item.getId() == 93909 || item.getId() == 93910 || item.getId() == 91076)
		{
			return true;
		}
		else if (!(item instanceof EtcItem))
		{
			return false;
		}
		else
		{
			IItemHandler ih = ItemHandler.getInstance().getHandler((EtcItem) item);
			return ih != null && ih.getClass().getSimpleName().equals("ExtractableItems");
		}
	}

	private boolean isGroceryMisc(ItemTemplate item)
	{
		return !item.isEquipable() && !this.isEnhancementItem(item) && !this.isCollection(item) && !item.isPotion() && !item.isScroll() && !this.isTicket(item) && !this.isPackOrCraft(item);
	}

	protected boolean isCollection(ItemTemplate item)
	{
		for (CollectionDataHolder collectionHolder : CollectionData.getInstance().getCollections())
		{
			for (ItemEnchantHolder itemData : collectionHolder.getItems())
			{
				if (itemData.getId() == item.getId())
				{
					return true;
				}
			}
		}

		return false;
	}

	private boolean isCollectionEquipement(ItemTemplate item)
	{
		return this.isCollection(item) && item.isEquipable();
	}

	private boolean isCollectionEnchanted(ItemTemplate item)
	{
		return this.isCollection(item) && item.getName().contains("Spellbook: ");
	}

	protected boolean isCollectionMisc(ItemTemplate item)
	{
		return item.getId() >= 93906 && item.getId() <= 93910;
	}

	public static class ShopItem
	{
		private final TradeItem _item;
		private final Player _owner;
		private final PrivateStoreType _storeType;

		public ShopItem(TradeItem item, Player owner, PrivateStoreType storeType)
		{
			this._item = item;
			this._owner = owner;
			this._storeType = storeType;
		}

		public long getCount()
		{
			return this._item.getCount();
		}

		public ItemInfo getItemInfo()
		{
			return new ItemInfo(this._item);
		}

		public Player getOwner()
		{
			return this._owner;
		}

		public PrivateStoreType getStoreType()
		{
			return this._storeType;
		}

		public long getPrice()
		{
			return this._item.getPrice();
		}
	}

	private static enum StoreItemType
	{
		ALL((byte) -1),
		EQUIPMENT((byte) 0),
		ENHANCEMENT_OR_EXPING((byte) 2),
		GROCERY_OR_COLLECTION_MISC((byte) 4);

		private final byte _storeItemType;

		private StoreItemType(byte storeItemType)
		{
			this._storeItemType = storeItemType;
		}

		public static ExRequestPrivateStoreSearchList.StoreItemType findById(int id)
		{
			for (ExRequestPrivateStoreSearchList.StoreItemType storeItemType : values())
			{
				if (storeItemType.getValue() == id)
				{
					return storeItemType;
				}
			}

			return null;
		}

		public byte getValue()
		{
			return this._storeItemType;
		}
	}

	private static enum StoreSubItemType
	{
		ALL((byte) -1),
		WEAPON((byte) 0),
		ARMOR((byte) 1),
		ACCESSORY((byte) 2),
		EQUIPMENT_MISC((byte) 3),
		ENCHANT_SCROLL((byte) 8),
		LIFE_STONE((byte) 15),
		DYES((byte) 16),
		CRYSTAL((byte) 17),
		SPELLBOOK((byte) 18),
		ENHANCEMENT_MISC((byte) 19),
		POTION_SCROLL((byte) 20),
		TICKET((byte) 21),
		PACK_CRAFT((byte) 22),
		GROCERY_MISC((byte) 24);

		private final byte _storeSubItemType;

		private StoreSubItemType(byte storeSubItemType)
		{
			this._storeSubItemType = storeSubItemType;
		}

		public static ExRequestPrivateStoreSearchList.StoreSubItemType findById(int id)
		{
			for (ExRequestPrivateStoreSearchList.StoreSubItemType storeSubItemType : values())
			{
				if (storeSubItemType.getValue() == id)
				{
					return storeSubItemType;
				}
			}

			return null;
		}

		public byte getValue()
		{
			return this._storeSubItemType;
		}
	}

	private static enum StoreType
	{
		SELL((byte) 0),
		BUY((byte) 1),
		ALL((byte) 3);

		private final byte _storeType;

		private StoreType(byte storeType)
		{
			this._storeType = storeType;
		}

		public static ExRequestPrivateStoreSearchList.StoreType findById(int id)
		{
			for (ExRequestPrivateStoreSearchList.StoreType storeType : values())
			{
				if (storeType.getValue() == id)
				{
					return storeType;
				}
			}

			return null;
		}

		public byte getValue()
		{
			return this._storeType;
		}
	}
}
