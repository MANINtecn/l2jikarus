package net.sf.l2jdev.gameserver.model.options;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.model.actor.Playable;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillFinishType;
import net.sf.l2jdev.gameserver.network.serverpackets.SkillCoolTime;

public class Options
{
	private final int _id;
	private List<AbstractEffect> _effects = null;
	private List<Skill> _activeSkill = null;
	private List<Skill> _passiveSkill = null;
	private List<OptionSkillHolder> _activationSkills = null;

	public Options(int id)
	{
		this._id = id;
	}

	public int getId()
	{
		return this._id;
	}

	public void addEffect(AbstractEffect effect)
	{
		if (this._effects == null)
		{
			this._effects = new ArrayList<>();
		}

		this._effects.add(effect);
	}

	public List<AbstractEffect> getEffects()
	{
		return this._effects;
	}

	public boolean hasEffects()
	{
		return this._effects != null;
	}

	public boolean hasActiveSkills()
	{
		return this._activeSkill != null;
	}

	public List<Skill> getActiveSkills()
	{
		return this._activeSkill;
	}

	public void addActiveSkill(Skill holder)
	{
		if (this._activeSkill == null)
		{
			this._activeSkill = new ArrayList<>();
		}

		this._activeSkill.add(holder);
	}

	public boolean hasPassiveSkills()
	{
		return this._passiveSkill != null;
	}

	public List<Skill> getPassiveSkills()
	{
		return this._passiveSkill;
	}

	public void addPassiveSkill(Skill holder)
	{
		if (this._passiveSkill == null)
		{
			this._passiveSkill = new ArrayList<>();
		}

		this._passiveSkill.add(holder);
	}

	public boolean hasActivationSkills()
	{
		return this._activationSkills != null;
	}

	public boolean hasActivationSkills(OptionSkillType type)
	{
		if (this._activationSkills != null)
		{
			for (OptionSkillHolder holder : this._activationSkills)
			{
				if (holder.getSkillType() == type)
				{
					return true;
				}
			}
		}

		return false;
	}

	public List<OptionSkillHolder> getActivationSkills()
	{
		return this._activationSkills;
	}

	public List<OptionSkillHolder> getActivationSkills(OptionSkillType type)
	{
		List<OptionSkillHolder> temp = new ArrayList<>();
		if (this._activationSkills != null)
		{
			for (OptionSkillHolder holder : this._activationSkills)
			{
				if (holder.getSkillType() == type)
				{
					temp.add(holder);
				}
			}
		}

		return temp;
	}

	public void addActivationSkill(OptionSkillHolder holder)
	{
		if (this._activationSkills == null)
		{
			this._activationSkills = new ArrayList<>();
		}

		this._activationSkills.add(holder);
	}

	public void apply(Playable playable)
	{
		if (this.hasEffects())
		{
			BuffInfo info = new BuffInfo(playable, playable, null, true, null, this);

			for (AbstractEffect effect : this._effects)
			{
				if (effect.isInstant())
				{
					if (effect.calcSuccess(info.getEffector(), info.getEffected(), info.getSkill()))
					{
						effect.instant(info.getEffector(), info.getEffected(), info.getSkill(), info.getItem());
					}
				}
				else
				{
					effect.continuousInstant(info.getEffector(), info.getEffected(), info.getSkill(), info.getItem());
					effect.pump(playable, info.getSkill());
					if (effect.canStart(info.getEffector(), info.getEffected(), info.getSkill()))
					{
						info.addEffect(effect);
					}
				}
			}

			if (!info.getEffects().isEmpty())
			{
				playable.getEffectList().add(info);
			}
		}

		if (this.hasActiveSkills())
		{
			for (Skill skill : this._activeSkill)
			{
				this.addSkill(playable, skill);
			}
		}

		if (this.hasPassiveSkills())
		{
			for (Skill skill : this._passiveSkill)
			{
				this.addSkill(playable, skill);
			}
		}

		if (this.hasActivationSkills())
		{
			for (OptionSkillHolder holder : this._activationSkills)
			{
				playable.addTriggerSkill(holder);
			}
		}

		playable.getStat().recalculateStats(true);
		if (playable.isPlayer())
		{
			playable.asPlayer().sendSkillList();
		}
	}

	public void remove(Playable playable)
	{
		if (this.hasEffects())
		{
			for (BuffInfo info : playable.getEffectList().getOptions())
			{
				if (info.getOption() == this)
				{
					playable.getEffectList().remove(info, SkillFinishType.NORMAL, true, true);
				}
			}
		}

		if (this.hasActiveSkills())
		{
			for (Skill skill : this._activeSkill)
			{
				playable.removeSkill(skill, false);
			}
		}

		if (this.hasPassiveSkills())
		{
			for (Skill skill : this._passiveSkill)
			{
				playable.removeSkill(skill, true);
			}
		}

		if (this.hasActivationSkills())
		{
			for (OptionSkillHolder holder : this._activationSkills)
			{
				playable.removeTriggerSkill(holder);
			}
		}

		playable.getStat().recalculateStats(true);
		if (playable.isPlayer())
		{
			playable.asPlayer().sendSkillList();
		}
	}

	public void addSkill(Playable playable, Skill skill)
	{
		boolean updateTimeStamp = false;
		playable.addSkill(skill);
		if (skill.isActive())
		{
			long remainingTime = playable.getSkillRemainingReuseTime(skill.getReuseHashCode());
			if (remainingTime > 0L)
			{
				playable.addTimeStamp(skill, remainingTime);
				playable.disableSkill(skill, remainingTime);
			}

			updateTimeStamp = true;
		}

		if (updateTimeStamp && playable.isPlayer())
		{
			playable.sendPacket(new SkillCoolTime(playable.asPlayer()));
		}
	}
}
