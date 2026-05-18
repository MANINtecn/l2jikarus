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

import net.sf.l2jdev.gameserver.cache.HtmCache;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.EnchantItemGroupsData;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.handler.IAdminCommandHandler;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author CostyKiller
 */
public class AdminEnchant implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_seteh", // 6
		"admin_setec", // 10
		"admin_seteg", // 9
		"admin_setel", // 11
		"admin_seteb", // 12
		"admin_setew", // 7
		"admin_setes", // 8
		"admin_setle", // 1
		"admin_setre", // 2
		"admin_setlf", // 4
		"admin_setrf", // 5
		"admin_seten", // 3
		"admin_setun", // 0
		"admin_setba", // 13
		"admin_setbe", // belt
		"admin_seth1", // L Hair
		"admin_seth2", // R Hair
		"admin_setbr", // Brooch
		"admin_setbt", // L Bracelet
		"admin_setsb", // Seed (R) Bracelet
		"admin_seta1", // Agathion SLOT1
		"admin_seta2", // Agathion SLOT2
		"admin_seta3", // Agathion SLOT3
		"admin_seta4", // Agathion SLOT4
		"admin_seta5", // Agathion SLOT5
		"admin_sett1", // Talisman SLOT1
		"admin_sett2", // Talisman SLOT2
		"admin_sett3", // Talisman SLOT3
		"admin_sett4", // Talisman SLOT4
		"admin_sett5", // Talisman SLOT5
		"admin_sett6", // Talisman SLOT6
		"admin_setj1", // Jewel SLOT1
		"admin_setj2", // Jewel SLOT2
		"admin_setj3", // Jewel SLOT3
		"admin_setj4", // Jewel SLOT4
		"admin_setj5", // Jewel SLOT5
		"admin_setj6", // Jewel SLOT6
		"admin_agathion",
		"admin_talisman",
		"admin_jewels",
		"admin_enchant"
	};

	@Override
	public boolean onCommand(String command, Player activeChar)
	{
		int currentPage = 0;
		if (command.equals("admin_enchant"))
		{
			currentPage = 1;
			showMainPage(activeChar, currentPage);
		}
		else if (command.equals("admin_agathion"))
		{
			currentPage = 2;
			showMainPage(activeChar, currentPage);
		}
		else if (command.equals("admin_talisman"))
		{
			currentPage = 3;
			showMainPage(activeChar, currentPage);
		}
		else if (command.equals("admin_jewels"))
		{
			currentPage = 4;
			showMainPage(activeChar, currentPage);
		}
		else
		{
			int slot = -1;
			if (command.startsWith("admin_seteh"))
			{
				slot = Inventory.PAPERDOLL_HEAD;
			}
			else if (command.startsWith("admin_setec"))
			{
				slot = Inventory.PAPERDOLL_CHEST;
			}
			else if (command.startsWith("admin_seteg"))
			{
				slot = Inventory.PAPERDOLL_GLOVES;
			}
			else if (command.startsWith("admin_seteb"))
			{
				slot = Inventory.PAPERDOLL_FEET;
			}
			else if (command.startsWith("admin_setel"))
			{
				slot = Inventory.PAPERDOLL_LEGS;
			}
			else if (command.startsWith("admin_setew"))
			{
				slot = Inventory.PAPERDOLL_RHAND;
			}
			else if (command.startsWith("admin_setes"))
			{
				slot = Inventory.PAPERDOLL_LHAND;
			}
			else if (command.startsWith("admin_setle"))
			{
				slot = Inventory.PAPERDOLL_LEAR;
			}
			else if (command.startsWith("admin_setre"))
			{
				slot = Inventory.PAPERDOLL_REAR;
			}
			else if (command.startsWith("admin_setlf"))
			{
				slot = Inventory.PAPERDOLL_LFINGER;
			}
			else if (command.startsWith("admin_setrf"))
			{
				slot = Inventory.PAPERDOLL_RFINGER;
			}
			else if (command.startsWith("admin_seten"))
			{
				slot = Inventory.PAPERDOLL_NECK;
			}
			else if (command.startsWith("admin_setun"))
			{
				slot = Inventory.PAPERDOLL_UNDER;
			}
			else if (command.startsWith("admin_setba"))
			{
				slot = Inventory.PAPERDOLL_CLOAK;
			}
			else if (command.startsWith("admin_setbe"))
			{
				slot = Inventory.PAPERDOLL_BELT;
			}
			else if (command.startsWith("admin_seth1"))
			{
				slot = Inventory.PAPERDOLL_HAIR;
			}
			else if (command.startsWith("admin_seth2"))
			{
				slot = Inventory.PAPERDOLL_HAIR2;
			}
			else if (command.startsWith("admin_setbr"))
			{
				slot = Inventory.PAPERDOLL_BROOCH;
			}
			else if (command.startsWith("admin_setj1"))
			{
				slot = Inventory.PAPERDOLL_BROOCH_JEWEL1;
			}
			else if (command.startsWith("admin_setj2"))
			{
				slot = Inventory.PAPERDOLL_BROOCH_JEWEL2;
			}
			else if (command.startsWith("admin_setj3"))
			{
				slot = Inventory.PAPERDOLL_BROOCH_JEWEL3;
			}
			else if (command.startsWith("admin_setj4"))
			{
				slot = Inventory.PAPERDOLL_BROOCH_JEWEL4;
			}
			else if (command.startsWith("admin_setj5"))
			{
				slot = Inventory.PAPERDOLL_BROOCH_JEWEL5;
			}
			else if (command.startsWith("admin_setj6"))
			{
				slot = Inventory.PAPERDOLL_BROOCH_JEWEL6;
			}
			else if (command.startsWith("admin_setbt")) // bracelet
			{
				slot = Inventory.PAPERDOLL_RBRACELET;
			}
			else if (command.startsWith("admin_setsb")) // seed bracelet
			{
				slot = Inventory.PAPERDOLL_LBRACELET;
			}
			else if (command.startsWith("admin_seta1"))
			{
				slot = Inventory.PAPERDOLL_AGATHION1;
			}
			else if (command.startsWith("admin_seta2"))
			{
				slot = Inventory.PAPERDOLL_AGATHION2;
			}
			else if (command.startsWith("admin_seta3"))
			{
				slot = Inventory.PAPERDOLL_AGATHION3;
			}
			else if (command.startsWith("admin_seta4"))
			{
				slot = Inventory.PAPERDOLL_AGATHION4;
			}
			else if (command.startsWith("admin_seta5"))
			{
				slot = Inventory.PAPERDOLL_AGATHION5;
			}
			else if (command.startsWith("admin_sett1"))
			{
				slot = Inventory.PAPERDOLL_DECO1;
			}
			else if (command.startsWith("admin_sett2"))
			{
				slot = Inventory.PAPERDOLL_DECO2;
			}
			else if (command.startsWith("admin_sett3"))
			{
				slot = Inventory.PAPERDOLL_DECO3;
			}
			else if (command.startsWith("admin_sett4"))
			{
				slot = Inventory.PAPERDOLL_DECO4;
			}
			else if (command.startsWith("admin_sett5"))
			{
				slot = Inventory.PAPERDOLL_DECO5;
			}
			else if (command.startsWith("admin_sett6"))
			{
				slot = Inventory.PAPERDOLL_DECO6;
			}

			if (slot != -1)
			{
				try
				{
					final int enchIn = Integer.parseInt(command.substring(12));
					int ench = enchIn;

					// check value
					if ((ench < 0) || (ench > 127))
					{
						activeChar.sendSysMessage("New enchant value can only be 0 - 127.");
					}
					else
					{
						setEnchant(activeChar, ench, slot);
					}
				}
				catch (StringIndexOutOfBoundsException e)
				{
					// Quality of life change. If no input - set enchant value to 0.
					activeChar.sendSysMessage("Auto-Set Enchant value to 0.");
					setEnchant(activeChar, 0, slot);
				}
				catch (NumberFormatException e)
				{
					activeChar.sendSysMessage("Please specify a valid new enchant value.");
				}
			}

			// show the enchant menu after an action
			showMainPage(activeChar, currentPage);
		}

		return true;
	}

	private static void setEnchant(Player activeChar, int ench, int slot)
	{
		// Get the target.
		final Player player = activeChar.getTarget() != null ? activeChar.getTarget().asPlayer() : activeChar;
		if (player == null)
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}

		// Now we need to find the equipped weapon of the targeted character...
		Item itemInstance = null;

		// Only attempt to enchant if there is a weapon equipped.
		final Item paperdollInstance = player.getInventory().getPaperdollItem(slot);
		if ((paperdollInstance != null) && (paperdollInstance.getLocationSlot() == slot))
		{
			itemInstance = paperdollInstance;
		}

		if (itemInstance != null)
		{
			final int curEnchant = itemInstance.getEnchantLevel();

			// Set enchant value.
			int enchant = ench;
			if (PlayerConfig.OVER_ENCHANT_PROTECTION && !player.isGM())
			{
				if (itemInstance.isWeapon())
				{
					if (enchant > EnchantItemGroupsData.getInstance().getMaxWeaponEnchant())
					{
						activeChar.sendSysMessage("Maximum enchantment for weapon items is " + EnchantItemGroupsData.getInstance().getMaxWeaponEnchant() + ".");
						enchant = EnchantItemGroupsData.getInstance().getMaxWeaponEnchant();
					}
				}
				else if (itemInstance.getTemplate().getType2() == ItemTemplate.TYPE2_ACCESSORY)
				{
					if (enchant > EnchantItemGroupsData.getInstance().getMaxAccessoryEnchant())
					{
						activeChar.sendSysMessage("Maximum enchantment for accessory items is " + EnchantItemGroupsData.getInstance().getMaxAccessoryEnchant() + ".");
						enchant = EnchantItemGroupsData.getInstance().getMaxAccessoryEnchant();
					}
				}
				else if (enchant > EnchantItemGroupsData.getInstance().getMaxArmorEnchant())
				{
					activeChar.sendSysMessage("Maximum enchantment for armor items is " + EnchantItemGroupsData.getInstance().getMaxArmorEnchant() + ".");
					enchant = EnchantItemGroupsData.getInstance().getMaxArmorEnchant();
				}
			}

			itemInstance.setEnchantLevel(enchant);

			// Send packets.
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(itemInstance);
			player.sendInventoryUpdate(iu);
			player.broadcastCharInfo();

			// Information.
			activeChar.sendSysMessage("Changed enchantment of " + player.getName() + "'s " + itemInstance.getTemplate().getName() + " from " + curEnchant + " to " + enchant + ".");
			player.sendMessage("Admin has changed the enchantment of your " + itemInstance.getTemplate().getName() + " from " + curEnchant + " to " + enchant + ".");
		}
	}

	private static void showMainPage(Player activeChar, int currentPage)
	{
		final Player player = activeChar.getTarget() != null ? activeChar.getTarget().asPlayer() : activeChar;
		if (player == null)
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}

		if (currentPage == 1)
		{
			AdminHtml.showAdminHtml(activeChar, "enchant.htm");
		}
		else if (currentPage == 2)
		{
			String getVars = HtmCache.getInstance().getHtm(activeChar, "data/html/admin/enchantAgathion.htm");
			Item findItem = null;
			int currentEnch = 0;
			for (int i = 0; i < 5; i++)
			{
				final ItemTemplate item = ItemData.getInstance().getTemplate(player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_AGATHION1 + i));
				findItem = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_AGATHION1 + i);
				if (findItem != null) // null check for unequipped slots
				{
					currentEnch = findItem.getEnchantLevel();
				}

				// If no agathion in slot - returns blank square icon
				if (item == null)
				{
					getVars = getVars.replace("%ag" + i + "_icon%", "L2UI_CT1.Windows.Windows_DF_TooltipBG");
					getVars = getVars.replace("%ag" + i + "_ench%", " ");
				}
				else
				{
					getVars = getVars.replace("%ag" + i + "_icon%", item.getIcon() == null ? "icon.etc_question_mark_i00" : item.getIcon());

					// if enchant value is 0 - show "blank instead of 0
					if (currentEnch != 0)
					{
						getVars = getVars.replace("%ag" + i + "_ench%", Integer.toString(currentEnch)); // send ench value
					}
					else
					{
						getVars = getVars.replace("%ag" + i + "_ench%", " "); // send "space" so displays icon correctly
					}
				}
			}

			final NpcHtmlMessage html = new NpcHtmlMessage(0, 1);
			html.setHtml(getVars);
			activeChar.sendPacket(html);
		}
		else if (currentPage == 3)
		{
			String getVars = HtmCache.getInstance().getHtm(activeChar, "data/html/admin/enchantTalisman.htm");
			Item findItem = null;
			int currentEnch = 0;
			for (int i = 0; i < 6; i++)
			{
				final ItemTemplate item = ItemData.getInstance().getTemplate(player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DECO1 + i));
				findItem = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_DECO1 + i);
				if (findItem != null) // null check for unequipped slots
				{
					currentEnch = findItem.getEnchantLevel();
				}

				// If no talisman in slot - returns blank square icon
				if (item == null)
				{
					getVars = getVars.replace("%t" + i + "_icon%", "L2UI_CT1.Windows.Windows_DF_TooltipBG");
					getVars = getVars.replace("%t" + i + "_ench%", " ");
				}
				else
				{
					getVars = getVars.replace("%t" + i + "_icon%", item.getIcon() == null ? "icon.etc_question_mark_i00" : item.getIcon());

					// if enchant value is 0 - show "blank instead of 0
					if (currentEnch != 0)
					{
						getVars = getVars.replace("%t" + i + "_ench%", Integer.toString(currentEnch)); // send ench value
					}
					else
					{
						getVars = getVars.replace("%t" + i + "_ench%", " "); // send "space" so displays icon correctly
					}
				}
			}

			final NpcHtmlMessage html = new NpcHtmlMessage(0, 1);
			html.setHtml(getVars);
			activeChar.sendPacket(html);
		}
		else if (currentPage == 4)
		{
			String getVars = HtmCache.getInstance().getHtm(activeChar, "data/html/admin/enchantJewels.htm");
			Item findItem = null;
			int currentEnch = 0;
			for (int i = 0; i < 6; i++)
			{
				final ItemTemplate item = ItemData.getInstance().getTemplate(player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_BROOCH_JEWEL1 + i));
				findItem = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_BROOCH_JEWEL1 + i);
				if (findItem != null) // null check for unequipped slots
				{
					currentEnch = findItem.getEnchantLevel();
				}

				// If no jewel in slot - returns blank square icon
				if (item == null)
				{
					getVars = getVars.replace("%j" + i + "_icon%", "L2UI_CT1.Windows.Windows_DF_TooltipBG");
					getVars = getVars.replace("%j" + i + "_ench%", " ");
				}
				else
				{
					getVars = getVars.replace("%j" + i + "_icon%", item.getIcon() == null ? "icon.etc_question_mark_i00" : item.getIcon());

					// if enchant value is 0 - show "blank instead of 0
					if (currentEnch != 0)
					{
						getVars = getVars.replace("%j" + i + "_ench%", Integer.toString(currentEnch)); // send ench value
					}
					else
					{
						getVars = getVars.replace("%j" + i + "_ench%", " "); // send "space" so displays icon correctly
					}
				}
			}

			final NpcHtmlMessage html = new NpcHtmlMessage(0, 1);
			html.setHtml(getVars);
			activeChar.sendPacket(html);
		}
	}

	@Override
	public String[] getCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
