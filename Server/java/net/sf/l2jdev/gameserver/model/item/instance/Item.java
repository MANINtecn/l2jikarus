package net.sf.l2jdev.gameserver.model.item.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.OlympiadConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.custom.TransmogConfig;
import net.sf.l2jdev.gameserver.data.holders.PreparedMultisellListHolder;
import net.sf.l2jdev.gameserver.data.xml.AgathionData;
import net.sf.l2jdev.gameserver.data.xml.AppearanceItemData;
import net.sf.l2jdev.gameserver.data.xml.ArmorSetData;
import net.sf.l2jdev.gameserver.data.xml.EnchantItemOptionsData;
import net.sf.l2jdev.gameserver.data.xml.EnsoulData;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.data.xml.OptionData;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.IdManager;
import net.sf.l2jdev.gameserver.managers.ItemsOnGroundManager;
import net.sf.l2jdev.gameserver.managers.SiegeGuardManager;
import net.sf.l2jdev.gameserver.model.ArmorSet;
import net.sf.l2jdev.gameserver.model.DropProtection;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.VariationInstance;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.WorldRegion;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.request.AutoPeelRequest;
import net.sf.l2jdev.gameserver.model.conditions.Condition;
import net.sf.l2jdev.gameserver.model.ensoul.EnsoulOption;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerAugment;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerItemDrop;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerItemPickup;
import net.sf.l2jdev.gameserver.model.events.holders.item.OnItemBypassEvent;
import net.sf.l2jdev.gameserver.model.events.holders.item.OnItemTalk;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.item.Armor;
import net.sf.l2jdev.gameserver.model.item.EtcItem;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.item.appearance.AppearanceStone;
import net.sf.l2jdev.gameserver.model.item.appearance.AppearanceType;
import net.sf.l2jdev.gameserver.model.item.enchant.attribute.AttributeHolder;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.enums.ItemSkillType;
import net.sf.l2jdev.gameserver.model.item.holders.AgathionSkillHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ArmorsetSkillHolder;
import net.sf.l2jdev.gameserver.model.item.type.EtcItemType;
import net.sf.l2jdev.gameserver.model.item.type.ItemType;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.options.EnchantOptions;
import net.sf.l2jdev.gameserver.model.options.Options;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.SkillConditionScope;
import net.sf.l2jdev.gameserver.model.variables.ItemVariables;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.UserInfoType;
import net.sf.l2jdev.gameserver.network.serverpackets.DropItem;
import net.sf.l2jdev.gameserver.network.serverpackets.GetItem;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SkillCoolTime;
import net.sf.l2jdev.gameserver.network.serverpackets.SpawnItem;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.taskmanagers.ItemAppearanceTaskManager;
import net.sf.l2jdev.gameserver.taskmanagers.ItemLifeTimeTaskManager;
import net.sf.l2jdev.gameserver.taskmanagers.ItemManaTaskManager;
import net.sf.l2jdev.gameserver.util.GMAudit;

public class Item extends WorldObject
{
	private static final Logger LOGGER = Logger.getLogger(Item.class.getName());
	private static final Logger LOG_ITEMS = Logger.getLogger("item");
	private int _ownerId;
	private Player _owner;
	private int _dropperObjectId = 0;
	private long _count = 1L;
	private long _initCount;
	private long _time;
	private boolean _decrease = false;
	private final int _itemId;
	private final ItemTemplate _itemTemplate;
	private ItemLocation _loc;
	private int _locData;
	private int _enchantLevel;
	private boolean _wear;
	private VariationInstance _augmentation = null;
	private int _mana = -1;
	private boolean _consumingMana = false;
	private int _type1;
	private int _type2;
	private long _dropTime;
	private boolean _published = false;
	private boolean _protected;
	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int REMOVED = 3;
	public static final int MODIFIED = 2;
	public static final int[] DEFAULT_ENCHANT_OPTIONS = new int[]
	{
		0,
		0,
		0
	};
	private int _lastChange = 2;
	private boolean _existsInDb;
	private boolean _storedInDb;
	private final ReentrantLock _dbLock = new ReentrantLock();
	private Map<AttributeType, AttributeHolder> _elementals = null;
	private ScheduledFuture<?> _itemLootShedule = null;
	private final DropProtection _dropProtection = new DropProtection();
	private final List<Options> _enchantOptions = new ArrayList<>();
	private final EnsoulOption[] _ensoulOptions = new EnsoulOption[2];
	private final EnsoulOption[] _ensoulSpecialOptions = new EnsoulOption[1];
	private boolean _isBlessed = false;

