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

import net.sf.l2jdev.gameserver.ai.Action;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.actor.instance.Defender;
import net.sf.l2jdev.gameserver.model.actor.instance.FortCommander;
import net.sf.l2jdev.gameserver.model.actor.instance.SiegeFlag;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.effects.EffectFlag;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.util.LocationUtil;

/**
 * Fear effect implementation.
 * @author littlecrow
 */
public class Fear extends AbstractEffect
{
	private static final int FEAR_RANGE = 500;

	public Fear(StatSet params)
	{
	}

	@Override
	public long getEffectFlags()
	{
		return EffectFlag.FEAR.getMask();
	}

	@Override
	public boolean canStart(Creature effector, Creature effected, Skill skill)
	{
		if ((effected == null) || effected.isRaid())
		{
			return false;
		}

		return effected.isPlayer() || effected.isSummon() || (effected.isAttackable() //
			&& !((effected instanceof Defender) || (effected instanceof FortCommander) //
				|| (effected instanceof SiegeFlag) || (effected.getTemplate().getRace() == Race.SIEGE_WEAPON)));
	}

	@Override
	public int getTicks()
	{
		return 5;
	}

	@Override
	public boolean onActionTime(Creature effector, Creature effected, Skill skill, Item item)
	{
		fearAction(null, effected);
		return false;
	}

	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		effected.getAI().notifyAction(Action.AFRAID);
		fearAction(effector, effected);
	}

	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		if (!effected.isPlayer())
		{
			effected.getAI().notifyAction(Action.THINK);
		}
	}

	private static void fearAction(Creature effector, Creature effected)
	{
		final double radians = Math.toRadians((effector != null) ? LocationUtil.calculateAngleFrom(effector, effected) : LocationUtil.convertHeadingToDegree(effected.getHeading()));

		final int posX = (int) (effected.getX() + (FEAR_RANGE * Math.cos(radians)));
		final int posY = (int) (effected.getY() + (FEAR_RANGE * Math.sin(radians)));
		final int posZ = effected.getZ();

		final Location destination = GeoEngine.getInstance().getValidLocation(effected.getX(), effected.getY(), effected.getZ(), posX, posY, posZ, effected.getInstanceWorld());
		effected.getAI().setIntention(Intention.MOVE_TO, destination);
	}
}
