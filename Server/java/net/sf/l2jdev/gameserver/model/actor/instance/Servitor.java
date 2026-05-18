package net.sf.l2jdev.gameserver.model.actor.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.sql.CharSummonTable;
import net.sf.l2jdev.gameserver.data.sql.SummonEffectTable;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.model.skill.EffectScope;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillFinishType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SetSummonRemainTime;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class Servitor extends Summon implements Runnable
{
	protected static final Logger log = Logger.getLogger(Servitor.class.getName());
	public static final String ADD_SKILL_SAVE = "REPLACE INTO character_summon_skills_save (ownerId,ownerClassIndex,summonSkillId,skill_id,skill_level,remaining_time,buff_index) VALUES (?,?,?,?,?,?,?)";
	public static final String RESTORE_SKILL_SAVE = "SELECT skill_id,skill_level,remaining_time,buff_index FROM character_summon_skills_save WHERE ownerId=? AND ownerClassIndex=? AND summonSkillId=? ORDER BY buff_index ASC";
	public static final String DELETE_SKILL_SAVE = "DELETE FROM character_summon_skills_save WHERE ownerId=? AND ownerClassIndex=? AND summonSkillId=?";
	private float _expMultiplier = 0.0F;
	private ItemHolder _itemConsume;
	private int _lifeTime;
	private int _lifeTimeRemaining;
	private int _consumeItemInterval;
	private int _consumeItemIntervalRemaining;
	protected Future<?> _summonLifeTask;
	private int _referenceSkill;

	public Servitor(NpcTemplate template, Player owner)
	{
		super(template, owner);
		this.setInstanceType(InstanceType.Servitor);
		this.setShowSummonAnimation(true);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		if (this._summonLifeTask == null)
		{
			this._summonLifeTask = ThreadPool.scheduleAtFixedRate(this, 0L, 5000L);
		}
	}

	@Override
	public int getLevel()
	{
		return this.getTemplate() != null ? this.getTemplate().getLevel() : 0;
	}

	@Override
	public int getSummonType()
	{
		return 1;
	}

	public void setExpMultiplier(float expMultiplier)
	{
		this._expMultiplier = expMultiplier;
	}

	public float getExpMultiplier()
	{
		return this._expMultiplier;
	}

	public void setItemConsume(ItemHolder item)
	{
		this._itemConsume = item;
	}

	public ItemHolder getItemConsume()
	{
		return this._itemConsume;
	}

	public void setItemConsumeInterval(int interval)
	{
		this._consumeItemInterval = interval;
		this._consumeItemIntervalRemaining = interval;
	}

	public int getItemConsumeInterval()
	{
		return this._consumeItemInterval;
	}

	public void setLifeTime(int lifeTime)
	{
		this._lifeTime = lifeTime;
		this._lifeTimeRemaining = lifeTime;
	}

	public int getLifeTime()
	{
		return this._lifeTime;
	}

	public void setLifeTimeRemaining(int time)
	{
		this._lifeTimeRemaining = time;
	}

	public int getLifeTimeRemaining()
	{
		return this._lifeTimeRemaining;
	}

	public void setReferenceSkill(int skillId)
	{
		this._referenceSkill = skillId;
	}

	public int getReferenceSkill()
	{
		return this._referenceSkill;
	}

	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		if (this._summonLifeTask != null)
		{
			this._summonLifeTask.cancel(false);
		}

		return true;
	}

	@Override
	public void doCast(Skill skill)
	{
		int petLevel = this.getLevel();
		int skillLevel = petLevel / 10;
		if (petLevel >= 70)
		{
			skillLevel += (petLevel - 65) / 10;
		}

		if (skillLevel < 1)
		{
			skillLevel = 1;
		}

		if (this.isServitor())
		{
			skillLevel = skill.getLevel();
		}

		Skill skillToCast = SkillData.getInstance().getSkill(skill.getId(), skillLevel);
		if (skillToCast != null)
		{
			super.doCast(skillToCast);
		}
		else
		{
			super.doCast(skill);
		}
	}

	@Override
	public void setRestoreSummon(boolean value)
	{
		this._restoreSummon = value;
	}

	@Override
	public void stopSkillEffects(SkillFinishType type, int skillId)
	{
		super.stopSkillEffects(type, skillId);
		Map<Integer, Collection<SummonEffectTable.SummonEffect>> servitorEffects = SummonEffectTable.getInstance().getServitorEffects(this.getOwner());
		if (servitorEffects != null)
		{
			Collection<SummonEffectTable.SummonEffect> effects = servitorEffects.get(this._referenceSkill);
			if (effects != null && !effects.isEmpty())
			{
				for (SummonEffectTable.SummonEffect effect : effects)
				{
					Skill skill = effect.getSkill();
					if (skill != null && skill.getId() == skillId)
					{
						effects.remove(effect);
					}
				}
			}
		}
	}

	@Override
	public void storeMe()
	{
		if (this._referenceSkill != 0)
		{
			if (PlayerConfig.RESTORE_SERVITOR_ON_RECONNECT)
			{
				if (this.isDead())
				{
					CharSummonTable.getInstance().removeServitor(this.getOwner(), this.getObjectId());
				}
				else
				{
					CharSummonTable.getInstance().saveSummon(this);
				}
			}
		}
	}

	@Override
	public void storeEffect(boolean storeEffects)
	{
		if (PlayerConfig.SUMMON_STORE_SKILL_COOLTIME)
		{
			if (this.getOwner() != null && !this.getOwner().isInOlympiadMode())
			{
				if (SummonEffectTable.getInstance().getServitorEffectsOwner().getOrDefault(this.getOwner().getObjectId(), Collections.emptyMap()).containsKey(this.getOwner().getClassIndex()))
				{
					SummonEffectTable.getInstance().getServitorEffects(this.getOwner()).getOrDefault(this.getReferenceSkill(), Collections.emptyList()).clear();
				}

				try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM character_summon_skills_save WHERE ownerId=? AND ownerClassIndex=? AND summonSkillId=?");)
				{
					statement.setInt(1, this.getOwner().getObjectId());
					statement.setInt(2, this.getOwner().getClassIndex());
					statement.setInt(3, this._referenceSkill);
					statement.execute();
					int buffIndex = 0;
					Collection<Long> storedSkills = ConcurrentHashMap.newKeySet();
					if (storeEffects)
					{
						try (PreparedStatement ps2 = con.prepareStatement("REPLACE INTO character_summon_skills_save (ownerId,ownerClassIndex,summonSkillId,skill_id,skill_level,remaining_time,buff_index) VALUES (?,?,?,?,?,?,?)"))
						{
							for (BuffInfo info : this.getEffectList().getEffects())
							{
								if (info != null)
								{
									Skill skill = info.getSkill();
									if (!skill.isDeleteAbnormalOnLeave() && skill.isSharedWithSummon() && skill.getAbnormalType() != AbnormalType.LIFE_FORCE_OTHERS && (!skill.isToggle() || skill.isNecessaryToggle()) && (!skill.isDance() || PlayerConfig.ALT_STORE_DANCES) && !storedSkills.contains(skill.getReuseHashCode()))
									{
										storedSkills.add(skill.getReuseHashCode());
										ps2.setInt(1, this.getOwner().getObjectId());
										ps2.setInt(2, this.getOwner().getClassIndex());
										ps2.setInt(3, this._referenceSkill);
										ps2.setInt(4, skill.getId());
										ps2.setInt(5, skill.getLevel());
										ps2.setInt(6, info.getTime());
										ps2.setInt(7, ++buffIndex);
										ps2.addBatch();
										if (!SummonEffectTable.getInstance().getServitorEffectsOwner().containsKey(this.getOwner().getObjectId()))
										{
											SummonEffectTable.getInstance().getServitorEffectsOwner().put(this.getOwner().getObjectId(), new HashMap<>());
										}

										if (!SummonEffectTable.getInstance().getServitorEffectsOwner().get(this.getOwner().getObjectId()).containsKey(this.getOwner().getClassIndex()))
										{
											SummonEffectTable.getInstance().getServitorEffectsOwner().get(this.getOwner().getObjectId()).put(this.getOwner().getClassIndex(), new HashMap<>());
										}

										if (!SummonEffectTable.getInstance().getServitorEffects(this.getOwner()).containsKey(this.getReferenceSkill()))
										{
											SummonEffectTable.getInstance().getServitorEffects(this.getOwner()).put(this.getReferenceSkill(), ConcurrentHashMap.newKeySet());
										}

										SummonEffectTable.getInstance().getServitorEffects(this.getOwner()).get(this.getReferenceSkill()).add(new SummonEffectTable.SummonEffect(skill, info.getTime()));
									}
								}
							}

							ps2.executeBatch();
						}
					}
				}
				catch (Exception var16)
				{
					LOGGER.log(Level.WARNING, "Could not store summon effect data: ", var16);
				}
			}
		}
	}

	@Override
	public void restoreEffects()
	{
		if (!this.getOwner().isInOlympiadMode())
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				if (!SummonEffectTable.getInstance().getServitorEffectsOwner().containsKey(this.getOwner().getObjectId()) || !SummonEffectTable.getInstance().getServitorEffectsOwner().get(this.getOwner().getObjectId()).containsKey(this.getOwner().getClassIndex()) || !SummonEffectTable.getInstance().getServitorEffects(this.getOwner()).containsKey(this.getReferenceSkill()))
				{
					try (PreparedStatement statement = con.prepareStatement("SELECT skill_id,skill_level,remaining_time,buff_index FROM character_summon_skills_save WHERE ownerId=? AND ownerClassIndex=? AND summonSkillId=? ORDER BY buff_index ASC"))
					{
						statement.setInt(1, this.getOwner().getObjectId());
						statement.setInt(2, this.getOwner().getClassIndex());
						statement.setInt(3, this._referenceSkill);

						try (ResultSet rset = statement.executeQuery())
						{
							while (rset.next())
							{
								int effectCurTime = rset.getInt("remaining_time");
								Skill skill = SkillData.getInstance().getSkill(rset.getInt("skill_id"), rset.getInt("skill_level"));
								if (skill != null && skill.hasEffects(EffectScope.GENERAL))
								{
									if (!SummonEffectTable.getInstance().getServitorEffectsOwner().containsKey(this.getOwner().getObjectId()))
									{
										SummonEffectTable.getInstance().getServitorEffectsOwner().put(this.getOwner().getObjectId(), new HashMap<>());
									}

									if (!SummonEffectTable.getInstance().getServitorEffectsOwner().get(this.getOwner().getObjectId()).containsKey(this.getOwner().getClassIndex()))
									{
										SummonEffectTable.getInstance().getServitorEffectsOwner().get(this.getOwner().getObjectId()).put(this.getOwner().getClassIndex(), new HashMap<>());
									}

									if (!SummonEffectTable.getInstance().getServitorEffects(this.getOwner()).containsKey(this.getReferenceSkill()))
									{
										SummonEffectTable.getInstance().getServitorEffects(this.getOwner()).put(this.getReferenceSkill(), ConcurrentHashMap.newKeySet());
									}

									SummonEffectTable.getInstance().getServitorEffects(this.getOwner()).get(this.getReferenceSkill()).add(new SummonEffectTable.SummonEffect(skill, effectCurTime));
								}
							}
						}
					}
				}

				try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_summon_skills_save WHERE ownerId=? AND ownerClassIndex=? AND summonSkillId=?"))
				{
					statement.setInt(1, this.getOwner().getObjectId());
					statement.setInt(2, this.getOwner().getClassIndex());
					statement.setInt(3, this._referenceSkill);
					statement.executeUpdate();
				}
			}
			catch (Exception var27)
			{
				LOGGER.log(Level.WARNING, "Could not restore " + this + " active effect data: " + var27.getMessage(), var27);
			}
			finally
			{
				if (SummonEffectTable.getInstance().getServitorEffectsOwner().containsKey(this.getOwner().getObjectId()) && SummonEffectTable.getInstance().getServitorEffectsOwner().get(this.getOwner().getObjectId()).containsKey(this.getOwner().getClassIndex()) && SummonEffectTable.getInstance().getServitorEffects(this.getOwner()).containsKey(this.getReferenceSkill()))
				{
					for (SummonEffectTable.SummonEffect se : SummonEffectTable.getInstance().getServitorEffects(this.getOwner()).get(this.getReferenceSkill()))
					{
						if (se != null)
						{
							se.getSkill().applyEffects(this, this, false, se.getEffectCurTime());
						}
					}
				}
			}
		}
	}

	@Override
	public void unSummon(Player owner)
	{
		if (this._summonLifeTask != null)
		{
			this._summonLifeTask.cancel(false);
		}

		super.unSummon(owner);
		if (!this._restoreSummon)
		{
			CharSummonTable.getInstance().removeServitor(owner, this.getObjectId());
		}

		owner.setRecallCreature(null);
	}

	@Override
	public boolean destroyItem(ItemProcessType process, int objectId, long count, WorldObject reference, boolean sendMessage)
	{
		return this.getOwner().destroyItem(process, objectId, count, reference, sendMessage);
	}

	@Override
	public boolean destroyItemByItemId(ItemProcessType process, int itemId, long count, WorldObject reference, boolean sendMessage)
	{
		return this.getOwner().destroyItemByItemId(process, itemId, count, reference, sendMessage);
	}

	@Override
	public AttributeType getAttackElement()
	{
		return this.getOwner() != null ? this.getOwner().getAttackElement() : super.getAttackElement();
	}

	@Override
	public int getAttackElementValue(AttributeType attackAttribute)
	{
		return this.getOwner() != null ? this.getOwner().getAttackElementValue(attackAttribute) : super.getAttackElementValue(attackAttribute);
	}

	@Override
	public int getDefenseElementValue(AttributeType defenseAttribute)
	{
		return this.getOwner() != null ? this.getOwner().getDefenseElementValue(defenseAttribute) : super.getDefenseElementValue(defenseAttribute);
	}

	@Override
	public boolean isServitor()
	{
		return true;
	}

	@Override
	public Servitor asServitor()
	{
		return this;
	}

	@Override
	public void run()
	{
		boolean hasLifetime = this._lifeTime > 0;
		if (hasLifetime)
		{
			this._lifeTimeRemaining -= 5000;
		}

		if (!this.isDead() && this.isSpawned())
		{
			if (hasLifetime && this._lifeTimeRemaining < 0)
			{
				this.sendPacket(SystemMessageId.YOUR_SERVITOR_PASSED_AWAY);
				this.unSummon(this.getOwner());
			}
			else
			{
				if (this._consumeItemInterval > 0)
				{
					this._consumeItemIntervalRemaining -= 5000;
					if (this._consumeItemIntervalRemaining <= 0 && this._itemConsume.getCount() > 0L && this._itemConsume.getId() > 0 && !this.isDead())
					{
						if (this.destroyItemByItemId(null, this._itemConsume.getId(), this._itemConsume.getCount(), this, false))
						{
							SystemMessage msg = new SystemMessage(SystemMessageId.A_SUMMONED_MONSTER_USES_S1);
							msg.addItemName(this._itemConsume.getId());
							this.sendPacket(msg);
							this._consumeItemIntervalRemaining = this._consumeItemInterval;
						}
						else
						{
							this.sendPacket(SystemMessageId.SINCE_YOU_DO_NOT_HAVE_ENOUGH_ITEMS_TO_MAINTAIN_THE_SERVITOR_S_STAY_THE_SERVITOR_HAS_DISAPPEARED);
							this.unSummon(this.getOwner());
						}
					}
				}

				if (hasLifetime)
				{
					this.sendPacket(new SetSummonRemainTime(this._lifeTime, this._lifeTimeRemaining));
				}

				if (this.calculateDistance3D(this.getOwner()) > 2000.0)
				{
					this.getAI().setIntention(Intention.FOLLOW, this.getOwner());
				}
			}
		}
		else
		{
			if (this._summonLifeTask != null)
			{
				this._summonLifeTask.cancel(false);
			}
		}
	}

	@Override
	public void doPickupItem(WorldObject object)
	{
	}
}
