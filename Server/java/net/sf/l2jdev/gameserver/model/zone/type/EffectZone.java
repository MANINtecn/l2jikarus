package net.sf.l2jdev.gameserver.model.zone.type;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillFinishType;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;
import net.sf.l2jdev.gameserver.network.serverpackets.EtcStatusUpdate;

public class EffectZone extends ZoneType
{
	int _chance = 100;
	private int _initialDelay = 0;
	private int _reuse = 30000;
	protected boolean _bypassConditions;
	private boolean _isShowDangerIcon;
	private boolean _removeEffectsOnExit;
	protected Map<Integer, Integer> _skills;
	protected volatile Future<?> _task;

	public EffectZone(int id)
	{
		super(id);
		this.setTargetType(InstanceType.Playable);
		this._bypassConditions = false;
		this._isShowDangerIcon = true;
		this._removeEffectsOnExit = false;
	}

	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
			case "chance":
				this._chance = Integer.parseInt(value);
				break;
			case "initialDelay":
				this._initialDelay = Integer.parseInt(value);
				break;
			case "reuse":
				this._reuse = Integer.parseInt(value);
				break;
			case "bypassSkillConditions":
				this._bypassConditions = Boolean.parseBoolean(value);
				break;
			case "maxDynamicSkillCount":
				this._skills = new ConcurrentHashMap<>(Integer.parseInt(value));
				break;
			case "showDangerIcon":
				this._isShowDangerIcon = Boolean.parseBoolean(value);
				break;
			case "skillIdLvl":
				String[] propertySplit = value.split(";");
				this._skills = new ConcurrentHashMap<>(propertySplit.length);

				for (String skill : propertySplit)
				{
					String[] skillSplit = skill.split("-");
					if (skillSplit.length != 2)
					{
						LOGGER.warning(this.getClass().getSimpleName() + ": invalid config property -> skillsIdLvl \"" + skill + "\"");
					}
					else
					{
						try
						{
							this._skills.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
						}
						catch (NumberFormatException var12)
						{
							if (!skill.isEmpty())
							{
								LOGGER.warning(this.getClass().getSimpleName() + ": invalid config property -> skillsIdLvl \"" + skillSplit[0] + "\"" + skillSplit[1]);
							}
						}
					}
				}
				break;
			case "removeEffectsOnExit":
				this._removeEffectsOnExit = Boolean.parseBoolean(value);
				break;
			default:
				super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(Creature creature)
	{
		if (this._skills != null)
		{
			Future<?> task = this._task;
			if (task == null)
			{
				synchronized (this)
				{
					task = this._task;
					if (task == null)
					{
						this._task = ThreadPool.scheduleAtFixedRate(new EffectZone.ApplySkill(), this._initialDelay, this._reuse);
					}
				}
			}
		}

		if (creature.isPlayer())
		{
			creature.setInsideZone(ZoneId.ALTERED, true);
			if (this._isShowDangerIcon)
			{
				creature.setInsideZone(ZoneId.DANGER_AREA, true);
				creature.sendPacket(new EtcStatusUpdate(creature.asPlayer()));
			}
		}
	}

	@Override
	protected void onExit(Creature creature)
	{
		if (creature.isPlayer())
		{
			creature.setInsideZone(ZoneId.ALTERED, false);
			if (this._isShowDangerIcon)
			{
				creature.setInsideZone(ZoneId.DANGER_AREA, false);
				if (!creature.isInsideZone(ZoneId.DANGER_AREA))
				{
					creature.sendPacket(new EtcStatusUpdate(creature.asPlayer()));
				}
			}

			if (this._removeEffectsOnExit && this._skills != null)
			{
				for (Entry<Integer, Integer> e : this._skills.entrySet())
				{
					Skill skill = SkillData.getInstance().getSkill(e.getKey(), e.getValue());
					if (skill != null && creature.isAffectedBySkill(skill.getId()))
					{
						creature.stopSkillEffects(SkillFinishType.REMOVED, skill.getId());
					}
				}
			}
		}

		if (this.getCharactersInside().isEmpty() && this._task != null)
		{
			this._task.cancel(true);
			this._task = null;
		}
	}

	public int getChance()
	{
		return this._chance;
	}

	public void addSkill(int skillId, int skillLevel)
	{
		if (skillLevel < 1)
		{
			this.removeSkill(skillId);
		}
		else
		{
			if (this._skills == null)
			{
				synchronized (this)
				{
					if (this._skills == null)
					{
						this._skills = new ConcurrentHashMap<>(3);
					}
				}
			}

			this._skills.put(skillId, skillLevel);
		}
	}

	public void removeSkill(int skillId)
	{
		if (this._skills != null)
		{
			this._skills.remove(skillId);
		}
	}

	public void clearSkills()
	{
		if (this._skills != null)
		{
			this._skills.clear();
		}
	}

	public int getSkillLevel(int skillId)
	{
		return this._skills != null && this._skills.containsKey(skillId) ? this._skills.get(skillId) : 0;
	}

	private class ApplySkill implements Runnable
	{
		protected ApplySkill()
		{
			Objects.requireNonNull(EffectZone.this);
			super();
			if (EffectZone.this._skills == null)
			{
				throw new IllegalStateException("No skills defined.");
			}
		}

		@Override
		public void run()
		{
			if (EffectZone.this.isEnabled())
			{
				if (EffectZone.this.getCharactersInside().isEmpty())
				{
					if (EffectZone.this._task != null)
					{
						EffectZone.this._task.cancel(false);
						EffectZone.this._task = null;
					}
				}
				else
				{
					for (Creature character : EffectZone.this.getCharactersInside())
					{
						if (character != null && character.isPlayer() && !character.isDead() && Rnd.get(100) < EffectZone.this._chance)
						{
							for (Entry<Integer, Integer> e : EffectZone.this._skills.entrySet())
							{
								Skill skill = SkillData.getInstance().getSkill(e.getKey(), e.getValue());
								if (skill != null && (EffectZone.this._bypassConditions || skill.checkCondition(character, character, false)) && character.getAffectedSkillLevel(skill.getId()) < skill.getLevel())
								{
									skill.activateSkill(character, character);
								}
							}
						}
					}
				}
			}
		}
	}
}
