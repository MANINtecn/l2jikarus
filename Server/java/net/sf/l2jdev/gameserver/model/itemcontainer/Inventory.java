package net.sf.l2jdev.gameserver.model.itemcontainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.cache.PaperdollCache;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.custom.TransmogConfig;
import net.sf.l2jdev.gameserver.data.xml.AgathionData;
import net.sf.l2jdev.gameserver.data.xml.AppearanceItemData;
import net.sf.l2jdev.gameserver.data.xml.ArmorSetData;
import net.sf.l2jdev.gameserver.managers.ItemManager;
import net.sf.l2jdev.gameserver.model.ArmorSet;
import net.sf.l2jdev.gameserver.model.VariationInstance;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Playable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerItemUnequip;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.appearance.AppearanceStone;
import net.sf.l2jdev.gameserver.model.item.appearance.AppearanceType;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.enums.ItemSkillType;
import net.sf.l2jdev.gameserver.model.item.holders.AgathionSkillHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ArmorsetSkillHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemSkillHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.ArmorType;
import net.sf.l2jdev.gameserver.model.item.type.EtcItemType;
import net.sf.l2jdev.gameserver.model.item.type.WeaponType;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.SkillConditionScope;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillFinishType;
import net.sf.l2jdev.gameserver.network.enums.InventorySlot;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUserInfoEquipSlot;
import net.sf.l2jdev.gameserver.network.serverpackets.SkillCoolTime;

public abstract class Inventory extends ItemContainer
{
	protected static final Logger LOGGER = Logger.getLogger(Inventory.class.getName());
	public static final int ADENA_ID = 57;
	public static final int ANCIENT_ADENA_ID = 5575;
	public static final int BEAUTY_TICKET_ID = 36308;
	public static final int AIR_STONE_ID = 39461;
	public static final int TEMPEST_STONE_ID = 39592;
	public static final int ELCYUM_CRYSTAL_ID = 36514;
	public static final int LCOIN_ID = 91663;
	public static final long MAX_ADENA = PlayerConfig.MAX_ADENA;
	public static final int CLAN_EXP = 94481;
	public static final int SP_POUCH = 98232;
	public static final int SP_POINTS = 15624;
	public static final int PAPERDOLL_UNDER = 0;
	public static final int PAPERDOLL_HEAD = 1;
	public static final int PAPERDOLL_HAIR = 2;
	public static final int PAPERDOLL_HAIR2 = 3;
	public static final int PAPERDOLL_NECK = 4;
	public static final int PAPERDOLL_RHAND = 5;
	public static final int PAPERDOLL_CHEST = 6;
	public static final int PAPERDOLL_LHAND = 7;
	public static final int PAPERDOLL_REAR = 8;
	public static final int PAPERDOLL_LEAR = 9;
	public static final int PAPERDOLL_GLOVES = 10;
	public static final int PAPERDOLL_LEGS = 11;
	public static final int PAPERDOLL_FEET = 12;
	public static final int PAPERDOLL_RFINGER = 13;
	public static final int PAPERDOLL_LFINGER = 14;
	public static final int PAPERDOLL_LBRACELET = 15;
	public static final int PAPERDOLL_RBRACELET = 16;
	public static final int PAPERDOLL_AGATHION1 = 17;
	public static final int PAPERDOLL_AGATHION2 = 18;
	public static final int PAPERDOLL_AGATHION3 = 19;
	public static final int PAPERDOLL_AGATHION4 = 20;
	public static final int PAPERDOLL_AGATHION5 = 21;
	public static final int PAPERDOLL_DECO1 = 22;
	public static final int PAPERDOLL_DECO2 = 23;
	public static final int PAPERDOLL_DECO3 = 24;
	public static final int PAPERDOLL_DECO4 = 25;
	public static final int PAPERDOLL_DECO5 = 26;
	public static final int PAPERDOLL_DECO6 = 27;
	public static final int PAPERDOLL_CLOAK = 28;
	public static final int PAPERDOLL_BELT = 29;
	public static final int PAPERDOLL_BROOCH = 30;
	public static final int PAPERDOLL_BROOCH_JEWEL1 = 31;
	public static final int PAPERDOLL_BROOCH_JEWEL2 = 32;
	public static final int PAPERDOLL_BROOCH_JEWEL3 = 33;
	public static final int PAPERDOLL_BROOCH_JEWEL4 = 34;
	public static final int PAPERDOLL_BROOCH_JEWEL5 = 35;
	public static final int PAPERDOLL_BROOCH_JEWEL6 = 36;
	public static final int PAPERDOLL_ARTIFACT_BOOK = 37;
	public static final int PAPERDOLL_ARTIFACT1 = 38;
	public static final int PAPERDOLL_ARTIFACT2 = 39;
	public static final int PAPERDOLL_ARTIFACT3 = 40;
	public static final int PAPERDOLL_ARTIFACT4 = 41;
	public static final int PAPERDOLL_ARTIFACT5 = 42;
	public static final int PAPERDOLL_ARTIFACT6 = 43;
	public static final int PAPERDOLL_ARTIFACT7 = 44;
	public static final int PAPERDOLL_ARTIFACT8 = 45;
	public static final int PAPERDOLL_ARTIFACT9 = 46;
	public static final int PAPERDOLL_ARTIFACT10 = 47;
	public static final int PAPERDOLL_ARTIFACT11 = 48;
	public static final int PAPERDOLL_ARTIFACT12 = 49;
	public static final int PAPERDOLL_ARTIFACT13 = 50;
	public static final int PAPERDOLL_ARTIFACT14 = 51;
	public static final int PAPERDOLL_ARTIFACT15 = 52;
	public static final int PAPERDOLL_ARTIFACT16 = 53;
	public static final int PAPERDOLL_ARTIFACT17 = 54;
	public static final int PAPERDOLL_ARTIFACT18 = 55;
	public static final int PAPERDOLL_ARTIFACT19 = 56;
	public static final int PAPERDOLL_ARTIFACT20 = 57;
	public static final int PAPERDOLL_ARTIFACT21 = 58;
	public static final int PAPERDOLL_TOTALSLOTS = 59;
	public static final double MAX_ARMOR_WEIGHT = 12000.0;
	private final Item[] _paperdoll;
	private final List<Inventory.PaperdollListener> _paperdollListeners;
	private final PaperdollCache _paperdollCache = new PaperdollCache();
	protected int _totalWeight;
	private int _wearedMask;
	private long _blockedItemSlotsMask;

	protected Inventory()
	{
		this._paperdoll = new Item[59];
		this._paperdollListeners = new ArrayList<>();
		if (this instanceof PlayerInventory)
		{
			this.addPaperdollListener(Inventory.ArmorSetListener.getInstance());
			this.addPaperdollListener(Inventory.BowCrossRodListener.getInstance());
			this.addPaperdollListener(Inventory.ItemSkillsListener.getInstance());
			this.addPaperdollListener(Inventory.BraceletListener.getInstance());
			this.addPaperdollListener(Inventory.BroochListener.getInstance());
			this.addPaperdollListener(Inventory.AgathionBraceletListener.getInstance());
			this.addPaperdollListener(Inventory.ArtifactBookListener.getInstance());
		}
		else if (this instanceof PetInventory)
		{
			this.addPaperdollListener(Inventory.ArmorSetListener.getInstance());
			this.addPaperdollListener(Inventory.ItemSkillsListener.getInstance());
		}

		this.addPaperdollListener(Inventory.StatsListener.getInstance());
	}

