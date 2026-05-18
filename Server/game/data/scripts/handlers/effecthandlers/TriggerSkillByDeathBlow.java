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
import net.sf.l2jdev.gameserver.handler.TargetHandler;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureDamageReceived;
import net.sf.l2jdev.gameserver.model.events.listeners.ConsumerEventListener;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.SkillCaster;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.model.skill.targets.TargetType;

/**
 * Trigger Skill By Death Blow effect implementation.
 * @author Sdw
 */
public class TriggerSkillByDeathBlow extends AbstractEffect
{
	private final int _minAttackerLevel;
	private final int _maxAttackerLevel;
	private final int _chance;
	private final SkillHolder _skill;
	private final TargetType _targetType;
	private final InstanceType _attackerType;

	public TriggerSkillByDeathBlow(StatSet params)
	{
		_minAttackerLevel = params.getInt("minAttackerLevel", 1);
		_maxAttackerLevel = params.getInt("maxAttackerLevel", Integer.MAX_VALUE);
		_chance = params.getInt("chance", 100);
		_skill = new SkillHolder(params.getInt("skillId"), params.getInt("skillLevel", 1));
		_targetType = params.getEnum("targetType", TargetType.class, TargetType.SELF);
		_attackerType = params.getEnum("attackerType", InstanceType.class, InstanceType.Creature);
	}

	private void onDamageReceivedEvent(OnCreatureDamageReceived event)
	{
		if ((event.getDamage() < event.getTarget().getCurrentHp()) || (_chance == 0) || (_skill.getSkillLevel() == 0) || (event.getAttacker() == event.getTarget()))
		{
			return;
		}

		if ((event.getAttacker().getLevel() < _minAttackerLevel) || (event.getAttacker().getLevel() > _maxAttackerLevel))
		{
			return;
		}

		if (((_chance < 100) && (Rnd.get(100) > _chance)) || !event.getAttacker().getInstanceType().isType(_attackerType))
		{
			return;
		}

		final Skill triggerSkill = _skill.getSkill();
		WorldObject target = null;
		try
		{
			target = TargetHandler.getInstance().getHandler(_targetType).getTarget(event.getTarget(), event.getAttacker(), triggerSkill, false, false, false);
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Exception in ITargetTypeHandler.getTarget(): " + e.getMessage(), e);
		}

		if ((target != null) && target.isCreature())
		{
			SkillCaster.triggerCast(event.getTarget(), target.asCreature(), triggerSkill);
		}
	}

	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		effected.removeListenerIf(EventType.ON_CREATURE_DAMAGE_RECEIVED, listener -> listener.getOwner() == this);
	}

	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		effected.addListener(new ConsumerEventListener(effected, EventType.ON_CREATURE_DAMAGE_RECEIVED, (OnCreatureDamageReceived event) -> onDamageReceivedEvent(event), this));
	}
}
