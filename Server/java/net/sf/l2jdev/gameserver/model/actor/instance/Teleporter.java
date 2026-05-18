package net.sf.l2jdev.gameserver.model.actor.instance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.gameserver.data.xml.TeleporterData;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.TeleportType;
import net.sf.l2jdev.gameserver.model.actor.holders.npc.TeleporterQuestRecommendationHolder;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.script.QuestState;
import net.sf.l2jdev.gameserver.model.teleporter.TeleportHolder;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.teleports.ExShowTeleportUi;

public class Teleporter extends Merchant
{
	private static final Logger LOGGER = Logger.getLogger(Teleporter.class.getName());
	private static final Map<Integer, List<TeleporterQuestRecommendationHolder>> QUEST_RECOMENDATIONS = new HashMap<>();

	public Teleporter(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.Teleporter);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return attacker.isMonster() || super.isAutoAttackable(attacker);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String var4 = st.nextToken();
		switch (var4)
		{
			case "showNoblesSelect":
				this.sendHtmlMessage(player, "data/html/teleporter/" + (player.isNoble() ? "nobles_select" : "not_nobles") + ".htm");
				break;
			case "showTeleports":
			{
				String listName = st.hasMoreTokens() ? st.nextToken() : TeleportType.NORMAL.name();
				TeleportHolder holder = TeleporterData.getInstance().getHolder(this.getId(), listName);
				if (holder == null)
				{
					LOGGER.warning(player + " requested show teleports for list with name " + listName + " at NPC " + this.getId() + "!");
					return;
				}
				holder.showTeleportList(player, this);
				break;
			}
			case "showTeleportList":
				player.sendPacket(ExShowTeleportUi.STATIC_PACKET);
				break;
			case "showTeleportsHunting":
			{
				String huntListName = st.hasMoreTokens() ? st.nextToken() : TeleportType.HUNTING.name();
				TeleportHolder huntHolder = TeleporterData.getInstance().getHolder(this.getId(), huntListName);
				if (huntHolder == null)
				{
					LOGGER.warning(player + " requested show teleports for hunting list with name " + huntListName + " at NPC " + this.getId() + "!");
					return;
				}
				huntHolder.showTeleportList(player, this);
				break;
			}
			case "teleport":
			{
				if (st.countTokens() != 2)
				{
					LOGGER.warning(player + " send unhandled teleport command: " + command);
					return;
				}
				String tpListName = st.nextToken();
				TeleportHolder tpHolder = TeleporterData.getInstance().getHolder(this.getId(), tpListName);
				if (tpHolder == null)
				{
					LOGGER.warning(player + " requested unknown teleport list: " + tpListName + " for npc: " + this.getId() + "!");
					return;
				}
				tpHolder.doTeleport(player, this, this.parseNextInt(st, -1));
				break;
			}
			case "chat":
				int val = 0;

				try
				{
					val = Integer.parseInt(command.substring(5));
				}
				catch (NumberFormatException | IndexOutOfBoundsException var8)
				{
				}

				this.showChatWindow(player, val);
				break;
			default:
				super.onBypassFeedback(player, command);
		}
	}

	public int parseNextInt(StringTokenizer st, int defaultVal)
	{
		if (st.hasMoreTokens())
		{
			String token = st.nextToken();
			if (StringUtil.isNumeric(token))
			{
				return Integer.parseInt(token);
			}
		}

		return defaultVal;
	}

	@Override
	public String getHtmlPath(int npcId, int value, Player player)
	{
		String pom;
		if (value == 0)
		{
			pom = String.valueOf(npcId);
			if (player != null && QUEST_RECOMENDATIONS.containsKey(npcId))
			{
				for (TeleporterQuestRecommendationHolder rec : QUEST_RECOMENDATIONS.get(npcId))
				{
					QuestState qs = player.getQuestState(rec.getQuestName());
					if (qs != null && qs.isStarted())
					{
						for (int cond : rec.getConditions())
						{
							if (cond == -1 || qs.isCond(cond))
							{
								pom = rec.getHtml();
								return "data/html/teleporter/" + pom + ".htm";
							}
						}
					}
				}
			}
		}
		else
		{
			pom = npcId + "-" + value;
		}

		return "data/html/teleporter/" + pom + ".htm";
	}

	@Override
	public void showChatWindow(Player player)
	{
		if (CastleManager.getInstance().getCastle(this) == null)
		{
			super.showChatWindow(player);
		}
		else
		{
			String filename = "data/html/teleporter/castleteleporter-no.htm";
			if (player.getClan() != null && this.getCastle().getOwnerId() == player.getClanId())
			{
				filename = this.getHtmlPath(this.getId(), 0, player);
			}
			else if (this.getCastle().getSiege().isInProgress())
			{
				filename = "data/html/teleporter/castleteleporter-busy.htm";
			}

			this.sendHtmlMessage(player, filename);
		}
	}

	private void sendHtmlMessage(Player player, String filename)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
		html.setFile(player, filename);
		html.replace("%objectId%", String.valueOf(this.getObjectId()));
		html.replace("%npcname%", this.getName());
		player.sendPacket(html);
	}
}
