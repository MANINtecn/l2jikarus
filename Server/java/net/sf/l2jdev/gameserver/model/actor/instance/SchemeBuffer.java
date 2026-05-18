package net.sf.l2jdev.gameserver.model.actor.instance;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.custom.SchemeBufferConfig;
import net.sf.l2jdev.gameserver.data.SchemeBufferTable;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2jdev.gameserver.util.HtmlUtil;

public class SchemeBuffer extends Npc
{
	public static final int PAGE_LIMIT = 6;

	public SchemeBuffer(NpcTemplate template)
	{
		super(template);
	}

	@Override
	public void onBypassFeedback(Player player, String commandValue)
	{
		String command = commandValue.replace("createscheme ", "createscheme;");
		StringTokenizer st = new StringTokenizer(command, ";");
		String currentCommand = st.nextToken();
		if (currentCommand.startsWith("menu"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
			html.setFile(player, this.getHtmlPath(this.getId(), 0, player));
			html.replace("%objectId%", this.getObjectId());
			player.sendPacket(html);
		}
		else if (currentCommand.startsWith("cleanup"))
		{
			player.stopAllEffects();
			Summon summon = player.getPet();
			if (summon != null)
			{
				summon.stopAllEffects();
			}

			player.getServitors().values().forEach(servitor -> servitor.stopAllEffects());
			NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
			html.setFile(player, this.getHtmlPath(this.getId(), 0, player));
			html.replace("%objectId%", this.getObjectId());
			player.sendPacket(html);
		}
		else if (currentCommand.startsWith("heal"))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
			Summon summon = player.getPet();
			if (summon != null)
			{
				summon.setCurrentHpMp(summon.getMaxHp(), summon.getMaxMp());
			}

			player.getServitors().values().forEach(servitor -> servitor.setCurrentHpMp(servitor.getMaxHp(), servitor.getMaxMp()));
			NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
			html.setFile(player, this.getHtmlPath(this.getId(), 0, player));
			html.replace("%objectId%", this.getObjectId());
			player.sendPacket(html);
		}
		else if (currentCommand.startsWith("support"))
		{
			this.showGiveBuffsWindow(player);
		}
		else if (currentCommand.startsWith("givebuffs"))
		{
			String schemeName = st.nextToken();
			int cost = Integer.parseInt(st.nextToken());
			boolean buffSummons = st.hasMoreTokens() && st.nextToken().equalsIgnoreCase("pet");
			if (buffSummons && player.getPet() == null && !player.hasServitors())
			{
				player.sendMessage("You don't have a pet.");
			}
			else if (cost == 0 || SchemeBufferConfig.BUFFER_ITEM_ID == 57 && player.reduceAdena(ItemProcessType.FEE, cost, this, true) || SchemeBufferConfig.BUFFER_ITEM_ID != 57 && player.destroyItemByItemId(ItemProcessType.FEE, SchemeBufferConfig.BUFFER_ITEM_ID, cost, player, true))
			{
				for (int skillId : SchemeBufferTable.getInstance().getScheme(player.getObjectId(), schemeName))
				{
					Skill skill = SkillData.getInstance().getSkill(skillId, SchemeBufferTable.getInstance().getAvailableBuff(skillId).getLevel());
					if (buffSummons)
					{
						if (player.getPet() != null)
						{
							skill.applyEffects(this, player.getPet());
						}

						player.getServitors().values().forEach(servitor -> skill.applyEffects(this, servitor));
					}
					else
					{
						skill.applyEffects(this, player);
					}
				}
			}
		}
		else if (currentCommand.startsWith("editschemes"))
		{
			this.showEditSchemeWindow(player, st.nextToken(), st.nextToken(), Integer.parseInt(st.nextToken()));
		}
		else if (currentCommand.startsWith("skill"))
		{
			String groupType = st.nextToken();
			String schemeName = st.nextToken();
			int skillIdx = Integer.parseInt(st.nextToken());
			int page = Integer.parseInt(st.nextToken());
			List<Integer> skills = SchemeBufferTable.getInstance().getScheme(player.getObjectId(), schemeName);
			if (currentCommand.startsWith("skillselect") && !schemeName.equalsIgnoreCase("none"))
			{
				Skill skill = SkillData.getInstance().getSkill(skillIdx, SkillData.getInstance().getMaxLevel(skillIdx));
				if (skill.isDance())
				{
					if (getCountOf(skills, true) < PlayerConfig.DANCES_MAX_AMOUNT)
					{
						skills.add(skillIdx);
					}
					else
					{
						player.sendMessage("This scheme has reached the maximum amount of dances/songs.");
					}
				}
				else if (getCountOf(skills, false) < player.getStat().getMaxBuffCount())
				{
					skills.add(skillIdx);
				}
				else
				{
					player.sendMessage("This scheme has reached the maximum amount of buffs.");
				}
			}
			else if (currentCommand.startsWith("skillunselect"))
			{
				skills.remove(Integer.valueOf(skillIdx));
			}

			this.showEditSchemeWindow(player, groupType, schemeName, page);
		}
		else if (currentCommand.startsWith("createscheme"))
		{
			try
			{
				String schemeName = st.nextToken().trim();
				if (schemeName.length() > 14)
				{
					player.sendMessage("Scheme's name must contain up to 14 chars.");
					return;
				}

				if (!StringUtil.isAlphaNumeric(schemeName.replace(" ", "").replace(".", "").replace(",", "").replace("-", "").replace("+", "").replace("!", "").replace("?", "")))
				{
					player.sendMessage("Please use plain alphanumeric characters.");
					return;
				}

				Map<String, List<Integer>> schemes = SchemeBufferTable.getInstance().getPlayerSchemes(player.getObjectId());
				if (schemes != null)
				{
					if (schemes.size() == SchemeBufferConfig.BUFFER_MAX_SCHEMES)
					{
						player.sendMessage("Maximum schemes amount is already reached.");
						return;
					}

					if (schemes.containsKey(schemeName))
					{
						player.sendMessage("The scheme name already exists.");
						return;
					}
				}

				SchemeBufferTable.getInstance().setScheme(player.getObjectId(), schemeName.trim(), new ArrayList<>());
				this.showGiveBuffsWindow(player);
			}
			catch (Exception var13)
			{
				player.sendMessage("Scheme's name must contain up to 14 chars.");
			}
		}
		else if (currentCommand.startsWith("deletescheme"))
		{
			try
			{
				String schemeNamex = st.nextToken();
				Map<String, List<Integer>> schemes = SchemeBufferTable.getInstance().getPlayerSchemes(player.getObjectId());
				if (schemes != null && schemes.containsKey(schemeNamex))
				{
					schemes.remove(schemeNamex);
				}
			}
			catch (Exception var12)
			{
				player.sendMessage("This scheme name is invalid.");
			}

			this.showGiveBuffsWindow(player);
		}
	}

