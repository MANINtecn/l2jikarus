/*
 * Copyright (c) 2013 L2jBAN-JDEV
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package handlers.effecthandlers;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.effects.EffectType;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureSkillFinishCast;
import net.sf.l2jdev.gameserver.model.events.listeners.ConsumerEventListener;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

/**
 * Trigger heal percent by skill effect implementation.
 * @author NasSeKa
 */
public class TriggerHealPercentBySkill extends AbstractEffect
{
	private final int _castSkillId;
	private final int _chance;
	private final int _power;

	public TriggerHealPercentBySkill(StatSet params)
	{
		_castSkillId = params.getInt("castSkillId");
		_chance = params.getInt("chance", 100);
		_power = params.getInt("power", 0);
	}

	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		if ((_chance == 0) || (_castSkillId == 0))
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

	@Override
	public EffectType getEffectType()
	{
		return EffectType.HEAL;
	}

	private void onSkillUseEvent(OnCreatureSkillFinishCast event)
	{
		if (_castSkillId != event.getSkill().getId())
		{
			return;
		}

		final WorldObject target = event.getTarget();
		if (target == null)
		{
			return;
		}

		final Player player = target.asPlayer();
		if ((player == null) || player.isDead() || player.isHpBlocked())
		{
			return;
		}

		if ((_chance < 100) && (Rnd.get(100) > _chance))
		{
			return;
		}

		double amount = 0;
		final double power = _power;
		final boolean full = (power == 100.0);

		amount = full ? player.getMaxHp() : (player.getMaxHp() * power) / 100.0;

		// Prevents overheal.
		amount = Math.min(amount, Math.max(0, player.getMaxRecoverableHp() - player.getCurrentHp()));
		if (amount >= 0)
		{
			if (amount != 0)
			{
				player.setCurrentHp(amount + player.getCurrentHp(), false);
				player.broadcastStatusUpdate(player);
			}

			SystemMessage sm;
			sm = new SystemMessage(SystemMessageId.YOU_VE_RECOVERED_S1_HP);
			sm.addInt((int) amount);
			player.sendPacket(sm);
		}
	}
}