	protected abstract ItemLocation getEquipLocation();

	private Inventory.ChangeRecorder newRecorder()
	{
		return new Inventory.ChangeRecorder(this);
	}

	public Item dropItem(ItemProcessType process, Item item, Player actor, Object reference)
	{
		if (item == null)
		{
			return null;
		}
		synchronized (item)
		{
			if (!this._items.contains(item))
			{
				return null;
			}
			this.removeItem(item);
			item.setOwnerId(process, 0, actor, reference);
			item.setItemLocation(ItemLocation.VOID);
			item.setLastChange(3);
			item.updateDatabase();
			this.refreshWeight();
			return item;
		}
	}

	public Item dropItem(ItemProcessType process, int objectId, long count, Player actor, Object reference)
	{
		Item item = this.getItemByObjectId(objectId);
		if (item == null)
		{
			return null;
		}
		synchronized (item)
		{
			if (!this._items.contains(item))
			{
				return null;
			}

			if (item.getCount() > count)
			{
				item.changeCount(process, -count, actor, reference);
				item.setLastChange(2);
				item.updateDatabase();
				Item newItem = ItemManager.createItem(process, item.getId(), count, actor, reference);
				newItem.updateDatabase();
				this.refreshWeight();
				return newItem;
			}
		}

		return this.dropItem(process, item, actor, reference);
	}

	@Override
	protected void addItem(Item item)
	{
		super.addItem(item);
		if (item.isEquipped())
		{
			this.equipItem(item);
		}
	}

	@Override
	protected boolean removeItem(Item item)
	{
		for (int i = 0; i < this._paperdoll.length; i++)
		{
			if (this._paperdoll[i] == item)
			{
				this.unEquipItemInSlot(i);
			}
		}

		return super.removeItem(item);
	}

	public Item getPaperdollItem(int slot)
	{
		return this._paperdoll[slot];
	}

	public boolean isPaperdollSlotEmpty(int slot)
	{
		return this._paperdoll[slot] == null;
	}

	public boolean isPaperdollSlotNotEmpty(int slot)
	{
		return this._paperdoll[slot] != null;
	}

	public boolean isItemEquipped(int itemId)
	{
		for (Item item : this._paperdoll)
		{
			if (item != null && item.getId() == itemId)
			{
				return true;
			}
		}

		return false;
	}

	public static int getPaperdollIndex(BodyPart bodyPart)
	{
		return bodyPart == null ? -1 : bodyPart.getPaperdollSlot();
	}

	public Item getPaperdollItemByBodyPart(BodyPart bodyPart)
	{
		int index = getPaperdollIndex(bodyPart);
		return index == -1 ? null : this._paperdoll[index];
	}

	public Item getPaperdollItemByItemId(int itemId)
	{
		for (Item item : this._paperdoll)
		{
			if (item != null && item.getId() == itemId)
			{
				return item;
			}
		}

		return null;
	}

	public int getPaperdollItemId(int slot)
	{
		Item item = this._paperdoll[slot];
		if (item != null)
		{
			if (TransmogConfig.ENABLE_TRANSMOG)
			{
				int transmogId = item.getTransmogId();
				if (transmogId > 0)
				{
					return transmogId;
				}
			}

			return item.getId();
		}
		return 0;
	}

	public int getPaperdollItemDisplayId(int slot)
	{
		Item item = this._paperdoll[slot];
		if (item != null)
		{
			if (TransmogConfig.ENABLE_TRANSMOG)
			{
				int transmogId = item.getTransmogId();
				if (transmogId > 0)
				{
					return transmogId;
				}
			}

			return item.getDisplayId();
		}
		return 0;
	}

	public int getPaperdollItemVisualId(int slot)
	{
		Item item = this._paperdoll[slot];
		if (slot == InventorySlot.RHAND.getSlot() && this.getOwner().isPlayer())
		{
			return this.getOwner().asPlayer().getWeaponShiftedDisplayId();
		}
		return item != null ? item.getVisualId() : 0;
	}

	public VariationInstance getPaperdollAugmentation(int slot)
	{
		Item item = this._paperdoll[slot];
		return item != null ? item.getAugmentation() : null;
	}

	public int getPaperdollObjectId(int slot)
	{
		Item item = this._paperdoll[slot];
		return item != null ? item.getObjectId() : 0;
	}

	public synchronized void addPaperdollListener(Inventory.PaperdollListener listener)
	{
		if (!this._paperdollListeners.contains(listener))
		{
			this._paperdollListeners.add(listener);
		}
	}

	public synchronized void removePaperdollListener(Inventory.PaperdollListener listener)
	{
		this._paperdollListeners.remove(listener);
	}

