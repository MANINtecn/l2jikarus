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
package handlers.skillconditionhandlers;

import net.sf.l2jdev.gameserver.data.enums.CategoryType;
import net.sf.l2jdev.gameserver.data.xml.CategoryData;
import net.sf.l2jdev.gameserver.data.xml.TransformData;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.transform.Transform;
import net.sf.l2jdev.gameserver.model.skill.ISkillCondition;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

/**
 * @author Sdw, BAN-JDEV
 */
public class CanTransformSkillCondition implements ISkillCondition
{
	private final Transform transform;

	public CanTransformSkillCondition(StatSet params)
	{
		final int transformId = params.getInt("transformId", -1);
		if (transformId > 0)
		{
			transform = TransformData.getInstance().getTransform(transformId);
		}
		else
		{
			transform = null;
		}
	}

	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		final Player player = caster.asPlayer();
		if ((player == null) || player.isAlikeDead() || player.isCursedWeaponEquipped())
		{
			return false;
		}

		if (transform != null)
		{
			if (transform.isFlying() && (player.getX() > World.GRACIA_MAX_X))
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_TRANSFORM_IN_THIS_LOCATION);
				return false;
			}

			if (transform.isRiding() && CategoryData.getInstance().isInCategory(CategoryType.VANGUARD_ALL_CLASS, player.getPlayerClass().getId()))
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_TRANSFORM_WHILE_RIDING_A_GUARDIAN);
				return false;
			}
		}

		if (player.isSitting())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_TRANSFORM_WHILE_SITTING);
			return false;
		}

		if (player.isTransformed())
		{
			player.sendPacket(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
			return false;
		}

		if (player.isInWater())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_TRANSFORM_WHILE_IN_THE_WATER);
			return false;
		}

		if (player.isFlyingMounted() || player.isMounted())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_TRANSFORM_WHILE_RIDING_A_GUARDIAN);
			return false;
		}

		return true;
	}
}
