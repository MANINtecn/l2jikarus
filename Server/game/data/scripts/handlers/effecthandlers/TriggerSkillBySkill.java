/*
 * This file is part of the L2J BAN-JDEV project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import java.util.logging.Level;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.handler.TargetHandler;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureSkillFinishCast;
import net.sf.l2jdev.gameserver.model.events.listeners.ConsumerEventListener;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.SkillCaster;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillFinishType;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.model.skill.targets.TargetType;

/**
 * Trigger Skill By Skill effect implementation.
 * @author Zealar
 */
public class TriggerSkillBySkill extends AbstractEffect
{
	private final int _castSkillId;
	private final int _castSkillLevel;
	private final int _chance;
	private final SkillHolder _skill;
	private final int _skillLevelScaleTo;
	private final TargetType _targetType;
	private final boolean _replace;

	public TriggerSkillBySkill(StatSet params)
	{
		_castSkillId = params.getInt("castSkillId");
		_castSkillLevel = params.getInt("castSkillLevel", 0);
		_chance = params.getInt("chance", 100);
		_skill = new SkillHolder(params.getInt("skillId", 0), params.getInt("skillLevel", 0));
		_skillLevelScaleTo = params.getInt("skillLevelScaleTo", 0);
		_targetType = params.getEnum("targetType", TargetType.class, TargetType.TARGET);
		_replace = params.getBoolean("replace", true);
	}

	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		if ((_chance == 0) || (_skill.getSkillId() == 0) || (_skill.getSkillLevel() == 0) || (_castSkillId == 0))
		{
			return;
		}

		effected.addListener(new ConsumerEventListener(effected, EventType.ON_CREATURE_SKILL_FINISH_CAST, (OnCreatureSkillFinishCast event) -> onSkillUseEvent(event), this));
	}

	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		effected.removeListenerIf(EventType.ON_CREATURE_SKILL_FINISH_CAST, listener -> listener.getOwner() == this);
	}

	private void onSkillUseEvent(OnCreatureSkillFinishCast event)
	{
		if ((_castSkillId != event.getSkill().getId()) || ((_castSkillLevel > 0) && (_castSkillLevel != event.getSkill().getLevel())))
		{
			return;
		}

		WorldObject target = event.getTarget();
		if (target == null)
		{
			return;
		}

		if (!target.isCreature())
		{
			return;
		}

		if ((_chance < 100) && (Rnd.get(100) > _chance))
		{
			return;
		}

		target = null;
		try
		{
			target = TargetHandler.getInstance().getHandler(_targetType).getTarget(event.getCaster(), event.getTarget(), _skill.getSkill(), false, false, false);
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Exception in ITargetTypeHandler.getTarget(): " + e.getMessage(), e);
		}

		if ((target == null) || !target.isCreature())
		{
			return;
		}

		final Skill triggerSkill;
		if (_skillLevelScaleTo <= 0)
		{
			triggerSkill = _skill.getSkill();
		}
		else
		{
			final BuffInfo buffInfo = target.asCreature().getEffectList().getBuffInfoBySkillId(_skill.getSkillId());
			if (buffInfo != null)
			{
				triggerSkill = SkillData.getInstance().getSkill(_skill.getSkillId(), Math.min(_skillLevelScaleTo, buffInfo.getSkill().getLevel() + 1));

				if (event.getCaster().isSkillDisabled(buffInfo.getSkill()))
				{
					if ((_replace) && (buffInfo.getSkill().getLevel() == _skillLevelScaleTo))
					{
						target.asCreature().stopSkillEffects(SkillFinishType.SILENT, triggerSkill.getId());
					}
					return;
				}
			}
			else
			{
				triggerSkill = _skill.getSkill();
			}
		}

		// Remove existing effect, otherwise time will not be renewed at max level.
		if (_replace)
		{
			target.asCreature().stopSkillEffects(SkillFinishType.SILENT, triggerSkill.getId());
		}

		SkillCaster.triggerCast(event.getCaster(), target.asCreature(), triggerSkill);
	}
}