	public synchronized Item setPaperdollItem(int slot, Item item)
	{
		Creature owner = this.getOwner();
		Item old = this._paperdoll[slot];
		if (old != item)
		{
			if (old != null)
			{
				if (owner.isPlayer())
				{
					owner.asPlayer().getCombatPower().removeItemCombatPower(old);
				}

				this._paperdoll[slot] = null;
				this._paperdollCache.getPaperdollItems().remove(old);
				old.setItemLocation(this.getBaseLocation());
				old.setLastChange(2);
				int mask = 0;

				for (int i = 0; i < 59; i++)
				{
					Item pi = this._paperdoll[i];
					if (pi != null)
					{
						mask |= pi.getTemplate().getItemMask();
					}
				}

				this._wearedMask = mask;

				for (Inventory.PaperdollListener listener : this._paperdollListeners)
				{
					if (listener != null)
					{
						listener.notifyUnequiped(slot, old, this);
					}
				}

				old.updateDatabase();
				if (slot >= 17 && slot <= 21 && owner.isPlayer())
				{
					AgathionSkillHolder agathionSkills = AgathionData.getInstance().getSkills(old.getId());
					if (agathionSkills != null)
					{
						boolean update = false;
						Player player = owner.asPlayer();

						for (Skill skill : agathionSkills.getMainSkills(old.getEnchantLevel()))
						{
							player.removeSkill(skill, false, skill.isPassive());
							update = true;
						}

						for (Skill skill : agathionSkills.getSubSkills(old.getEnchantLevel()))
						{
							player.removeSkill(skill, false, skill.isPassive());
							update = true;
						}

						if (update)
						{
							player.sendSkillList();
						}
					}
				}
			}

			if (item != null)
			{
				if (owner.isPlayer())
				{
					owner.asPlayer().getCombatPower().addItemCombatPower(item);
				}

				this._paperdoll[slot] = item;
				this._paperdollCache.getPaperdollItems().add(item);
				item.setItemLocation(this.getEquipLocation(), slot);
				item.setLastChange(2);
				this._wearedMask = this._wearedMask | item.getTemplate().getItemMask();

				for (Inventory.PaperdollListener listenerx : this._paperdollListeners)
				{
					if (listenerx != null)
					{
						listenerx.notifyEquiped(slot, item, this);
					}
				}

				item.updateDatabase();
				if (slot >= 17 && slot <= 21 && owner.isPlayer())
				{
					AgathionSkillHolder agathionSkills = AgathionData.getInstance().getSkills(item.getId());
					if (agathionSkills != null)
					{
						boolean update = false;
						Player player = owner.asPlayer();
						if (slot == 17)
						{
							for (Skill skill : agathionSkills.getMainSkills(item.getEnchantLevel()))
							{
								if (!skill.isPassive() || skill.checkConditions(SkillConditionScope.PASSIVE, player, player))
								{
									player.addSkill(skill, false);
									update = true;
								}
							}
						}

						for (Skill skillx : agathionSkills.getSubSkills(item.getEnchantLevel()))
						{
							if (!skillx.isPassive() || skillx.checkConditions(SkillConditionScope.PASSIVE, player, player))
							{
								player.addSkill(skillx, false);
								update = true;
							}
						}

						if (update)
						{
							player.sendSkillList();
						}
					}
				}
			}

			this._paperdollCache.clearCachedStats();
			owner.getStat().recalculateStats(!owner.isPlayer());
			if (owner.isPlayer())
			{
				owner.sendPacket(new ExUserInfoEquipSlot(owner.asPlayer()));
			}
		}

		if (old != null && owner != null && owner.isPlayer())
		{
			Player playerx = owner.asPlayer();
			if (slot == 16 && !playerx.hasEnteredWorld())
			{
				for (ItemSkillHolder skillxx : old.getTemplate().getAllSkills())
				{
					playerx.addSkill(skillxx.getSkill(), false);
				}
			}

			if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_ITEM_UNEQUIP, old.getTemplate()))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerItemUnequip(playerx, old), old.getTemplate());
			}
		}

		return old;
	}

	public int getWearedMask()
	{
		return this._wearedMask;
	}

	public List<Item> unEquipItemInBodySlotAndRecord(BodyPart bodyPart)
	{
		Inventory.ChangeRecorder recorder = this.newRecorder();

		try
		{
			this.unEquipItemInBodySlot(bodyPart);
		}
		finally
		{
			this.removePaperdollListener(recorder);
		}

		return recorder.getChangedItems();
	}

	public Item unEquipItemInSlot(int pdollSlot)
	{
		return this.setPaperdollItem(pdollSlot, null);
	}

	public List<Item> unEquipItemInSlotAndRecord(int slot)
	{
		Inventory.ChangeRecorder recorder = this.newRecorder();

		try
		{
			this.unEquipItemInSlot(slot);
		}
		finally
		{
			this.removePaperdollListener(recorder);
		}

		return recorder.getChangedItems();
	}

	public Item unEquipItemInBodySlot(BodyPart bodyPart)
	{
		if (bodyPart == BodyPart.HAIRALL)
		{
			this.setPaperdollItem(2, null);
		}

		int pdollSlot = BodyPart.getPaperdollIndex(bodyPart);
		return pdollSlot >= 0 ? this.setPaperdollItem(pdollSlot, null) : null;
	}

	public List<Item> equipItemAndRecord(Item item)
	{
		Inventory.ChangeRecorder recorder = this.newRecorder();

		try
		{
			this.equipItem(item);
		}
		finally
		{
			this.removePaperdollListener(recorder);
		}

		return recorder.getChangedItems();
	}

	public void equipItem(Item item)
	{
		if (this.getOwner().isPlayer())
		{
			if (this.getOwner().asPlayer().isInStoreMode())
			{
				return;
			}

			Player player = this.getOwner().asPlayer();
			if (!player.isGM() && !player.isHero() && item.isHeroItem())
			{
				return;
			}
		}

		BodyPart bodyPart = item.getTemplate().getBodyPart();
		Item formal = this.getPaperdollItem(6);
		if (item.getId() != 21163 && formal != null && formal.getTemplate().getBodyPart() == BodyPart.ALLDRESS)
		{
			switch (bodyPart)
			{
				case LR_HAND:
				case L_HAND:
				case R_HAND:
				case LEGS:
				case FEET:
				case GLOVES:
				case HEAD:
					return;
			}
		}

		switch (bodyPart)
		{
			case LR_HAND:
				Item lh = this.getPaperdollItem(7);
				if (lh != null && lh.isArmor() && lh.getArmorItem().getItemType() == ArmorType.SHIELD)
				{
					this.setPaperdollItem(7, null);
				}

				this.setPaperdollItem(5, item);
				break;
			case L_HAND:
				Item rh = this.getPaperdollItem(5);
				if (rh != null && rh.getTemplate().getBodyPart() == BodyPart.LR_HAND && (rh.getItemType() != WeaponType.FISHINGROD || item.getItemType() != EtcItemType.LURE) && (!item.isArmor() || item.getArmorItem().getItemType() != ArmorType.SIGIL))
				{
					this.setPaperdollItem(5, null);
				}

				this.setPaperdollItem(7, item);
				break;
			case R_HAND:
				this.setPaperdollItem(5, item);
				break;
			case LEGS:
				Item chest = this.getPaperdollItem(6);
				if (chest != null && chest.getTemplate().getBodyPart() == BodyPart.FULL_ARMOR)
				{
					this.setPaperdollItem(6, null);
				}

				this.setPaperdollItem(11, item);
				break;
			case FEET:
				this.setPaperdollItem(12, item);
				break;
			case GLOVES:
				this.setPaperdollItem(10, item);
				break;
			case HEAD:
				this.setPaperdollItem(1, item);
				break;
			case L_EAR:
			case R_EAR:
			case LR_EAR:
				if (this._paperdoll[9] == null)
				{
					this.setPaperdollItem(9, item);
				}
				else if (this._paperdoll[8] == null)
				{
					this.setPaperdollItem(8, item);
				}
				else
				{
					this.setPaperdollItem(9, item);
				}
				break;
			case L_FINGER:
			case R_FINGER:
			case LR_FINGER:
				if (this._paperdoll[14] == null)
				{
					this.setPaperdollItem(14, item);
				}
				else if (this._paperdoll[13] == null)
				{
					this.setPaperdollItem(13, item);
				}
				else
				{
					this.setPaperdollItem(14, item);
				}
				break;
			case NECK:
				this.setPaperdollItem(4, item);
				break;
			case FULL_ARMOR:
				this.setPaperdollItem(11, null);
				this.setPaperdollItem(6, item);
				break;
			case CHEST:
				this.setPaperdollItem(6, item);
				break;
			case HAIR:
				Item hair = this.getPaperdollItem(2);
				if (hair != null && hair.getTemplate().getBodyPart() == BodyPart.HAIRALL)
				{
					this.setPaperdollItem(3, null);
				}
				else
				{
					this.setPaperdollItem(2, null);
				}

				this.setPaperdollItem(2, item);
				break;
			case HAIR2:
				Item hair2 = this.getPaperdollItem(2);
				if (hair2 != null && hair2.getTemplate().getBodyPart() == BodyPart.HAIRALL)
				{
					this.setPaperdollItem(2, null);
				}
				else
				{
					this.setPaperdollItem(3, null);
				}

				this.setPaperdollItem(3, item);
				break;
			case HAIRALL:
				this.setPaperdollItem(3, null);
				this.setPaperdollItem(2, item);
				break;
			case UNDERWEAR:
				this.setPaperdollItem(0, item);
				break;
			case BACK:
				this.setPaperdollItem(28, item);
				break;
			case L_BRACELET:
				this.setPaperdollItem(15, item);
				break;
			case R_BRACELET:
				this.setPaperdollItem(16, item);
				break;
			case DECO:
				this.equipTalisman(item);
				break;
			case BELT:
				this.setPaperdollItem(29, item);
				break;
			case ALLDRESS:
				this.setPaperdollItem(11, null);
				this.setPaperdollItem(7, null);
				this.setPaperdollItem(5, null);
				this.setPaperdollItem(1, null);
				this.setPaperdollItem(12, null);
				this.setPaperdollItem(10, null);
				this.setPaperdollItem(6, item);
				break;
			case BROOCH:
				this.setPaperdollItem(30, item);
				break;
			case BROOCH_JEWEL:
				this.equipBroochJewel(item);
				break;
			case AGATHION:
				this.equipAgathion(item);
				break;
			case ARTIFACT_BOOK:
				this.setPaperdollItem(37, item);
				break;
			case ARTIFACT:
				this.equipArtifact(item);
				break;
			default:
				LOGGER.warning("Unknown body slot " + bodyPart + " for Item ID: " + item.getId());
		}
	}

	@Override
	protected void refreshWeight()
	{
		long weight = 0L;

		for (Item item : this._items)
		{
			if (item != null && item.getTemplate() != null)
			{
				weight += item.getTemplate().getWeight() * item.getCount();
			}
		}

		this._totalWeight = (int) Math.min(weight, 2147483647L);
	}

	public int getTotalWeight()
	{
		return this._totalWeight;
	}

	public void reduceAmmunitionCount(EtcItemType type)
	{
	}

	public Item findArrowForBow(ItemTemplate bow)
	{
		if (bow == null)
		{
			return null;
		}
		Item arrow = null;

		for (Item item : this._items)
		{
			if (item.isEtcItem() && item.getEtcItem().getItemType() == EtcItemType.ARROW && item.getTemplate().getCrystalTypePlus() == bow.getCrystalTypePlus())
			{
				arrow = item;
				break;
			}
		}

		return arrow;
	}

	public Item findBoltForCrossBow(ItemTemplate crossbow)
	{
		Item bolt = null;

		for (Item item : this._items)
		{
			if (item.isEtcItem() && item.getEtcItem().getItemType() == EtcItemType.BOLT && item.getTemplate().getCrystalTypePlus() == crossbow.getCrystalTypePlus())
			{
				bolt = item;
				break;
			}
		}

		return bolt;
	}

	public Item findElementalOrbForPistols(ItemTemplate pistols)
	{
		Item orb = null;

		for (Item item : this._items)
		{
			if (item.isEtcItem() && item.getEtcItem().getItemType() == EtcItemType.ELEMENTAL_ORB && item.getTemplate().getCrystalTypePlus() == pistols.getCrystalTypePlus())
			{
				orb = item;
				break;
			}
		}

		return orb;
	}

	@Override
	public void restore()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM items WHERE owner_id=? AND (loc=? OR loc=?) ORDER BY loc_data");)
		{
			ps.setInt(1, this.getOwnerId());
			ps.setString(2, this.getBaseLocation().name());
			ps.setString(3, this.getEquipLocation().name());

			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					try
					{
						Item item = new Item(rs);
						if (this.getOwner().isPlayer())
						{
							Player player = this.getOwner().asPlayer();
							if (!player.isGM() && !player.isHero() && item.isHeroItem())
							{
								item.setItemLocation(ItemLocation.INVENTORY);
							}
						}

						World.getInstance().addObject(item);
						if (item.isStackable() && this.getItemByItemId(item.getId()) != null)
						{
							this.addItem(ItemProcessType.RESTORE, item, this.getOwner().asPlayer(), null);
						}
						else
						{
							this.addItem(item);
						}
					}
					catch (Exception var9)
					{
						LOGGER.warning("Could not restore item " + rs.getInt("item_id") + " for " + this.getOwner());
					}
				}
			}

			this.refreshWeight();
		}
		catch (Exception var13)
		{
			LOGGER.log(Level.WARNING, "Could not restore inventory: " + var13.getMessage(), var13);
		}
	}

	public int getTalismanSlots()
	{
		return this.getOwner().asPlayer().getStat().getTalismanSlots();
	}

	private void equipTalisman(Item item)
	{
		if (this.getTalismanSlots() != 0)
		{
			for (int i = 22; i < 22 + this.getTalismanSlots(); i++)
			{
				if (this._paperdoll[i] != null && this.getPaperdollItemId(i) == item.getId())
				{
					this.setPaperdollItem(i, item);
					return;
				}
			}

			for (int ix = 22; ix < 22 + this.getTalismanSlots(); ix++)
			{
				if (this._paperdoll[ix] == null)
				{
					this.setPaperdollItem(ix, item);
					return;
				}
			}
		}
	}

	public int getArtifactSlots()
	{
		return this.getOwner().asPlayer().getStat().getArtifactSlots();
	}

	private void equipArtifact(Item item)
	{
		int artifactSlots = this.getArtifactSlots();
		if (artifactSlots != 0)
		{
			int locationSlot = item.getLocationSlot();
			if (locationSlot >= 38 && locationSlot <= 58 && BodyPart.fromPaperdollSlot(locationSlot) == null)
			{
				this.setPaperdollItem(locationSlot, item);
				item.setItemLocation(ItemLocation.PAPERDOLL, locationSlot);
			}
			else
			{
				switch (item.getTemplate().getArtifactSlot())
				{
					case 1:
						for (int slotxxx = 50; slotxxx < 50 + artifactSlots; slotxxx++)
						{
							if (slotxxx <= 52 && this._paperdoll[slotxxx] == null)
							{
								this.setPaperdollItem(slotxxx, item);
								item.setItemLocation(ItemLocation.PAPERDOLL, slotxxx);
								return;
							}
						}
						break;
					case 2:
						for (int slotxx = 53; slotxx < 53 + artifactSlots; slotxx++)
						{
							if (slotxx <= 55 && this._paperdoll[slotxx] == null)
							{
								this.setPaperdollItem(slotxx, item);
								item.setItemLocation(ItemLocation.PAPERDOLL, slotxx);
								return;
							}
						}
						break;
					case 3:
						for (int slotx = 56; slotx < 56 + artifactSlots; slotx++)
						{
							if (slotx <= 58 && this._paperdoll[slotx] == null)
							{
								this.setPaperdollItem(slotx, item);
								item.setItemLocation(ItemLocation.PAPERDOLL, slotx);
								return;
							}
						}
						break;
					case 4:
						for (int slot = 38; slot < 38 + 4 * artifactSlots; slot++)
						{
							if (slot <= 49 && this._paperdoll[slot] == null)
							{
								this.setPaperdollItem(slot, item);
								item.setItemLocation(ItemLocation.PAPERDOLL, slot);
								return;
							}
						}
				}
			}
		}
	}

	public int getBroochJewelSlots()
	{
		return this.getOwner().asPlayer().getStat().getBroochJewelSlots();
	}

	private void equipBroochJewel(Item item)
	{
		if (this.getBroochJewelSlots() != 0)
		{
			for (int i = 31; i < 31 + this.getBroochJewelSlots(); i++)
			{
				if (this._paperdoll[i] != null && this.getPaperdollItemId(i) == item.getId())
				{
					this.setPaperdollItem(i, item);
					return;
				}
			}

			for (int ix = 31; ix < 31 + this.getBroochJewelSlots(); ix++)
			{
				if (this._paperdoll[ix] == null)
				{
					this.setPaperdollItem(ix, item);
					return;
				}
			}
		}
	}

	public int getAgathionSlots()
	{
		return this.getOwner().asPlayer().getStat().getAgathionSlots();
	}

	private void equipAgathion(Item item)
	{
		if (this.getAgathionSlots() != 0)
		{
			for (int i = 17; i < 17 + this.getAgathionSlots(); i++)
			{
				if (this._paperdoll[i] != null && this.getPaperdollItemId(i) == item.getId())
				{
					this.setPaperdollItem(i, item);
					return;
				}
			}

			for (int ix = 17; ix < 17 + this.getAgathionSlots(); ix++)
			{
				if (this._paperdoll[ix] == null)
				{
					this.setPaperdollItem(ix, item);
					return;
				}
			}
		}
	}

	public boolean canEquipCloak()
	{
		return this.getOwner().asPlayer().getStat().canEquipCloak();
	}

	public void reloadEquippedItems()
	{
		for (Item item : this._paperdoll)
		{
			if (item != null)
			{
				int slot = item.getLocationSlot();

				for (Inventory.PaperdollListener listener : this._paperdollListeners)
				{
					if (listener != null)
					{
						listener.notifyUnequiped(slot, item, this);
						listener.notifyEquiped(slot, item, this);
					}
				}
			}
		}

		if (this.getOwner().isPlayer())
		{
			this.getOwner().sendPacket(new ExUserInfoEquipSlot(this.getOwner().asPlayer()));
		}
	}

	public int getArmorSetEnchant()
	{
		Creature creature = this.getOwner();
		return creature != null && creature.isPlayable() ? this._paperdollCache.getArmorSetEnchant(creature.asPlayable()) : 0;
	}

	public int getWeaponEnchant()
	{
		Item item = this.getPaperdollItem(5);
		return item != null ? item.getEnchantLevel() : 0;
	}

	public void blockItemSlot(BodyPart bodyPart)
	{
		this._blockedItemSlotsMask = this._blockedItemSlotsMask | bodyPart.getMask();
	}

	public void unblockItemSlot(BodyPart bodyPart)
	{
		this._blockedItemSlotsMask = this._blockedItemSlotsMask & ~bodyPart.getMask();
	}

	public boolean isItemSlotBlocked(BodyPart bodyPart)
	{
		long bodyPartValue = bodyPart.getMask();
		return (this._blockedItemSlotsMask & bodyPartValue) == bodyPartValue;
	}

	public void setBlockedItemSlotsMask(BodyPart bodyPart)
	{
		this._blockedItemSlotsMask = bodyPart.getMask();
	}

	@SafeVarargs
	public final Collection<Item> getPaperdollItems(Predicate<Item>... filters)
	{
		if (filters.length == 0)
		{
			return this._paperdollCache.getPaperdollItems();
		}
		Predicate<Item> filter = Objects::nonNull;

		for (Predicate<Item> additionalFilter : filters)
		{
			filter = filter.and(additionalFilter);
		}

		List<Item> items = new LinkedList<>();

		for (Item item : this._paperdoll)
		{
			if (filter.test(item))
			{
				items.add(item);
			}
		}

		return items;
	}

	@SafeVarargs
	public final int getPaperdollItemCount(Predicate<Item>... filters)
	{
		if (filters.length == 0)
		{
			return this._paperdollCache.getPaperdollItems().size();
		}
		Predicate<Item> filter = Objects::nonNull;

		for (Predicate<Item> additionalFilter : filters)
		{
			filter = filter.and(additionalFilter);
		}

		int count = 0;

		for (Item item : this._paperdoll)
		{
			if (filter.test(item))
			{
				count++;
			}
		}

		return count;
	}

	public PaperdollCache getPaperdollCache()
	{
		return this._paperdollCache;
	}

	private static class AgathionBraceletListener implements Inventory.PaperdollListener
	{
		private static Inventory.AgathionBraceletListener instance = new Inventory.AgathionBraceletListener();

		public static Inventory.AgathionBraceletListener getInstance()
		{
			return instance;
		}

		@Override
		public void notifyUnequiped(int slot, Item item, Inventory inventory)
		{
			Player player = item.asPlayer();
			if (player == null || !player.isChangingClass())
			{
				if (item.getTemplate().getBodyPart() == BodyPart.L_BRACELET)
				{
					inventory.unEquipItemInSlot(17);
					inventory.unEquipItemInSlot(18);
					inventory.unEquipItemInSlot(19);
					inventory.unEquipItemInSlot(20);
					inventory.unEquipItemInSlot(21);
				}
			}
		}

		@Override
		public void notifyEquiped(int slot, Item item, Inventory inventory)
		{
		}
	}

	private static class ArmorSetListener implements Inventory.PaperdollListener
	{
		private static Inventory.ArmorSetListener instance = new Inventory.ArmorSetListener();

		public static Inventory.ArmorSetListener getInstance()
		{
			return instance;
		}

		@Override
		public void notifyEquiped(int slot, Item item, Inventory inventory)
		{
			if (inventory.getOwner().isPlayable())
			{
				Playable playable = inventory.getOwner().asPlayable();
				boolean update = false;
				if (verifyAndApply(playable, item, Item::getId))
				{
					update = true;
				}

				int itemVisualId = item.getVisualId();
				if (itemVisualId > 0)
				{
					int appearanceStoneId = item.getAppearanceStoneId();
					AppearanceStone stone = AppearanceItemData.getInstance().getStone(appearanceStoneId > 0 ? appearanceStoneId : itemVisualId);
					if (stone != null && stone.getType() == AppearanceType.FIXED && verifyAndApply(playable, item, Item::getVisualId))
					{
						update = true;
					}
				}

				if (playable.isPlayer())
				{
					if (update)
					{
						playable.asPlayer().sendSkillList();
					}

					if (item.getTemplate().getBodyPart() == BodyPart.BROOCH_JEWEL || item.getTemplate().getBodyPart() == BodyPart.BROOCH)
					{
						playable.asPlayer().updateActiveBroochJewel();
					}
				}
			}
		}

		private static boolean applySkills(Playable playable, Item item, ArmorSet armorSet, Function<Item, Integer> idProvider)
		{
			long piecesCount = armorSet.getPieceCount(playable, idProvider);
			if (piecesCount < armorSet.getMinimumPieces())
			{
				return false;
			}
			boolean updateTimeStamp = false;
			boolean update = false;

			for (ArmorsetSkillHolder holder : armorSet.getSkills())
			{
				if (playable.getSkillLevel(holder.getSkillId()) < holder.getSkillLevel() && holder.validateConditions(playable, armorSet, idProvider))
				{
					Skill itemSkill = holder.getSkill();
					if (itemSkill == null)
					{
						Inventory.LOGGER.warning("Inventory.ArmorSetListener.addSkills: Incorrect skill: " + holder);
					}
					else if (!itemSkill.isPassive() || itemSkill.checkConditions(SkillConditionScope.PASSIVE, playable, playable))
					{
						playable.addSkill(itemSkill);
						if (itemSkill.isActive())
						{
							if (item != null && !playable.hasSkillReuse(itemSkill.getReuseHashCode()))
							{
								int equipDelay = item.getEquipReuseDelay();
								if (equipDelay > 0)
								{
									playable.addTimeStamp(itemSkill, equipDelay);
									playable.disableSkill(itemSkill, equipDelay);
								}
							}

							if (!itemSkill.hasNegativeEffect() && !itemSkill.isTransformation() && PlayerConfig.ARMOR_SET_EQUIP_ACTIVE_SKILL_REUSE > 0 && playable.asPlayer().hasEnteredWorld())
							{
								playable.addTimeStamp(itemSkill, itemSkill.getReuseDelay() > 0 ? itemSkill.getReuseDelay() : PlayerConfig.ARMOR_SET_EQUIP_ACTIVE_SKILL_REUSE);
							}

							updateTimeStamp = true;
						}

						update = true;
					}
				}
			}

			if (updateTimeStamp && playable.isPlayer())
			{
				playable.sendPacket(new SkillCoolTime(playable.asPlayer()));
			}

			return update;
		}

		private static boolean verifyAndApply(Playable playable, Item item, Function<Item, Integer> idProvider)
		{
			boolean update = false;

			for (ArmorSet armorSet : ArmorSetData.getInstance().getSets(idProvider.apply(item)))
			{
				if (applySkills(playable, item, armorSet, idProvider))
				{
					update = true;
				}
			}

			return update;
		}

		private static boolean verifyAndRemove(Playable playable, Item item, Function<Item, Integer> idProvider)
		{
			boolean update = false;

			for (ArmorSet armorSet : ArmorSetData.getInstance().getSets(idProvider.apply(item)))
			{
				for (ArmorsetSkillHolder holder : armorSet.getSkills())
				{
					if (!holder.validateConditions(playable, armorSet, idProvider))
					{
						Skill itemSkill = holder.getSkill();
						if (itemSkill == null)
						{
							Inventory.LOGGER.warning("Inventory.ArmorSetListener.removeSkills: Incorrect skill: " + holder);
						}
						else if (playable.removeSkill(itemSkill, itemSkill.isPassive()) != null)
						{
							update = true;
						}
					}
				}

				if (applySkills(playable, item, armorSet, idProvider))
				{
					update = true;
				}
			}

			return update;
		}

		@Override
		public void notifyUnequiped(int slot, Item item, Inventory inventory)
		{
			if (inventory.getOwner().isPlayable())
			{
				Playable playable = inventory.getOwner().asPlayable();
				boolean remove = false;
				if (verifyAndRemove(playable, item, Item::getId))
				{
					remove = true;
				}

				int itemVisualId = item.getVisualId();
				if (itemVisualId > 0)
				{
					int appearanceStoneId = item.getAppearanceStoneId();
					AppearanceStone stone = AppearanceItemData.getInstance().getStone(appearanceStoneId > 0 ? appearanceStoneId : itemVisualId);
					if (stone != null && stone.getType() == AppearanceType.FIXED && verifyAndRemove(playable, item, Item::getVisualId))
					{
						remove = true;
					}
				}

				if (playable.isPlayer())
				{
					if (remove)
					{
						Player player = playable.asPlayer();
						player.checkItemRestriction();
						player.sendSkillList();
					}

					if (item.getTemplate().getBodyPart() == BodyPart.BROOCH_JEWEL || item.getTemplate().getBodyPart() == BodyPart.BROOCH)
					{
						playable.asPlayer().updateActiveBroochJewel();
					}
				}
			}
		}
	}

	private static class ArtifactBookListener implements Inventory.PaperdollListener
	{
		private static Inventory.ArtifactBookListener instance = new Inventory.ArtifactBookListener();

		public static Inventory.ArtifactBookListener getInstance()
		{
			return instance;
		}

		@Override
		public void notifyUnequiped(int slot, Item item, Inventory inventory)
		{
			Player player = item.asPlayer();
			if (player == null || !player.isChangingClass())
			{
				if (item.getTemplate().getBodyPart() == BodyPart.ARTIFACT_BOOK)
				{
					inventory.unEquipItemInSlot(38);
					inventory.unEquipItemInSlot(39);
					inventory.unEquipItemInSlot(40);
					inventory.unEquipItemInSlot(41);
					inventory.unEquipItemInSlot(42);
					inventory.unEquipItemInSlot(43);
					inventory.unEquipItemInSlot(44);
					inventory.unEquipItemInSlot(45);
					inventory.unEquipItemInSlot(46);
					inventory.unEquipItemInSlot(47);
					inventory.unEquipItemInSlot(48);
					inventory.unEquipItemInSlot(49);
					inventory.unEquipItemInSlot(50);
					inventory.unEquipItemInSlot(51);
					inventory.unEquipItemInSlot(52);
					inventory.unEquipItemInSlot(53);
					inventory.unEquipItemInSlot(54);
					inventory.unEquipItemInSlot(55);
					inventory.unEquipItemInSlot(56);
					inventory.unEquipItemInSlot(57);
					inventory.unEquipItemInSlot(58);
				}
			}
		}

		@Override
		public void notifyEquiped(int slot, Item item, Inventory inventory)
		{
		}
	}

	private static class BowCrossRodListener implements Inventory.PaperdollListener
	{
		private static Inventory.BowCrossRodListener instance = new Inventory.BowCrossRodListener();

		public static Inventory.BowCrossRodListener getInstance()
		{
			return instance;
		}

		@Override
		public void notifyUnequiped(int slot, Item item, Inventory inventory)
		{
			if (slot == 5 && item.isWeapon())
			{
				switch (item.getWeaponItem().getItemType())
				{
					case BOW:
					case CROSSBOW:
					case TWOHANDCROSSBOW:
						Item leftHandItemx = inventory.getPaperdollItem(7);
						if (leftHandItemx != null && leftHandItemx.getItemType() != ArmorType.SIGIL)
						{
							inventory.setPaperdollItem(7, null);
						}

						Player ownerx = inventory.getOwner().asPlayer();
						if (ownerx != null)
						{
							ownerx.removeAmmunitionSkills();
						}
						break;
					case PISTOLS:
						Player owner = inventory.getOwner().asPlayer();
						if (owner != null)
						{
							owner.removeAmmunitionSkills();
						}
						break;
					case FISHINGROD:
						Item leftHandItem = inventory.getPaperdollItem(7);
						if (leftHandItem != null)
						{
							inventory.setPaperdollItem(7, null);
						}
				}
			}
		}

		@Override
		public void notifyEquiped(int slot, Item item, Inventory inventory)
		{
		}
	}

	private static class BraceletListener implements Inventory.PaperdollListener
	{
		private static Inventory.BraceletListener instance = new Inventory.BraceletListener();

		public static Inventory.BraceletListener getInstance()
		{
			return instance;
		}

		@Override
		public void notifyUnequiped(int slot, Item item, Inventory inventory)
		{
			Player player = item.asPlayer();
			if (player == null || !player.isChangingClass())
			{
				if (item.getTemplate().getBodyPart() == BodyPart.R_BRACELET)
				{
					inventory.unEquipItemInSlot(22);
					inventory.unEquipItemInSlot(23);
					inventory.unEquipItemInSlot(24);
					inventory.unEquipItemInSlot(25);
					inventory.unEquipItemInSlot(26);
					inventory.unEquipItemInSlot(27);
				}
			}
		}

		@Override
		public void notifyEquiped(int slot, Item item, Inventory inventory)
		{
		}
	}

	private static class BroochListener implements Inventory.PaperdollListener
	{
		private static Inventory.BroochListener instance = new Inventory.BroochListener();

		public static Inventory.BroochListener getInstance()
		{
			return instance;
		}

		@Override
		public void notifyUnequiped(int slot, Item item, Inventory inventory)
		{
			Player player = item.asPlayer();
			if (player == null || !player.isChangingClass())
			{
				if (item.getTemplate().getBodyPart() == BodyPart.BROOCH)
				{
					inventory.unEquipItemInSlot(31);
					inventory.unEquipItemInSlot(32);
					inventory.unEquipItemInSlot(33);
					inventory.unEquipItemInSlot(34);
					inventory.unEquipItemInSlot(35);
					inventory.unEquipItemInSlot(36);
				}
			}
		}

		@Override
		public void notifyEquiped(int slot, Item item, Inventory inventory)
		{
		}
	}

	private static class ChangeRecorder implements Inventory.PaperdollListener
	{
		private final Inventory _inventory;
		private final List<Item> _changed = new ArrayList<>(1);

		ChangeRecorder(Inventory inventory)
		{
			this._inventory = inventory;
			this._inventory.addPaperdollListener(this);
		}

		@Override
		public void notifyEquiped(int slot, Item item, Inventory inventory)
		{
			this._changed.add(item);
		}

		@Override
		public void notifyUnequiped(int slot, Item item, Inventory inventory)
		{
			this._changed.add(item);
		}

		public List<Item> getChangedItems()
		{
			return this._changed;
		}
	}

	private static class ItemSkillsListener implements Inventory.PaperdollListener
	{
		private static Inventory.ItemSkillsListener instance = new Inventory.ItemSkillsListener();

		public static Inventory.ItemSkillsListener getInstance()
		{
			return instance;
		}

		@Override
		public void notifyUnequiped(int slot, Item item, Inventory inventory)
		{
			if (inventory.getOwner().isPlayable())
			{
				Playable playable = inventory.getOwner().asPlayable();
				ItemTemplate it = item.getTemplate();
				Map<Integer, Skill> addedSkills = new HashMap<>(1);
				Map<Integer, Skill> removedSkills = new HashMap<>(1);
				boolean update = false;
				boolean updateTimestamp = false;
				if (item.isAugmented())
				{
					item.getAugmentation().removeBonus(playable);
				}

				playable.getStat().recalculateStats(true);
				item.clearEnchantStats();
				item.clearSpecialAbilities();
				if (it.hasSkills())
				{
					long remainingItemCount = inventory.getPaperdollItems(equippedItem -> equippedItem.getId() == item.getId()).size();
					if (remainingItemCount == 0L)
					{
						List<ItemSkillHolder> onEnchantSkills = it.getSkills(ItemSkillType.ON_ENCHANT);
						if (onEnchantSkills != null)
						{
							for (ItemSkillHolder holder : onEnchantSkills)
							{
								if (item.getEnchantLevel() >= holder.getValue())
								{
									Skill skill = holder.getSkill();
									if (skill != null)
									{
										removedSkills.putIfAbsent(skill.getId(), skill);
										update = true;
									}
								}
							}
						}

						List<ItemSkillHolder> normalSkills = it.getSkills(ItemSkillType.NORMAL);
						if (normalSkills != null)
						{
							for (ItemSkillHolder holderx : normalSkills)
							{
								Skill skill = holderx.getSkill();
								if (skill != null)
								{
									removedSkills.putIfAbsent(skill.getId(), skill);
									update = true;
								}
							}
						}
					}

					if (item.isArmor())
					{
						for (Item itm : inventory.getItems())
						{
							if (itm.isEquipped() && !itm.equals(item))
							{
								List<ItemSkillHolder> otherNormalSkills = itm.getTemplate().getSkills(ItemSkillType.NORMAL);
								if (otherNormalSkills != null)
								{
									for (ItemSkillHolder holderxx : otherNormalSkills)
									{
										if (playable.getSkillLevel(holderxx.getSkillId()) == 0)
										{
											Skill skill = holderxx.getSkill();
											if (skill != null)
											{
												Skill existingSkill = addedSkills.get(skill.getId());
												if (existingSkill != null)
												{
													if (existingSkill.getLevel() < skill.getLevel())
													{
														addedSkills.put(skill.getId(), skill);
													}
												}
												else
												{
													addedSkills.put(skill.getId(), skill);
												}

												if (skill.isActive() && !playable.hasSkillReuse(skill.getReuseHashCode()))
												{
													int equipDelay = item.getEquipReuseDelay();
													if (equipDelay > 0)
													{
														playable.addTimeStamp(skill, equipDelay);
														playable.disableSkill(skill, equipDelay);
													}

													updateTimestamp = true;
												}

												update = true;
											}
										}
									}
								}
							}
						}
					}
				}

				for (Item equipped : inventory.getPaperdollItems())
				{
					if (equipped.getTemplate().hasSkills())
					{
						List<ItemSkillHolder> otherEnchantSkills = equipped.getTemplate().getSkills(ItemSkillType.ON_ENCHANT);
						List<ItemSkillHolder> otherBlessingSkills = equipped.getTemplate().getSkills(ItemSkillType.ON_BLESSING);
						if (otherEnchantSkills != null || otherBlessingSkills != null)
						{
							if (otherEnchantSkills != null)
							{
								for (ItemSkillHolder holderxxx : otherEnchantSkills)
								{
									if (equipped.getEnchantLevel() >= holderxxx.getValue())
									{
										Skill skill = holderxxx.getSkill();
										if (skill != null && skill.isPassive() && !skill.checkConditions(SkillConditionScope.PASSIVE, playable, playable))
										{
											removedSkills.putIfAbsent(skill.getId(), skill);
											update = true;
										}
									}
								}
							}

							if (otherBlessingSkills != null && equipped.isBlessed())
							{
								for (ItemSkillHolder holderxxxx : otherBlessingSkills)
								{
									Skill skill = holderxxxx.getSkill();
									if (skill != null && skill.isPassive() && !skill.checkConditions(SkillConditionScope.PASSIVE, playable, playable))
									{
										removedSkills.putIfAbsent(skill.getId(), skill);
										update = true;
									}
								}
							}
						}
					}
				}

				for (Skill skill : playable.getAllSkills())
				{
					if (skill.isToggle() && playable.isAffectedBySkill(skill.getId()) && !skill.checkConditions(SkillConditionScope.GENERAL, playable, playable) || it.isWeapon() && skill.isRemovedOnUnequipWeapon())
					{
						playable.stopSkillEffects(SkillFinishType.REMOVED, skill.getId());
						update = true;
					}
				}

				if (slot < 18 || slot > 21)
				{
					it.forEachSkill(ItemSkillType.ON_UNEQUIP, holderxxxxx -> holderxxxxx.getSkill().activateSkill(playable, playable, item));
				}

				if (update)
				{
					for (Skill skillx : removedSkills.values())
					{
						playable.removeSkill(skillx, skillx.isPassive());
					}

					for (Skill skillx : addedSkills.values())
					{
						playable.addSkill(skillx);
					}

					if (playable.isPlayer())
					{
						playable.asPlayer().sendSkillList();
					}
				}

				if (updateTimestamp && playable.isPlayer())
				{
					playable.sendPacket(new SkillCoolTime(playable.asPlayer()));
				}

				if (item.isWeapon())
				{
					playable.unchargeAllShots();
				}
			}
		}

		@Override
		public void notifyEquiped(int slot, Item item, Inventory inventory)
		{
			if (inventory.getOwner().isPlayable())
			{
				Playable playable = inventory.getOwner().asPlayable();
				Map<Integer, Skill> addedSkills = new HashMap<>(1);
				boolean updateTimestamp = false;
				if (item.isAugmented())
				{
					item.getAugmentation().applyBonus(playable);
				}

				playable.getStat().recalculateStats(true);
				item.applyEnchantStats();
				item.applySpecialAbilities();
				if (item.getTemplate().hasSkills())
				{
					List<ItemSkillHolder> onEnchantSkills = item.getTemplate().getSkills(ItemSkillType.ON_ENCHANT);
					if (onEnchantSkills != null)
					{
						for (ItemSkillHolder holder : onEnchantSkills)
						{
							if (playable.getSkillLevel(holder.getSkillId()) < holder.getSkillLevel() && item.getEnchantLevel() >= holder.getValue())
							{
								Skill skill = holder.getSkill();
								if (skill != null && (!skill.isPassive() || skill.checkConditions(SkillConditionScope.PASSIVE, playable, playable)))
								{
									Skill existingSkill = addedSkills.get(skill.getId());
									if (existingSkill != null)
									{
										if (existingSkill.getLevel() < skill.getLevel())
										{
											addedSkills.put(skill.getId(), skill);
										}
									}
									else
									{
										addedSkills.put(skill.getId(), skill);
									}

									if (skill.isActive() && !skill.hasNegativeEffect() && !skill.isTransformation() && PlayerConfig.ITEM_EQUIP_ACTIVE_SKILL_REUSE > 0 && playable.asPlayer().hasEnteredWorld())
									{
										playable.addTimeStamp(skill, skill.getReuseDelay() > 0 ? skill.getReuseDelay() : PlayerConfig.ITEM_EQUIP_ACTIVE_SKILL_REUSE);
										updateTimestamp = true;
									}
								}
							}
						}
					}

					if (item.isBlessed())
					{
						List<ItemSkillHolder> onBlessingSkills = item.getTemplate().getSkills(ItemSkillType.ON_BLESSING);
						if (onBlessingSkills != null)
						{
							for (ItemSkillHolder holderx : onBlessingSkills)
							{
								if (playable.getSkillLevel(holderx.getSkillId()) < holderx.getSkillLevel() && item.getEnchantLevel() >= holderx.getValue())
								{
									Skill skill = holderx.getSkill();
									if (skill != null && (!skill.isPassive() || skill.checkConditions(SkillConditionScope.PASSIVE, playable, playable)))
									{
										Skill existingSkillx = addedSkills.get(skill.getId());
										if (existingSkillx != null)
										{
											if (existingSkillx.getLevel() < skill.getLevel())
											{
												addedSkills.put(skill.getId(), skill);
											}
										}
										else
										{
											addedSkills.put(skill.getId(), skill);
										}
									}
								}
							}
						}
					}

					List<ItemSkillHolder> normalSkills = item.getTemplate().getSkills(ItemSkillType.NORMAL);
					if (normalSkills != null)
					{
						for (ItemSkillHolder holderxx : normalSkills)
						{
							if (playable.getSkillLevel(holderxx.getSkillId()) < holderxx.getSkillLevel())
							{
								Skill skill = holderxx.getSkill();
								if (skill != null && (!skill.isPassive() || skill.checkConditions(SkillConditionScope.PASSIVE, playable, playable)))
								{
									Skill existingSkillx = addedSkills.get(skill.getId());
									if (existingSkillx != null)
									{
										if (existingSkillx.getLevel() < skill.getLevel())
										{
											addedSkills.put(skill.getId(), skill);
										}
									}
									else
									{
										addedSkills.put(skill.getId(), skill);
									}

									if (skill.isActive())
									{
										if (!playable.hasSkillReuse(skill.getReuseHashCode()))
										{
											int equipDelay = item.getEquipReuseDelay();
											if (equipDelay > 0)
											{
												playable.addTimeStamp(skill, equipDelay);
												playable.disableSkill(skill, equipDelay);
											}
										}

										if (!skill.hasNegativeEffect() && !skill.isTransformation() && PlayerConfig.ITEM_EQUIP_ACTIVE_SKILL_REUSE > 0 && playable.asPlayer().hasEnteredWorld())
										{
											playable.addTimeStamp(skill, skill.getReuseDelay() > 0 ? skill.getReuseDelay() : PlayerConfig.ITEM_EQUIP_ACTIVE_SKILL_REUSE);
										}

										updateTimestamp = true;
									}
								}
							}
						}
					}
				}

				for (Item equipped : inventory.getPaperdollItems())
				{
					if (equipped.getTemplate().hasSkills())
					{
						List<ItemSkillHolder> otherEnchantSkills = equipped.getTemplate().getSkills(ItemSkillType.ON_ENCHANT);
						List<ItemSkillHolder> otherBlessingSkills = equipped.getTemplate().getSkills(ItemSkillType.ON_BLESSING);
						if (otherEnchantSkills != null || otherBlessingSkills != null)
						{
							if (otherEnchantSkills != null)
							{
								for (ItemSkillHolder holderxxx : otherEnchantSkills)
								{
									if (playable.getSkillLevel(holderxxx.getSkillId()) < holderxxx.getSkillLevel() && equipped.getEnchantLevel() >= holderxxx.getValue())
									{
										Skill skill = holderxxx.getSkill();
										if (skill != null && (!skill.isPassive() || skill.checkConditions(SkillConditionScope.PASSIVE, playable, playable)))
										{
											Skill existingSkillxx = addedSkills.get(skill.getId());
											if (existingSkillxx != null)
											{
												if (existingSkillxx.getLevel() < skill.getLevel())
												{
													addedSkills.put(skill.getId(), skill);
												}
											}
											else
											{
												addedSkills.put(skill.getId(), skill);
											}

											if (skill.isActive() && !skill.hasNegativeEffect() && !skill.isTransformation() && PlayerConfig.ITEM_EQUIP_ACTIVE_SKILL_REUSE > 0 && playable.asPlayer().hasEnteredWorld())
											{
												playable.addTimeStamp(skill, skill.getReuseDelay() > 0 ? skill.getReuseDelay() : PlayerConfig.ITEM_EQUIP_ACTIVE_SKILL_REUSE);
												updateTimestamp = true;
											}
										}
									}
								}
							}

							if (otherBlessingSkills != null)
							{
								for (ItemSkillHolder holderxxxx : otherBlessingSkills)
								{
									if (playable.getSkillLevel(holderxxxx.getSkillId()) < holderxxxx.getSkillLevel() && equipped.isBlessed())
									{
										Skill skill = holderxxxx.getSkill();
										if (skill != null && (!skill.isPassive() || skill.checkConditions(SkillConditionScope.PASSIVE, playable, playable)))
										{
											Skill existingSkillxxx = addedSkills.get(skill.getId());
											if (existingSkillxxx != null)
											{
												if (existingSkillxxx.getLevel() < skill.getLevel())
												{
													addedSkills.put(skill.getId(), skill);
												}
											}
											else
											{
												addedSkills.put(skill.getId(), skill);
											}
										}
									}
								}
							}
						}
					}
				}

				if (slot < 18 || slot > 21)
				{
					item.getTemplate().forEachSkill(ItemSkillType.ON_EQUIP, holderxxxxx -> holderxxxxx.getSkill().activateSkill(playable, playable, item));
				}

				if (!addedSkills.isEmpty())
				{
					for (Skill skill : addedSkills.values())
					{
						playable.addSkill(skill);
					}

					if (playable.isPlayer())
					{
						playable.asPlayer().sendSkillList();
					}
				}

				if (updateTimestamp && playable.isPlayer())
				{
					playable.sendPacket(new SkillCoolTime(playable.asPlayer()));
				}
			}
		}
	}

	public interface PaperdollListener
	{
		void notifyEquiped(int var1, Item var2, Inventory var3);

		void notifyUnequiped(int var1, Item var2, Inventory var3);
	}

	private static class StatsListener implements Inventory.PaperdollListener
	{
		private static Inventory.StatsListener instance = new Inventory.StatsListener();

		public static Inventory.StatsListener getInstance()
		{
			return instance;
		}

		@Override
		public void notifyUnequiped(int slot, Item item, Inventory inventory)
		{
			inventory.getOwner().getStat().recalculateStats(true);
		}

		@Override
		public void notifyEquiped(int slot, Item item, Inventory inventory)
		{
			inventory.getOwner().getStat().recalculateStats(true);
		}
	}
}
