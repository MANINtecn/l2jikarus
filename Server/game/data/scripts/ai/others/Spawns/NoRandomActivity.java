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
package ai.others.Spawns;

import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.script.Script;
import net.sf.l2jdev.gameserver.model.spawns.SpawnGroup;
import net.sf.l2jdev.gameserver.model.spawns.SpawnTemplate;

/**
 * @author UnAfraid
 */
public class NoRandomActivity extends Script
{
	private NoRandomActivity()
	{
	}

	@Override
	public void onSpawnNpc(SpawnTemplate template, SpawnGroup group, Npc npc)
	{
		final StatSet parameters = template.getParameters();
		if (parameters == null)
		{
			return;
		}

		npc.setRandomAnimation(!parameters.getBoolean("disableRandomAnimation", false));
		final boolean randomWalk = !parameters.getBoolean("disableRandomWalk", false);
		npc.setRandomWalking(randomWalk);

		final Spawn spawn = npc.getSpawn();
		if (spawn == null)
		{
			return;
		}

		spawn.setRandomWalking(randomWalk);
	}

	public static void main(String[] args)
	{
		new NoRandomActivity();
	}
}
