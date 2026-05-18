package net.sf.l2jdev.gameserver.model.skill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.data.xml.SkillEnchantData;
import net.sf.l2jdev.gameserver.data.xml.SkillTreeData;
import net.sf.l2jdev.gameserver.handler.AffectScopeHandler;
import net.sf.l2jdev.gameserver.handler.IAffectScopeHandler;
import net.sf.l2jdev.gameserver.handler.ITargetTypeHandler;
import net.sf.l2jdev.gameserver.handler.TargetHandler;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.effects.EffectFlag;
import net.sf.l2jdev.gameserver.model.effects.EffectType;
import net.sf.l2jdev.gameserver.model.item.enums.ShotType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.enums.BasicProperty;
import net.sf.l2jdev.gameserver.model.skill.enums.NextActionType;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillFinishType;
import net.sf.l2jdev.gameserver.model.skill.holders.AttachSkillHolder;
import net.sf.l2jdev.gameserver.model.skill.targets.AffectObject;
import net.sf.l2jdev.gameserver.model.skill.targets.AffectScope;
import net.sf.l2jdev.gameserver.model.skill.targets.TargetType;
import net.sf.l2jdev.gameserver.model.stats.BasicPropertyResist;
import net.sf.l2jdev.gameserver.model.stats.Formulas;
import net.sf.l2jdev.gameserver.model.stats.TraitType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class Skill
{
	private static final Logger LOGGER = Logger.getLogger(Skill.class.getName());
	private final int _id;
	private final int _level;
	private final int _subLevel;
	private final int _displayId;
	private final int _displayLevel;
	private final String _name;
	private final SkillOperateType _operateType;
	private final int _magic;
	private final TraitType _traitType;
	private final boolean _staticReuse;
	private final int _mpConsume;
	private final int _mpInitialConsume;
	private final int _mpPerChanneling;
	private final int _hpConsume;
	private final int _itemConsumeCount;
	private final int _itemConsumeId;
	private final int _famePointConsume;
	private final int _clanRepConsume;
	private final int _castRange;
	private final int _effectRange;
	private final boolean _isAbnormalInstant;
	private final int _abnormalLevel;
	private final AbnormalType _abnormalType;
	private final AbnormalType _subordinationAbnormalType;
	private final int _abnormalTime;
	private Set<AbnormalVisualEffect> _abnormalVisualEffects;
	private final boolean _stayAfterDeath;
	private final boolean _isRecoveryHerb;
	private final int _refId;
	private final int _hitTime;
	private final double _hitCancelTime;
	private final int _coolTime;
	private final long _reuseHashCode;
	private final int _reuseDelay;
	private final int _reuseDelayGroup;
	private final int _magicLevel;
	private final int _lvlBonusRate;
	private final int _activateRate;
	private final int _minChance;
	private final int _maxChance;
	private final TargetType _targetType;
	private final AffectScope _affectScope;
	private final AffectObject _affectObject;
	private final int _affectRange;
	private final int[] _fanRange = new int[4];
	private final int[] _affectLimit = new int[3];
	private final int[] _affectHeight = new int[2];
	private final NextActionType _nextAction;
	private final boolean _removedOnAnyActionExceptMove;
	private final boolean _removedOnDamage;
	private final boolean _removedOnUnequipWeapon;
	private final boolean _blockedInOlympiad;
	private final AttributeType _attributeType;
	private final int _attributeValue;
	private final BasicProperty _basicProperty;
	private final int _minPledgeClass;
	private final int _lightSoulMaxConsume;
	private final int _shadowSoulMaxConsume;
	private final int _chargeConsume;
	private final boolean _isTriggeredSkill;
	private final int _effectPoint;
	private final Map<SkillConditionScope, List<ISkillCondition>> _conditionLists = new EnumMap<>(SkillConditionScope.class);
	private final Map<EffectScope, List<AbstractEffect>> _effectLists = new EnumMap<>(EffectScope.class);
	private final boolean _isDebuff;
	private final boolean _isSuicideAttack;
	private final boolean _canBeDispelled;
	private final boolean _excludedFromCheck;
	private final boolean _withoutAction;
	private final String _icon;
	private volatile Byte[] _effectTypes;
	private final int _channelingSkillId;
	private final long _channelingStart;
	private final long _channelingTickInterval;
	private final boolean _isMentoring;
	private final int _doubleCastSkill;
	private final boolean _canDoubleCast;
	private final boolean _canCastWhileDisabled;
	private final boolean _isSharedWithSummon;
	private final boolean _isNecessaryToggle;
	private final boolean _deleteAbnormalOnLeave;
	private final boolean _irreplaceableBuff;
	private final boolean _blockActionUseSkill;
	private final int _toggleGroupId;
	private final int _attachToggleGroupId;
	private final List<AttachSkillHolder> _attachSkills;
	private final Set<AbnormalType> _abnormalResists;
	private final double _magicCriticalRate;
	private final SkillBuffType _buffType;
	private final boolean _displayInList;
	private final boolean _isHidingMessages;
	private final int _alternateEnemySkillId;
	private final int _alternateAllySkillId;

	public Skill(StatSet set)
	{
		this._id = set.getInt(".id");
		this._level = set.getInt(".level");
		this._subLevel = set.getInt(".subLevel", 0);
		this._refId = set.getInt(".referenceId", 0);
		this._displayId = set.getInt(".displayId", this._id);
		this._displayLevel = set.getInt(".displayLevel", this._level);
		this._name = set.getString(".name", "");
		this._operateType = set.getEnum("operateType", SkillOperateType.class);
		this._magic = set.getInt("isMagic", 0);
		this._traitType = set.getEnum("trait", TraitType.class, TraitType.NONE);
		this._staticReuse = set.getBoolean("staticReuse", false);
		this._mpConsume = set.getInt("mpConsume", 0);
		this._mpInitialConsume = set.getInt("mpInitialConsume", 0);
		this._mpPerChanneling = set.getInt("mpPerChanneling", this._mpConsume);
		this._hpConsume = set.getInt("hpConsume", 0);
		this._itemConsumeCount = set.getInt("itemConsumeCount", 0);
		this._itemConsumeId = set.getInt("itemConsumeId", 0);
		this._famePointConsume = set.getInt("famePointConsume", 0);
		this._clanRepConsume = set.getInt("clanRepConsume", 0);
		this._castRange = set.getInt("castRange", -1);
		this._effectRange = set.getInt("effectRange", -1);
		this._abnormalLevel = set.getInt("abnormalLevel", 0);
		this._abnormalType = set.getEnum("abnormalType", AbnormalType.class, AbnormalType.NONE);
		this._subordinationAbnormalType = set.getEnum("subordinationAbnormalType", AbnormalType.class, AbnormalType.NONE);
		int abnormalTime = set.getInt("abnormalTime", 0);
		if (PlayerConfig.ENABLE_MODIFY_SKILL_DURATION && PlayerConfig.SKILL_DURATION_LIST.containsKey(this._id) && this._operateType != SkillOperateType.T)
		{
			if (this._level < 100 || this._level > 140)
			{
				abnormalTime = PlayerConfig.SKILL_DURATION_LIST.get(this._id);
			}
			else if (this._level >= 100 && this._level < 140)
			{
				abnormalTime += PlayerConfig.SKILL_DURATION_LIST.get(this._id);
			}
		}

		this._abnormalTime = abnormalTime;
		this._isAbnormalInstant = set.getBoolean("abnormalInstant", false);
		this.parseAbnormalVisualEffect(set.getString("abnormalVisualEffect", null));
		this._stayAfterDeath = set.getBoolean("stayAfterDeath", false);
		this._hitTime = set.getInt("hitTime", 0);
		this._hitCancelTime = set.getDouble("hitCancelTime", 0.0);
		this._coolTime = set.getInt("coolTime", 0);
		this._isDebuff = set.getBoolean("isDebuff", false);
		this._isRecoveryHerb = set.getBoolean("isRecoveryHerb", false);
		if (PlayerConfig.ENABLE_MODIFY_SKILL_REUSE && PlayerConfig.SKILL_REUSE_LIST.containsKey(this._id))
		{
			this._reuseDelay = PlayerConfig.SKILL_REUSE_LIST.get(this._id);
		}
		else
		{
			this._reuseDelay = set.getInt("reuseDelay", 0);
		}

		this._reuseDelayGroup = set.getInt("reuseDelayGroup", -1);
		this._reuseHashCode = SkillData.getSkillHashCode(this._reuseDelayGroup > 0 ? this._reuseDelayGroup : this._id, this._level, this._subLevel);
		this._targetType = set.getEnum("targetType", TargetType.class, TargetType.SELF);
		this._affectScope = set.getEnum("affectScope", AffectScope.class, AffectScope.SINGLE);
		this._affectObject = set.getEnum("affectObject", AffectObject.class, AffectObject.ALL);
		this._affectRange = set.getInt("affectRange", 0);
		String fanRange = set.getString("fanRange", null);
		if (fanRange != null)
		{
			try
			{
				String[] valuesSplit = fanRange.split(";");
				this._fanRange[0] = Integer.parseInt(valuesSplit[0]);
				this._fanRange[1] = Integer.parseInt(valuesSplit[1]);
				this._fanRange[2] = Integer.parseInt(valuesSplit[2]);
				this._fanRange[3] = Integer.parseInt(valuesSplit[3]);
			}
			catch (Exception var16)
			{
				throw new IllegalArgumentException("SkillId: " + this._id + " invalid fanRange value: " + fanRange + ", \"unk;startDegree;fanAffectRange;fanAffectAngle\" required");
			}
		}

		String affectLimit = set.getString("affectLimit", null);
		if (affectLimit != null)
		{
			try
			{
				String[] valuesSplit = affectLimit.split("-");
				this._affectLimit[0] = Integer.parseInt(valuesSplit[0]);
				this._affectLimit[1] = Integer.parseInt(valuesSplit[1]);
				if (valuesSplit.length > 2)
				{
					this._affectLimit[2] = Integer.parseInt(valuesSplit[2]);
				}
			}
			catch (Exception var15)
			{
				throw new IllegalArgumentException("SkillId: " + this._id + " invalid affectLimit value: " + affectLimit + ", \"minAffected-additionalRandom\" required");
			}
		}

		String affectHeight = set.getString("affectHeight", null);
		if (affectHeight != null)
		{
			try
			{
				String[] valuesSplit = affectHeight.split(";");
				this._affectHeight[0] = Integer.parseInt(valuesSplit[0]);
				this._affectHeight[1] = Integer.parseInt(valuesSplit[1]);
			}
			catch (Exception var14)
			{
				throw new IllegalArgumentException("SkillId: " + this._id + " invalid affectHeight value: " + affectHeight + ", \"minHeight-maxHeight\" required");
			}

			if (this._affectHeight[0] > this._affectHeight[1])
			{
				throw new IllegalArgumentException("SkillId: " + this._id + " invalid affectHeight value: " + affectHeight + ", \"minHeight-maxHeight\" required, minHeight is higher than maxHeight!");
			}
		}

		this._magicLevel = set.getInt("magicLevel", 0);
		this._lvlBonusRate = set.getInt("lvlBonusRate", 0);
		this._activateRate = set.getInt("activateRate", -1);
		this._minChance = set.getInt("minChance", PlayerConfig.MIN_ABNORMAL_STATE_SUCCESS_RATE);
		this._maxChance = set.getInt("maxChance", PlayerConfig.MAX_ABNORMAL_STATE_SUCCESS_RATE);
		this._nextAction = set.getEnum("nextAction", NextActionType.class, NextActionType.NONE);
		this._removedOnAnyActionExceptMove = set.getBoolean("removedOnAnyActionExceptMove", false);
		this._removedOnDamage = set.getBoolean("removedOnDamage", false);
		this._removedOnUnequipWeapon = set.getBoolean("removedOnUnequipWeapon", false);
		this._blockedInOlympiad = set.getBoolean("blockedInOlympiad", false);
		this._attributeType = set.getEnum("attributeType", AttributeType.class, AttributeType.NONE);
		this._attributeValue = set.getInt("attributeValue", 0);
		this._basicProperty = set.getEnum("basicProperty", BasicProperty.class, BasicProperty.NONE);
		this._isSuicideAttack = set.getBoolean("isSuicideAttack", false);
		this._minPledgeClass = set.getInt("minPledgeClass", 0);
		this._lightSoulMaxConsume = set.getInt("lightSoulMaxConsume", 0);
		this._shadowSoulMaxConsume = set.getInt("shadowSoulMaxConsume", 0);
		this._chargeConsume = set.getInt("chargeConsume", 0);
		this._isTriggeredSkill = set.getBoolean("isTriggeredSkill", false);
		this._effectPoint = set.getInt("effectPoint", 0);
		this._canBeDispelled = set.getBoolean("canBeDispelled", true);
		this._excludedFromCheck = set.getBoolean("excludedFromCheck", false);
		this._withoutAction = set.getBoolean("withoutAction", false);
		this._icon = set.getString("icon", "icon.skill0000");
		this._channelingSkillId = set.getInt("channelingSkillId", 0);
		this._channelingTickInterval = (long) set.getFloat("channelingTickInterval", 2000.0F) * 1000L;
		this._channelingStart = (long) (set.getFloat("channelingStart", 0.0F) * 1000.0F);
		this._isMentoring = set.getBoolean("isMentoring", false);
		this._doubleCastSkill = set.getInt("doubleCastSkill", 0);
		this._canDoubleCast = set.getBoolean("canDoubleCast", false);
		this._canCastWhileDisabled = set.getBoolean("canCastWhileDisabled", false);
		this._isSharedWithSummon = set.getBoolean("isSharedWithSummon", true);
		this._isNecessaryToggle = set.getBoolean("isNecessaryToggle", false);
		this._deleteAbnormalOnLeave = set.getBoolean("deleteAbnormalOnLeave", false);
		this._irreplaceableBuff = set.getBoolean("irreplaceableBuff", false);
		this._blockActionUseSkill = set.getBoolean("blockActionUseSkill", false);
		this._toggleGroupId = set.getInt("toggleGroupId", -1);
		this._attachToggleGroupId = set.getInt("attachToggleGroupId", -1);
		this._attachSkills = set.getList("attachSkillList", StatSet.class, Collections.emptyList()).stream().map(AttachSkillHolder::fromStatSet).collect(Collectors.toList());
		String abnormalResist = set.getString("abnormalResists", null);
		if (abnormalResist != null)
		{
			String[] abnormalResistStrings = abnormalResist.split(";");
			if (abnormalResistStrings.length > 0)
			{
				this._abnormalResists = new HashSet<>(abnormalResistStrings.length);

				for (String s : abnormalResistStrings)
				{
					try
					{
						this._abnormalResists.add(AbnormalType.valueOf(s));
					}
					catch (Exception var13)
					{
						LOGGER.log(Level.WARNING, "Skill ID[" + this._id + "] Expected AbnormalType for abnormalResists but found " + s, var13);
					}
				}
			}
			else
			{
				this._abnormalResists = Collections.emptySet();
			}
		}
		else
		{
			this._abnormalResists = Collections.emptySet();
		}

		this._magicCriticalRate = set.getDouble("magicCriticalRate", 0.0);
		this._buffType = this._isTriggeredSkill ? SkillBuffType.TRIGGER : (this.isToggle() ? SkillBuffType.TOGGLE : (this.isDance() ? SkillBuffType.DANCE : (this._isDebuff ? SkillBuffType.DEBUFF : (!this.isHealingPotionSkill() ? SkillBuffType.BUFF : SkillBuffType.NONE))));
		this._displayInList = set.getBoolean("displayInList", true);
		this._isHidingMessages = set.getBoolean("isHidingMessages", false);
		this._alternateEnemySkillId = set.getInt("alternateEnemySkillId", 0);
		this._alternateAllySkillId = set.getInt("alternateAllySkillId", 0);
	}

	public TraitType getTraitType()
	{
		return this._traitType;
	}

	public AttributeType getAttributeType()
	{
		return this._attributeType;
	}

	public int getAttributeValue()
	{
		return this._attributeValue;
	}

	public boolean isAOE()
	{
		switch (this._affectScope)
		{
			case FAN:
			case FAN_PB:
			case POINT_BLANK:
			case RANGE:
			case RING_RANGE:
			case SQUARE:
			case SQUARE_PB:
				return true;
			default:
				return false;
		}
	}

	public boolean isSuicideAttack()
	{
		return this._isSuicideAttack;
	}

	public boolean allowOnTransform()
	{
		return this.isPassive();
	}

	public boolean isAbnormalInstant()
	{
		return this._isAbnormalInstant;
	}

	public AbnormalType getAbnormalType()
	{
		return this._abnormalType;
	}

	public AbnormalType getSubordinationAbnormalType()
	{
		return this._subordinationAbnormalType;
	}

	public int getAbnormalLevel()
	{
		return this._abnormalLevel;
	}

	public int getAbnormalTime()
	{
		return this._abnormalTime;
	}

	public Set<AbnormalVisualEffect> getAbnormalVisualEffects()
	{
		return this._abnormalVisualEffects != null ? this._abnormalVisualEffects : Collections.emptySet();
	}

	public boolean hasAbnormalVisualEffects()
	{
		return this._abnormalVisualEffects != null && !this._abnormalVisualEffects.isEmpty();
	}

	public int getMagicLevel()
	{
		return this._magicLevel;
	}

	public int getLvlBonusRate()
	{
		return this._lvlBonusRate;
	}

	public int getActivateRate()
	{
		return this._activateRate;
	}

	public int getMinChance()
	{
		return this._minChance;
	}

	public int getMaxChance()
	{
		return this._maxChance;
	}

	public boolean isRemovedOnAnyActionExceptMove()
	{
		return this._removedOnAnyActionExceptMove;
	}

	public boolean isRemovedOnDamage()
	{
		return this._removedOnDamage;
	}

	public boolean isRemovedOnUnequipWeapon()
	{
		return this._removedOnUnequipWeapon;
	}

	public boolean isBlockedInOlympiad()
	{
		return this._blockedInOlympiad;
	}

	public int getChannelingSkillId()
	{
		return this._channelingSkillId;
	}

	public NextActionType getNextAction()
	{
		return this._nextAction;
	}

	public int getCastRange()
	{
		return this._castRange;
	}

	public int getEffectRange()
	{
		return this._effectRange;
	}

	public int getHpConsume()
	{
		return this._hpConsume;
	}

	public int getId()
	{
		return this._id;
	}

	public boolean isDebuff()
	{
		return this._isDebuff;
	}

	public boolean isRecoveryHerb()
	{
		return this._isRecoveryHerb;
	}

	public int getDisplayId()
	{
		return this._displayId;
	}

	public int getDisplayLevel()
	{
		return this._displayLevel;
	}

	public BasicProperty getBasicProperty()
	{
		return this._basicProperty;
	}

	public int getItemConsumeCount()
	{
		return this._itemConsumeCount;
	}

	public int getItemConsumeId()
	{
		return this._itemConsumeId;
	}

	public int getFamePointConsume()
	{
		return this._famePointConsume;
	}

	public int getClanRepConsume()
	{
		return this._clanRepConsume;
	}

	public int getLevel()
	{
		return this._level;
	}

	public int getSubLevel()
	{
		return this._subLevel;
	}

	public int getMagicType()
	{
		return this._magic;
	}

	public boolean isPhysical()
	{
		return this._magic == 0;
	}

	public boolean isMagic()
	{
		return this._magic == 1;
	}

	public boolean isStatic()
	{
		return this._magic == 2;
	}

	public boolean isDance()
	{
		return this._magic == 3;
	}

	public boolean isStaticReuse()
	{
		return this._staticReuse;
	}

	public int getMpConsume()
	{
		return this._mpConsume;
	}

	public int getMpInitialConsume()
	{
		return this._mpInitialConsume;
	}

	public int getMpPerChanneling()
	{
		return this._mpPerChanneling;
	}

	public String getName()
	{
		return this._name;
	}

	public int getReuseDelay()
	{
		return this._reuseDelay;
	}

	public int getReuseDelayGroup()
	{
		return this._reuseDelayGroup;
	}

	public long getReuseHashCode()
	{
		return this._reuseHashCode;
	}

	public int getHitTime()
	{
		return this._hitTime;
	}

	public double getHitCancelTime()
	{
		return this._hitCancelTime;
	}

	public int getCoolTime()
	{
		return this._coolTime;
	}

	public TargetType getTargetType()
	{
		return this._targetType;
	}

	public AffectScope getAffectScope()
	{
		return this._affectScope;
	}

	public AffectObject getAffectObject()
	{
		return this._affectObject;
	}

	public int getAffectRange()
	{
		return this._affectRange;
	}

	public int[] getFanRange()
	{
		return this._fanRange;
	}

	public int getAffectLimit()
	{
		return this._affectLimit[0] <= 0 && this._affectLimit[1] <= 0 ? 0 : this._affectLimit[0] + Rnd.get(this._affectLimit[1]);
	}

	public int getAffectHeightMin()
	{
		return this._affectHeight[0];
	}

	public int getAffectHeightMax()
	{
		return this._affectHeight[1];
	}

	public boolean isActive()
	{
		return this._operateType.isActive();
	}

	public boolean isPassive()
	{
		return this._operateType.isPassive();
	}

	public boolean isToggle()
	{
		return this._operateType.isToggle();
	}

	public boolean isAura()
	{
		return this._operateType.isAura();
	}

	public boolean isHidingMessages()
	{
		return this._isHidingMessages || this._operateType.isHidingMessages();
	}

	public boolean isNotBroadcastable()
	{
		return this._operateType.isNotBroadcastable();
	}

	public boolean isContinuous()
	{
		return this._operateType.isContinuous() || this.isSelfContinuous();
	}

	public boolean isFlyType()
	{
		return this._operateType.isFlyType();
	}

	public boolean isSelfContinuous()
	{
		return this._operateType.isSelfContinuous();
	}

	public boolean isChanneling()
	{
		return this._operateType.isChanneling();
	}

	public boolean isTriggeredSkill()
	{
		return this._isTriggeredSkill;
	}

	public boolean isSynergySkill()
	{
		return this._operateType.isSynergy();
	}

	public SkillOperateType getOperateType()
	{
		return this._operateType;
	}

	public boolean isTransformation()
	{
		return this._abnormalType == AbnormalType.TRANSFORM || this._abnormalType == AbnormalType.CHANGEBODY;
	}

	public int getEffectPoint()
	{
		return this._effectPoint;
	}

	public boolean useSoulShot()
	{
		return this.hasEffectType(EffectType.PHYSICAL_ATTACK, EffectType.PHYSICAL_ATTACK_HP_LINK);
	}

	public boolean useSpiritShot()
	{
		return this._magic == 1;
	}

	public boolean useFishShot()
	{
		return this.hasEffectType(EffectType.FISHING);
	}

	public int getMinPledgeClass()
	{
		return this._minPledgeClass;
	}

	public boolean isHeroSkill()
	{
		return SkillTreeData.getInstance().isHeroSkill(this._id, this._level);
	}

	public boolean isGMSkill()
	{
		return SkillTreeData.getInstance().isGMSkill(this._id, this._level);
	}

	public boolean is7Signs()
	{
		return this._id > 4360 && this._id < 4367;
	}

	public boolean isHealingPotionSkill()
	{
		return this._abnormalType == AbnormalType.HP_RECOVER;
	}

	public int getMaxLightSoulConsumeCount()
	{
		return this._lightSoulMaxConsume;
	}

	public int getMaxShadowSoulConsumeCount()
	{
		return this._shadowSoulMaxConsume;
	}

	public int getChargeConsumeCount()
	{
		return this._chargeConsume;
	}

	public boolean isStayAfterDeath()
	{
		return this._stayAfterDeath || this._irreplaceableBuff || this._isNecessaryToggle;
	}

	public boolean hasNegativeEffect()
	{
		return this._effectPoint < 0;
	}

	public int getAlternateEnemySkillId()
	{
		return this._alternateEnemySkillId;
	}

	public int getAlternateAllySkillId()
	{
		return this._alternateAllySkillId;
	}

	public boolean checkCondition(Creature creature, WorldObject object, boolean sendMessage)
	{
		if (!creature.isFakePlayer() && (!creature.isGM() || GeneralConfig.GM_SKILL_RESTRICTION))
		{
			if (creature.isPlayer() && creature.asPlayer().isMounted() && this.hasNegativeEffect() && !MountEnabledSkillList.contains(this._id))
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
				sm.addSkillName(this._id);
				creature.sendPacket(sm);
				return false;
			}
			else if (this.checkConditions(SkillConditionScope.GENERAL, creature, object) && this.checkConditions(SkillConditionScope.TARGET, creature, object))
			{
				return true;
			}
			else
			{
				if (sendMessage && (creature != object || !this.hasNegativeEffect()))
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
					sm.addSkillName(this._id);
					creature.sendPacket(sm);
				}

				return false;
			}
		}
		return true;
	}

	public WorldObject getTarget(Creature creature, boolean forceUse, boolean dontMove, boolean sendMessage)
	{
		return this.getTarget(creature, creature.getTarget(), forceUse, dontMove, sendMessage);
	}

	public WorldObject getTarget(Creature creature, WorldObject seletedTarget, boolean forceUse, boolean dontMove, boolean sendMessage)
	{
		ITargetTypeHandler handler = TargetHandler.getInstance().getHandler(this.getTargetType());
		if (handler != null)
		{
			try
			{
				return handler.getTarget(creature, seletedTarget, this, forceUse, dontMove, sendMessage);
			}
			catch (Exception var8)
			{
				LOGGER.log(Level.WARNING, "Exception in Skill.getTarget(): " + var8.getMessage(), var8);
			}
		}

		creature.sendMessage("Target type of skill " + this + " is not currently handled.");
		return null;
	}

	public List<WorldObject> getTargetsAffected(Creature creature, WorldObject target)
	{
		if (target == null)
		{
			return null;
		}
		IAffectScopeHandler handler = AffectScopeHandler.getInstance().getHandler(this.getAffectScope());
		if (handler != null)
		{
			try
			{
				List<WorldObject> result = new LinkedList<>();
				handler.forEachAffected(creature, target, this, result::add);
				if (creature != null && creature.isMonster() && !this.hasNegativeEffect())
				{
					result.removeIf(wo -> wo.isPlayable());
				}

				return result;
			}
			catch (Exception var5)
			{
				LOGGER.log(Level.WARNING, "Exception in Skill.getTargetsAffected(): " + var5.getMessage(), var5);
			}
		}

		if (creature != null)
		{
			creature.sendMessage("Target affect scope of skill " + this + " is not currently handled.");
		}

		return null;
	}

	public void forEachTargetAffected(Creature creature, WorldObject target, Consumer<? super WorldObject> action)
	{
		if (target != null)
		{
			IAffectScopeHandler handler = AffectScopeHandler.getInstance().getHandler(this.getAffectScope());
			if (handler != null)
			{
				try
				{
					handler.forEachAffected(creature, target, this, action);
				}
				catch (Exception var6)
				{
					LOGGER.log(Level.WARNING, "Exception in Skill.forEachTargetAffected(): " + var6.getMessage(), var6);
				}
			}
			else
			{
				creature.sendMessage("Target affect scope of skill " + this + " is not currently handled.");
			}
		}
	}

	public void addEffect(EffectScope effectScope, AbstractEffect effect)
	{
		this._effectLists.computeIfAbsent(effectScope, _ -> new ArrayList<>()).add(effect);
	}

	public List<AbstractEffect> getEffects(EffectScope effectScope)
	{
		return this._effectLists.get(effectScope);
	}

	public boolean hasEffects(EffectScope effectScope)
	{
		List<AbstractEffect> effects = this._effectLists.get(effectScope);
		return effects != null && !effects.isEmpty();
	}

	public void applyEffectScope(EffectScope effectScope, BuffInfo info, boolean applyInstantEffects, boolean addContinuousEffects)
	{
		if (effectScope != null && this.hasEffects(effectScope))
		{
			for (AbstractEffect effect : this.getEffects(effectScope))
			{
				if (effect.isInstant())
				{
					if (applyInstantEffects && effect.calcSuccess(info.getEffector(), info.getEffected(), this))
					{
						effect.instant(info.getEffector(), info.getEffected(), this, info.getItem());
					}
				}
				else if (addContinuousEffects)
				{
					if (applyInstantEffects)
					{
						effect.continuousInstant(info.getEffector(), info.getEffected(), this, info.getItem());
					}

					if (effect.canStart(info.getEffector(), info.getEffected(), this))
					{
						info.addEffect(effect);
					}

					if (info.getEffected().isPlayer() && !this.hasNegativeEffect())
					{
						info.getEffected().asPlayer().getStatus().startHpMpRegeneration();
					}
				}
			}
		}
	}

	public void applyEffects(Creature effector, Creature effected)
	{
		this.applyEffects(effector, effected, false, false, true, 0, null);
	}

	public void applyEffects(Creature effector, Creature effected, Item item)
	{
		this.applyEffects(effector, effected, false, false, true, 0, item);
	}

	public void applyEffects(Creature effector, Creature effected, boolean instant, int abnormalTime)
	{
		this.applyEffects(effector, effected, false, false, instant, abnormalTime, null);
	}

	public void applyEffects(Creature effector, Creature effected, boolean self, boolean passive, boolean instant, int abnormalTime, Item item)
	{
		if (effected != null)
		{
			if (!effected.isIgnoringSkillEffects(this._id, this._level))
			{
				int skillId = effector.isPlayable() ? effector.asPlayable().getReplacementSkill(this._id) : this._id;
				boolean addContinuousEffects = !passive && (this._operateType.isToggle() || this._operateType.isContinuous() && Formulas.calcEffectSuccess(effector, effected, this));
				if (!self && !passive)
				{
					BuffInfo info;
					if (skillId != this._id)
					{
						info = new BuffInfo(effector, effected, SkillData.getInstance().getSkill(skillId, this.getLevel(), this.getSubLevel()), !instant, item, null);
					}
					else
					{
						info = new BuffInfo(effector, effected, this, !instant, item, null);
					}

					if (addContinuousEffects && abnormalTime > 0)
					{
						info.setAbnormalTime(abnormalTime);
					}

					this.applyEffectScope(EffectScope.GENERAL, info, instant, addContinuousEffects);
					EffectScope pvpOrPveEffectScope = effector.isPlayable() && effected.isAttackable() ? EffectScope.PVE : (effector.isPlayable() && effected.isPlayable() ? EffectScope.PVP : null);
					this.applyEffectScope(pvpOrPveEffectScope, info, instant, addContinuousEffects);
					if (addContinuousEffects)
					{
						BuffInfo existingInfo = this._operateType.isAura() ? effected.getEffectList().getBuffInfoBySkillId(skillId) : null;
						if (existingInfo != null)
						{
							existingInfo.resetAbnormalTime(info.getAbnormalTime());
						}
						else
						{
							effected.getEffectList().add(info);
						}

						if (this._isDebuff && this._basicProperty != BasicProperty.NONE && effected.hasBasicPropertyResist())
						{
							BasicPropertyResist resist = effected.getBasicPropertyResist(this._basicProperty);
							resist.increaseResistLevel();
						}
					}

					if (this._isSharedWithSummon && effected.isPlayer() && !this.isTransformation() && (addContinuousEffects && this.isContinuous() && !this._isDebuff || this._isRecoveryHerb))
					{
						if (effected.hasServitors())
						{
							effected.getServitors().values().forEach(s -> this.applyEffects(effector, s, this._isRecoveryHerb, 0));
						}

						if (effected.hasPet())
						{
							this.applyEffects(effector, effector.getPet(), this._isRecoveryHerb, 0);
						}
					}
				}

				if (self)
				{
					addContinuousEffects = !passive && (this._operateType.isToggle() || this._operateType.isSelfContinuous() && Formulas.calcEffectSuccess(effector, effector, this));
					BuffInfo infox;
					if (skillId != this._id)
					{
						infox = new BuffInfo(effector, effected, SkillData.getInstance().getSkill(skillId, this.getLevel(), this.getSubLevel()), !instant, item, null);
					}
					else
					{
						infox = new BuffInfo(effector, effector, this, !instant, item, null);
					}

					if (addContinuousEffects && abnormalTime > 0)
					{
						infox.setAbnormalTime(abnormalTime);
					}

					this.applyEffectScope(EffectScope.SELF, infox, instant, addContinuousEffects);
					if (addContinuousEffects)
					{
						BuffInfo existingInfox = this._operateType.isAura() ? effector.getEffectList().getBuffInfoBySkillId(skillId) : null;
						if (existingInfox != null)
						{
							existingInfox.resetAbnormalTime(infox.getAbnormalTime());
						}
						else
						{
							infox.getEffector().getEffectList().add(infox);
						}
					}

					if (addContinuousEffects && this._isSharedWithSummon && infox.getEffected().isPlayer() && this.isContinuous() && !this._isDebuff && infox.getEffected().hasServitors())
					{
						infox.getEffected().getServitors().values().forEach(s -> this.applyEffects(effector, s, false, 0));
					}
				}

				if (passive)
				{
					BuffInfo infoxx;
					if (skillId != this._id)
					{
						infoxx = new BuffInfo(effector, effected, SkillData.getInstance().getSkill(skillId, this.getLevel(), this.getSubLevel()), true, item, null);
					}
					else
					{
						infoxx = new BuffInfo(effector, effector, this, true, item, null);
					}

					this.applyEffectScope(EffectScope.GENERAL, infoxx, false, true);
					effector.getEffectList().add(infoxx);
				}
			}
		}
	}

	public void applyChannelingEffects(Creature effector, Creature effected)
	{
		if (effected != null)
		{
			BuffInfo info = new BuffInfo(effector, effected, this, false, null, null);
			this.applyEffectScope(EffectScope.CHANNELING, info, true, true);
		}
	}

	public void activateSkill(Creature caster, Collection<WorldObject> targets)
	{
		this.activateSkill(caster, null, targets);
	}

	public void activateSkill(Creature caster, Item item, Collection<WorldObject> targets)
	{
		if (caster.isPlayer() && !this.isContinuous() && !this.isDebuff() && !this.isPassive())
		{
			caster.asPlayer().setLastSkillUsed(this);
		}

		for (WorldObject target : targets)
		{
			if (target.isCreature() && (!target.isSummon() || this.isSharedWithSummon()))
			{
				Creature skillTarget = target.asCreature();
				if (!Formulas.calcBuffDebuffReflection(skillTarget, this))
				{
					this.applyEffects(caster, skillTarget, item);
				}
				else
				{
					this.applyEffects(skillTarget, caster, false, 0);
					BuffInfo info = new BuffInfo(caster, skillTarget, this, false, item, null);
					this.applyEffectScope(EffectScope.GENERAL, info, true, false);
					EffectScope pvpOrPveEffectScope = caster.isPlayable() && skillTarget.isAttackable() ? EffectScope.PVE : (caster.isPlayable() && skillTarget.isPlayable() ? EffectScope.PVP : null);
					this.applyEffectScope(pvpOrPveEffectScope, info, true, false);
				}
			}
		}

		if (this.hasEffects(EffectScope.SELF))
		{
			if (caster.isAffectedBySkill(this._id))
			{
				caster.stopSkillEffects(SkillFinishType.REMOVED, this._id);
			}

			this.applyEffects(caster, caster, true, false, true, 0, item);
		}

		if (!caster.isCubic())
		{
			if (this.useSpiritShot())
			{
				caster.unchargeShot(caster.isChargedShot(ShotType.BLESSED_SPIRITSHOTS) ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS);
			}
			else if (this.useSoulShot())
			{
				caster.unchargeShot(caster.isChargedShot(ShotType.BLESSED_SOULSHOTS) ? ShotType.BLESSED_SOULSHOTS : ShotType.SOULSHOTS);
			}
		}

		if (this._isSuicideAttack)
		{
			caster.doDie(caster);
		}
	}

	public void activateSkill(Creature caster, WorldObject target)
	{
		this.activateSkill(caster, target, null);
	}

	public void activateSkill(Creature caster, WorldObject target, Item item)
	{
		if (caster.isPlayer() && !this.isContinuous() && !this.isDebuff() && !this.isPassive())
		{
			caster.asPlayer().setLastSkillUsed(this);
		}

		if (target.isCreature() && (!target.isSummon() || this.isSharedWithSummon()))
		{
			Creature skillTarget = target.asCreature();
			if (Formulas.calcBuffDebuffReflection(skillTarget, this))
			{
				this.applyEffects(skillTarget, caster, false, 0);
				BuffInfo info = new BuffInfo(caster, skillTarget, this, false, item, null);
				this.applyEffectScope(EffectScope.GENERAL, info, true, false);
				EffectScope pvpOrPveEffectScope = caster.isPlayable() && skillTarget.isAttackable() ? EffectScope.PVE : (caster.isPlayable() && skillTarget.isPlayable() ? EffectScope.PVP : null);
				this.applyEffectScope(pvpOrPveEffectScope, info, true, false);
			}
			else
			{
				this.applyEffects(caster, skillTarget, item);
			}
		}

		if (this.hasEffects(EffectScope.SELF))
		{
			if (caster.isAffectedBySkill(this._id))
			{
				caster.stopSkillEffects(SkillFinishType.REMOVED, this._id);
			}

			this.applyEffects(caster, caster, true, false, true, 0, item);
		}

		if (!caster.isCubic())
		{
			if (this.useSpiritShot())
			{
				caster.unchargeShot(caster.isChargedShot(ShotType.BLESSED_SPIRITSHOTS) ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS);
			}
			else if (this.useSoulShot())
			{
				caster.unchargeShot(caster.isChargedShot(ShotType.BLESSED_SOULSHOTS) ? ShotType.BLESSED_SOULSHOTS : ShotType.SOULSHOTS);
			}
		}

		if (this._isSuicideAttack)
		{
			caster.doDie(caster);
		}
	}

	public void addCondition(SkillConditionScope skillConditionScope, ISkillCondition skillCondition)
	{
		this._conditionLists.computeIfAbsent(skillConditionScope, _ -> new ArrayList<>()).add(skillCondition);
	}

	public boolean checkConditions(SkillConditionScope skillConditionScope, Creature caster, WorldObject target)
	{
		List<ISkillCondition> conditions = this._conditionLists.get(skillConditionScope);
		if (conditions == null)
		{
			return true;
		}
		for (ISkillCondition condition : conditions)
		{
			if (!condition.canUse(caster, this, target))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString()
	{
		return "Skill " + this._name + "(" + this._id + "," + this._level + "," + this._subLevel + ")";
	}

	public int getReferenceItemId()
	{
		return this._refId;
	}

	public boolean canBeDispelled()
	{
		return this._canBeDispelled;
	}

	public boolean canBeStolen()
	{
		return !this.isPassive() && !this.isToggle() && !this._isDebuff && !this._irreplaceableBuff && !this.isHeroSkill() && !this.isGMSkill() && (!this.isStatic() || this.getId() == CommonSkill.CARAVANS_SECRET_MEDICINE.getId()) && this._canBeDispelled;
	}

	public boolean isClanSkill()
	{
		return SkillTreeData.getInstance().isClanSkill(this._id, this._level);
	}

	public boolean isExcludedFromCheck()
	{
		return this._excludedFromCheck;
	}

	public boolean isWithoutAction()
	{
		return this._withoutAction;
	}

	private void parseAbnormalVisualEffect(String abnormalVisualEffects)
	{
		if (abnormalVisualEffects != null)
		{
			String[] data = abnormalVisualEffects.split(";");
			Set<AbnormalVisualEffect> aves = new HashSet<>(1);

			for (String aveString : data)
			{
				AbnormalVisualEffect ave = AbnormalVisualEffect.findByName(aveString);
				if (ave != null)
				{
					aves.add(ave);
				}
				else
				{
					LOGGER.warning("Invalid AbnormalVisualEffect(" + this + ") found for Skill(" + aveString + ")");
				}
			}

			if (!aves.isEmpty())
			{
				this._abnormalVisualEffects = aves;
			}
		}
	}

	public boolean hasEffectType(EffectType effectType, EffectType... effectTypes)
	{
		if (this._effectTypes == null)
		{
			synchronized (this)
			{
				if (this._effectTypes == null)
				{
					Set<Byte> effectTypesSet = new HashSet<>();

					for (List<AbstractEffect> effectList : this._effectLists.values())
					{
						if (effectList != null)
						{
							for (AbstractEffect effect : effectList)
							{
								effectTypesSet.add((byte) effect.getEffectType().ordinal());
							}
						}
					}

					Byte[] effectTypesArray = effectTypesSet.toArray(new Byte[effectTypesSet.size()]);
					Arrays.sort(effectTypesArray);
					this._effectTypes = effectTypesArray;
				}
			}
		}

		if (Arrays.binarySearch(this._effectTypes, Byte.valueOf((byte) effectType.ordinal())) >= 0)
		{
			return true;
		}
		for (EffectType type : effectTypes)
		{
			if (Arrays.binarySearch(this._effectTypes, Byte.valueOf((byte) type.ordinal())) >= 0)
			{
				return true;
			}
		}

		return false;
	}

	public boolean hasEffectType(EffectScope effectScope, EffectType effectType, EffectType... effectTypes)
	{
		if (this.hasEffects(effectScope))
		{
			return false;
		}
		for (AbstractEffect effect : this._effectLists.get(effectScope))
		{
			if (effectType == effect.getEffectType())
			{
				return true;
			}

			for (EffectType type : effectTypes)
			{
				if (type == effect.getEffectType())
				{
					return true;
				}
			}
		}

		return false;
	}

	public String getIcon()
	{
		return this._icon;
	}

	public long getChannelingTickInterval()
	{
		return this._channelingTickInterval;
	}

	public long getChannelingTickInitialDelay()
	{
		return this._channelingStart;
	}

	public boolean isMentoring()
	{
		return this._isMentoring;
	}

	public Skill getAttachedSkill(Creature creature)
	{
		if (this._doubleCastSkill > 0 && creature.isAffected(EffectFlag.DOUBLE_CAST))
		{
			return SkillData.getInstance().getSkill(this.getDoubleCastSkill(), this.getLevel(), this.getSubLevel());
		}
		else if (this._attachToggleGroupId > 0 && this._attachSkills != null)
		{
			int toggleSkillId = 0;

			for (BuffInfo info : creature.getEffectList().getEffects())
			{
				if (info.getSkill().getToggleGroupId() == this._attachToggleGroupId)
				{
					toggleSkillId = info.getSkill().getId();
					break;
				}
			}

			if (toggleSkillId == 0)
			{
				return null;
			}
			AttachSkillHolder attachedSkill = null;

			for (AttachSkillHolder ash : this._attachSkills)
			{
				if (ash.getRequiredSkillId() == toggleSkillId)
				{
					attachedSkill = ash;
					break;
				}
			}

			return attachedSkill == null ? null : SkillData.getInstance().getSkill(attachedSkill.getSkillId(), Math.min(SkillData.getInstance().getMaxLevel(attachedSkill.getSkillId()), this._level), this._subLevel);
		}
		else
		{
			return null;
		}
	}

	public boolean canDoubleCast()
	{
		return this._canDoubleCast;
	}

	public int getDoubleCastSkill()
	{
		return this._doubleCastSkill;
	}

	public boolean canCastWhileDisabled()
	{
		return this._canCastWhileDisabled;
	}

	public boolean isSharedWithSummon()
	{
		return this._isSharedWithSummon;
	}

	public boolean isNecessaryToggle()
	{
		return this._isNecessaryToggle;
	}

	public boolean isDeleteAbnormalOnLeave()
	{
		return this._deleteAbnormalOnLeave;
	}

	public boolean isIrreplaceableBuff()
	{
		return this._irreplaceableBuff;
	}

	public boolean isDisplayInList()
	{
		return this._displayInList;
	}

	public boolean isBlockActionUseSkill()
	{
		return this._blockActionUseSkill;
	}

	public int getToggleGroupId()
	{
		return this._toggleGroupId;
	}

	public int getAttachToggleGroupId()
	{
		return this._attachToggleGroupId;
	}

	public List<AttachSkillHolder> getAttachSkills()
	{
		return this._attachSkills;
	}

	public Set<AbnormalType> getAbnormalResists()
	{
		return this._abnormalResists;
	}

	public double getMagicCriticalRate()
	{
		return this._magicCriticalRate;
	}

	public SkillBuffType getBuffType()
	{
		return this._buffType;
	}

	public boolean isEnchantable()
	{
		return SkillEnchantData.getInstance().getSkillEnchant(this.getId()) != null;
	}
}
