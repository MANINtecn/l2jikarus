package net.sf.l2jdev.gameserver.model.actor.transform;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.data.xml.SkillTreeData;
import net.sf.l2jdev.gameserver.model.SkillLearn;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.appearance.PlayerAppearance;
import net.sf.l2jdev.gameserver.model.actor.enums.player.Sex;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerTransform;
import net.sf.l2jdev.gameserver.model.item.type.WeaponType;
import net.sf.l2jdev.gameserver.model.itemcontainer.InventoryBlockType;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.serverpackets.ExBasicActionList;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUserInfoEquipSlot;
import net.sf.l2jdev.gameserver.network.serverpackets.SkillCoolTime;

public class Transform
{
	private final int _id;
	private final int _displayId;
	private final TransformType _type;
	private final boolean _canSwim;
	private final int _spawnHeight;
	private final boolean _canAttack;
	private final boolean _isVisual;
	private final String _name;
	private final String _title;
	private TransformTemplate _maleTemplate;
	private TransformTemplate _femaleTemplate;

	public Transform(StatSet set)
	{
		this._id = set.getInt("id");
		this._displayId = set.getInt("displayId", this._id);
		this._type = set.getEnum("type", TransformType.class, TransformType.COMBAT);
		this._canSwim = set.getInt("can_swim", 0) == 1;
		this._canAttack = set.getInt("normal_attackable", 1) == 1;
		this._isVisual = set.getInt("is_visual", 1) == 1;
		this._spawnHeight = set.getInt("spawn_height", 0);
		this._name = set.getString("setName", null);
		this._title = set.getString("setTitle", null);
	}

	public int getId()
	{
		return this._id;
	}

	public int getDisplayId()
	{
		return this._displayId;
	}

	public TransformType getType()
	{
		return this._type;
	}

	public boolean canSwim()
	{
		return this._canSwim;
	}

	public boolean canAttack()
	{
		return this._canAttack;
	}

	public boolean isVisual()
	{
		return this._isVisual;
	}

	public int getSpawnHeight()
	{
		return this._spawnHeight;
	}

	public String getName()
	{
		return this._name;
	}

	public String getTitle()
	{
		return this._title;
	}

	public TransformTemplate getTemplate(Creature creature)
	{
		if (creature.isPlayer())
		{
			return creature.asPlayer().getAppearance().isFemale() ? this._femaleTemplate : this._maleTemplate;
		}
		else if (creature.isNpc())
		{
			return creature.asNpc().getTemplate().getSex() == Sex.FEMALE ? this._femaleTemplate : this._maleTemplate;
		}
		else
		{
			return null;
		}
	}

	public void setTemplate(boolean male, TransformTemplate template)
	{
		if (male)
		{
			this._maleTemplate = template;
		}
		else
		{
			this._femaleTemplate = template;
		}
	}

	public boolean isStance()
	{
		return this._type == TransformType.MODE_CHANGE;
	}

	public boolean isCombat()
	{
		return this._type == TransformType.COMBAT;
	}

	public boolean isNonCombat()
	{
		return this._type == TransformType.NON_COMBAT;
	}

	public boolean isFlying()
	{
		return this._type == TransformType.FLYING;
	}

	public boolean isCursed()
	{
		return this._type == TransformType.CURSED;
	}

	public boolean isRiding()
	{
		return this._type == TransformType.RIDING_MODE;
	}

	public boolean isPureStats()
	{
		return this._type == TransformType.PURE_STAT;
	}

	public boolean canUseWeaponStats()
	{
		return this._type == TransformType.COMBAT || this._type == TransformType.MODE_CHANGE;
	}

	public float getCollisionHeight(Creature creature, float defaultCollisionHeight)
	{
		TransformTemplate template = this.getTemplate(creature);
		return template != null && template.getCollisionHeight() != null ? template.getCollisionHeight() : defaultCollisionHeight;
	}

	public float getCollisionRadius(Creature creature, float defaultCollisionRadius)
	{
		TransformTemplate template = this.getTemplate(creature);
		return template != null && template.getCollisionRadius() != null ? template.getCollisionRadius() : defaultCollisionRadius;
	}

