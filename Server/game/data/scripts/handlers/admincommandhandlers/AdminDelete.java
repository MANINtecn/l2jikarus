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
package handlers.admincommandhandlers;

import java.util.Collections;
import java.util.List;

import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.gameserver.data.xml.SpawnData;
import net.sf.l2jdev.gameserver.handler.AdminCommandHandler;
import net.sf.l2jdev.gameserver.handler.IAdminCommandHandler;
import net.sf.l2jdev.gameserver.managers.DatabaseSpawnManager;
import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.spawns.NpcSpawnTemplate;
import net.sf.l2jdev.gameserver.model.spawns.SpawnGroup;
import net.sf.l2jdev.gameserver.model.spawns.SpawnTemplate;
import net.sf.l2jdev.gameserver.model.zone.type.SpawnTerritory;

/**
 * @author BAN-JDEV
 */
public class AdminDelete implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_delete", // supports range parameter
		"admin_delete_group" // for territory spawns
	};

	@Override
	public boolean onCommand(String command, Player activeChar)
	{
		if (command.contains("group"))
		{
			handleDeleteGroup(activeChar);
		}
		else if (command.startsWith("admin_delete"))
		{
			final String[] split = command.split(" ");
			handleDelete(activeChar, (split.length > 1) && StringUtil.isNumeric(split[1]) ? Integer.parseInt(split[1]) : 0);
		}

		return true;
	}

	private static void handleDelete(Player player, int range)
	{
		if (range > 0)
		{
			World.getInstance().forEachVisibleObjectInRange(player, Npc.class, range, target -> deleteNpc(player, target));
			return;
		}

		final WorldObject obj = player.getTarget();
		if (obj instanceof Npc)
		{
			deleteNpc(player, obj.asNpc());
		}
		else
		{
			player.sendSysMessage("Incorrect target.");
		}
	}

	private static void handleDeleteGroup(Player player)
	{
		final WorldObject obj = player.getTarget();
		if (obj instanceof Npc)
		{
			deleteGroup(player, obj.asNpc());
		}
		else
		{
			player.sendSysMessage("Incorrect target.");
		}
	}

	private static void deleteNpc(Player player, Npc target)
	{
		final Spawn spawn = target.getSpawn();
		if (spawn != null)
		{
			final NpcSpawnTemplate npcSpawnTemplate = spawn.getNpcSpawnTemplate();
			final SpawnGroup group = npcSpawnTemplate != null ? npcSpawnTemplate.getGroup() : null;
			List<SpawnTerritory> territories = group != null ? group.getTerritories() : Collections.emptyList();
			if (territories.isEmpty())
			{
				final SpawnTemplate spawnTemplate = npcSpawnTemplate != null ? npcSpawnTemplate.getSpawnTemplate() : null;
				if (spawnTemplate != null)
				{
					territories = spawnTemplate.getTerritories();
				}
			}

			if (territories.isEmpty())
			{
				target.deleteMe();
				spawn.stopRespawn();
				if (DatabaseSpawnManager.getInstance().isDefined(spawn.getId()))
				{
					DatabaseSpawnManager.getInstance().deleteSpawn(spawn, true);
				}
				else
				{
					SpawnData.getInstance().deleteSpawn(spawn);
				}

				player.sendSysMessage("Deleted " + target.getName() + " from " + target.getObjectId() + ".");
			}
			else
			{
				AdminCommandHandler.getInstance().onCommand(player, AdminDelete.ADMIN_COMMANDS[1], true);
			}
		}
	}

	private static void deleteGroup(Player player, Npc target)
	{
		final Spawn spawn = target.getSpawn();
		if (spawn != null)
		{
			final NpcSpawnTemplate npcSpawnTemplate = spawn.getNpcSpawnTemplate();
			final SpawnGroup group = npcSpawnTemplate != null ? npcSpawnTemplate.getGroup() : null;
			List<SpawnTerritory> territories = group != null ? group.getTerritories() : Collections.emptyList();
			boolean simpleTerritory = false;
			if (territories.isEmpty())
			{
				final SpawnTemplate spawnTemplate = npcSpawnTemplate != null ? npcSpawnTemplate.getSpawnTemplate() : null;
				if (spawnTemplate != null)
				{
					territories = spawnTemplate.getTerritories();
					simpleTerritory = true;
				}
			}

			if (territories.isEmpty())
			{
				player.sendSysMessage("Incorrect target.");
			}
			else
			{
				target.deleteMe();
				spawn.stopRespawn();
				if (DatabaseSpawnManager.getInstance().isDefined(spawn.getId()))
				{
					DatabaseSpawnManager.getInstance().deleteSpawn(spawn, true);
				}
				else
				{
					SpawnData.getInstance().deleteSpawn(spawn);
				}

				if (group != null)
				{
					for (NpcSpawnTemplate template : group.getSpawns())
					{
						template.despawn();
					}
				}
				else if (simpleTerritory && (npcSpawnTemplate != null))
				{
					npcSpawnTemplate.despawn();
				}

				player.sendSysMessage("Deleted " + target.getName() + " group from " + target.getObjectId() + ".");
			}
		}
	}

	@Override
	public String[] getCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
