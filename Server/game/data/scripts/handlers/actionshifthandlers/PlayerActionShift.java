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
package handlers.actionshifthandlers;

import net.sf.l2jdev.gameserver.handler.AdminCommandHandler;
import net.sf.l2jdev.gameserver.handler.IActionShiftHandler;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;

public class PlayerActionShift implements IActionShiftHandler
{
	@Override
	public boolean onAction(Player player, WorldObject target, boolean interact)
	{
		if (player.isGM())
		{
			// Check if the GM already target this l2pcinstance
			if (player.getTarget() != target)
			{
				// Set the target of the Player player
				player.setTarget(target);
			}

			AdminCommandHandler.getInstance().onCommand(player, "admin_character_info " + target.getName(), true);
		}

		return true;
	}

	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.Player;
	}
}
