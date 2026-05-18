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
package ai.others;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.ListenerRegisterType;
import net.sf.l2jdev.gameserver.model.events.annotations.RegisterEvent;
import net.sf.l2jdev.gameserver.model.events.annotations.RegisterType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLogin;
import net.sf.l2jdev.gameserver.model.script.Script;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.model.variables.PlayerVariables;

/**
 * @author BAN-JDEV
 */
public class FirstLoginBuffs extends Script
{
	private static final SkillHolder[] SAMURAI_BUFFS =
	{
		new SkillHolder(89117, 1), // Wind
		new SkillHolder(89118, 1), // Forest
		new SkillHolder(89119, 1), // Fire
		new SkillHolder(89120, 1), // Mountain
	};

	private static final SkillHolder[] GENERAL_BUFFS =
	{
		new SkillHolder(1068, 1), // Might
		new SkillHolder(1040, 1), // Shield
		new SkillHolder(1204, 1), // Wind Walk
		new SkillHolder(1086, 1), // Haste
		new SkillHolder(1085, 1), // Acumen
	};

	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		if (!PlayerConfig.ENABLE_FIRST_LOGIN_BUFFS)
		{
			return;
		}

		final Player player = event.getPlayer();
		if (player == null)
		{
			return;
		}

		final PlayerVariables variables = player.getVariables();
		if (!variables.contains(PlayerVariables.FIRST_LOGIN_BUFF))
		{
			return;
		}

		variables.remove(PlayerVariables.FIRST_LOGIN_BUFF);

		switch (player.getPlayerClass())
		{
			case ASHIGARU:
			{
				for (SkillHolder holder : SAMURAI_BUFFS)
				{
					holder.getSkill().applyEffects(player, player);
				}
				break;
			}
			case WARG_0:
			case BLOOD_ROSE_0:
			{
				for (SkillHolder holder : GENERAL_BUFFS)
				{
					holder.getSkill().applyEffects(player, player);
				}
				break;
			}
			default:
			{
				for (SkillHolder holder : GENERAL_BUFFS)
				{
					holder.getSkill().applyEffects(player, player);
				}
				break;
			}
		}
	}

	public static void main(String[] args)
	{
		new FirstLoginBuffs();
	}
}
