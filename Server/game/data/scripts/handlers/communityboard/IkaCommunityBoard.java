package handlers.communityboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.managers.PremiumManager;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.cache.HtmCache;
import net.sf.l2jdev.gameserver.data.xml.ClassListData;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
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
	// Tarefa GLOBAL unica: varre todos os players online e usa potion conforme o threshold salvo.
	// Assim funciona automatico apos relogar/RR (a variavel IKA_MP_THRESHOLD persiste no char).
	private static volatile boolean AUTO_TASK_STARTED = false;

	private static synchronized void ensureAutoTask()
	{
		if (AUTO_TASK_STARTED)
		{
			return;
		}
		AUTO_TASK_STARTED = true;
		ThreadPool.scheduleAtFixedRate(() ->
		{
			try
			{
				for (Player player : World.getInstance().getPlayers())
				{
					if (player == null || !player.isOnline() || player.isAlikeDead())
					{
						continue;
					}
					final int mpThreshold = player.getVariables().getInt("IKA_MP_THRESHOLD", 0);
					if (mpThreshold <= 0)
					{
						continue;
					}
					final double mpPercent = (player.getCurrentMp() / player.getMaxMp()) * 100.0;
					if (mpPercent < mpThreshold)
					{
						final Item potion = player.getInventory().getItemByItemId(MP_POTION_ID);
						if (potion != null)
						{
							final IItemHandler handler = ItemHandler.getInstance().getHandler(potion.getEtcItem());
							if (handler != null)
							{
								handler.onItemUse(player, potion, false);
							}
						}
					}
				}
			}
			catch (Exception ignored)
			{
			}
		}, 2000, 2000);
	}

	private static final String[] RACES = { "Human", "Elf", "Dark Elf", "Orc", "Dwarf", "Kamael", "Sylph" };

	// Start Kit Basico - itens entregues 1x por personagem. Formato "itemId:count;itemId:count"
	// TODO: ajustar lista final dos itens do start basico
	private static final String BASIC_START_ITEMS = "57:100000";

	// Premium Account: 50 Ikoin = 30 dias. Por CONTA. Nao pode acumular (so recompra apos expirar).
	private static final int PREMIUM_COST_IKOIN = 50;
	private static final int PREMIUM_DAYS = 30;

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
		"_bbsika_start",
		"_bbsika_buyoffer",
		"_bbsika_buyoffer_1",
		"_bbsika_buyoffer_2",
		"_bbsika_premium",
		"_bbsika_buypremium",
	};

	@Override
	public String[] getCommandList()
	{
		return COMMANDS;
	}

	@Override
	public boolean onCommand(String command, Player player)
	{
		ensureAutoTask(); // garante a tarefa global de auto-potion rodando (idempotente)
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
					player.getVariables().storeMe();
					ensureAutoTask();
					player.sendMessage("Auto-use MP: ativado em " + threshold + "% (persiste apos relogar).");
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
			player.getVariables().storeMe();
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
		else if (command.equals("_bbsika_premium"))
		{
			showPremiumPage(player);
		}
		else if (command.equals("_bbsika_buypremium"))
		{
			buyPremium(player);
		}
		else if (command.equals("_bbsika_start_basic"))
		{
			claimBasicStart(player);
			showMainPage(player);
		}
		else if (command.startsWith("_bbsika_buyoffer_"))
		{
			int offerId = 1;
			try
			{
				offerId = Integer.parseInt(command.replace("_bbsika_buyoffer_", "").trim());
			}
			catch (NumberFormatException ignored)
			{
			}
			buyOffer(player, offerId);
			showMainPage(player);
		}

		return false;
	}

	// ======== START KIT BASICO (1x por personagem) ========

	private void claimBasicStart(Player player)
	{
		if (player.getVariables().getBoolean("IKA_START_BASIC", false))
		{
			player.sendMessage("[Start] Voce ja resgatou o Start Kit Basico neste personagem.");
			return;
		}
		for (String entry : BASIC_START_ITEMS.split(";"))
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
		player.getVariables().set("IKA_START_BASIC", true);
		player.getVariables().storeMe();
		player.sendMessage("[Start] Start Kit Basico resgatado! Bom jogo.");
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
		boolean basicClaimed = player.getVariables().getBoolean("IKA_START_BASIC", false);
		String startBtn = basicClaimed
			? "<font color=\"44FF44\">RESGATADO</font>"
			: "<button value=\"RESGATAR\" action=\"bypass _bbsika_start_basic\" width=92 height=24 back=\"L2EssenceCommunity.donate_items_btn_over\" fore=\"L2EssenceCommunity.donate_items_btn\">";
		return html
			.replace("%online%", String.valueOf(World.getInstance().getPlayers().size()))
			.replace("%player_name%", player.getName())
			.replace("%player_level%", String.valueOf(player.getLevel()))
			.replace("%mp_auto%", mpThreshold > 0 ? mpThreshold + "%" : "OFF")
			.replace("%ikoin%", String.valueOf(getPlayerCredits(player)))
			.replace("%start_basic_btn%", startBtn)
			.replace("%offer_block2%", buildOfferBlock(2))
			.replace("%offer_block%", buildOfferBlock(1));
	}

	// ======== OFERTA LIMITADA (gerenciada pelo site/admin via game_offer) ========

	private String buildOfferBlock(int offerId)
	{
		// Espaco reservado constante: com OU sem oferta o layout nao muda
		final String SPACER = "<tr><td height=52></td></tr>";
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT item_id, count, price_ikoin, title, icon FROM game_offer WHERE id=? AND active=1"))
		{
			ps.setInt(1, offerId);
			try (ResultSet rs = ps.executeQuery())
			{
				if (!rs.next())
				{
					return SPACER; // sem oferta: mantem a mesma altura
				}
				int itemId = rs.getInt("item_id");
				int count = rs.getInt("count");
				int price = rs.getInt("price_ikoin");
				String title = rs.getString("title");
				String iconOverride = rs.getString("icon");

				ItemTemplate tmpl = ItemData.getInstance().getTemplate(itemId);
				String itemName = tmpl != null ? tmpl.getName() : "Item " + itemId;
				String icon = (iconOverride != null && !iconOverride.isEmpty()) ? iconOverride
					: (tmpl != null && tmpl.getIcon() != null ? tmpl.getIcon() : "L2EssenceCommunity.agathions");

				StringBuilder b = new StringBuilder();
				b.append("<tr><td height=6></td></tr>");
				b.append("<tr><td><table><tr>");
				b.append("<td width=16></td>");
				b.append("<td width=48 align=center><img src=\"").append(icon).append("\" width=40 height=40></td>");
				b.append("<td width=190><font name=\"hs15\" color=\"FFAA44\">").append(title == null ? "Oferta Limitada" : title).append("</font><br1>");
				b.append("<font color=\"888888\">").append(itemName);
				if (count > 1)
				{
					b.append(" x").append(count);
				}
				b.append("</font></td>");
				b.append("<td width=100 align=center>");
				b.append("<font color=\"LEVEL\">").append(price).append(" Ikoin</font><br1>");
				b.append("<button value=\"Comprar\" action=\"bypass _bbsika_buyoffer_").append(offerId).append("\" width=92 height=24 back=\"L2EssenceCommunity.buy_premium_btn_over\" fore=\"L2EssenceCommunity.buy_premium_btn\">");
				b.append("</td>");
				b.append("<td width=200></td>");
				b.append("</tr></table></td></tr>");
				return b.toString();
			}
		}
		catch (Exception e)
		{
			return SPACER;
		}
	}

	private void buyOffer(Player player, int offerId)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			int itemId, count, price;
			try (PreparedStatement ps0 = con.prepareStatement("SELECT item_id, count, price_ikoin FROM game_offer WHERE id=? AND active=1"))
			{
				ps0.setInt(1, offerId);
				ResultSet rs = ps0.executeQuery();
				if (!rs.next())
				{
					player.sendMessage("[Oferta] Nenhuma oferta ativa no momento.");
					return;
				}
				itemId = rs.getInt("item_id");
				count = rs.getInt("count");
				price = rs.getInt("price_ikoin");
			}

			long balance = getPlayerCredits(player);
			if (balance < price)
			{
				player.sendMessage("[Oferta] Ikoin insuficiente. Voce tem " + balance + ", precisa de " + price + ".");
				return;
			}

			// debita Ikoin
			try (PreparedStatement ps = con.prepareStatement("UPDATE ikoin_balance SET balance=balance-?, updated_at=? WHERE account_name=?"))
			{
				ps.setInt(1, price);
				ps.setLong(2, System.currentTimeMillis());
				ps.setString(3, player.getAccountName());
				ps.executeUpdate();
			}
			try (PreparedStatement ps = con.prepareStatement("INSERT INTO ikoin_transactions (account_name, amount, type, description, created_at) VALUES (?,?,?,?,?)"))
			{
				ps.setString(1, player.getAccountName());
				ps.setInt(2, -price);
				ps.setString(3, "spend");
				ps.setString(4, "Oferta Limitada: item " + itemId);
				ps.setLong(5, System.currentTimeMillis());
				ps.executeUpdate();
			}

			// entrega o item
			player.addItem(ItemProcessType.REWARD, itemId, count, player, true);
			player.sendMessage("[Oferta] Compra realizada! Item entregue no inventario.");
		}
		catch (Exception e)
		{
			player.sendMessage("[Oferta] Erro ao processar a compra.");
		}
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
		c.append("<br><center>");
		c.append("<table width=500 cellpadding=4>");
		c.append("<tr><td align=center><font color=\"AAAAAA\">MP Atual: <font color=\"6699FF\">").append(mpPct).append("%</font>  |  Status: ").append(mpStatus).append("</font></td></tr>");
		c.append("<tr><td height=12></td></tr>");
		c.append("<tr><td align=center><table cellpadding=0 cellspacing=4><tr>");
		for (int pct : new int[]{20, 30, 40, 50, 60, 70})
		{
			String back = (mpThreshold == pct) ? "L2EssenceCommunity.buy_premium_btn_over" : "L2EssenceCommunity.buy_premium_btn";
			c.append("<td><button value=\"").append(pct).append("%\" action=\"bypass _bbsika_setmp_").append(pct).append("\" width=70 height=27 back=\"").append(back).append("\" fore=\"L2EssenceCommunity.buy_premium_btn\"></td>");
		}
		c.append("</tr></table></td></tr>");
		c.append("<tr><td height=30></td></tr>");
		c.append("<tr><td align=center><button value=\"Desativar\" action=\"bypass _bbsika_disablemp\" width=120 height=27 back=\"L2EssenceCommunity.donate_items_btn_over\" fore=\"L2EssenceCommunity.donate_items_btn\"></td></tr>");
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
		c.append("<table width=540 cellpadding=0 cellspacing=0>");
		c.append("<tr><td height=12></td></tr>");
		c.append("<tr><td align=center><font color=\"CDB67F\" name=\"hs15\">ACCOUNT SERVICE</font></td></tr>");
		c.append("<tr><td height=4></td></tr>");
		c.append("<tr><td><img src=\"L2UI.SquareGray\" width=540 height=1></td></tr>");
		c.append("<tr><td height=10></td></tr>");

		// Saldo de Ikoin destacado
		c.append("<tr><td align=center>");
		c.append("<table width=320 background=\"L2EssenceCommunity.home_server_info_bg\" cellpadding=8><tr>");
		c.append("<td align=center width=50><img src=\"L2EssenceCommunity.premium_crown\" width=32 height=22></td>");
		c.append("<td><font color=\"888888\">Seu saldo:</font></td>");
		c.append("<td align=right><font color=\"CDB67F\" name=\"hs18\">").append(credits).append("</font> <font color=\"CDB67F\">Ikoin</font></td>");
		c.append("</tr></table>");
		c.append("</td></tr>");
		c.append("<tr><td height=14></td></tr>");

		// Destaque Premium (28 dias / 50 Ikoin)
		c.append("<tr><td align=center>");
		c.append("<table width=520 background=\"l2ui_ct1.ComboBox_DF_Dropmenu_Bg\" cellpadding=8><tr>");
		c.append("<td width=44 align=center><img src=\"L2EssenceCommunity.premium_crown\" width=32 height=22></td>");
		c.append("<td><font color=\"CDB67F\">Premium Account</font><br1><font color=\"99CC66\">+100% XP/SP e Drop</font> <font color=\"696969\">- ").append(PREMIUM_DAYS).append(" dias</font></td>");
		c.append("<td width=160 align=center>");
		c.append("<button value=\"VER PREMIUM\" action=\"bypass _bbsika_premium\" width=140 height=24 back=\"L2EssenceCommunity.buy_premium_btn_over\" fore=\"L2EssenceCommunity.buy_premium_btn\">");
		c.append("</td></tr></table>");
		c.append("</td></tr>");
		c.append("<tr><td height=14></td></tr>");

		// Cards de servicos (2 colunas) - {nome, custo, icone}
		String[][] services = {
			{"Trocar Classe", "150", "L2EssenceCommunity.change_class_base", "1"},
			{"Trocar Nick", "100", "L2EssenceCommunity.rename", "1"},
			{"Trocar Sexo", "20", "L2EssenceCommunity.change_sex", "1"},
			{"Doar Ikoin", "-", "L2EssenceCommunity.adena", "0"},
			{"Vender Personagem", "-", "L2EssenceCommunity.misc_items", "0"},
		};

		c.append("<tr><td><table width=540 cellpadding=0 cellspacing=0>");
		for (int i = 0; i < services.length; i += 2)
		{
			c.append("<tr>");
			c.append(buildServiceCard(services[i]));
			c.append("<td width=10></td>");
			if (i + 1 < services.length)
			{
				c.append(buildServiceCard(services[i + 1]));
			}
			else
			{
				c.append("<td width=265></td>");
			}
			c.append("</tr>");
			c.append("<tr><td colspan=3 height=10></td></tr>");
		}
		c.append("</table></td></tr>");
		c.append("<tr><td height=6></td></tr>");
		c.append("<tr><td align=center><font color=\"696969\">Servicos ativados em breve. Custo debitado do seu saldo Ikoin.</font></td></tr>");
		c.append("</table>");

		CommunityBoardHandler.separateAndSend(buildFrame(buildNav("account"), c.toString()), player);
	}

	// ======== PREMIUM (28 dias / 50 Ikoin, por conta) ========

	private void showPremiumPage(Player player)
	{
		final long credits = getPlayerCredits(player);
		final long now = System.currentTimeMillis();
		final long expiration = PremiumManager.getInstance().getPremiumExpiration(player.getAccountName());
		final boolean active = expiration > now;

		StringBuilder c = new StringBuilder();
		c.append("<table width=540 cellpadding=0 cellspacing=0>");
		c.append("<tr><td height=12></td></tr>");
		c.append("<tr><td align=center><font color=\"CDB67F\" name=\"hs15\">PREMIUM ACCOUNT</font></td></tr>");
		c.append("<tr><td height=4></td></tr>");
		c.append("<tr><td><img src=\"L2UI.SquareGray\" width=540 height=1></td></tr>");
		c.append("<tr><td height=10></td></tr>");

		// Saldo
		c.append("<tr><td align=center>");
		c.append("<table width=320 background=\"L2EssenceCommunity.home_server_info_bg\" cellpadding=8><tr>");
		c.append("<td align=center width=50><img src=\"L2EssenceCommunity.premium_crown\" width=32 height=22></td>");
		c.append("<td><font color=\"888888\">Seu saldo:</font></td>");
		c.append("<td align=right><font color=\"CDB67F\" name=\"hs18\">").append(credits).append("</font> <font color=\"CDB67F\">Ikoin</font></td>");
		c.append("</tr></table>");
		c.append("</td></tr>");
		c.append("<tr><td height=12></td></tr>");

		// Beneficios
		c.append("<tr><td align=center><font color=\"FFFFFF\">").append(PREMIUM_DAYS).append(" dias de Premium por <font color=\"FFAA00\">").append(PREMIUM_COST_IKOIN).append(" Ikoin</font>.</font></td></tr>");
		c.append("<tr><td height=4></td></tr>");
		c.append("<tr><td align=center><font color=\"99CC66\">+100% XP / SP &nbsp; +100% Drop &nbsp; Pesca exclusiva</font></td></tr>");
		c.append("<tr><td align=center><font color=\"696969\">Vale para todos os personagens da conta.</font></td></tr>");
		c.append("<tr><td height=14></td></tr>");

		if (active)
		{
			final String until = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(expiration);
			c.append("<tr><td align=center><font color=\"99CC66\" name=\"hs12\">Premium ATIVO ate ").append(until).append(".</font></td></tr>");
			c.append("<tr><td height=4></td></tr>");
			c.append("<tr><td align=center><font color=\"696969\">Voce so pode comprar de novo quando expirar.</font></td></tr>");
		}
		else
		{
			c.append("<tr><td align=center>");
			c.append("<button value=\"COMPRAR PREMIUM (").append(PREMIUM_DAYS).append(" dias)\" action=\"bypass _bbsika_buypremium\" width=240 height=33 back=\"L2EssenceCommunity.buy_premium_btn_over\" fore=\"L2EssenceCommunity.buy_premium_btn\">");
			c.append("</td></tr>");
		}
		c.append("</table>");

		CommunityBoardHandler.separateAndSend(buildFrame(buildNav("account"), c.toString()), player);
	}

	private void buyPremium(Player player)
	{
		final String account = player.getAccountName();
		final long now = System.currentTimeMillis();

		// Trava: 1x a cada 28 dias (nao acumula enquanto ativo)
		if (PremiumManager.getInstance().getPremiumExpiration(account) > now)
		{
			player.sendMessage("[Premium] Voce ja tem Premium ativo. Aguarde expirar para comprar de novo.");
			showPremiumPage(player);
			return;
		}

		final long balance = getPlayerCredits(player);
		if (balance < PREMIUM_COST_IKOIN)
		{
			player.sendMessage("[Premium] Ikoin insuficiente. Voce tem " + balance + ", precisa de " + PREMIUM_COST_IKOIN + ".");
			showPremiumPage(player);
			return;
		}

		try (Connection con = DatabaseFactory.getConnection())
		{
			// debita Ikoin
			try (PreparedStatement ps = con.prepareStatement("UPDATE ikoin_balance SET balance=balance-?, updated_at=? WHERE account_name=?"))
			{
				ps.setInt(1, PREMIUM_COST_IKOIN);
				ps.setLong(2, now);
				ps.setString(3, account);
				ps.executeUpdate();
			}
			try (PreparedStatement ps = con.prepareStatement("INSERT INTO ikoin_transactions (account_name, amount, type, description, created_at) VALUES (?,?,?,?,?)"))
			{
				ps.setString(1, account);
				ps.setInt(2, -PREMIUM_COST_IKOIN);
				ps.setString(3, "spend");
				ps.setString(4, "Premium Account " + PREMIUM_DAYS + " dias");
				ps.setLong(5, now);
				ps.executeUpdate();
			}
		}
		catch (Exception e)
		{
			player.sendMessage("[Premium] Erro ao processar a compra. Tente novamente.");
			return;
		}

		// ativa o premium na conta (aplica imediatamente se online; relog reaplica via account_premium)
		PremiumManager.getInstance().addPremiumTime(account, PREMIUM_DAYS, TimeUnit.DAYS);

		player.sendMessage("[Premium] Premium Account ativado por " + PREMIUM_DAYS + " dias! Aproveite o boost de XP e Drop.");
		showPremiumPage(player);
	}

	private String buildServiceCard(String[] s)
	{
		// s = {nome, custo, icone, ativo("1") }
		boolean disponivel = "0".equals(s[3]);
		String custo = s[1].equals("-") ? "" : s[1] + " Ikoin";
		StringBuilder b = new StringBuilder();
		b.append("<td width=265>");
		b.append("<table width=265 background=\"l2ui_ct1.ComboBox_DF_Dropmenu_Bg\" cellpadding=8><tr>");
		b.append("<td width=44 align=center><img src=\"").append(s[2]).append("\" width=32 height=32></td>");
		b.append("<td>");
		b.append("<font color=\"CDB67F\">").append(s[0]).append("</font><br1>");
		if (disponivel)
		{
			b.append("<font color=\"696969\">Em breve</font>");
		}
		else
		{
			b.append("<font color=\"FFAA00\">").append(custo).append("</font>");
		}
		b.append("</td>");
		b.append("<td width=70 align=center>");
		b.append("<button value=\"").append(disponivel ? "Soon" : "Usar").append("\" action=\"bypass _bbsika_account\" width=60 height=22 back=\"L2EssenceCommunity.buy_premium_btn_over\" fore=\"L2EssenceCommunity.buy_premium_btn\">");
		b.append("</td>");
		b.append("</tr></table>");
		b.append("</td>");
		return b.toString();
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