	@Override
	public String getHtmlPath(int npcId, int value, Player player)
	{
		String filename = "";
		if (value == 0)
		{
			filename = Integer.toString(npcId);
		}
		else
		{
			filename = npcId + "-" + value;
		}

		return "data/html/mods/SchemeBuffer/" + filename + ".htm";
	}

	private void showGiveBuffsWindow(Player player)
	{
		StringBuilder sb = new StringBuilder(200);
		Map<String, List<Integer>> schemes = SchemeBufferTable.getInstance().getPlayerSchemes(player.getObjectId());
		if (schemes != null && !schemes.isEmpty())
		{
			for (Entry<String, List<Integer>> scheme : schemes.entrySet())
			{
				int cost = getFee(scheme.getValue());
				sb.append("<font color=\"LEVEL\">" + scheme.getKey() + " [" + scheme.getValue().size() + " skill(s)]" + (cost > 0 ? " - cost: " + NumberFormat.getInstance(Locale.ENGLISH).format(cost) : "") + "</font><br1>");
				sb.append("<a action=\"bypass -h npc_%objectId%_givebuffs;" + scheme.getKey() + ";" + cost + "\">Use on Me</a>&nbsp;|&nbsp;");
				sb.append("<a action=\"bypass -h npc_%objectId%_givebuffs;" + scheme.getKey() + ";" + cost + ";pet\">Use on Pet</a>&nbsp;|&nbsp;");
				sb.append("<a action=\"bypass npc_%objectId%_editschemes;Buffs;" + scheme.getKey() + ";1\">Edit</a>&nbsp;|&nbsp;");
				sb.append("<a action=\"bypass npc_%objectId%_deletescheme;" + scheme.getKey() + "\">Delete</a><br>");
			}
		}
		else
		{
			sb.append("<font color=\"LEVEL\">You haven't defined any scheme.</font>");
		}

		NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
		html.setFile(player, this.getHtmlPath(this.getId(), 1, player));
		html.replace("%schemes%", sb.toString());
		html.replace("%max_schemes%", SchemeBufferConfig.BUFFER_MAX_SCHEMES);
		html.replace("%objectId%", this.getObjectId());
		player.sendPacket(html);
	}

