package net.sf.l2jdev.gameserver.model.item;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcSkillSee;
import net.sf.l2jdev.gameserver.model.item.enums.ItemSkillType;
import net.sf.l2jdev.gameserver.model.item.type.WeaponType;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.stats.Formulas;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class Weapon extends ItemTemplate
{
	private WeaponType _type;
	private boolean _isMagicWeapon;
	private boolean _isImmortalityWeapon;
	private int _soulShotCount;
	private int _spiritShotCount;
	private int _mpConsume;
	private int _baseAttackRange;
	private int _baseAttackRadius;
	private int _baseAttackAngle;
	private int _changeWeaponId;
	private int _reducedSoulshot;
	private int _reducedSoulshotChance;
	private int _reducedMpConsume;
	private int _reducedMpConsumeChance;
	private boolean _isForceEquip;
	private boolean _isAttackWeapon;
	private boolean _useWeaponSkillsOnly;

	public Weapon(StatSet set)
	{
		super(set);
	}

	@Override
	public void set(StatSet set)
	{
		super.set(set);
		this._type = WeaponType.valueOf(set.getString("weapon_type", "none").toUpperCase());
		this._type1 = 0;
		this._type2 = 0;
		this._isMagicWeapon = set.getBoolean("is_magic_weapon", false);
		this._isImmortalityWeapon = set.getBoolean("is_immortality_weapon", false);
		this._soulShotCount = set.getInt("soulshots", 0);
		this._spiritShotCount = set.getInt("spiritshots", 0);
		this._mpConsume = set.getInt("mp_consume", 0);
		this._baseAttackRange = set.getInt("attack_range", 40);
		String[] damageRange = set.getString("damage_range", "").split(";");
		if (damageRange.length > 1 && StringUtil.isNumeric(damageRange[2]) && StringUtil.isNumeric(damageRange[3]))
		{
			this._baseAttackRadius = Integer.parseInt(damageRange[2]);
			this._baseAttackAngle = Integer.parseInt(damageRange[3]);
		}
		else
		{
			this._baseAttackRadius = 40;
			this._baseAttackAngle = 0;
		}

		String[] reducedSoulshots = set.getString("reduced_soulshot", "").split(",");
		this._reducedSoulshotChance = reducedSoulshots.length == 2 ? Integer.parseInt(reducedSoulshots[0]) : 0;
		this._reducedSoulshot = reducedSoulshots.length == 2 ? Integer.parseInt(reducedSoulshots[1]) : 0;
		String[] reducedMpConsume = set.getString("reduced_mp_consume", "").split(",");
		this._reducedMpConsumeChance = reducedMpConsume.length == 2 ? Integer.parseInt(reducedMpConsume[0]) : 0;
		this._reducedMpConsume = reducedMpConsume.length == 2 ? Integer.parseInt(reducedMpConsume[1]) : 0;
		this._changeWeaponId = set.getInt("change_weaponId", 0);
		this._isForceEquip = set.getBoolean("isForceEquip", false);
		this._isAttackWeapon = set.getBoolean("isAttackWeapon", true);
		this._useWeaponSkillsOnly = set.getBoolean("useWeaponSkillsOnly", false);
		if (this._reuseDelay == 0 && this._type.isRanged())
		{
			this._reuseDelay = 1500;
		}
	}

	@Override
	public WeaponType getItemType()
	{
		return this._type;
	}

	@Override
	public int getItemMask()
	{
		return this._type.mask();
	}

	@Override
	public boolean isWeapon()
	{
		return true;
	}

	@Override
	public boolean isMagicWeapon()
	{
		return this._isMagicWeapon;
	}

	public boolean isImmortalityWeapon()
	{
		return this._isImmortalityWeapon;
	}

	public int getSoulShotCount()
	{
		return this._soulShotCount;
	}

	public int getSpiritShotCount()
	{
		return this._spiritShotCount;
	}

	public int getReducedSoulShot()
	{
		return this._reducedSoulshot;
	}

	public int getReducedSoulShotChance()
	{
		return this._reducedSoulshotChance;
	}

	public int getMpConsume()
	{
		return this._mpConsume;
	}

	public int getBaseAttackRange()
	{
		return this._baseAttackRange;
	}

	public int getBaseAttackRadius()
	{
		return this._baseAttackRadius;
	}

	public int getBaseAttackAngle()
	{
		return this._baseAttackAngle;
	}

	public int getReducedMpConsume()
	{
		return this._reducedMpConsume;
	}

	public int getReducedMpConsumeChance()
	{
		return this._reducedMpConsumeChance;
	}

	public int getChangeWeaponId()
	{
		return this._changeWeaponId;
	}

	public boolean isForceEquip()
	{
		return this._isForceEquip;
	}

	public boolean isAttackWeapon()
	{
		return this._isAttackWeapon;
	}

	public boolean useWeaponSkillsOnly()
	{
		return this._useWeaponSkillsOnly;
	}

	public void applyConditionalSkills(Creature caster, Creature target, Skill trigger, ItemSkillType type)
	{
		this.forEachSkill(type, holder -> {
			Skill skill = holder.getSkill();
			if (Rnd.get(100) < holder.getChance())
			{
				if (type == ItemSkillType.ON_MAGIC_SKILL)
				{
					if ((trigger.hasNegativeEffect() != skill.hasNegativeEffect()) || (trigger.isMagic() != skill.isMagic()) || trigger.isToggle())
					{
						return;
					}

					if (skill.hasNegativeEffect() && Formulas.calcShldUse(caster, target) == 2)
					{
						return;
					}
				}

				if (skill.checkCondition(caster, target, true))
				{
					skill.activateSkill(caster, target);
					if (type == ItemSkillType.ON_MAGIC_SKILL)
					{
						if (caster.isPlayer())
						{
							World.getInstance().forEachVisibleObjectInRange(caster, Npc.class, 1000, npc -> {
								if (EventDispatcher.getInstance().hasListener(EventType.ON_NPC_SKILL_SEE, npc))
								{
									EventDispatcher.getInstance().notifyEventAsync(new OnNpcSkillSee(npc, caster.asPlayer(), skill, false, target), npc);
								}
							});
						}

						if (caster.isPlayer())
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_ACTIVATED);
							sm.addSkillName(skill);
							caster.sendPacket(sm);
						}
					}
				}
			}
		});
	}
}
