package handlers.communityboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.cache.HtmCache;
import net.sf.l2jdev.gameserver.data.xml.ClassListData;
import net.sf.l2jdev.gameserver.handler.CommunityBoardHandler;
import net.sf.l2jdev.gameserver.handler.IItemHandler;
import net.sf.l2jdev.gameserver.handler.IParseBoardHandler;
import net.sf.l2jdev.gameserver.handler.ItemHandler;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class IkaCommunityBoard implements IParseBoardHandler
{
	private static final int MP_POTION_ID = 49854;
	private static final Map<Integer, ScheduledFuture<?>> AUTO_TASKS = new ConcurrentHashMap<>();

	private static final String[] RACES = { "Human", "Elf", "Dark Elf", "Orc", "Dwarf", "Kamael", "Sylph" };

	private static final String[] COMMANDS =
	{
		"_bbshome",
		"_bbstop",
		"_bbsika",
		"_bbsika_setmp",
		"_bbsika_disablemp",
		"_bbsika_referal",
		"_bbsika_rankings",
		"_bbsika_inspect",
		"_bbsika_autopotion",
		"_bbsika_referralpage",
		"_bbsika_account",
	};

	@Override
	public String[] getCommandList()
	{
		return COMMANDS;
	}

	@Override
	public boolean onCommand(String command, Player player)
	{
		if (command.equals("_bbshome") || command.equals("_bbsika"))
		{
			showMainPage(player);
		}
		else if (command.startsWith("_bbstop;"))
		{
			showPage(player, command.replace("_bbstop;", ""));
		}
		else if (command.startsWith("_bbsika_setmp_"))
		{
			try
			{
				int threshold = Integer.parseInt(command.replace("_bbsika_setmp_", ""));
				if (threshold > 0 && threshold <= 90)
				{
					player.getVariables().set("IKA_MP_THRESHOLD", threshold);
					startAutoTask(player);
					player.sendMessage("Auto-use MP: ativado em " + threshold + "%");
				}
			}
			catch (NumberFormatException ignored)
			{
			}
			showAutoPage(player);
		}
		else if (command.equals("_bbsika_disablemp"))
		{
			player.getVariables().set("IKA_MP_THRESHOLD", 0);
			stopAutoTask(player.getObjectId());
			player.sendMessage("Auto-use MP: desativado.");
			showAutoPage(player);
		}
		else if (command.equals("_bbsika_referralpage"))
		{
			showReferralPage(player);
		}
		else if (command.startsWith("_bbsika_referal"))
		{
			// aceita "_bbsika_referal CODIGO" (campo edit) ou "_bbsika_referal_CODIGO"
			String code = command.substring("_bbsika_referal".length()).replace("_", " ").trim().toUpperCase();
			if (code.isEmpty() || code.startsWith("$"))
			{
				showReferralPage(player, "<font color=\"FF4444\">Digite um codigo no campo acima.</font>");
			}
			else
			{
				String msg = redeemCode(player, code);
				showReferralPage(player, msg);
			}
		}
		else if (command.startsWith("_bbsika_rankings"))
		{
			String type = command.replace("_bbsika_rankings", "").replace("_", "");
			showRankingsPage(player, type.isEmpty() ? "level" : type);
		}
		else if (command.startsWith("_bbsika_inspect_"))
		{
			String name = command.replace("_bbsika_inspect_", "").trim();
			// campo vazio passa "$inspectName" literal - ignora
			if (name.startsWith("$") || name.isEmpty())
			{
				name = "";
			}
			showInspectPage(player, name);
		}
		else if (command.equals("_bbsika_inspect"))
		{
			showInspectPage(player, "");
		}
		else if (command.equals("_bbsika_autopotion"))
		{
			showAutoPage(player);
		}
		else if (command.equals("_bbsika_account"))
		{
			showAccountPage(player);
		}

		return false;
	}

	// ======== AUTO-USE ========

	private static void startAutoTask(Player player)
	{
		stopAutoTask(player.getObjectId());
		ScheduledFuture<?> task = ThreadPool.scheduleAtFixedRate(() ->
		{
			if (!player.isOnline() || player.isAlikeDead())
			{
				stopAutoTask(player.getObjectId());
				return;
			}
			int mpThreshold = player.getVariables().getInt("IKA_MP_THRESHOLD", 0);
			if (mpThreshold <= 0)
			{
				stopAutoTask(player.getObjectId());
				return;
			}
			double mpPercent = (player.getCurrentMp() / player.getMaxMp()) * 100.0;
			if (mpPercent < mpThreshold)
			{
				Item potion = player.getInventory().getItemByItemId(MP_POTION_ID);
				if (potion != null)
				{
					IItemHandler handler = ItemHandler.getInstance().getHandler(potion.getEtcItem());
					if (handler != null)
					{
						handler.onItemUse(player, potion, false);
					}
				}
			}
		}, 2000, 2000);
		AUTO_TASKS.put(player.getObjectId(), task);
	}

	public static void stopAutoTask(int objectId)
	{
		ScheduledFuture<?> task = AUTO_TASKS.remove(objectId);
		if (task != null && !task.isCancelled())
		{
			task.cancel(false);
		}
	}

	// ======== NAVEGAÇÃO COMUM ========

	private String buildNav(String active)
	{
		StringBuilder n = new StringBuilder();
		n.append("<table width=180 cellpadding=0 cellspacing=2>");
		n.append("<tr><td height=10></td></tr>");
		n.append(navBtn("Home", "_bbshome", "L2EssenceCommunity.home_btn", active.equals("home")));
		n.append(navBtn("Auto Potion", "_bbsika_setmp_0_open", "L2EssenceCommunity.gmshop_btn", active.equals("auto")));
		n.append(navBtn("Referral", "_bbsika_referal_open", "L2EssenceCommunity.buffer_btn", active.equals("referral")));
		n.append(navBtn("Voice", "_bbshome", "L2EssenceCommunity.teleport_btn", active.equals("voice")));
		n.append(navBtn("Account", "_bbsika_account", "L2EssenceCommunity.acc_services_btn", active.equals("account")));
		n.append(navBtn("Rankings", "_bbsika_rankings", "L2EssenceCommunity.rankings_btn", active.equals("rankings")));
		n.append(navBtn("Inspecionar", "_bbsika_inspect", "L2EssenceCommunity.itembroker_btn", active.equals("inspect")));
		n.append("</table>");
		return n.toString();
	}

	private String navBtn(String label, String bypass, String fore, boolean active)
	{
		String back = active ? fore + "_over" : fore;
		return "<tr><td><button value=\"" + label + "\" action=\"bypass " + bypass + "\" width=177 height=33 back=\"" + back + "\" fore=\"" + fore + "\"></td></tr>";
	}

	private String buildFrame(String nav, String content)
	{
		return "<html noscrollbar><body><br><center>" +
			"<table width=\"770\"><tr><td>" +
			"<table><tr>" +
			"<td><table width=\"180\" height=\"490\" background=\"l2ui_ct1.ComboBox_DF_Dropmenu_Bg\"><tr><td>" +
			"<table width=180><tr><td align=center><img src=\"L2EssenceCommunity.logo\" width=143 height=100></td></tr></table>" +
			"<table width=180><tr><td align=center><img src=\"L2EssenceCommunity.effect_top\" width=167 height=18></td></tr></table>" +
			nav +
			"<table width=180><tr><td align=center><img src=\"L2EssenceCommunity.effect_bottom\" width=167 height=18></td></tr></table>" +
			"</td></tr></table></td>" +
			"<td><table width=\"570\" height=\"490\" background=\"l2ui_ct1.ComboBox_DF_Dropmenu_Bg\"><tr><td width=10></td><td>" +
			content +
			"</td></tr></table></td>" +
			"</tr></table>" +
			"</td></tr></table>" +
			"</center></body></html>";
	}

	// ======== HOME ========

	private void showMainPage(Player player)
	{
		String htmlFile = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/home.html");
		if (htmlFile != null)
		{
			htmlFile = applyCommonVars(htmlFile, player);
			CommunityBoardHandler.separateAndSend(htmlFile, player);
			return;
		}
		CommunityBoardHandler.separateAndSend("<html><body>Board em manutencao.</body></html>", player);
	}

	private String applyCommonVars(String html, Player player)
	{
		int mpThreshold = player.getVariables().getInt("IKA_MP_THRESHOLD", 0);
		return html
			.replace("%online%", String.valueOf(World.getInstance().getPlayers().size()))
			.replace("%player_name%", player.getName())
			.replace("%player_level%", String.valueOf(player.getLevel()))
			.replace("%mp_auto%", mpThreshold > 0 ? mpThreshold + "%" : "OFF")
			.replace("%ikoin%", String.valueOf(getPlayerCredits(player)));
	}

	private void showPage(Player player, String page)
	{
		if (page.isEmpty() || !page.endsWith(".html"))
		{
			showMainPage(player);
			return;
		}
		String html = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/" + page);
		if (html == null)
		{
			showMainPage(player);
			return;
		}
		CommunityBoardHandler.separateAndSend(applyCommonVars(html, player), player);
	}

	// ======== AUTO POTION PAGE ========

	private void showAutoPage(Player player)
	{
		int mpThreshold = player.getVariables().getInt("IKA_MP_THRESHOLD", 0);
		String mpStatus = mpThreshold > 0 ? "<font color=\"44FF44\">Ativo em " + mpThreshold + "%</font>" : "<font color=\"FF4444\">Desativado</font>";
		int mpPct = (int) ((player.getCurrentMp() / player.getMaxMp()) * 100);

		StringBuilder c = new StringBuilder();
		c.append("<br><center><font color=\"CDB67F\" name=\"hs15\">AUTO POTION</font></center><br>");
		c.append("<center><img src=\"L2UI.SquareGray\" width=540 height=1></center><br>");
		c.append("<center>");
		c.append("<table width=500 cellpadding=4>");
		c.append("<tr><td colspan=2 align=center><font color=\"AAAAAA\">MP Atual: <font color=\"6699FF\">").append(mpPct).append("%</font>  |  Status: ").append(mpStatus).append("</font></td></tr>");
		c.append("<tr><td height=10></td></tr>");
		c.append("<tr><td colspan=2 align=center><font color=\"888888\">Usar potion MP (id ").append(MP_POTION_ID).append(") quando MP cair abaixo de:</font></td></tr>");
		c.append("<tr><td height=6></td></tr>");
		c.append("<tr><td colspan=2 align=center><table cellpadding=0 cellspacing=4><tr>");
		for (int pct : new int[]{20, 30, 40, 50, 60, 70})
		{
			String back = (mpThreshold == pct) ? "L2EssenceCommunity.buy_premium_btn_over" : "L2EssenceCommunity.buy_premium_btn";
			c.append("<td><button value=\"").append(pct).append("%\" action=\"bypass _bbsika_setmp_").append(pct).append("\" width=70 height=27 back=\"").append(back).append("\" fore=\"L2EssenceCommunity.buy_premium_btn\"></td>");
		}
		c.append("</tr></table></td></tr>");
		c.append("<tr><td height=8></td></tr>");
		c.append("<tr><td colspan=2 align=center><button value=\"Desativar\" action=\"bypass _bbsika_disablemp\" width=120 height=27 back=\"L2EssenceCommunity.donate_items_btn_over\" fore=\"L2EssenceCommunity.donate_items_btn\"></td></tr>");
		c.append("</table></center>");

		CommunityBoardHandler.separateAndSend(buildFrame(buildNav("auto"), c.toString()), player);
	}

	// ======== RESGATE DE CODIGO ========

	private String redeemCode(Player player, String code)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			// 1. Busca o codigo
			try (PreparedStatement ps = con.prepareStatement("SELECT items, active, max_uses, uses FROM promo_codes WHERE code=?"))
			{
				ps.setString(1, code);
				try (ResultSet rs = ps.executeQuery())
				{
					if (!rs.next())
					{
						return "<font color=\"FF4444\">Codigo invalido.</font>";
					}
					if (rs.getInt("active") == 0)
					{
						return "<font color=\"FF4444\">Este codigo nao esta ativo.</font>";
					}
					int maxUses = rs.getInt("max_uses");
					int uses = rs.getInt("uses");
					if (maxUses > 0 && uses >= maxUses)
					{
						return "<font color=\"FF4444\">Este codigo ja atingiu o limite de usos.</font>";
					}
					String items = rs.getString("items");

					// 2. Verifica se essa conta ja usou
					try (PreparedStatement ps2 = con.prepareStatement("SELECT 1 FROM promo_redeemed WHERE code=? AND account_name=?"))
					{
						ps2.setString(1, code);
						ps2.setString(2, player.getAccountName());
						try (ResultSet rs2 = ps2.executeQuery())
						{
							if (rs2.next())
							{
								return "<font color=\"FFAA00\">Voce ja resgatou este codigo.</font>";
							}
						}
					}

					// 3. Entrega os itens
					for (String entry : items.split(";"))
					{
						String[] parts = entry.trim().split(":");
						if (parts.length == 2)
						{
							try
							{
								int itemId = Integer.parseInt(parts[0].trim());
								long count = Long.parseLong(parts[1].trim());
								player.addItem(net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType.REWARD, itemId, count, player, true);
							}
							catch (NumberFormatException ignored)
							{
							}
						}
					}

					// 4. Registra o resgate
					try (PreparedStatement ps3 = con.prepareStatement("INSERT INTO promo_redeemed (code, account_name, redeemed_at) VALUES (?,?,?)"))
					{
						ps3.setString(1, code);
						ps3.setString(2, player.getAccountName());
						ps3.setLong(3, System.currentTimeMillis());
						ps3.executeUpdate();
					}

					// 5. Incrementa o contador
					try (PreparedStatement ps4 = con.prepareStatement("UPDATE promo_codes SET uses=uses+1 WHERE code=?"))
					{
						ps4.setString(1, code);
						ps4.executeUpdate();
					}

					return "<font color=\"44FF44\">Codigo '" + code + "' resgatado! Verifique seu inventario.</font>";
				}
			}
		}
		catch (Exception e)
		{
			return "<font color=\"FF4444\">Erro ao processar o codigo. Tente novamente.</font>";
		}
	}

	// ======== REFERRAL PAGE ========

	private void showReferralPage(Player player)
	{
		showReferralPage(player, "");
	}

	private void showReferralPage(Player player, String message)
	{
		StringBuilder c = new StringBuilder();
		c.append("<table width=530 cellpadding=0 cellspacing=0>");
		c.append("<tr><td height=18></td></tr>");
		c.append("<tr><td align=center><font color=\"CDB67F\" name=\"hs15\">CODIGO REFERRAL / PROMO</font></td></tr>");
		c.append("<tr><td height=4></td></tr>");
		c.append("<tr><td><img src=\"L2UI.SquareGray\" width=530 height=1></td></tr>");
		c.append("<tr><td height=18></td></tr>");
		c.append("<tr><td align=center><font color=\"888888\">Digite seu codigo abaixo e clique em RESGATAR:</font></td></tr>");
		c.append("<tr><td height=12></td></tr>");
		c.append("<tr><td align=center>");
		c.append("<table cellpadding=0 cellspacing=0><tr>");
		c.append("<td><edit var=\"refCode\" width=240 height=20 length=\"30\"></td>");
		c.append("<td width=8></td>");
		c.append("<td><button value=\"RESGATAR\" action=\"bypass _bbsika_referal $refCode\" width=110 height=27 back=\"L2EssenceCommunity.donate_items_btn_over\" fore=\"L2EssenceCommunity.donate_items_btn\"></td>");
		c.append("</tr></table>");
		c.append("</td></tr>");
		if (message != null && !message.isEmpty())
		{
			c.append("<tr><td height=12></td></tr>");
			c.append("<tr><td align=center>").append(message).append("</td></tr>");
		}
		c.append("<tr><td height=20></td></tr>");
		c.append("<tr><td><img src=\"L2UI.SquareGray\" width=530 height=1></td></tr>");
		c.append("<tr><td height=10></td></tr>");
		c.append("<tr><td align=center><font color=\"888888\">Tambem funciona no chat do jogo: <font color=\"CDB67F\">.code SEUCODIGO</font></font></td></tr>");
		c.append("<tr><td height=8></td></tr>");
		c.append("<tr><td align=center><font color=\"696969\">Codigos liberados em lives de streamers e eventos oficiais.</font></td></tr>");
		c.append("<tr><td height=4></td></tr>");
		c.append("<tr><td align=center><font color=\"696969\">Cada codigo so pode ser resgatado uma vez por conta.</font></td></tr>");
		c.append("</table>");
		CommunityBoardHandler.separateAndSend(buildFrame(buildNav("referral"), c.toString()), player);
	}


	// ======== RANKINGS ========

	private void showRankingsPage(Player player, String type)
	{
		StringBuilder c = new StringBuilder();
		c.append("<br><center><font color=\"CDB67F\" name=\"hs15\">RANKINGS</font></center><br>");
		c.append("<center><img src=\"L2UI.SquareGray\" width=540 height=1></center><br>");

		// Abas
		c.append("<center><table cellpadding=0 cellspacing=3><tr>");
		for (String[] tab : new String[][]{{"level","Nivel"},{"pvp","PvP"},{"adena","Adena"},{"lcoin","L-Coin"},{"onlinetime","Online"}})
		{
			String back = type.equals(tab[0]) ? "L2EssenceCommunity.buy_premium_btn_over" : "L2EssenceCommunity.buy_premium_btn";
			c.append("<td><button value=\"").append(tab[1]).append("\" action=\"bypass _bbsika_rankings_").append(tab[0]).append("\" width=90 height=25 back=\"").append(back).append("\" fore=\"L2EssenceCommunity.buy_premium_btn\"></td>");
		}
		c.append("</tr></table></center><br>");

		// Tabela de resultados
		c.append("<center><table width=510 cellpadding=0 cellspacing=0 background=\"l2ui_ct1.ComboBox_DF_Dropmenu_Bg\">");
		c.append("<tr>");
		c.append("<td width=35 align=center><font color=\"CDB67F\">#</font></td>");
		c.append("<td width=170><font color=\"CDB67F\">Nome</font></td>");
		c.append("<td width=165><font color=\"CDB67F\">Classe</font></td>");
		c.append("<td width=100 align=right><font color=\"CDB67F\">").append(getColLabel(type)).append("</font></td>");
		c.append("<td width=10></td>");
		c.append("</tr>");
		c.append("<tr><td colspan=5><img src=\"L2UI.SquareGray\" width=510 height=1></td></tr>");

		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(buildRankQuery(type)))
		{
			try (ResultSet rs = ps.executeQuery())
			{
				int pos = 1;
				while (rs.next() && pos <= 20)
				{
					String name = rs.getString("char_name");
					int classId = rs.getInt("classid");
					long value = rs.getLong("val");
					String className = getClassName(classId);
					String color = pos == 1 ? "FFD700" : pos == 2 ? "C0C0C0" : pos == 3 ? "CD7F32" : "AAAAAA";
					String medal = pos <= 3 ? "&#9733; " : "";

					c.append("<tr>");
					c.append("<td width=35 align=center><font color=\"").append(color).append("\">").append(medal).append(pos).append("</font></td>");
					c.append("<td width=170><font color=\"FFFFFF\">").append(name).append("</font></td>");
					c.append("<td width=165><font color=\"888888\">").append(className).append("</font></td>");
					c.append("<td width=100 align=right><font color=\"").append(color).append("\">").append(formatValue(type, value)).append("</font></td>");
					c.append("<td width=10></td>");
					c.append("</tr>");
					pos++;
				}
			}
		}
		catch (Exception e)
		{
			c.append("<tr><td colspan=4 align=center><font color=\"FF4444\">Erro ao carregar ranking.</font></td></tr>");
		}

		c.append("</table></center>");
		CommunityBoardHandler.separateAndSend(buildFrame(buildNav("rankings"), c.toString()), player);
	}

	private String buildRankQuery(String type)
	{
		switch (type)
		{
			case "pvp":
				return "SELECT char_name, classid, pvpkills AS val FROM characters WHERE accesslevel=0 ORDER BY pvpkills DESC LIMIT 20";
			case "adena":
				return "SELECT c.char_name, c.classid, COALESCE(i.count,0) AS val FROM characters c LEFT JOIN items i ON i.owner_id=c.charId AND i.item_id=57 AND i.loc='INVENTORY' WHERE c.accesslevel=0 ORDER BY val DESC LIMIT 20";
			case "lcoin":
				return "SELECT c.char_name, c.classid, COALESCE(i.count,0) AS val FROM characters c LEFT JOIN items i ON i.owner_id=c.charId AND i.item_id=91663 AND i.loc='INVENTORY' WHERE c.accesslevel=0 ORDER BY val DESC LIMIT 20";
			case "onlinetime":
				return "SELECT char_name, classid, onlinetime AS val FROM characters WHERE accesslevel=0 ORDER BY onlinetime DESC LIMIT 20";
			default: // level
				return "SELECT char_name, classid, level AS val FROM characters WHERE accesslevel=0 ORDER BY level DESC LIMIT 20";
		}
	}

	private String getColLabel(String type)
	{
		switch (type)
		{
			case "pvp": return "PvP Kills";
			case "adena": return "Adena";
			case "lcoin": return "L-Coin";
			case "onlinetime": return "Tempo";
			default: return "Nivel";
		}
	}

	private String formatValue(String type, long value)
	{
		if (type.equals("adena") || type.equals("lcoin"))
		{
			if (value >= 1000000) return (value / 1000000) + "M";
			if (value >= 1000) return (value / 1000) + "k";
		}
		if (type.equals("onlinetime"))
		{
			long hours = value / 3600;
			return hours + "h";
		}
		return String.valueOf(value);
	}

	// ======== ACCOUNT PAGE ========

	private void showAccountPage(Player player)
	{
		long credits = getPlayerCredits(player);

		StringBuilder c = new StringBuilder();
		c.append("<table width=530 cellpadding=0 cellspacing=0>");
		c.append("<tr><td height=10></td></tr>");
		c.append("<tr><td align=center><font color=\"CDB67F\" name=\"hs15\">ACCOUNT SERVICE</font></td></tr>");
		c.append("<tr><td height=4></td></tr>");
		c.append("<tr><td><img src=\"L2UI.SquareGray\" width=530 height=1></td></tr>");
		c.append("<tr><td height=8></td></tr>");

		// Saldo de Ikoin
		c.append("<tr><td align=center>");
		c.append("<table width=300 background=\"l2ui_ct1.ComboBox_DF_Dropmenu_Bg\" cellpadding=6><tr>");
		c.append("<td align=center><img src=\"L2EssenceCommunity.premium_crown\" width=24 height=18></td>");
		c.append("<td><font color=\"888888\">Seu saldo Ikoin:</font></td>");
		c.append("<td align=right><font color=\"CDB67F\" name=\"hs12\">").append(credits).append("</font></td>");
		c.append("</tr></table>");
		c.append("</td></tr>");
		c.append("<tr><td height=10></td></tr>");
		c.append("<tr><td><img src=\"L2UI.SquareGray\" width=530 height=1></td></tr>");
		c.append("<tr><td height=8></td></tr>");

		// Lista de servicos
		String[][] services = {
			{"Trocar Classe", "150"},
			{"Trocar Nick", "50"},
			{"Trocar Sexo", "50"},
			{"Trocar Raca", "100"},
			{"Doar Ikoin", "-"},
			{"Vender Personagem", "-"},
		};

		c.append("<tr><td>");
		c.append("<table width=530 cellpadding=0 cellspacing=0>");
		c.append("<tr>");
		c.append("<td width=10></td>");
		c.append("<td width=250><font color=\"AAAAAA\">Servico</font></td>");
		c.append("<td width=100 align=center><font color=\"AAAAAA\">Custo</font></td>");
		c.append("<td width=150 align=center><font color=\"AAAAAA\">Acao</font></td>");
		c.append("<td width=10></td>");
		c.append("</tr>");
		c.append("<tr><td colspan=5><img src=\"L2UI.SquareGray\" width=530 height=1></td></tr>");

		for (String[] s : services)
		{
			c.append("<tr><td height=6></td></tr>");
			c.append("<tr>");
			c.append("<td width=10></td>");
			c.append("<td width=250><font color=\"CDB67F\">").append(s[0]).append("</font></td>");
			c.append("<td width=100 align=center><font color=\"FFAA00\">").append(s[1].equals("-") ? "--" : s[1] + " IK").append("</font></td>");
			c.append("<td width=150 align=center><font color=\"696969\">Em breve</font></td>");
			c.append("<td width=10></td>");
			c.append("</tr>");
			c.append("<tr><td height=6></td></tr>");
			c.append("<tr><td colspan=5><img src=\"L2UI.SquareGray\" width=530 height=1></td></tr>");
		}

		c.append("</table></td></tr>");
		c.append("</table>");

		CommunityBoardHandler.separateAndSend(buildFrame(buildNav("account"), c.toString()), player);
	}

	private long getPlayerCredits(Player player)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT balance FROM ikoin_balance WHERE account_name=?"))
		{
			ps.setString(1, player.getAccountName());
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					return rs.getLong("balance");
				}
			}
		}
		catch (Exception e)
		{
			// sem saldo cadastrado ainda
		}
		return 0;
	}

	// ======== INSPECIONAR ========

	private void showInspectPage(Player player, String targetName)
	{
		StringBuilder c = new StringBuilder();
		c.append("<br><center><font color=\"CDB67F\" name=\"hs15\">INSPECIONAR JOGADOR</font></center><br>");
		c.append("<center><img src=\"L2UI.SquareGray\" width=540 height=1></center><br>");

		c.append("<table width=540 cellpadding=0 cellspacing=0>");
		c.append("<tr><td height=15></td></tr>");
		c.append("<tr><td align=center><edit var=\"inspectName\" width=280 height=15></edit></td></tr>");
		c.append("<tr><td height=8></td></tr>");
		c.append("<tr><td align=center><button value=\"Buscar Jogador\" action=\"bypass _bbsika_inspect_$inspectName\" width=160 height=27 back=\"L2EssenceCommunity.donate_items_btn_over\" fore=\"L2EssenceCommunity.donate_items_btn\"></td></tr>");
		c.append("<tr><td height=15></td></tr>");
		c.append("</table>");

		if (!targetName.isEmpty())
		{
			// Busca online primeiro
			Player target = World.getInstance().getPlayer(targetName);
			if (target != null)
			{
				buildInspectFromPlayer(c, target);
			}
			else
			{
				buildInspectFromDB(c, targetName);
			}
		}

		CommunityBoardHandler.separateAndSend(buildFrame(buildNav("inspect"), c.toString()), player);
	}

	private void buildInspectFromPlayer(StringBuilder c, Player target)
	{
		int hpPct = (int)((target.getCurrentHp() / target.getMaxHp()) * 100);
		int mpPct = (int)((target.getCurrentMp() / target.getMaxMp()) * 100);
		int cpPct = (int)((target.getCurrentCp() / target.getMaxCp()) * 100);

		c.append("<center><table width=500 background=\"l2ui_ct1.ComboBox_DF_Dropmenu_Bg\" cellpadding=4>");
		c.append("<tr><td colspan=2 align=center><font color=\"44FF44\">&#9679; Online</font>  <font color=\"CDB67F\" name=\"hs12\">").append(target.getName()).append("</font></td></tr>");
		c.append("<tr><td colspan=2><img src=\"L2UI.SquareGray\" width=480 height=1></td></tr>");
		c.append("<tr><td width=240><font color=\"888888\">Nivel:</font> <font color=\"FFFFFF\">").append(target.getLevel()).append("</font></td>");
		c.append("<td width=240><font color=\"888888\">Classe:</font> <font color=\"FFFFFF\">").append(getClassName(target.getActiveClass())).append("</font></td></tr>");
		c.append("<tr><td><font color=\"888888\">Raca:</font> <font color=\"FFFFFF\">").append(getRaceName(target.getRace().ordinal())).append("</font></td>");
		c.append("<td><font color=\"888888\">PvP / PK:</font> <font color=\"FF6666\">").append(target.getPvpKills()).append("</font> / <font color=\"AA4444\">").append(target.getPkKills()).append("</font></td></tr>");
		c.append("<tr><td colspan=2 height=6></td></tr>");
		c.append("<tr><td><font color=\"888888\">HP:</font> ").append(buildBar(hpPct, "FF3333", "330000", 200)).append("</td>");
		c.append("<td><font color=\"888888\">CP:</font> ").append(buildBar(cpPct, "33CC33", "003300", 200)).append("</td></tr>");
		c.append("<tr><td colspan=2><font color=\"888888\">MP:</font> ").append(buildBar(mpPct, "3366FF", "000033", 430)).append("</td></tr>");
		c.append("</table></center>");
	}

	private void buildInspectFromDB(StringBuilder c, String name)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT char_name, level, classid, race, pvpkills, pkKills, sex FROM characters WHERE char_name=? AND accesslevel=0 LIMIT 1"))
		{
			ps.setString(1, name);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					c.append("<center><table width=500 background=\"l2ui_ct1.ComboBox_DF_Dropmenu_Bg\" cellpadding=4>");
					c.append("<tr><td colspan=2 align=center><font color=\"888888\">&#9679; Offline</font>  <font color=\"CDB67F\" name=\"hs12\">").append(rs.getString("char_name")).append("</font></td></tr>");
					c.append("<tr><td colspan=2><img src=\"L2UI.SquareGray\" width=480 height=1></td></tr>");
					c.append("<tr><td width=240><font color=\"888888\">Nivel:</font> <font color=\"FFFFFF\">").append(rs.getInt("level")).append("</font></td>");
					c.append("<td width=240><font color=\"888888\">Classe:</font> <font color=\"FFFFFF\">").append(getClassName(rs.getInt("classid"))).append("</font></td></tr>");
					c.append("<tr><td><font color=\"888888\">Raca:</font> <font color=\"FFFFFF\">").append(getRaceName(rs.getInt("race"))).append("</font></td>");
					c.append("<td><font color=\"888888\">PvP / PK:</font> <font color=\"FF6666\">").append(rs.getInt("pvpkills")).append("</font> / <font color=\"AA4444\">").append(rs.getInt("pkKills")).append("</font></td></tr>");
					c.append("</table></center>");
				}
				else
				{
					c.append("<center><font color=\"FF4444\">Jogador '").append(name).append("' nao encontrado.</font></center>");
				}
			}
		}
		catch (Exception e)
		{
			c.append("<center><font color=\"FF4444\">Erro ao buscar jogador.</font></center>");
		}
	}

	private String buildBar(int pct, String fillColor, String bgColor, int width)
	{
		int fill = (int)(width * pct / 100.0);
		return "<table width=" + width + " height=10 cellpadding=0 cellspacing=0 bgcolor=\"" + bgColor + "\"><tr>" +
			"<td><table width=" + fill + " height=10 bgcolor=\"" + fillColor + "\"><tr><td></td></tr></table></td>" +
			"</tr></table> <font color=\"888888\">" + pct + "%</font>";
	}

	private String getClassName(int classId)
	{
		try
		{
			return ClassListData.getInstance().getClass(classId).getClassName();
		}
		catch (Exception e)
		{
			return "Classe " + classId;
		}
	}

	private String getRaceName(int race)
	{
		return (race >= 0 && race < RACES.length) ? RACES[race] : "Race " + race;
	}
}