	private void showEditSchemeWindow(Player player, String groupType, String schemeName, int page)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
		List<Integer> schemeSkills = SchemeBufferTable.getInstance().getScheme(player.getObjectId(), schemeName);
		html.setFile(player, this.getHtmlPath(this.getId(), 2, player));
		html.replace("%schemename%", schemeName);
		html.replace("%count%", getCountOf(schemeSkills, false) + " / " + player.getStat().getMaxBuffCount() + " buffs, " + getCountOf(schemeSkills, true) + " / " + PlayerConfig.DANCES_MAX_AMOUNT + " dances/songs");
		html.replace("%typesframe%", getTypesFrame(groupType, schemeName));
		html.replace("%skilllistframe%", this.getGroupSkillList(player, groupType, schemeName, page));
		html.replace("%objectId%", this.getObjectId());
		player.sendPacket(html);
	}

	private String getGroupSkillList(Player player, String groupType, String schemeName, int pageValue)
	{
		List<Integer> skills = SchemeBufferTable.getInstance().getSkillsIdsByType(groupType);
		if (skills.isEmpty())
		{
			return "That group doesn't contain any skills.";
		}
		int max = HtmlUtil.countPageNumber(skills.size(), 6);
		int page = pageValue;
		if (pageValue > max)
		{
			page = max;
		}

		skills = skills.subList((page - 1) * 6, Math.min(page * 6, skills.size()));
		List<Integer> schemeSkills = SchemeBufferTable.getInstance().getScheme(player.getObjectId(), schemeName);
		StringBuilder sb = new StringBuilder(skills.size() * 150);
		int row = 0;

		for (int skillId : skills)
		{
			sb.append(row % 2 == 0 ? "<table width=\"280\" bgcolor=\"000000\"><tr>" : "<table width=\"280\"><tr>");
			Skill skill = SkillData.getInstance().getSkill(skillId, 1);
			if (schemeSkills.contains(skillId))
			{
				sb.append("<td height=40 width=40><img src=\"" + skill.getIcon() + "\" width=32 height=32></td><td width=190>" + skill.getName() + "<br1><font color=\"B09878\">" + SchemeBufferTable.getInstance().getAvailableBuff(skillId).getDescription() + "</font></td><td><button value=\" \" action=\"bypass npc_%objectId%_skillunselect;" + groupType + ";" + schemeName + ";" + skillId + ";" + page + "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>");
			}
			else
			{
				sb.append("<td height=40 width=40><img src=\"" + skill.getIcon() + "\" width=32 height=32></td><td width=190>" + skill.getName() + "<br1><font color=\"B09878\">" + SchemeBufferTable.getInstance().getAvailableBuff(skillId).getDescription() + "</font></td><td><button value=\" \" action=\"bypass npc_%objectId%_skillselect;" + groupType + ";" + schemeName + ";" + skillId + ";" + page + "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>");
			}

			sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
			row++;
		}

		sb.append("<br><img src=\"L2UI.SquareGray\" width=277 height=1><table width=\"100%\" bgcolor=000000><tr>");
		if (page > 1)
		{
			sb.append("<td align=left width=70><a action=\"bypass npc_" + this.getObjectId() + "_editschemes;" + groupType + ";" + schemeName + ";" + (page - 1) + "\">Previous</a></td>");
		}
		else
		{
			sb.append("<td align=left width=70>Previous</td>");
		}

		sb.append("<td align=center width=100>Page " + page + "</td>");
		if (page < max)
		{
			sb.append("<td align=right width=70><a action=\"bypass npc_" + this.getObjectId() + "_editschemes;" + groupType + ";" + schemeName + ";" + (page + 1) + "\">Next</a></td>");
		}
		else
		{
			sb.append("<td align=right width=70>Next</td>");
		}

		sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
		return sb.toString();
	}

	private static String getTypesFrame(String groupType, String schemeName)
	{
		StringBuilder sb = new StringBuilder(500);
		sb.append("<table>");
		int count = 0;

		for (String type : SchemeBufferTable.getInstance().getSkillTypes())
		{
			if (count == 0)
			{
				sb.append("<tr>");
			}

			if (groupType.equalsIgnoreCase(type))
			{
				sb.append("<td width=65>" + type + "</td>");
			}
			else
			{
				sb.append("<td width=65><a action=\"bypass npc_%objectId%_editschemes;" + type + ";" + schemeName + ";1\">" + type + "</a></td>");
			}

			if (++count == 4)
			{
				sb.append("</tr>");
				count = 0;
			}
		}

		if (!sb.toString().endsWith("</tr>"))
		{
			sb.append("</tr>");
		}

		sb.append("</table>");
		return sb.toString();
	}

	private static int getFee(List<Integer> list)
	{
		if (SchemeBufferConfig.BUFFER_STATIC_BUFF_COST > 0)
		{
			return list.size() * SchemeBufferConfig.BUFFER_STATIC_BUFF_COST;
		}
		int fee = 0;

		for (int sk : list)
		{
			fee += SchemeBufferTable.getInstance().getAvailableBuff(sk).getPrice();
		}

		return fee;
	}

	private static int getCountOf(List<Integer> skills, boolean dances)
	{
		int count = 0;

		for (int skillId : skills)
		{
			if (SkillData.getInstance().getSkill(skillId, 1).isDance() == dances)
			{
				count++;
			}
		}

		return count;
	}
}
