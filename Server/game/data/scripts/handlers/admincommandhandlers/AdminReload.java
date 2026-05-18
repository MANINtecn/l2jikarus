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

import java.io.File;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.gameserver.cache.HtmCache;
import net.sf.l2jdev.gameserver.config.ConfigLoader;
import net.sf.l2jdev.gameserver.config.ServerConfig;
import net.sf.l2jdev.gameserver.data.sql.CrestTable;
import net.sf.l2jdev.gameserver.data.xml.AdenLaboratoryData;
import net.sf.l2jdev.gameserver.data.xml.AdminData;
import net.sf.l2jdev.gameserver.data.xml.AppearanceItemData;
import net.sf.l2jdev.gameserver.data.xml.ArmorSetData;
import net.sf.l2jdev.gameserver.data.xml.AttendanceRewardData;
import net.sf.l2jdev.gameserver.data.xml.BuyListData;
import net.sf.l2jdev.gameserver.data.xml.CombinationItemsData;
import net.sf.l2jdev.gameserver.data.xml.DoorData;
import net.sf.l2jdev.gameserver.data.xml.EnchantItemData;
import net.sf.l2jdev.gameserver.data.xml.EnchantItemGroupsData;
import net.sf.l2jdev.gameserver.data.xml.EnchantItemOptionsData;
import net.sf.l2jdev.gameserver.data.xml.EquipmentUpgradeData;
import net.sf.l2jdev.gameserver.data.xml.FishingData;
import net.sf.l2jdev.gameserver.data.xml.ItemCrystallizationData;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.data.xml.LimitShopClanData;
import net.sf.l2jdev.gameserver.data.xml.LimitShopCraftData;
import net.sf.l2jdev.gameserver.data.xml.LimitShopData;
import net.sf.l2jdev.gameserver.data.xml.MultisellData;
import net.sf.l2jdev.gameserver.data.xml.NpcData;
import net.sf.l2jdev.gameserver.data.xml.NpcNameLocalisationData;
import net.sf.l2jdev.gameserver.data.xml.OptionData;
import net.sf.l2jdev.gameserver.data.xml.PrimeShopData;
import net.sf.l2jdev.gameserver.data.xml.RandomCraftData;
import net.sf.l2jdev.gameserver.data.xml.SayuneData;
import net.sf.l2jdev.gameserver.data.xml.SendMessageLocalisationData;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.data.xml.TeleporterData;
import net.sf.l2jdev.gameserver.data.xml.TransformData;
import net.sf.l2jdev.gameserver.data.xml.VariationData;
import net.sf.l2jdev.gameserver.handler.IAdminCommandHandler;
import net.sf.l2jdev.gameserver.managers.CursedWeaponsManager;
import net.sf.l2jdev.gameserver.managers.FakePlayerChatManager;
import net.sf.l2jdev.gameserver.managers.InstanceManager;
import net.sf.l2jdev.gameserver.managers.ScriptManager;
import net.sf.l2jdev.gameserver.managers.WalkingManager;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.NpcStringId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.scripting.ScriptEngine;

/**
 * @author NosBit, BAN-JDEV
 */
public class AdminReload implements IAdminCommandHandler
{
	private static final Logger LOGGER = Logger.getLogger(AdminReload.class.getName());

	private static final String[] ADMIN_COMMANDS =
	{
		"admin_reload"
	};

	private static final String RELOAD_USAGE = "Usage: //reload <config|access|npc|quest [quest_id|quest_name]|walker|htm[l] [file|directory]|multisell|buylist|teleport|skill|item|door|effect|handler|enchant|options|fishing>";