	public void onTransform(Creature creature, boolean addSkills)
	{
		if (this._type != TransformType.MODE_CHANGE)
		{
			creature.abortAttack();
			creature.abortCast();
		}

		Player player = creature.asPlayer();
		if (creature.isPlayer() && player.isMounted())
		{
			player.dismount();
		}

		TransformTemplate template = this.getTemplate(creature);
		if (template != null)
		{
			if (this.isFlying())
			{
				creature.setFlying(true);
			}

			creature.setXYZ(creature.getX(), creature.getY(), (int) (creature.getZ() + this.getCollisionHeight(creature, 0.0F)));
			if (!creature.isPlayer())
			{
				creature.broadcastInfo();
			}
			else
			{
				PlayerAppearance appearance = player.getAppearance();
				if (this._name != null)
				{
					appearance.setVisibleName(this._name);
				}

				if (this._title != null)
				{
					appearance.setVisibleTitle(this._title);
				}

				if (addSkills)
				{
					for (SkillHolder h : template.getSkills())
					{
						player.addTransformSkill(h.getSkill());
					}

					for (AdditionalSkillHolder h : template.getAdditionalSkills())
					{
						if (player.getLevel() >= h.getMinLevel())
						{
							player.addTransformSkill(h.getSkill());
						}
					}

					for (SkillLearn s : SkillTreeData.getInstance().getCollectSkillTree().values())
					{
						Skill skill = player.getKnownSkill(s.getSkillId());
						if (skill != null)
						{
							player.addTransformSkill(skill);
						}
					}
				}

				if (!template.getAdditionalItems().isEmpty())
				{
					List<Integer> allowed = new ArrayList<>();
					List<Integer> notAllowed = new ArrayList<>();

					for (AdditionalItemHolder holder : template.getAdditionalItems())
					{
						if (holder.isAllowedToUse())
						{
							allowed.add(holder.getId());
						}
						else
						{
							notAllowed.add(holder.getId());
						}
					}

					if (!allowed.isEmpty())
					{
						player.getInventory().setInventoryBlock(allowed, InventoryBlockType.WHITELIST);
					}

					if (!notAllowed.isEmpty())
					{
						player.getInventory().setInventoryBlock(notAllowed, InventoryBlockType.BLACKLIST);
					}
				}

				if (template.hasBasicActionList())
				{
					player.sendPacket(new ExBasicActionList(template.getBasicActionList()));
				}

				if (this._type != TransformType.MODE_CHANGE)
				{
					player.getEffectList().stopAllToggles();
				}

				ThreadPool.schedule(() -> {
					player.sendSkillList();
					player.sendPacket(new SkillCoolTime(player));
				}, 1000L);
				player.broadcastUserInfo();
				if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_TRANSFORM, player))
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnPlayerTransform(player, this.getId()), player);
				}
			}

			creature.updateAbnormalVisualEffects();
		}
	}

	public void onUntransform(Creature creature)
	{
		creature.abortAttack();
		creature.abortCast();
		TransformTemplate template = this.getTemplate(creature);
		if (template != null)
		{
			if (this.isFlying())
			{
				creature.setFlying(false);
			}

			if (creature.isPlayer())
			{
				Player player = creature.asPlayer();
				PlayerAppearance appearance = player.getAppearance();
				if (this._name != null)
				{
					appearance.setVisibleName(null);
				}

				if (this._title != null)
				{
					appearance.setVisibleTitle(null);
				}

				player.removeAllTransformSkills();
				if (!template.getAdditionalItems().isEmpty())
				{
					player.getInventory().unblock();
				}

				player.sendPacket(ExBasicActionList.STATIC_PACKET);
				if (!player.getEffectList().stopEffects(AbnormalType.TRANSFORM))
				{
					player.getEffectList().stopEffects(AbnormalType.CHANGEBODY);
				}

				ThreadPool.schedule(() -> {
					player.sendSkillList();
					player.sendPacket(new SkillCoolTime(player));
				}, 1000L);
				player.broadcastUserInfo();
				player.sendPacket(new ExUserInfoEquipSlot(player));
				if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_TRANSFORM, player))
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnPlayerTransform(player, 0), player);
				}
			}
			else
			{
				creature.broadcastInfo();
			}
		}
	}

	public void onLevelUp(Player player)
	{
		TransformTemplate template = this.getTemplate(player);
		if (template != null && !template.getAdditionalSkills().isEmpty())
		{
			for (AdditionalSkillHolder holder : template.getAdditionalSkills())
			{
				if (player.getLevel() >= holder.getMinLevel() && player.getSkillLevel(holder.getSkillId()) < holder.getSkillLevel())
				{
					player.addTransformSkill(holder.getSkill());
				}
			}
		}
	}

	public WeaponType getBaseAttackType(Creature creature, WeaponType defaultAttackType)
	{
		TransformTemplate template = this.getTemplate(creature);
		if (template != null)
		{
			WeaponType weaponType = template.getBaseAttackType();
			if (weaponType != null)
			{
				return weaponType;
			}
		}

		return defaultAttackType;
	}

	public double getStats(Creature creature, Stat stat, double defaultValue)
	{
		double val = defaultValue;
		TransformTemplate template = this.getTemplate(creature);
		if (template != null)
		{
			val = template.getStats(stat, defaultValue);
			TransformLevelData data = template.getData(creature.getLevel());
			if (data != null)
			{
				val = data.getStats(stat, defaultValue);
			}
		}

		return val;
	}

	public int getBaseDefBySlot(Player player, int slot)
	{
		int defaultValue = player.getTemplate().getBaseDefBySlot(slot);
		TransformTemplate template = this.getTemplate(player);
		return template == null ? defaultValue : template.getDefense(slot, defaultValue);
	}

	public double getLevelMod(Creature creature)
	{
		double val = 1.0;
		TransformTemplate template = this.getTemplate(creature);
		if (template != null)
		{
			TransformLevelData data = template.getData(creature.getLevel());
			if (data != null)
			{
				val = data.getLevelMod();
			}
		}

		return val;
	}
}
