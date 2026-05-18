package net.sf.l2jdev.gameserver.model.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.OlympiadConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.custom.CustomDepositableItemsConfig;
import net.sf.l2jdev.gameserver.model.ExtractableProduct;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.commission.CommissionItemType;
import net.sf.l2jdev.gameserver.model.conditions.Condition;
import net.sf.l2jdev.gameserver.model.events.ListenersContainer;
import net.sf.l2jdev.gameserver.model.item.enchant.attribute.AttributeHolder;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.enums.ItemGrade;
import net.sf.l2jdev.gameserver.model.item.enums.ItemSkillType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemSkillHolder;
import net.sf.l2jdev.gameserver.model.item.type.ActionType;
import net.sf.l2jdev.gameserver.model.item.type.CrystalType;
import net.sf.l2jdev.gameserver.model.item.type.EtcItemType;
import net.sf.l2jdev.gameserver.model.item.type.ItemType;
import net.sf.l2jdev.gameserver.model.item.type.MaterialType;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.model.stats.functions.FuncAdd;
import net.sf.l2jdev.gameserver.model.stats.functions.FuncSet;
import net.sf.l2jdev.gameserver.model.stats.functions.FuncTemplate;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public abstract class ItemTemplate extends ListenersContainer
{
	protected static final Logger LOGGER = Logger.getLogger(ItemTemplate.class.getName());
	public static final int TYPE1_WEAPON_RING_EARRING_NECKLACE = 0;
	public static final int TYPE1_SHIELD_ARMOR = 1;
	public static final int TYPE1_ITEM_QUESTITEM_ADENA = 4;
	public static final int TYPE2_WEAPON = 0;
	public static final int TYPE2_SHIELD_ARMOR = 1;
	public static final int TYPE2_ACCESSORY = 2;
	public static final int TYPE2_QUEST = 3;
	public static final int TYPE2_MONEY = 4;
	public static final int TYPE2_OTHER = 5;
	private int _itemId;
	private int _displayId;
	private String _name;
	private String _additionalName;
	private String _icon;
	private int _weight;
	private boolean _stackable;
	private MaterialType _materialType;
	private CrystalType _crystalType;
	private int _equipReuseDelay;
	private int _duration;
	private long _time;
	private int _autoDestroyTime;
	private BodyPart _bodyPart;
	private int _referencePrice;
	private int _crystalCount;
	private boolean _sellable;
	private boolean _dropable;
	private boolean _destroyable;
	private boolean _tradeable;
	private boolean _depositable;
	private boolean _enchantable;
	private int _enchantLimit;
	private int _ensoulNormalSlots;
	private int _ensoulSpecialSlots;
	private boolean _elementable;
	private boolean _questUsableItem;
	private boolean _questItem;
	private boolean _freightable;
	private boolean _allowSelfResurrection;
	private boolean _isOlyRestricted;
	private boolean _isEventRestricted;
	private boolean _forNpc;
	private boolean _common;
	private boolean _heroItem;
	private boolean _pvpItem;
	private boolean _immediateEffect;
	private boolean _exImmediateEffect;
	private int _defaultEnchantLevel;
	private ActionType _defaultAction;
	protected int _type1;
	protected int _type2;
	private Map<AttributeType, AttributeHolder> _elementals = null;
	protected Map<Stat, FuncTemplate> _funcTemplates;
	protected List<Condition> _preConditions;
	private List<ItemSkillHolder> _skills;
	private int _useSkillDisTime;
	protected int _reuseDelay;
	private int _sharedReuseGroup;
	private CommissionItemType _commissionItemType;
	private boolean _isAppearanceable;
	private boolean _isBlessed;
	private boolean _isSealed;
	private int _artifactSlot;
	private int _gearScore;

	protected ItemTemplate(StatSet set)
	{
		this.set(set);
	}

	public void set(StatSet set)
	{
		this._itemId = set.getInt("item_id");
		this._displayId = set.getInt("displayId", this._itemId);
		this._name = set.getString("name");
		this._additionalName = set.getString("additionalName", null);
		this._icon = set.getString("icon", null);
		this._weight = set.getInt("weight", 0);
		this._materialType = set.getEnum("material", MaterialType.class, MaterialType.STEEL);
		this._equipReuseDelay = set.getInt("equip_reuse_delay", 0) * 1000;
		this._duration = set.getInt("duration", -1);
		this._time = set.getInt("time", -1);
		this._autoDestroyTime = set.getInt("auto_destroy_time", -1) * 1000;
		this._bodyPart = BodyPart.fromName(set.getString("bodypart", "none"));
		this._referencePrice = set.getInt("price", 0);
		this._crystalType = set.getEnum("crystal_type", CrystalType.class, CrystalType.NONE);
		this._crystalCount = set.getInt("crystal_count", 0);
		this._stackable = set.getBoolean("is_stackable", false);
		this._sellable = set.getBoolean("is_sellable", true);
		this._dropable = set.getBoolean("is_dropable", true);
		this._destroyable = set.getBoolean("is_destroyable", true);
		this._tradeable = set.getBoolean("is_tradable", true);
		this._questUsableItem = set.getBoolean("is_questusable", false);
		this._questItem = this._questUsableItem || set.getBoolean("is_questitem", false);
		if (CustomDepositableItemsConfig.CUSTOM_DEPOSITABLE_ENABLED)
		{
			this._depositable = !this._questItem || CustomDepositableItemsConfig.CUSTOM_DEPOSITABLE_QUEST_ITEMS;
		}
		else
		{
			this._depositable = set.getBoolean("is_depositable", true);
		}

		this._ensoulNormalSlots = set.getInt("ensoulNormalSlots", 0);
		this._ensoulSpecialSlots = set.getInt("ensoulSpecialSlots", 0);
		this._elementable = set.getBoolean("element_enabled", false);
		this._enchantable = set.getBoolean("enchant_enabled", false);
		this._enchantLimit = set.getInt("enchant_limit", 0);
		this._freightable = set.getBoolean("is_freightable", false);
		this._allowSelfResurrection = set.getBoolean("allow_self_resurrection", false);
		this._isOlyRestricted = set.getBoolean("is_oly_restricted", false);
		this._isEventRestricted = set.getBoolean("is_event_restricted", false);
		this._forNpc = set.getBoolean("for_npc", false);
		this._isAppearanceable = set.getBoolean("isAppearanceable", false);
		this._isBlessed = set.getBoolean("blessed", false);
		this._artifactSlot = set.getInt("artifactSlot", 0);
		this._gearScore = set.getInt("gearScore", 0);
		this._immediateEffect = set.getBoolean("immediate_effect", false);
		this._exImmediateEffect = set.getBoolean("ex_immediate_effect", false);
		this._defaultAction = set.getEnum("default_action", ActionType.class, ActionType.NONE);
		this._useSkillDisTime = set.getInt("useSkillDisTime", 0);
		this._defaultEnchantLevel = set.getInt("enchanted", 0);
		this._reuseDelay = set.getInt("reuse_delay", 0);
		this._sharedReuseGroup = set.getInt("shared_reuse_group", 0);
		this._commissionItemType = set.getEnum("commissionItemType", CommissionItemType.class, CommissionItemType.OTHER_ITEM);
		this._common = this._itemId >= 11605 && this._itemId <= 12361;
		this._heroItem = this._itemId >= 6611 && this._itemId <= 6621 || this._itemId >= 9388 && this._itemId <= 9390 || this._itemId == 6842;
		this._pvpItem = this._itemId >= 10667 && this._itemId <= 10835 || this._itemId >= 12852 && this._itemId <= 12977 || this._itemId >= 14363 && this._itemId <= 14525 || this._itemId == 14528 || this._itemId == 14529 || this._itemId == 14558 || this._itemId >= 15913 && this._itemId <= 16024 || this._itemId >= 16134 && this._itemId <= 16147 || this._itemId == 16149 || this._itemId == 16151 || this._itemId == 16153 || this._itemId == 16155 || this._itemId == 16157 || this._itemId == 16159 || this._itemId >= 16168 && this._itemId <= 16176 || this._itemId >= 16179 && this._itemId <= 16220;
		this._isSealed = set.getBoolean("is_sealed", false);
		if (this._additionalName != null && this._additionalName.equals("Sealed"))
		{
			if (this._tradeable)
			{
				LOGGER.warning("Found tradeable [Sealed] item " + this._itemId);
			}

			if (this._dropable)
			{
				LOGGER.warning("Found dropable [Sealed] item " + this._itemId);
			}

			if (this._sellable)
			{
				LOGGER.warning("Found sellable [Sealed] item " + this._itemId);
			}

			this._isSealed = true;
		}
	}

	public abstract ItemType getItemType();

	public boolean isEtcItem()
	{
		return false;
	}

	public boolean isArmor()
	{
		return false;
	}

	public boolean isWeapon()
	{
		return false;
	}

	public boolean isMagicWeapon()
	{
		return false;
	}

	public int getEquipReuseDelay()
	{
		return this._equipReuseDelay;
	}

	public int getDuration()
	{
		return this._duration;
	}

	public long getTime()
	{
		return this._time;
	}

	public int getAutoDestroyTime()
	{
		return this._autoDestroyTime;
	}

	public int getId()
	{
		return this._itemId;
	}

	public int getDisplayId()
	{
		return this._displayId;
	}

	public abstract int getItemMask();

	public MaterialType getMaterialType()
	{
		return this._materialType;
	}

	public int getType2()
	{
		return this._type2;
	}

	public int getWeight()
	{
		return this._weight;
	}

	public boolean isCrystallizable()
	{
		return this._crystalType != CrystalType.NONE && this._crystalCount > 0;
	}

	public ItemGrade getItemGrade()
	{
		return ItemGrade.valueOf(this._crystalType);
	}

	public CrystalType getCrystalType()
	{
		return this._crystalType;
	}

	public int getCrystalItemId()
	{
		return this._crystalType.getCrystalId();
	}

	public CrystalType getCrystalTypePlus()
	{
		switch (this._crystalType)
		{
			case S80:
			case S84:
				return CrystalType.S;
			case R95:
			case R99:
				return CrystalType.R;
			default:
				return this._crystalType;
		}
	}

	public int getCrystalCount()
	{
		return this._crystalCount;
	}

	public int getCrystalCount(int enchantLevel)
	{
		if (enchantLevel > 3)
		{
			switch (this._type2)
			{
				case 0:
					return this._crystalCount + this._crystalType.getCrystalEnchantBonusWeapon() * (2 * enchantLevel - 3);
				case 1:
				case 2:
					return this._crystalCount + this._crystalType.getCrystalEnchantBonusArmor() * (3 * enchantLevel - 6);
				default:
					return this._crystalCount;
			}
		}
		else if (enchantLevel > 0)
		{
			switch (this._type2)
			{
				case 0:
					return this._crystalCount + this._crystalType.getCrystalEnchantBonusWeapon() * enchantLevel;
				case 1:
				case 2:
					return this._crystalCount + this._crystalType.getCrystalEnchantBonusArmor() * enchantLevel;
				default:
					return this._crystalCount;
			}
		}
		else
		{
			return this._crystalCount;
		}
	}

	public String getName()
	{
		return this._name;
	}

	public String getAdditionalName()
	{
		return this._additionalName;
	}

	public Collection<AttributeHolder> getAttributes()
	{
		return this._elementals != null ? this._elementals.values() : null;
	}

	public AttributeHolder getAttribute(AttributeType type)
	{
		return this._elementals != null ? this._elementals.get(type) : null;
	}

	public void setAttributes(AttributeHolder holder)
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

	public BodyPart getBodyPart()
	{
		return this._bodyPart;
	}

	public BodyPart getPetBodyPart()
	{
		return this._bodyPart == BodyPart.HAIRALL ? BodyPart.HAIR : this._bodyPart;
	}

	public int getType1()
	{
		return this._type1;
	}

	public boolean isStackable()
	{
		return this._stackable;
	}

	public boolean isEquipable()
	{
		return this._bodyPart != BodyPart.NONE && !(this.getItemType() instanceof EtcItemType);
	}

	public int getReferencePrice()
	{
		return this._referencePrice;
	}

	public boolean isSellable()
	{
		return this._sellable;
	}

	public boolean isDropable()
	{
		return this._dropable;
	}

	public boolean isDestroyable()
	{
		return this._destroyable;
	}

	public boolean isTradeable()
	{
		return this._tradeable;
	}

	public boolean isDepositable()
	{
		return this._depositable;
	}

	public boolean isEnchantable()
	{
		return Arrays.binarySearch(PlayerConfig.ENCHANT_BLACKLIST, this._itemId) < 0 && this._enchantable;
	}

	public int getEnchantLimit()
	{
		return this._enchantLimit > 0 ? this._enchantLimit : 0;
	}

	public int getEnsoulSlots()
	{
		return this._ensoulNormalSlots;
	}

	public int getSpecialEnsoulSlots()
	{
		return this._ensoulSpecialSlots;
	}

	public boolean isElementable()
	{
		return this._elementable;
	}

	public boolean isCommon()
	{
		return this._common;
	}

	public boolean isHeroItem()
	{
		return this._heroItem;
	}

	public boolean isPvpItem()
	{
		return this._pvpItem;
	}

	public boolean isPotion()
	{
		return this.getItemType() == EtcItemType.POTION;
	}

	public boolean isElixir()
	{
		return this.getItemType() == EtcItemType.ELIXIR;
	}

	public boolean isScroll()
	{
		return this.getItemType() == EtcItemType.SCROLL;
	}

	public void addFunctionTemplate(FuncTemplate template)
	{
		switch (template.getStat())
		{
			case FIRE_RES:
			case FIRE_POWER:
				this.setAttributes(new AttributeHolder(AttributeType.FIRE, (int) template.getValue()));
				break;
			case WATER_RES:
			case WATER_POWER:
				this.setAttributes(new AttributeHolder(AttributeType.WATER, (int) template.getValue()));
				break;
			case WIND_RES:
			case WIND_POWER:
				this.setAttributes(new AttributeHolder(AttributeType.WIND, (int) template.getValue()));
				break;
			case EARTH_RES:
			case EARTH_POWER:
				this.setAttributes(new AttributeHolder(AttributeType.EARTH, (int) template.getValue()));
				break;
			case HOLY_RES:
			case HOLY_POWER:
				this.setAttributes(new AttributeHolder(AttributeType.HOLY, (int) template.getValue()));
				break;
			case DARK_RES:
			case DARK_POWER:
				this.setAttributes(new AttributeHolder(AttributeType.DARK, (int) template.getValue()));
		}

		if (this._funcTemplates == null)
		{
			this._funcTemplates = new EnumMap<>(Stat.class);
		}

		if (this._funcTemplates.put(template.getStat(), template) != null)
		{
			LOGGER.warning("Item with id " + this._itemId + " has 2 func templates with same stat: " + template.getStat());
		}
	}

	public void attachCondition(Condition c)
	{
		if (this._preConditions == null)
		{
			this._preConditions = new ArrayList<>();
		}

		this._preConditions.add(c);
	}

	public List<Condition> getConditions()
	{
		return this._preConditions;
	}

	public boolean hasSkills()
	{
		return this._skills != null;
	}

	public List<ItemSkillHolder> getAllSkills()
	{
		return this._skills;
	}

	public List<ItemSkillHolder> getSkills(Predicate<ItemSkillHolder> condition)
	{
		if (this._skills == null)
		{
			return null;
		}
		List<ItemSkillHolder> result = new ArrayList<>();

		for (ItemSkillHolder skill : this._skills)
		{
			if (condition.test(skill))
			{
				result.add(skill);
			}
		}

		return result;
	}

	public List<ItemSkillHolder> getSkills(ItemSkillType type)
	{
		if (this._skills == null)
		{
			return null;
		}
		List<ItemSkillHolder> result = new ArrayList<>();

		for (ItemSkillHolder skill : this._skills)
		{
			if (skill.getType() == type)
			{
				result.add(skill);
			}
		}

		return result;
	}

	public void forEachSkill(ItemSkillType type, Consumer<ItemSkillHolder> action)
	{
		if (this._skills != null)
		{
			for (ItemSkillHolder skill : this._skills)
			{
				if (skill.getType() == type)
				{
					action.accept(skill);
				}
			}
		}
	}

	public void addSkill(ItemSkillHolder holder)
	{
		if (this._skills == null)
		{
			this._skills = new ArrayList<>();
		}

		this._skills.add(holder);
	}

	public boolean checkCondition(Creature creature, WorldObject object, boolean sendMessage)
	{
		if (creature.isGM() && !GeneralConfig.GM_ITEM_RESTRICTION)
		{
			return true;
		}
		if (creature.isPlayer())
		{
			if ((this.isOlyRestrictedItem() || this._heroItem) && creature.isPlayer() && creature.asPlayer().isInOlympiadMode())
			{
				if (this.isEquipable())
				{
					creature.sendPacket(SystemMessageId.THE_ITEM_CANNOT_BE_EQUIPPED_IN_THE_OLYMPIAD);
				}
				else
				{
					creature.sendPacket(SystemMessageId.THE_ITEM_CANNOT_BE_USED_IN_THE_OLYMPIAD);
				}

				return false;
			}

			if (this._isEventRestricted && creature.asPlayer().isOnEvent())
			{
				creature.sendMessage("You cannot use this item in the event.");
				return false;
			}
		}

		if (!this.isConditionAttached())
		{
			return true;
		}
		Creature target = object.isCreature() ? object.asCreature() : null;

		for (Condition preCondition : this._preConditions)
		{
			if (preCondition != null && !preCondition.test(creature, target, null, null))
			{
				if (creature.isSummon())
				{
					creature.sendPacket(SystemMessageId.THE_GUARDIAN_CANNOT_USE_THIS_ITEM);
					return false;
				}

				if (sendMessage)
				{
					String msg = preCondition.getMessage();
					int msgId = preCondition.getMessageId();
					if (msg != null)
					{
						creature.sendMessage(msg);
					}
					else if (msgId != 0)
					{
						SystemMessage sm = new SystemMessage(msgId);
						if (preCondition.isAddName())
						{
							sm.addItemName(this._itemId);
						}

						creature.sendPacket(sm);
					}
				}

				return false;
			}
		}

		return true;
	}

	public boolean isConditionAttached()
	{
		return this._preConditions != null && !this._preConditions.isEmpty();
	}

	public boolean isQuestUsableItem()
	{
		return this._questUsableItem;
	}

	public boolean isQuestItem()
	{
		return this._questItem;
	}

	public boolean isFreightable()
	{
		return this._freightable;
	}

	public boolean isAllowSelfResurrection()
	{
		return this._allowSelfResurrection;
	}

	public boolean isOlyRestrictedItem()
	{
		return this._isOlyRestricted || OlympiadConfig.LIST_OLY_RESTRICTED_ITEMS.contains(this._itemId);
	}

	public boolean isEventRestrictedItem()
	{
		return this._isEventRestricted;
	}

	public boolean isForNpc()
	{
		return this._forNpc;
	}

	public boolean isAppearanceable()
	{
		return this._isAppearanceable;
	}

	public boolean isBlessed()
	{
		return this._isBlessed;
	}

	public boolean isSealed()
	{
		return this._isSealed;
	}

	public int getArtifactSlot()
	{
		return this._artifactSlot;
	}

	public int getGearScore()
	{
		return this._gearScore;
	}

	public boolean hasExImmediateEffect()
	{
		return this._exImmediateEffect;
	}

	public boolean hasImmediateEffect()
	{
		return this._immediateEffect;
	}

	public ActionType getDefaultAction()
	{
		return this._defaultAction;
	}

	public int useSkillDisTime()
	{
		return this._useSkillDisTime;
	}

	public int getReuseDelay()
	{
		return this._reuseDelay;
	}

	public int getSharedReuseGroup()
	{
		return this._sharedReuseGroup;
	}

	public CommissionItemType getCommissionItemType()
	{
		return this._commissionItemType;
	}

	public String getIcon()
	{
		return this._icon;
	}

	public int getDefaultEnchantLevel()
	{
		return this._defaultEnchantLevel;
	}

	public boolean isPetItem()
	{
		return this.getItemType() == EtcItemType.PET_COLLAR;
	}

	public void addCapsuledItem(ExtractableProduct extractableProduct)
	{
	}

	public double getStats(Stat stat, double defaultValue)
	{
		if (this._funcTemplates != null)
		{
			FuncTemplate template = this._funcTemplates.get(stat);
			if (template != null && (template.getFunctionClass() == FuncAdd.class || template.getFunctionClass() == FuncSet.class))
			{
				return template.getValue();
			}
		}

		return defaultValue;
	}

	@Override
	public String toString()
	{
		return StringUtil.concat(this._name, "(", String.valueOf(this._itemId), ")");
	}
}