	@Override
	public boolean onCommand(String command, Player activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken();
		if (actualCommand.equalsIgnoreCase("admin_reload"))
		{
			if (!st.hasMoreTokens())
			{
				AdminHtml.showAdminHtml(activeChar, "reload.htm");
				activeChar.sendMessage(RELOAD_USAGE);
				return true;
			}

			final String type = st.nextToken();
			switch (type.toLowerCase())
			{
				case "config":
				{
					ConfigLoader.init();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Configs.");
					break;
				}
				case "access":
				{
					AdminData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Access.");
					break;
				}
				case "npc":
				{
					NpcData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Npcs.");
					break;
				}
				case "quest":
				{
					if (st.hasMoreElements())
					{
						final String value = st.nextToken();
						if (!StringUtil.isNumeric(value))
						{
							ScriptManager.getInstance().reload(value);
							AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Quest Name:" + value + ".");
						}
						else
						{
							final int questId = Integer.parseInt(value);
							ScriptManager.getInstance().reload(questId);
							AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Quest ID:" + questId + ".");
						}
					}
					else
					{
						ScriptManager.getInstance().reloadAllScripts();
						activeChar.sendSysMessage("All scripts have been reloaded.");
						AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Quests.");
					}
					break;
				}
				case "walker":
				{
					WalkingManager.getInstance().load();
					activeChar.sendSysMessage("All walkers have been reloaded");
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Walkers.");
					break;
				}
				case "htm":
				case "html":
				{
					if (st.hasMoreElements())
					{
						final String path = st.nextToken();
						final File file = new File(ServerConfig.DATAPACK_ROOT, "data/html/" + path);
						if (file.exists())
						{
							HtmCache.getInstance().reload(file);
							AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Htm File:" + file.getName() + ".");
						}
						else
						{
							activeChar.sendSysMessage("File or Directory does not exist.");
						}
					}
					else
					{
						HtmCache.getInstance().reload();
						activeChar.sendSysMessage("Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " megabytes on " + HtmCache.getInstance().getLoadedFiles() + " files loaded");
						AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Htms.");
					}
					break;
				}
				case "multisell":
				{
					MultisellData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Multisells.");
					break;
				}
				case "buylist":
				{
					BuyListData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Buylists.");
					break;
				}
				case "teleport":
				{
					TeleporterData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Teleports.");
					break;
				}
				case "skill":
				{
					SkillData.getInstance().reload();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Skills.");
					break;
				}
				case "item":
				{
					ItemData.getInstance().reload();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Items.");
					break;
				}
				case "door":
				{
					DoorData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Doors.");
					break;
				}
				case "zone":
				{
					ZoneManager.getInstance().reload();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Zones.");
					break;
				}
				case "cw":
				{
					CursedWeaponsManager.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Cursed Weapons.");
					break;
				}
				case "crest":
				{
					CrestTable.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Crests.");
					break;
				}
				case "effect":
				{
					try
					{
						ScriptEngine.getInstance().executeScript(ScriptEngine.EFFECT_MASTER_HANDLER_FILE);
						AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded effect master handler.");
					}
					catch (Exception e)
					{
						LOGGER.log(Level.WARNING, "Failed executing effect master handler!", e);
						activeChar.sendSysMessage("Error reloading effect master handler!");
					}
					break;
				}
				case "handler":
				{
					try
					{
						ScriptEngine.getInstance().executeScript(ScriptEngine.MASTER_HANDLER_FILE);
						AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded master handler.");
					}
					catch (Exception e)
					{
						LOGGER.log(Level.WARNING, "Failed executing master handler!", e);
						activeChar.sendSysMessage("Error reloading master handler!");
					}
					break;
				}
				case "enchant":
				{
					EnchantItemOptionsData.getInstance().load();
					EnchantItemGroupsData.getInstance().load();
					EnchantItemData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded item enchanting data.");
					break;
				}
				case "transform":
				{
					TransformData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded transform data.");
					break;
				}
				case "crystalizable":
				{
					ItemCrystallizationData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded item crystalization data.");
					break;
				}
				case "primeshop":
				{
					PrimeShopData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Prime Shop data.");
					break;
				}
				case "limitshop":
				{
					LimitShopData.getInstance().load();
					LimitShopCraftData.getInstance().load();
					LimitShopClanData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Limit Shop data.");
					break;
				}
				case "appearance":
				{
					AppearanceItemData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded appearance item data.");
					break;
				}
				case "sayune":
				{
					SayuneData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Sayune data.");
					break;
				}
				case "sets":
				{
					ArmorSetData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Armor sets data.");
					break;
				}
				case "options":
				{
					OptionData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Options data.");
					break;
				}
				case "fishing":
				{
					FishingData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Fishing data.");
					break;
				}
				case "attendance":
				{
					AttendanceRewardData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Attendance Reward data.");
					break;
				}
				case "fakeplayerchat":
				{
					FakePlayerChatManager.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Fake Player Chat data.");
					break;
				}
				case "localisations":
				{
					SystemMessageId.loadLocalisations();
					NpcStringId.loadLocalisations();
					SendMessageLocalisationData.getInstance().load();
					NpcNameLocalisationData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Localisation data.");
					break;
				}
				case "instance":
				{
					InstanceManager.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Instances data.");
					break;
				}
				case "combination":
				{
					CombinationItemsData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Combination data.");
					break;
				}
				case "equipmentupgrade":
				{
					EquipmentUpgradeData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Equipment Upgrade data.");
					break;
				}
				case "randomcraft":
				{
					RandomCraftData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Random Craft data.");
					break;
				}
				case "variation":
				{
					VariationData.getInstance().load();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Variation data.");
					break;
				}
				case "adenlab":
				{
					AdenLaboratoryData.getInstance().reload();
					AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Aden Laboratory data.");
					break;
				}
				default:
				{
					activeChar.sendMessage(RELOAD_USAGE);
					return true;
				}
			}

			activeChar.sendSysMessage("WARNING: There are several known issues regarding this feature. Reloading server data during runtime is STRONGLY NOT RECOMMENDED for live servers, just for developing environments.");
		}

		return true;
	}

	@Override
	public String[] getCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
