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
package handlers.usercommandhandlers;

import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.custom.FactionSystemConfig;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.handler.IUserCommandHandler;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.SkillCaster;
import net.sf.l2jdev.gameserver.model.skill.SkillCastingType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;

/**
 * Unstuck user command.
 */
public class Unstuck implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		52
	};

	@Override
	public boolean onCommand(int id, Player player)
	{
		if (player.isJailed())
		{
			player.sendMessage("You cannot use this function while you are jailed.");
			return false;
		}

		if (FactionSystemConfig.FACTION_SYSTEM_ENABLED && !player.isGood() && !player.isEvil())
		{
			player.sendMessage("You cannot use this function while you are neutral.");
			return false;
		}

		final int unstuckTimer = (player.getAccessLevel().isGm() ? 1000 : PlayerConfig.UNSTUCK_INTERVAL * 1000);

		if (player.isInOlympiadMode())
		{
			player.sendPacket(SystemMessageId.THE_SKILL_CANNOT_BE_USED_IN_THE_OLYMPIAD);
			return false;
		}

		if (player.isCastingNow(SkillCaster::isAnyNormalType) || player.isMovementDisabled() || player.isMuted() || player.isAlikeDead() || player.inObserverMode() || player.isCombatFlagEquipped())
		{
			return false;
		}

		final Skill escape = SkillData.getInstance().getSkill(2099, 1); // 5 minutes escape
		final Skill gmEscape = SkillData.getInstance().getSkill(2100, 1); // 1 second escape
		if (player.getAccessLevel().isGm())
		{
			if (gmEscape != null)
			{
				player.doCast(gmEscape);
				return true;
			}

			player.sendMessage("You use Escape: 1 second.");
		}
		else if ((PlayerConfig.UNSTUCK_INTERVAL == 300) && (escape != null))
		{
			// If unstuck is default (5min), send retail system message.
			player.sendPacket(SystemMessageId.YOU_ARE_STUCK_AND_WILL_BE_TELEPORTED_TO_THE_NEAREST_VILLAGE_IN_5_MIN);
			player.stopMove(null);
			player.abortCast();
			player.doCast(escape);
			return true;
		}
		else
		{
			final SkillCaster skillCaster = SkillCaster.castSkill(player, player.getTarget(), escape, null, SkillCastingType.NORMAL, false, false, unstuckTimer);
			if (skillCaster == null)
			{
				player.sendPacket(ActionFailed.get(SkillCastingType.NORMAL));
				player.getAI().setIntention(Intention.ACTIVE);
				return false;
			}

			if (PlayerConfig.UNSTUCK_INTERVAL > 100)
			{
				player.sendMessage("You use Escape: " + (unstuckTimer / 60000) + " minutes.");
			}
			else
			{
				player.sendMessage("You use Escape: " + (unstuckTimer / 1000) + " seconds.");
			}
		}

		return true;
	}

	@Override
	public int[] getCommandList()
	{
		return COMMAND_IDS;
	}
}