	public Item(int objectId, int itemId)
	{
		super(objectId);
		this.setInstanceType(InstanceType.Item);
		this._itemId = itemId;
		this._itemTemplate = ItemData.getInstance().getTemplate(itemId);
		if (this._itemId != 0 && this._itemTemplate != null)
		{
			super.setName(this._itemTemplate.getName());
			this._loc = ItemLocation.VOID;
			this._type1 = 0;
			this._type2 = 0;
			this._dropTime = 0L;
			this._mana = this._itemTemplate.getDuration();
			this._time = this._itemTemplate.getTime() == -1L ? -1L : System.currentTimeMillis() + this._itemTemplate.getTime() * 60L * 1000L;
			this.scheduleLifeTimeTask();
			this.scheduleVisualLifeTime();
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}

	public Item(int objectId, ItemTemplate itemTemplate)
	{
		super(objectId);
		this.setInstanceType(InstanceType.Item);
		this._itemId = itemTemplate.getId();
		this._itemTemplate = itemTemplate;
		if (this._itemId == 0)
		{
			throw new IllegalArgumentException();
		}
		super.setName(this._itemTemplate.getName());
		this._loc = ItemLocation.VOID;
		this._mana = this._itemTemplate.getDuration();
		this._time = this._itemTemplate.getTime() == -1L ? -1L : System.currentTimeMillis() + this._itemTemplate.getTime() * 60L * 1000L;
		this.scheduleLifeTimeTask();
		this.scheduleVisualLifeTime();
	}

	public Item(ResultSet rs) throws SQLException
	{
		this(rs.getInt("object_id"), ItemData.getInstance().getTemplate(rs.getInt("item_id")));
		this._count = rs.getLong("count");
		this._ownerId = rs.getInt("owner_id");
		this._loc = ItemLocation.valueOf(rs.getString("loc"));
		this._locData = rs.getInt("loc_data");
		this._enchantLevel = rs.getInt("enchant_level");
		this._type1 = rs.getInt("custom_type1");
		this._type2 = rs.getInt("custom_type2");
		this._mana = rs.getInt("mana_left");
		this._time = rs.getLong("time");
		this.scheduleLifeTimeTask();
		this.scheduleVisualLifeTime();
		this._existsInDb = true;
		this._storedInDb = true;
		if (this.isEquipable())
		{
			this.restoreAttributes();
			this.restoreSpecialAbilities();
		}

		this._isBlessed = this.getVariables().getBoolean("blessed", false);
	}

	public Item(int itemId)
	{
		this(IdManager.getInstance().getNextId(), itemId);
	}

	public void pickupMe(Creature creature)
	{
		if (creature.isPlayer())
		{
			Player player = creature.asPlayer();
			PreparedMultisellListHolder multisell = player.getMultiSell();
			if (multisell != null && multisell.isMaintainEnchantment())
			{
				player.setMultiSell(null);
			}
		}

		creature.broadcastPacket(new GetItem(this, creature.getObjectId()));
		WorldRegion oldregion = this.getWorldRegion();
		synchronized (this)
		{
			this.setSpawned(false);
		}

		Castle castle = CastleManager.getInstance().getCastle(this);
		if (castle != null && SiegeGuardManager.getInstance().getSiegeGuardByItem(castle.getResidenceId(), this.getId()) != null)
		{
			SiegeGuardManager.getInstance().removeTicket(this);
			ItemsOnGroundManager.getInstance().removeObject(this);
		}

		World.getInstance().removeVisibleObject(this, oldregion);
		this.setWorldRegion(null);
		if (creature.isPlayer() && EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_ITEM_PICKUP, this.getTemplate()))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerItemPickup(creature.asPlayer(), this), this.getTemplate());
		}
	}

	public void setOwnerId(ItemProcessType process, int ownerId, Player creator, Object reference)
	{
		this.setOwnerId(ownerId);
		if (GeneralConfig.LOG_ITEMS && !GeneralConfig.LOG_ITEMS_SMALL_LOG && !GeneralConfig.LOG_ITEMS_IDS_ONLY || GeneralConfig.LOG_ITEMS_SMALL_LOG && (this._itemTemplate.isEquipable() || this._itemTemplate.getId() == 57) || GeneralConfig.LOG_ITEMS_IDS_ONLY && GeneralConfig.LOG_ITEMS_IDS_LIST.contains(this._itemTemplate.getId()))
		{
			if (this._enchantLevel > 0)
			{
				LOG_ITEMS.info(StringUtil.concat("SETOWNER:", String.valueOf(process), ", item ", String.valueOf(this.getObjectId()), ":+", String.valueOf(this._enchantLevel), " ", this._itemTemplate.getName(), "(", String.valueOf(this._count), "), ", String.valueOf(creator), ", ", String.valueOf(reference)));
			}
			else
			{
				LOG_ITEMS.info(StringUtil.concat("SETOWNER:", String.valueOf(process), ", item ", String.valueOf(this.getObjectId()), ":", this._itemTemplate.getName(), "(", String.valueOf(this._count), "), ", String.valueOf(creator), ", ", String.valueOf(reference)));
			}
		}

		if (creator != null && creator.isGM() && GeneralConfig.GMAUDIT)
		{
			String targetName = creator.getTarget() != null ? creator.getTarget().getName() : "no-target";
			String referenceName = "no-reference";
			if (reference instanceof WorldObject)
			{
				referenceName = ((WorldObject) reference).getName() != null ? ((WorldObject) reference).getName() : "no-name";
			}
			else if (reference instanceof String)
			{
				referenceName = (String) reference;
			}

			GMAudit.logAction(creator.toString(), StringUtil.concat(String.valueOf(process), "(id: ", String.valueOf(this._itemId), " name: ", this.getName(), ")"), targetName, StringUtil.concat("Object referencing this action is: ", referenceName));
		}
	}

	public void setOwnerId(int ownerId)
	{
		if (ownerId != this._ownerId)
		{
			this.removeSkillsFromOwner();
			this._owner = null;
			this._ownerId = ownerId;
			this._storedInDb = false;
			this.giveSkillsToOwner();
		}
	}

	public int getOwnerId()
	{
		return this._ownerId;
	}

	public void setItemLocation(ItemLocation loc)
	{
		this.setItemLocation(loc, 0, true);
	}

	public void setItemLocation(ItemLocation loc, int locData)
	{
		this.setItemLocation(loc, locData, true);
	}

	public void setItemLocation(ItemLocation loc, int locData, boolean checkSkills)
	{
		if (loc != this._loc || locData != this._locData)
		{
			if (checkSkills)
			{
				this.removeSkillsFromOwner();
			}

			this._loc = loc;
			this._locData = locData;
			this._storedInDb = false;
			if (checkSkills)
			{
				this.giveSkillsToOwner();
			}
		}
	}

	public ItemLocation getItemLocation()
	{
		return this._loc;
	}

	public void setCount(long count)
	{
		if (this._count != count)
		{
			this._count = count >= -1L ? count : 0L;
			this._storedInDb = false;
		}
	}

	public long getCount()
	{
		return this._count;
	}

	public void changeCount(ItemProcessType process, long count, Player creator, Object reference)
	{
		if (count != 0L)
		{
			long old = this._count;
			long max = this._itemId == 57 ? Inventory.MAX_ADENA : Long.MAX_VALUE;
			if (count > 0L && this._count > max - count)
			{
				this.setCount(max);
			}
			else
			{
				this.setCount(this._count + count);
			}

			if (this._count < 0L)
			{
				this.setCount(0L);
			}

			this._storedInDb = false;
			if (process != null && process != ItemProcessType.NONE)
			{
				if (GeneralConfig.LOG_ITEMS && !GeneralConfig.LOG_ITEMS_SMALL_LOG && !GeneralConfig.LOG_ITEMS_IDS_ONLY || GeneralConfig.LOG_ITEMS_SMALL_LOG && (this._itemTemplate.isEquipable() || this._itemTemplate.getId() == 57) || GeneralConfig.LOG_ITEMS_IDS_ONLY && GeneralConfig.LOG_ITEMS_IDS_LIST.contains(this._itemTemplate.getId()))
				{
					if (this._enchantLevel > 0)
					{
						LOG_ITEMS.info(StringUtil.concat("CHANGE:", String.valueOf(process), ", item ", String.valueOf(this.getObjectId()), ":+", String.valueOf(this._enchantLevel), " ", this._itemTemplate.getName(), "(", String.valueOf(this._count), "), PrevCount(", String.valueOf(old), "), ", String.valueOf(creator), ", ", String.valueOf(reference)));
					}
					else
					{
						LOG_ITEMS.info(StringUtil.concat("CHANGE:", String.valueOf(process), ", item ", String.valueOf(this.getObjectId()), ":", this._itemTemplate.getName(), "(", String.valueOf(this._count), "), PrevCount(", String.valueOf(old), "), ", String.valueOf(creator), ", ", String.valueOf(reference)));
					}
				}

				if (creator != null && creator.isGM() && GeneralConfig.GMAUDIT)
				{
					String targetName = creator.getTarget() != null ? creator.getTarget().getName() : "no-target";
					String referenceName = "no-reference";
					if (reference instanceof WorldObject)
					{
						referenceName = ((WorldObject) reference).getName() != null ? ((WorldObject) reference).getName() : "no-name";
					}
					else if (reference instanceof String)
					{
						referenceName = (String) reference;
					}

					GMAudit.logAction(creator.toString(), StringUtil.concat(String.valueOf(process), "(id: ", String.valueOf(this._itemId), " objId: ", String.valueOf(this.getObjectId()), " name: ", this.getName(), " count: ", String.valueOf(count), ")"), targetName, StringUtil.concat("Object referencing this action is: ", referenceName));
				}
			}
		}
	}

	public boolean isEnchantable()
	{
		return this._loc != ItemLocation.INVENTORY && this._loc != ItemLocation.PAPERDOLL ? false : this._itemTemplate.isEnchantable();
	}

	public boolean isEquipable()
	{
		return this._itemTemplate.getBodyPart() != BodyPart.NONE;
	}

	public boolean isEquipped()
	{
		return this._loc == ItemLocation.PAPERDOLL || this._loc == ItemLocation.PET_EQUIP;
	}

	public boolean isPetEquipped()
	{
		return this._loc == ItemLocation.PET_EQUIP;
	}

	public int getLocationSlot()
	{
		return this._locData;
	}

	public ItemTemplate getTemplate()
	{
		return this._itemTemplate;
	}

	public int getCustomType1()
	{
		return this._type1;
	}

	public int getCustomType2()
	{
		return this._type2;
	}

	public void setCustomType1(int newtype)
	{
		this._type1 = newtype;
	}

	public void setCustomType2(int newtype)
	{
		this._type2 = newtype;
	}

	public void setDropTime(long time)
	{
		this._dropTime = time;
	}

	public long getDropTime()
	{
		return this._dropTime;
	}

	public ItemType getItemType()
	{
		return this._itemTemplate.getItemType();
	}

	@Override
	public int getId()
	{
		return this._itemId;
	}

	public int getDisplayId()
	{
		return this._itemTemplate.getDisplayId();
	}

	public boolean isEtcItem()
	{
		return this._itemTemplate instanceof EtcItem;
	}

	public boolean isWeapon()
	{
		return this._itemTemplate instanceof Weapon;
	}

	public boolean isArmor()
	{
		return this._itemTemplate instanceof Armor;
	}

	public EtcItem getEtcItem()
	{
		return this._itemTemplate instanceof EtcItem ? (EtcItem) this._itemTemplate : null;
	}

	public Weapon getWeaponItem()
	{
		return this._itemTemplate instanceof Weapon ? (Weapon) this._itemTemplate : null;
	}

	public Armor getArmorItem()
	{
		return this._itemTemplate instanceof Armor ? (Armor) this._itemTemplate : null;
	}

	public int getCrystalCount()
	{
		return this._itemTemplate.getCrystalCount(this._enchantLevel);
	}

	public long getReferencePrice()
	{
		return this._itemTemplate.getReferencePrice();
	}

	public String getItemName()
	{
		return this._itemTemplate.getName();
	}

	public int getReuseDelay()
	{
		return this._itemTemplate.getReuseDelay();
	}

	public int getSharedReuseGroup()
	{
		return this._itemTemplate.getSharedReuseGroup();
	}

	public int getLastChange()
	{
		return this._lastChange;
	}

	public void setLastChange(int lastChange)
	{
		this._lastChange = lastChange;
	}

	public boolean isStackable()
	{
		return this._itemTemplate.isStackable();
	}

	public boolean isDropable()
	{
		if (!this._itemTemplate.isDropable())
		{
			return this._itemTemplate.isSealed() && this._owner != null && this._owner.isDead() && this._owner.getReputation() < 0;
		}
		else if (this.isEquipable() && this.getTransmogId() > 0)
		{
			return false;
		}
		else
		{
			return this.isAugmented() ? PlayerConfig.ALT_ALLOW_AUGMENT_TRADE : this.getVisualId() == 0;
		}
	}

	public boolean isDestroyable()
	{
		if (!this._itemTemplate.isDestroyable())
		{
			return false;
		}
		return this.isAugmented() ? PlayerConfig.ALT_ALLOW_AUGMENT_DESTROY : true;
	}

	public boolean isTradeable()
	{
		if (!this._itemTemplate.isTradeable())
		{
			return false;
		}
		else if (this.isEquipable() && this.getTransmogId() > 0)
		{
			return false;
		}
		else
		{
			return this.isAugmented() ? PlayerConfig.ALT_ALLOW_AUGMENT_TRADE : true;
		}
	}

	public boolean isSellable()
	{
		if (!this._itemTemplate.isSellable())
		{
			return false;
		}
		else if (this.isEquipable() && this.getTransmogId() > 0)
		{
			return false;
		}
		else
		{
			return this.isAugmented() ? PlayerConfig.ALT_ALLOW_AUGMENT_TRADE : true;
		}
	}

	public boolean isDepositable(boolean isPrivateWareHouse)
	{
		return this._itemTemplate.isDepositable() && !this.isEquipped() ? isPrivateWareHouse || this.isTradeable() && !this.isShadowItem() : false;
	}

	public boolean isPotion()
	{
		return this._itemTemplate.isPotion();
	}

	public boolean isElixir()
	{
		return this._itemTemplate.isElixir();
	}

	public boolean isScroll()
	{
		return this._itemTemplate.isScroll();
	}

	public boolean isHeroItem()
	{
		return this._itemTemplate.isHeroItem();
	}

	public boolean isCommonItem()
	{
		return this._itemTemplate.isCommon();
	}

	public boolean isPvp()
	{
		return this._itemTemplate.isPvpItem();
	}

	public boolean isOlyRestrictedItem()
	{
		return this._itemTemplate.isOlyRestrictedItem();
	}

	public boolean isAvailable(Player player, boolean allowAdena, boolean allowNonTradeable)
	{
		Summon pet = player.getPet();
		return !this.isEquipped() && this._itemTemplate.getType2() != 3 && (this._itemTemplate.getType2() != 4 || this._itemTemplate.getType1() != 1) && (pet == null || this.getObjectId() != pet.getControlObjectId()) && !player.isProcessingItem(this.getObjectId()) && (allowAdena || this._itemId != 57) && !player.isCastingNow(s -> s.getSkill().getItemConsumeId() != this._itemId) && (allowNonTradeable || this.isTradeable() && (this._itemTemplate.getItemType() != EtcItemType.PET_COLLAR || !player.havePetInvItems()));
	}

	public int getEnchantLevel()
	{
		return this._enchantLevel;
	}

	public boolean isEnchanted()
	{
		return this._enchantLevel > 0;
	}

	public void setEnchantLevel(int level)
	{
		int newLevel = Math.max(0, level);
		if (this._enchantLevel != newLevel)
		{
			this.clearEnchantStats();
			Player player = this.asPlayer();
			if (this.isEquipped() && this._itemTemplate.getBodyPart() == BodyPart.AGATHION)
			{
				AgathionSkillHolder agathionSkills = AgathionData.getInstance().getSkills(this.getId());
				if (agathionSkills != null)
				{
					boolean update = false;

					for (Skill skill : agathionSkills.getMainSkills(this._enchantLevel))
					{
						player.removeSkill(skill, false, skill.isPassive());
						update = true;
					}

					for (Skill skill : agathionSkills.getSubSkills(this._enchantLevel))
					{
						player.removeSkill(skill, false, skill.isPassive());
						update = true;
					}

					if (this.getLocationSlot() == 17)
					{
						for (Skill skill : agathionSkills.getMainSkills(newLevel))
						{
							if (!skill.isPassive() || skill.checkConditions(SkillConditionScope.PASSIVE, player, player))
							{
								player.addSkill(skill, false);
								update = true;
							}
						}
					}

					for (Skill skillx : agathionSkills.getSubSkills(newLevel))
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

			if (player != null)
			{
				if (!this.isEtcItem() && this.isEquipped())
				{
					player.getCombatPower().removeItemCombatPower(this);
					this._enchantLevel = newLevel;
					player.getCombatPower().addItemCombatPower(this);
				}
				else
				{
					this._enchantLevel = newLevel;
				}

				this.applyEnchantStats();
				this._storedInDb = false;
				player.getInventory().getPaperdollCache().clearArmorSetEnchant();
			}
			else
			{
				this._enchantLevel = newLevel;
				this.applyEnchantStats();
				this._storedInDb = false;
			}
		}
	}

	public boolean isAugmented()
	{
		return this._augmentation != null;
	}

	public VariationInstance getAugmentation()
	{
		return this._augmentation;
	}

	public boolean setAugmentation(VariationInstance augmentation, boolean updateDatabase)
	{
		if (this._augmentation != null)
		{
			if (this.isEquipped())
			{
				this._augmentation.removeBonus(this.asPlayer());
			}

			this.removeAugmentation();
		}

		this._augmentation = augmentation;
		if (updateDatabase)
		{
			this.updateItemOptions();
		}

		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_AUGMENT, this.getTemplate()))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerAugment(this.asPlayer(), this, augmentation, true), this.getTemplate());
		}

		return true;
	}

	public void removeAugmentation()
	{
		if (this._augmentation != null)
		{
			VariationInstance augment = this._augmentation;
			this._augmentation = null;

			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM item_variations WHERE itemId = ?");)
			{
				ps.setInt(1, this.getObjectId());
				ps.executeUpdate();
			}
			catch (Exception var10)
			{
				LOGGER.log(Level.SEVERE, "Item could not remove augmentation for " + this + " from DB: ", var10);
			}

			if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_AUGMENT, this.getTemplate()))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerAugment(this.asPlayer(), this, augment, false), this.getTemplate());
			}
		}
	}

	public void restoreAttributes()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps1 = con.prepareStatement("SELECT mineralId,option1,option2,option3 FROM item_variations WHERE itemId=?"); PreparedStatement ps2 = con.prepareStatement("SELECT elemType,elemValue FROM item_elementals WHERE itemId=?");)
		{
			ps1.setInt(1, this.getObjectId());

			try (ResultSet rs = ps1.executeQuery())
			{
				if (rs.next())
				{
					int mineralId = rs.getInt("mineralId");
					int option1 = rs.getInt("option1");
					int option2 = rs.getInt("option2");
					int option3 = rs.getInt("option3");
					if (option1 > 0 || option2 > 0 || option3 > 0)
					{
						this._augmentation = new VariationInstance(mineralId, option1, option2, option3);
					}
				}
			}

			ps2.setInt(1, this.getObjectId());

			try (ResultSet rsx = ps2.executeQuery())
			{
				while (rsx.next())
				{
					byte attributeType = rsx.getByte(1);
					int attributeValue = rsx.getInt(2);
					if (attributeType != -1 && attributeValue != -1)
					{
						this.applyAttribute(new AttributeHolder(AttributeType.findByClientId(attributeType), attributeValue));
					}
				}
			}
		}
		catch (Exception var19)
		{
			LOGGER.log(Level.SEVERE, "Item could not restore augmentation and elemental data for " + this + " from DB: ", var19);
		}
	}

	public void updateItemOptions()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			this.updateItemOptions(con);
		}
		catch (SQLException var6)
		{
			LOGGER.log(Level.SEVERE, "Item could not update atributes for " + this + " from DB:", var6);
		}
	}

	private void updateItemOptions(Connection con)
	{
		try (PreparedStatement ps = con.prepareStatement("REPLACE INTO item_variations VALUES(?,?,?,?,?)"))
		{
			ps.setInt(1, this.getObjectId());
			if (this._augmentation != null)
			{
				ps.setInt(2, this._augmentation.getMineralId());
				ps.setInt(3, this._augmentation.getOption1Id());
				ps.setInt(4, this._augmentation.getOption2Id());
				ps.setInt(5, this._augmentation.getOption3Id());
			}
			else
			{
				ps.setInt(2, 0);
				ps.setInt(3, 0);
				ps.setInt(4, 0);
				ps.setInt(5, 0);
			}

			ps.executeUpdate();
		}
		catch (SQLException var7)
		{
			LOGGER.log(Level.SEVERE, "Item could not update atributes for " + this + " from DB: ", var7);
		}
	}

	public void updateItemElementals()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			this.updateItemElements(con);
		}
		catch (SQLException var6)
		{
			LOGGER.log(Level.SEVERE, "Item could not update elementals for " + this + " from DB: ", var6);
		}
	}

	private void updateItemElements(Connection con)
	{
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ?"))
		{
			ps.setInt(1, this.getObjectId());
			ps.executeUpdate();
		}
		catch (SQLException var10)
		{
			LOGGER.log(Level.SEVERE, "Item could not update elementals for " + this + " from DB: ", var10);
		}

		if (this._elementals != null)
		{
			try (PreparedStatement ps = con.prepareStatement("INSERT INTO item_elementals VALUES(?,?,?)"))
			{
				for (AttributeHolder attribute : this._elementals.values())
				{
					ps.setInt(1, this.getObjectId());
					ps.setByte(2, attribute.getType().getClientId());
					ps.setInt(3, attribute.getValue());
					ps.executeUpdate();
					ps.clearParameters();
				}
			}
			catch (SQLException var8)
			{
				LOGGER.log(Level.SEVERE, "Item could not update elementals for " + this + " from DB: ", var8);
			}
		}
	}

	public Collection<AttributeHolder> getAttributes()
	{
		return this._elementals != null ? this._elementals.values() : null;
	}

	public boolean hasAttributes()
	{
		return this._elementals != null && !this._elementals.isEmpty();
	}

	public AttributeHolder getAttribute(AttributeType type)
	{
		return this._elementals != null ? this._elementals.get(type) : null;
	}

	public AttributeHolder getAttackAttribute()
	{
		if (this.isWeapon())
		{
			if (this._itemTemplate.getAttributes() != null)
			{
				if (!this._itemTemplate.getAttributes().isEmpty())
				{
					return this._itemTemplate.getAttributes().iterator().next();
				}
			}
			else if (this._elementals != null && !this._elementals.isEmpty())
			{
				return this._elementals.values().iterator().next();
			}
		}

		return null;
	}

	public AttributeType getAttackAttributeType()
	{
		AttributeHolder holder = this.getAttackAttribute();
		return holder != null ? holder.getType() : AttributeType.NONE;
	}

	public int getAttackAttributePower()
	{
		AttributeHolder holder = this.getAttackAttribute();
		return holder != null ? holder.getValue() : 0;
	}

	public int getDefenceAttribute(AttributeType element)
	{
		if (this.isArmor())
		{
			if (this._itemTemplate.getAttributes() != null)
			{
				AttributeHolder attribute = this._itemTemplate.getAttribute(element);
				if (attribute != null)
				{
					return attribute.getValue();
				}
			}
			else if (this._elementals != null)
			{
				AttributeHolder attribute = this.getAttribute(element);
				if (attribute != null)
				{
					return attribute.getValue();
				}
			}
		}

		return 0;
	}

	private synchronized void applyAttribute(AttributeHolder holder)
	{
		if (this._elementals == null)
		{
			this._elementals = new LinkedHashMap<>(3);
			this._elementals.put(holder.getType(), holder);
		}
		else
		{
			AttributeHolder attribute = this.getAttribute(holder.getType());
			if (attribute != null)
			{
				attribute.setValue(holder.getValue());
			}
			else
			{
				this._elementals.put(holder.getType(), holder);
			}
		}
	}

	public void setAttribute(AttributeHolder holder, boolean updateDatabase)
	{
		this.applyAttribute(holder);
		if (updateDatabase)
		{
			this.updateItemElementals();
		}
	}

	public void clearAttribute(AttributeType type)
	{
		if (this._elementals != null && this.getAttribute(type) != null)
		{
			synchronized (this._elementals)
			{
				this._elementals.remove(type);
			}

			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ? AND elemType = ?");)
			{
				ps.setInt(1, this.getObjectId());
				ps.setByte(2, type.getClientId());
				ps.executeUpdate();
			}
			catch (Exception var11)
			{
				LOGGER.log(Level.SEVERE, "Item could not remove elemental enchant for " + this + " from DB: ", var11);
			}
		}
	}

	public void clearAllAttributes()
	{
		if (this._elementals != null)
		{
			synchronized (this._elementals)
			{
				this._elementals.clear();
			}

			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ?");)
			{
				ps.setInt(1, this.getObjectId());
				ps.executeUpdate();
			}
			catch (Exception var10)
			{
				LOGGER.log(Level.SEVERE, "Item could not remove all elemental enchant for " + this + " from DB: ", var10);
			}
		}
	}

	public boolean isShadowItem()
	{
		return this._mana >= 0;
	}

	public int getMana()
	{
		return this._mana;
	}

	public void decreaseMana(boolean resetConsumingMana)
	{
		this.decreaseMana(resetConsumingMana, 1);
	}

	public void decreaseMana(boolean resetConsumingMana, int count)
	{
		if (this.isShadowItem())
		{
			if (this._mana - count >= 0)
			{
				this._mana -= count;
			}
			else
			{
				this._mana = 0;
			}

			if (this._storedInDb)
			{
				this._storedInDb = false;
			}

			if (resetConsumingMana)
			{
				this._consumingMana = false;
			}

			Player player = this.asPlayer();
			if (player != null)
			{
				switch (this._mana)
				{
					case 1:
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_S_REMAINING_MANA_IS_NOW_1_IT_WILL_DISAPPEAR_SOON);
						sm.addItemName(this._itemTemplate);
						player.sendPacket(sm);
						break;
					}
					case 5:
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_S_REMAINING_MANA_IS_NOW_5);
						sm.addItemName(this._itemTemplate);
						player.sendPacket(sm);
						break;
					}
					case 10:
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_S_REMAINING_MANA_IS_NOW_10);
						sm.addItemName(this._itemTemplate);
						player.sendPacket(sm);
					}
				}

				if (this._mana == 0)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_S_REMAINING_MANA_IS_NOW_0_AND_THE_ITEM_HAS_DISAPPEARED);
					sm.addItemName(this._itemTemplate);
					player.sendPacket(sm);
					if (this.isEquipped())
					{
						InventoryUpdate iu = new InventoryUpdate();

						for (Item item : player.getInventory().unEquipItemInSlotAndRecord(this.getLocationSlot()))
						{
							iu.addModifiedItem(item);
						}

						player.sendInventoryUpdate(iu);
						player.broadcastUserInfo();
					}

					if (this._loc != ItemLocation.WAREHOUSE)
					{
						player.getInventory().destroyItem(ItemProcessType.DESTROY, this, player, null);
						InventoryUpdate iu = new InventoryUpdate();
						iu.addRemovedItem(this);
						player.sendInventoryUpdate(iu);
					}
					else
					{
						player.getWarehouse().destroyItem(ItemProcessType.DESTROY, this, player, null);
					}

					World.getInstance().removeObject(this);
				}
				else
				{
					if (!this._consumingMana && this.isEquipped())
					{
						this.scheduleConsumeManaTask();
					}

					if (this._loc != ItemLocation.WAREHOUSE)
					{
						InventoryUpdate iu = new InventoryUpdate();
						iu.addModifiedItem(this);
						player.sendInventoryUpdate(iu);
					}
				}
			}
		}
	}

	public void scheduleConsumeManaTask()
	{
		if (!this._consumingMana)
		{
			this._consumingMana = true;
			ItemManaTaskManager.getInstance().add(this);
		}
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}

	public void updateDatabase()
	{
		this.updateDatabase(false);
	}

	public void updateDatabase(boolean force)
	{
		this._dbLock.lock();

		try
		{
			if (this._existsInDb)
			{
				if (this._ownerId != 0 && this._loc != ItemLocation.VOID && this._loc != ItemLocation.REFUND && (this._count != 0L || this._loc == ItemLocation.LEASE))
				{
					if (!GeneralConfig.LAZY_ITEMS_UPDATE || force)
					{
						this.updateInDb();
					}
				}
				else
				{
					this.removeFromDb();
				}

				return;
			}

			if (this._ownerId != 0 && this._loc != ItemLocation.VOID && this._loc != ItemLocation.REFUND && (this._count != 0L || this._loc == ItemLocation.LEASE))
			{
				this.insertIntoDb();
				return;
			}
		}
		finally
		{
			this._dbLock.unlock();
		}
	}

	public void dropMe(Creature dropper, int locX, int locY, int locZ)
	{
		int x = locX;
		int y = locY;
		int z = locZ;
		if (dropper != null)
		{
			Instance instance = dropper.getInstanceWorld();
			Location dropDest = GeoEngine.getInstance().getValidLocation(dropper.getX(), dropper.getY(), dropper.getZ(), locX, locY, locZ, instance);
			x = dropDest.getX();
			y = dropDest.getY();
			z = dropDest.getZ();
			this.setInstance(instance);
		}
		else
		{
			this.setInstance(null);
		}

		this.setSpawned(true);
		this.setXYZ(x, y, z);
		this.setDropTime(System.currentTimeMillis());
		this.setDropperObjectId(dropper != null ? dropper.getObjectId() : 0);
		WorldRegion region = this.getWorldRegion();
		region.addVisibleObject(this);
		World.getInstance().addVisibleObject(this, region);
		if (GeneralConfig.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().save(this);
		}

		this.setDropperObjectId(0);
		if (dropper != null && dropper.isPlayer())
		{
			this._owner = null;
			if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_ITEM_DROP, this.getTemplate()))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerItemDrop(dropper.asPlayer(), this, new Location(x, y, z)), this.getTemplate());
			}
		}
	}

	private void updateInDb()
	{
		if (this._existsInDb && !this._wear && !this._storedInDb)
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,custom_type1=?,custom_type2=?,mana_left=?,time=? WHERE object_id = ?");)
			{
				ps.setInt(1, this._ownerId);
				ps.setLong(2, this._count);
				ps.setString(3, this._loc.name());
				ps.setInt(4, this._locData);
				ps.setInt(5, this._enchantLevel);
				ps.setInt(6, this._type1);
				ps.setInt(7, this._type2);
				ps.setInt(8, this._mana);
				ps.setLong(9, this._time);
				ps.setInt(10, this.getObjectId());
				ps.executeUpdate();
				this._existsInDb = true;
				this._storedInDb = true;
				if (this._augmentation != null)
				{
					this.updateItemOptions(con);
				}

				if (this._elementals != null)
				{
					this.updateItemElements(con);
				}

				this.updateSpecialAbilities(con);
			}
			catch (Exception var9)
			{
				LOGGER.log(Level.SEVERE, "Item could not update " + this + " in DB: Reason: " + var9.getMessage(), var9);
			}
		}
	}

	private void insertIntoDb()
	{
		if (!this._existsInDb && this.getObjectId() != 0 && !this._wear)
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,mana_left,time) VALUES (?,?,?,?,?,?,?,?,?,?,?)");)
			{
				ps.setInt(1, this._ownerId);
				ps.setInt(2, this._itemId);
				ps.setLong(3, this._count);
				ps.setString(4, this._loc.name());
				ps.setInt(5, this._locData);
				ps.setInt(6, this._enchantLevel);
				ps.setInt(7, this.getObjectId());
				ps.setInt(8, this._type1);
				ps.setInt(9, this._type2);
				ps.setInt(10, this._mana);
				ps.setLong(11, this._time);
				ps.executeUpdate();
				this._existsInDb = true;
				this._storedInDb = true;
				if (this._augmentation != null)
				{
					this.updateItemOptions(con);
				}

				if (this._elementals != null)
				{
					this.updateItemElements(con);
				}

				this.updateSpecialAbilities(con);
			}
			catch (Exception var9)
			{
				LOGGER.log(Level.SEVERE, "Item could not insert " + this + " into DB: Reason: " + var9.getMessage(), var9);
			}
		}
	}

	private void removeFromDb()
	{
		if (this._existsInDb && !this._wear)
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				try (PreparedStatement ps = con.prepareStatement("DELETE FROM items WHERE object_id = ?"))
				{
					ps.setInt(1, this.getObjectId());
					ps.executeUpdate();
				}

				try (PreparedStatement ps = con.prepareStatement("DELETE FROM item_variations WHERE itemId = ?"))
				{
					ps.setInt(1, this.getObjectId());
					ps.executeUpdate();
				}

				try (PreparedStatement ps = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ?"))
				{
					ps.setInt(1, this.getObjectId());
					ps.executeUpdate();
				}

				try (PreparedStatement ps = con.prepareStatement("DELETE FROM item_special_abilities WHERE objectId = ?"))
				{
					ps.setInt(1, this.getObjectId());
					ps.executeUpdate();
				}

				try (PreparedStatement ps = con.prepareStatement("DELETE FROM item_variables WHERE id = ?"))
				{
					ps.setInt(1, this.getObjectId());
					ps.executeUpdate();
				}
			}
			catch (Exception var32)
			{
				LOGGER.log(Level.SEVERE, "Item could not delete " + this + " in DB ", var32);
			}
			finally
			{
				this._existsInDb = false;
				this._storedInDb = false;
			}
		}
	}

	public void resetOwnerTimer()
	{
		if (this._itemLootShedule != null)
		{
			this._itemLootShedule.cancel(true);
			this._itemLootShedule = null;
		}
	}

	public void setItemLootShedule(ScheduledFuture<?> sf)
	{
		this._itemLootShedule = sf;
	}

	public ScheduledFuture<?> getItemLootShedule()
	{
		return this._itemLootShedule;
	}

	public void setProtected(boolean isProtected)
	{
		this._protected = isProtected;
	}

	public boolean isProtected()
	{
		return this._protected;
	}

	public boolean isAvailable()
	{
		if (!this._itemTemplate.isConditionAttached())
		{
			return true;
		}
		else if (this._loc != ItemLocation.PET && this._loc != ItemLocation.PET_EQUIP)
		{
			Player player = this.asPlayer();
			if (player != null)
			{
				for (Condition condition : this._itemTemplate.getConditions())
				{
					if (condition != null && !condition.testImpl(player, player, null, this._itemTemplate))
					{
						return false;
					}
				}

				if (player.hasRequest(AutoPeelRequest.class))
				{
					EtcItem etcItem = this.getEtcItem();
					if (etcItem != null && etcItem.getExtractableItems() != null)
					{
						return false;
					}
				}
			}

			return true;
		}
		else
		{
			return true;
		}
	}

	public void setCountDecrease(boolean decrease)
	{
		this._decrease = decrease;
	}

	public boolean getCountDecrease()
	{
		return this._decrease;
	}

	public void setInitCount(int initCount)
	{
		this._initCount = initCount;
	}

	public long getInitCount()
	{
		return this._initCount;
	}

	public void restoreInitCount()
	{
		if (this._decrease)
		{
			this.setCount(this._initCount);
		}
	}

	public boolean isTimeLimitedItem()
	{
		return this._time > 0L;
	}

	public long getTime()
	{
		return this._time;
	}

	public long getRemainingTime()
	{
		return this._time - System.currentTimeMillis();
	}

	public void endOfLife()
	{
		Player player = this.asPlayer();
		if (player != null)
		{
			if (this.isEquipped())
			{
				InventoryUpdate iu = new InventoryUpdate();

				for (Item item : player.getInventory().unEquipItemInSlotAndRecord(this.getLocationSlot()))
				{
					iu.addModifiedItem(item);
				}

				player.sendInventoryUpdate(iu);
			}

			if (this._loc != ItemLocation.WAREHOUSE)
			{
				player.getInventory().destroyItem(ItemProcessType.DESTROY, this, player, null);
				InventoryUpdate iu = new InventoryUpdate();
				iu.addRemovedItem(this);
				player.sendInventoryUpdate(iu);
			}
			else
			{
				player.getWarehouse().destroyItem(ItemProcessType.DESTROY, this, player, null);
			}

			player.sendPacket(new SystemMessage(SystemMessageId.S1_HAS_EXPIRED).addItemName(this._itemId));
			World.getInstance().removeObject(this);
		}
	}

	public void scheduleLifeTimeTask()
	{
		if (this.isTimeLimitedItem())
		{
			if (this.getRemainingTime() <= 0L)
			{
				this.endOfLife();
			}
			else
			{
				ItemLifeTimeTaskManager.getInstance().add(this, this.getTime());
			}
		}
	}

	public void setDropperObjectId(int id)
	{
		this._dropperObjectId = id;
	}

	@Override
	public void sendInfo(Player player)
	{
		if (this._dropperObjectId != 0)
		{
			player.sendPacket(new DropItem(this, this._dropperObjectId));
		}
		else
		{
			player.sendPacket(new SpawnItem(this));
		}
	}

	public DropProtection getDropProtection()
	{
		return this._dropProtection;
	}

	public boolean isPublished()
	{
		return this._published;
	}

	public void publish()
	{
		this._published = true;
	}

	@Override
	public boolean decayMe()
	{
		if (GeneralConfig.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().removeObject(this);
		}

		return super.decayMe();
	}

	public boolean isQuestItem()
	{
		return this._itemTemplate.isQuestItem();
	}

	public boolean isElementable()
	{
		return this._loc != ItemLocation.INVENTORY && this._loc != ItemLocation.PAPERDOLL ? false : this._itemTemplate.isElementable();
	}

	public boolean isFreightable()
	{
		return this._itemTemplate.isFreightable();
	}

	public int useSkillDisTime()
	{
		return this._itemTemplate.useSkillDisTime();
	}

	public int getOlyEnchantLevel()
	{
		Player player = this.asPlayer();
		int enchant = this._enchantLevel;
		if (player == null)
		{
			return enchant;
		}
		if (player.isInOlympiadMode())
		{
			if (this._itemTemplate.isWeapon())
			{
				if (OlympiadConfig.OLYMPIAD_WEAPON_ENCHANT_LIMIT >= 0 && enchant > OlympiadConfig.OLYMPIAD_WEAPON_ENCHANT_LIMIT)
				{
					enchant = OlympiadConfig.OLYMPIAD_WEAPON_ENCHANT_LIMIT;
				}
			}
			else if (OlympiadConfig.OLYMPIAD_ARMOR_ENCHANT_LIMIT >= 0 && enchant > OlympiadConfig.OLYMPIAD_ARMOR_ENCHANT_LIMIT)
			{
				enchant = OlympiadConfig.OLYMPIAD_ARMOR_ENCHANT_LIMIT;
			}
		}

		return enchant;
	}

	public boolean hasPassiveSkills()
	{
		return this._itemTemplate.getItemType() == EtcItemType.ENCHT_ATTR_RUNE && this._loc == ItemLocation.INVENTORY && this._ownerId > 0 && this._itemTemplate.getSkills(ItemSkillType.NORMAL) != null;
	}

	public void giveSkillsToOwner()
	{
		if (this.isEquipped() || this.hasPassiveSkills())
		{
			Player player = this.asPlayer();
			if (player != null)
			{
				this._itemTemplate.forEachSkill(ItemSkillType.NORMAL, holder -> {
					Skill skill = holder.getSkill();
					if (skill.isPassive())
					{
						if (player.isAffectedBySkill(skill.getId()))
						{
							int oldSkillLevel = player.getSkillLevel(skill.getId());
							if (oldSkillLevel < skill.getLevel())
							{
								player.removeSkill(skill, false);
								player.addSkill(skill, false);
							}
						}
						else
						{
							player.addSkill(skill, false);
						}
					}
				});
				this._itemTemplate.forEachSkill(ItemSkillType.ON_ENCHANT, holder -> {
					Skill skill = holder.getSkill();
					if (skill.isPassive() && this.getEnchantLevel() >= holder.getValue())
					{
						player.removeSkill(skill, false);
						player.addSkill(skill, false);
					}
				});
			}
		}
	}

	public void removeSkillsFromOwner()
	{
		if (this.hasPassiveSkills())
		{
			Player player = this.asPlayer();
			if (player != null)
			{
				this._itemTemplate.forEachSkill(ItemSkillType.NORMAL, holder -> {
					Skill skill = holder.getSkill();
					if (skill.isPassive())
					{
						int skillLevel = player.getSkillLevel(holder.getSkillId());
						if (skillLevel > 0 && skillLevel <= holder.getSkillLevel() || !skill.checkConditions(SkillConditionScope.PASSIVE, player, player))
						{
							player.removeSkill(skill, false, skill.isPassive());
						}
					}
					else
					{
						player.removeSkill(skill, false, skill.isPassive());
					}
				});
				player.getInventory().applyItemSkills();
			}
		}
	}

	@Override
	public boolean isItem()
	{
		return true;
	}

	@Override
	public Player asPlayer()
	{
		if (this._owner == null && this._ownerId != 0)
		{
			this._owner = World.getInstance().getPlayer(this._ownerId);
		}

		return this._owner;
	}

	public int getEquipReuseDelay()
	{
		return this._itemTemplate.getEquipReuseDelay();
	}

	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("Quest"))
		{
			String questName = command.substring(6);
			String event = null;
			int idx = questName.indexOf(32);
			if (idx > 0)
			{
				event = questName.substring(idx).trim();
			}

			if (event != null)
			{
				if (EventDispatcher.getInstance().hasListener(EventType.ON_ITEM_BYPASS_EVENT, this.getTemplate()))
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnItemBypassEvent(this, player, event), this.getTemplate());
				}
			}
			else if (EventDispatcher.getInstance().hasListener(EventType.ON_ITEM_TALK, this.getTemplate()))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnItemTalk(this, player), this.getTemplate());
			}
		}
	}

	public int[] getEnchantOptions()
	{
		EnchantOptions op = EnchantItemOptionsData.getInstance().getOptions(this);
		return op != null ? op.getOptions() : DEFAULT_ENCHANT_OPTIONS;
	}

	public Collection<EnsoulOption> getSpecialAbilities()
	{
		List<EnsoulOption> result = new ArrayList<>();

		for (EnsoulOption ensoulOption : this._ensoulOptions)
		{
			if (ensoulOption != null)
			{
				result.add(ensoulOption);
			}
		}

		return result;
	}

	public EnsoulOption getSpecialAbility(int index)
	{
		return this._ensoulOptions[index];
	}

	public Collection<EnsoulOption> getAdditionalSpecialAbilities()
	{
		List<EnsoulOption> result = new ArrayList<>();

		for (EnsoulOption ensoulSpecialOption : this._ensoulSpecialOptions)
		{
			if (ensoulSpecialOption != null)
			{
				result.add(ensoulSpecialOption);
			}
		}

		return result;
	}

	public EnsoulOption getAdditionalSpecialAbility(int index)
	{
		return this._ensoulSpecialOptions[index];
	}

	public void addSpecialAbility(EnsoulOption option, int position, int type, boolean updateInDB)
	{
		if (type != 1 || position >= 0 && position <= 1)
		{
			if (type != 2 || position == 0)
			{
				if (type == 1)
				{
					EnsoulOption oldOption = this._ensoulOptions[position];
					if (oldOption != null)
					{
						this.removeSpecialAbility(oldOption);
					}

					if (position < this._itemTemplate.getEnsoulSlots())
					{
						this._ensoulOptions[position] = option;
					}
				}
				else if (type == 2)
				{
					EnsoulOption oldOptionx = this._ensoulSpecialOptions[position];
					if (oldOptionx != null)
					{
						this.removeSpecialAbility(oldOptionx);
					}

					if (position < this._itemTemplate.getSpecialEnsoulSlots())
					{
						this._ensoulSpecialOptions[position] = option;
					}
				}

				if (updateInDB)
				{
					this.updateSpecialAbilities();
				}
			}
		}
	}

	public void removeSpecialAbility(int position, int type)
	{
		if (type == 1)
		{
			EnsoulOption option = this._ensoulOptions[position];
			if (option != null)
			{
				this.removeSpecialAbility(option);
				this._ensoulOptions[position] = null;
				if (position == 0)
				{
					EnsoulOption secondEnsoul = this._ensoulOptions[1];
					if (secondEnsoul != null)
					{
						this.removeSpecialAbility(secondEnsoul);
						this._ensoulOptions[1] = null;
						this.addSpecialAbility(secondEnsoul, 0, type, true);
					}
				}
			}
		}
		else if (type == 2)
		{
			EnsoulOption option = this._ensoulSpecialOptions[position];
			if (option != null)
			{
				this.removeSpecialAbility(option);
				this._ensoulSpecialOptions[position] = null;
			}
		}
	}

	public void clearSpecialAbilities()
	{
		for (EnsoulOption ensoulOption : this._ensoulOptions)
		{
			this.clearSpecialAbility(ensoulOption);
		}

		for (EnsoulOption ensoulSpecialOption : this._ensoulSpecialOptions)
		{
			this.clearSpecialAbility(ensoulSpecialOption);
		}
	}

	public void applySpecialAbilities()
	{
		if (this.isEquipped())
		{
			for (EnsoulOption ensoulOption : this._ensoulOptions)
			{
				this.applySpecialAbility(ensoulOption);
			}

			for (EnsoulOption ensoulSpecialOption : this._ensoulSpecialOptions)
			{
				this.applySpecialAbility(ensoulSpecialOption);
			}
		}
	}

	private void removeSpecialAbility(EnsoulOption option)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM item_special_abilities WHERE objectId = ? AND optionId = ?");)
		{
			ps.setInt(1, this.getObjectId());
			ps.setInt(2, option.getId());
			ps.execute();
			Skill skill = option.getSkill();
			if (skill != null)
			{
				Player player = this.asPlayer();
				if (player != null)
				{
					player.removeSkill(skill.getId());
				}
			}
		}
		catch (Exception var10)
		{
			LOGGER.log(Level.WARNING, "Item could not remove special ability for " + this, var10);
		}
	}

	private void applySpecialAbility(EnsoulOption option)
	{
		if (option != null)
		{
			Skill skill = option.getSkill();
			if (skill != null)
			{
				Player player = this.asPlayer();
				if (player != null && player.getSkillLevel(skill.getId()) != skill.getLevel())
				{
					player.addSkill(skill, false);
				}
			}
		}
	}

	private void clearSpecialAbility(EnsoulOption option)
	{
		if (option != null)
		{
			Skill skill = option.getSkill();
			if (skill != null)
			{
				Player player = this.asPlayer();
				if (player != null)
				{
					player.removeSkill(skill, false, true);
				}
			}
		}
	}

	private void restoreSpecialAbilities()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM item_special_abilities WHERE objectId = ? ORDER BY position");)
		{
			ps.setInt(1, this.getObjectId());

			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					int optionId = rs.getInt("optionId");
					int type = rs.getInt("type");
					int position = rs.getInt("position");
					EnsoulOption option = EnsoulData.getInstance().getOption(optionId);
					if (option != null)
					{
						this.addSpecialAbility(option, position, type, false);
					}
				}
			}
		}
		catch (Exception var14)
		{
			LOGGER.log(Level.WARNING, "Item could not restore special abilities for " + this, var14);
		}
	}

	public void updateSpecialAbilities()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			this.updateSpecialAbilities(con);
		}
		catch (Exception var6)
		{
			LOGGER.log(Level.WARNING, "Item could not update item special abilities", var6);
		}
	}

	private void updateSpecialAbilities(Connection con)
	{
		try (PreparedStatement ps = con.prepareStatement("INSERT INTO item_special_abilities (`objectId`, `type`, `optionId`, `position`) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE type = ?, optionId = ?, position = ?"))
		{
			ps.setInt(1, this.getObjectId());

			for (int i = 0; i < this._ensoulOptions.length; i++)
			{
				if (this._ensoulOptions[i] != null)
				{
					ps.setInt(2, 1);
					ps.setInt(3, this._ensoulOptions[i].getId());
					ps.setInt(4, i);
					ps.setInt(5, 1);
					ps.setInt(6, this._ensoulOptions[i].getId());
					ps.setInt(7, i);
					ps.execute();
				}
			}

			for (int ix = 0; ix < this._ensoulSpecialOptions.length; ix++)
			{
				if (this._ensoulSpecialOptions[ix] != null)
				{
					ps.setInt(2, 2);
					ps.setInt(3, this._ensoulSpecialOptions[ix].getId());
					ps.setInt(4, ix);
					ps.setInt(5, 2);
					ps.setInt(6, this._ensoulSpecialOptions[ix].getId());
					ps.setInt(7, ix);
					ps.execute();
				}
			}
		}
		catch (Exception var7)
		{
			LOGGER.log(Level.WARNING, "Item could not update item special abilities", var7);
		}
	}

	public void clearEnchantStats()
	{
		Player player = this.asPlayer();
		if (player == null)
		{
			this._enchantOptions.clear();
		}
		else
		{
			for (Options op : this._enchantOptions)
			{
				op.remove(player);
			}

			this._enchantOptions.clear();
		}
	}

	public void applyEnchantStats()
	{
		Player player = this.asPlayer();
		if (this.isEquipped() && player != null && this.getEnchantOptions() != DEFAULT_ENCHANT_OPTIONS)
		{
			for (int id : this.getEnchantOptions())
			{
				Options options = OptionData.getInstance().getOptions(id);
				if (options != null)
				{
					options.apply(player);
					this._enchantOptions.add(options);
				}
				else if (id != 0)
				{
					LOGGER.info("Item applyEnchantStats could not find option " + id + " " + this + " " + player);
				}
			}
		}
	}

	@Override
	public void setHeading(int heading)
	{
	}

	public void stopAllTasks()
	{
		ItemLifeTimeTaskManager.getInstance().remove(this);
		ItemAppearanceTaskManager.getInstance().remove(this);
	}

	public ItemVariables getVariables()
	{
		ItemVariables vars = this.getScript(ItemVariables.class);
		return vars != null ? vars : this.addScript(new ItemVariables(this.getObjectId()));
	}

	public int getVisualId()
	{
		int visualId = this.getVariables().getInt("visualId", 0);
		if (visualId > 0)
		{
			int appearanceStoneId = this.getVariables().getInt("visualAppearanceStoneId", 0);
			if (appearanceStoneId > 0)
			{
				AppearanceStone stone = AppearanceItemData.getInstance().getStone(appearanceStoneId);
				if (stone != null)
				{
					Player player = this.asPlayer();
					if (player != null)
					{
						if ((!stone.getRaces().isEmpty() && !stone.getRaces().contains(player.getRace())) || (!stone.getRacesNot().isEmpty() && stone.getRacesNot().contains(player.getRace())))
						{
							return 0;
						}
					}
				}
			}
		}

		return visualId;
	}

	public void setVisualId(int visualId)
	{
		this.setVisualId(visualId, true);
	}

	public void setVisualId(int visualId, boolean announce)
	{
		this.getVariables().set("visualId", visualId);
		if (visualId == 0)
		{
			ItemAppearanceTaskManager.getInstance().remove(this);
			this.onVisualLifeTimeEnd(announce);
		}
	}

	public int getAppearanceStoneId()
	{
		return this.getVariables().getInt("visualAppearanceStoneId", 0);
	}

	public long getVisualLifeTime()
	{
		return this.getVariables().getLong("visualAppearanceLifetime", 0L);
	}

	public void scheduleVisualLifeTime()
	{
		ItemAppearanceTaskManager.getInstance().remove(this);
		if (this.getVisualLifeTime() > 0L)
		{
			long endTime = this.getVisualLifeTime();
			if (endTime - System.currentTimeMillis() > 0L)
			{
				ItemAppearanceTaskManager.getInstance().add(this, endTime);
			}
			else
			{
				this.onVisualLifeTimeEnd();
			}
		}
	}

	public void onVisualLifeTimeEnd()
	{
		this.onVisualLifeTimeEnd(true);
	}

	public void onVisualLifeTimeEnd(boolean announce)
	{
		this.removeVisualSetSkills();
		ItemVariables vars = this.getVariables();
		vars.remove("visualId");
		vars.remove("visualAppearanceStoneId");
		vars.remove("visualAppearanceLifetime");
		vars.storeMe();
		Player player = this.asPlayer();
		if (player != null)
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(this);
			player.broadcastUserInfo(UserInfoType.APPAREANCE);
			player.sendInventoryUpdate(iu);
			if (announce)
			{
				if (this.isEnchanted())
				{
					player.sendPacket(new SystemMessage(SystemMessageId.S1_S2_THE_ITEM_S_TEMPORARY_APPEARANCE_HAS_BEEN_RESET).addInt(this._enchantLevel).addItemName(this));
				}
				else
				{
					player.sendPacket(new SystemMessage(SystemMessageId.S1_THE_ITEM_S_TEMPORARY_APPEARANCE_HAS_BEEN_RESET).addItemName(this));
				}
			}
		}
	}

	public boolean isBlessed()
	{
		return this._isBlessed;
	}

	public void setBlessed(boolean blessed)
	{
		this._isBlessed = blessed;
		ItemVariables vars = this.getVariables();
		if (!blessed)
		{
			vars.remove("blessed");
		}
		else
		{
			vars.set("blessed", true);
		}

		vars.storeMe();
	}

	public void removeVisualSetSkills()
	{
		if (this.isEquipped())
		{
			int appearanceStoneId = this.getAppearanceStoneId();
			if (appearanceStoneId > 0)
			{
				AppearanceStone stone = AppearanceItemData.getInstance().getStone(appearanceStoneId);
				if (stone != null && stone.getType() == AppearanceType.FIXED)
				{
					Player player = this.asPlayer();
					if (player != null)
					{
						boolean update = false;

						for (ArmorSet armorSet : ArmorSetData.getInstance().getSets(stone.getVisualId()))
						{
							if (armorSet.getPieceCount(player, Item::getVisualId) - 1L < armorSet.getMinimumPieces())
							{
								for (ArmorsetSkillHolder holder : armorSet.getSkills())
								{
									Skill skill = holder.getSkill();
									if (skill != null)
									{
										player.removeSkill(skill, false, skill.isPassive());
										update = true;
									}
								}
							}
						}

						if (update)
						{
							player.sendSkillList();
						}
					}
				}
			}
		}
	}

	public void applyVisualSetSkills()
	{
		if (this.isEquipped())
		{
			int appearanceStoneId = this.getAppearanceStoneId();
			if (appearanceStoneId > 0)
			{
				AppearanceStone stone = AppearanceItemData.getInstance().getStone(appearanceStoneId);
				if (stone != null && stone.getType() == AppearanceType.FIXED)
				{
					Player player = this.asPlayer();
					if (player != null)
					{
						boolean update = false;
						boolean updateTimeStamp = false;

						for (ArmorSet armorSet : ArmorSetData.getInstance().getSets(stone.getVisualId()))
						{
							if (armorSet.getPieceCount(player, Item::getVisualId) >= armorSet.getMinimumPieces())
							{
								for (ArmorsetSkillHolder holder : armorSet.getSkills())
								{
									if (player.getSkillLevel(holder.getSkillId()) < holder.getSkillLevel())
									{
										Skill skill = holder.getSkill();
										if (skill != null && (!skill.isPassive() || skill.checkConditions(SkillConditionScope.PASSIVE, player, player)))
										{
											player.addSkill(skill, false);
											update = true;
											if (skill.isActive())
											{
												if (!player.hasSkillReuse(skill.getReuseHashCode()))
												{
													int equipDelay = this.getEquipReuseDelay();
													if (equipDelay > 0)
													{
														player.addTimeStamp(skill, equipDelay);
														player.disableSkill(skill, equipDelay);
													}
												}

												if (!skill.hasNegativeEffect() && !skill.isTransformation() && PlayerConfig.ARMOR_SET_EQUIP_ACTIVE_SKILL_REUSE > 0 && player.hasEnteredWorld())
												{
													player.addTimeStamp(skill, skill.getReuseDelay() > 0 ? skill.getReuseDelay() : PlayerConfig.ARMOR_SET_EQUIP_ACTIVE_SKILL_REUSE);
												}

												updateTimeStamp = true;
											}
										}
									}
								}
							}
						}

						if (updateTimeStamp)
						{
							player.sendPacket(new SkillCoolTime(player));
						}

						if (update)
						{
							player.sendSkillList();
						}
					}
				}
			}
		}
	}

	public int getTransmogId()
	{
		return !TransmogConfig.ENABLE_TRANSMOG ? 0 : this.getVariables().getInt("transmogId", 0);
	}

	public void setTransmogId(int transmogId)
	{
		this.getVariables().set("transmogId", transmogId);
		this.getVariables().storeMe();
	}

	public void removeTransmog()
	{
		this.getVariables().remove("transmogId");
		this.getVariables().storeMe();
	}

	@Override
	public String toString()
	{
		return StringUtil.concat(this._itemTemplate.toString(), "[", String.valueOf(this.getObjectId()), "]");
	}
}
