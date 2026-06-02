package handlers.communityboard;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.handler.CommunityBoardHandler;
import net.sf.l2jdev.gameserver.handler.IItemHandler;
import net.sf.l2jdev.gameserver.handler.IParseBoardHandler;
import net.sf.l2jdev.gameserver.handler.ItemHandler;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

/**
 * L2 Ikarus Community Board
 * - Auto-use MP potion por threshold %
 * - Campo de codigo referral (valida no site via webhook futuro)
 */
public class IkaCommunityBoard implements IParseBoardHandler
{
	private static final int MP_POTION_ID = 49854;

	// tasks de auto-use por objectId do player
	private static final Map<Integer, ScheduledFuture<?>> AUTO_TASKS = new ConcurrentHashMap<>();

	private static final String[] COMMANDS =
	{
		"_bbshome",
		"_bbstop",
		"_bbsika",
		"_bbsika_setmp",
		"_bbsika_disablemp",
		"_bbsika_referal",
	};

	@Override
	public String[] getCommandList()
	{
		return COMMANDS;
	}

	@Override
	public boolean onCommand(String command, Player player)
	{
		if (command.equals("_bbshome") || command.equals("_bbstop") || command.equals("_bbsika"))
		{
			showMainPage(player);
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
			showMainPage(player);
		}
		else if (command.equals("_bbsika_disablemp"))
		{
			player.getVariables().set("IKA_MP_THRESHOLD", 0);
			stopAutoTask(player.getObjectId());
			player.sendMessage("Auto-use MP: desativado.");
			showMainPage(player);
		}
		else if (command.startsWith("_bbsika_referal_"))
		{
			// Placeholder — validacao futura via API do site
			String code = command.replace("_bbsika_referal_", "").trim().toUpperCase();
			if (code.isEmpty())
			{
				player.sendMessage("Digite um codigo valido.");
			}
			else
			{
				// TODO: HTTP request para o site validar o codigo
				player.sendMessage("Codigo '" + code + "' recebido. Aguarde validacao.");
			}
			showMainPage(player);
		}

		return false;
	}

	// ======== Auto-use task ========

	private static void startAutoTask(Player player)
	{
		stopAutoTask(player.getObjectId());
		ScheduledFuture<?> task = ThreadPool.scheduleAtFixedRate(() ->
		{
			if (player == null || !player.isOnline() || player.isAlikeDead())
			{
				stopAutoTask(player != null ? player.getObjectId() : -1);
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
						handler.useItem(player, potion, false);
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

	// ======== HTML ========

	private void showMainPage(Player player)
	{
		int mpThreshold = player.getVariables().getInt("IKA_MP_THRESHOLD", 0);
		String mpStatus = mpThreshold > 0 ? "<font color=\"44FF44\">Ativo em " + mpThreshold + "%</font>" : "<font color=\"FF4444\">Desativado</font>";

		int hpPct = (int) ((player.getCurrentHp() / player.getMaxHp()) * 100);
		int mpPct = (int) ((player.getCurrentMp() / player.getMaxMp()) * 100);
		int cpPct = (int) ((player.getCurrentCp() / player.getMaxCp()) * 100);

		int onlinePlayers = World.getInstance().getPlayers().size();

		StringBuilder sb = new StringBuilder();
		sb.append("<html noscrollbar><body>");
		sb.append("<table width=755><tr><td height=8></td></tr></table>");
		sb.append("<table width=755 cellpadding=0 cellspacing=0>");
		sb.append("<tr><td width=200 valign=top align=center>");

		// ===== NAVEGAÇÃO ESQUERDA =====
		sb.append("<table width=190 cellpadding=0 cellspacing=2>");
		sb.append("<tr><td height=10></td></tr>");
		sb.append("<tr><td align=center><font color=\"CDB67F\" name=\"hs12\">L2 IKARUS</font></td></tr>");
		sb.append("<tr><td height=6></td></tr>");
		sb.append("<tr><td><button value=\"  Home\" action=\"bypass _bbsika\" width=185 height=28 back=\"L2UI_CT1.HtmlWnd_DF_TextureKnight\" fore=\"L2UI_CT1.HtmlWnd_DF_TextureKnight\"></td></tr>");
		sb.append("<tr><td height=4></td></tr>");
		sb.append("<tr><td><button value=\"  Buffer\" action=\"bypass _bbstop;buffer/main.html\" width=185 height=28 back=\"L2UI_CT1.HtmlWnd_DF_Area_Down\" fore=\"L2UI_CT1.HtmlWnd_DF_Area\"></td></tr>");
		sb.append("<tr><td height=4></td></tr>");
		sb.append("<tr><td><button value=\"  Gatekeeper\" action=\"bypass _bbstop;gatekeeper/main.html\" width=185 height=28 back=\"L2UI_CT1.HtmlWnd_DF_Campaign_Down\" fore=\"L2UI_CT1.HtmlWnd_DF_Campaign\"></td></tr>");
		sb.append("<tr><td height=4></td></tr>");
		sb.append("<tr><td><button value=\"  Merchant\" action=\"bypass _bbstop;merchant/main.html\" width=185 height=28 back=\"L2UI_CT1.OlympiadWnd_DF_BuyEquip_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_BuyEquip\"></td></tr>");
		sb.append("<tr><td height=4></td></tr>");
		sb.append("<tr><td><button value=\"  Drop Search\" action=\"bypass _bbstop;dropsearch/main.html\" width=185 height=28 back=\"L2UI_CT1.HtmlWnd_DF_Area_Down\" fore=\"L2UI_CT1.HtmlWnd_DF_Area\"></td></tr>");
		sb.append("<tr><td height=4></td></tr>");
		sb.append("<tr><td><button value=\"  Premium\" action=\"bypass _bbstop;premium/main.html\" width=185 height=28 back=\"L2UI_CT1.OlympiadWnd_DF_Reward_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Reward\"></td></tr>");
		sb.append("</table>");

		sb.append("</td>");
		sb.append("<td width=8></td>");

		// ===== CONTEÚDO CENTRAL (dividido em 2 colunas) =====
		sb.append("<td valign=top>");
		sb.append("<table width=547 cellpadding=0 cellspacing=0 background=\"L2UI_CT1.Windows_DF_TooltipBG\">");
		sb.append("<tr><td height=8></td></tr>");

		// Título
		sb.append("<tr><td align=center><font color=\"CDB67F\" name=\"hs12\">BEM-VINDO AO L2 IKARUS</font></td></tr>");
		sb.append("<tr><td height=4></td></tr>");
		sb.append("<tr><td><img src=\"L2UI.SquareGray\" width=530 height=1></td></tr>");
		sb.append("<tr><td height=6></td></tr>");

		// Duas colunas: esquerda = server info | direita = personagem
		sb.append("<tr><td>");
		sb.append("<table width=547 cellpadding=0 cellspacing=0><tr>");

		// --- Coluna esquerda: Server Info + Referral ---
		sb.append("<td width=265 valign=top align=center>");
		sb.append("<table width=255 cellpadding=2 cellspacing=0>");
		sb.append("<tr><td align=center><font color=\"AAAAAA\">STATUS DO SERVIDOR</font></td></tr>");
		sb.append("<tr><td height=4></td></tr>");
		sb.append("<tr><td align=center><font color=\"44FF44\">&#9679;</font> <font color=\"FFFFFF\">Online: ").append(onlinePlayers).append(" jogadores</font></td></tr>");
		sb.append("<tr><td height=2></td></tr>");
		sb.append("<tr><td align=center><font color=\"888888\">Servidor: Bartz | D10 Global</font></td></tr>");
		sb.append("<tr><td height=2></td></tr>");
		sb.append("<tr><td align=center><font color=\"888888\">Max Level: 99 | EXP: x1</font></td></tr>");
		sb.append("<tr><td height=10></td></tr>");
		sb.append("<tr><td><img src=\"L2UI.SquareGray\" width=255 height=1></td></tr>");
		sb.append("<tr><td height=8></td></tr>");

		// Referral
		sb.append("<tr><td align=center><font color=\"AAAAAA\">CODIGO REFERRAL / PROMO</font></td></tr>");
		sb.append("<tr><td height=4></td></tr>");
		sb.append("<tr><td align=center><font color=\"888888\">Tem um codigo? Digite abaixo:</font></td></tr>");
		sb.append("<tr><td height=4></td></tr>");
		sb.append("<tr><td align=center>");
		sb.append("<edit var=\"refCode\" width=200 height=15></edit><br>");
		sb.append("<button value=\"Resgatar\" action=\"bypass _bbsika_referal_$refCode\" width=120 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		sb.append("</td></tr>");
		sb.append("<tr><td height=4></td></tr>");
		sb.append("<tr><td align=center><font color=\"696969\" name=\"hs9\">Codigos liberados por streamers e eventos</font></td></tr>");
		sb.append("</table>");
		sb.append("</td>");

		// Separador vertical
		sb.append("<td width=2><img src=\"L2UI.SquareGray\" width=1 height=380></td>");

		// --- Coluna direita: Personagem + Auto-use ---
		sb.append("<td width=278 valign=top align=center>");
		sb.append("<table width=268 cellpadding=2 cellspacing=0>");
		sb.append("<tr><td align=center><font color=\"AAAAAA\">MEU PERSONAGEM</font></td></tr>");
		sb.append("<tr><td height=4></td></tr>");
		sb.append("<tr><td align=center><font color=\"CDB67F\">").append(player.getName()).append("</font> <font color=\"888888\">Lv.").append(player.getLevel()).append("</font></td></tr>");
		sb.append("<tr><td height=6></td></tr>");

		// Barra HP
		sb.append("<tr><td>");
		sb.append("<table width=258 cellpadding=0 cellspacing=0><tr>");
		sb.append("<td width=25><font color=\"888888\" name=\"hs9\">HP</font></td>");
		sb.append("<td width=195 bgcolor=\"330000\"><table width=").append((int)(1.95 * hpPct)).append(" height=10 bgcolor=\"FF3333\"><tr><td></td></tr></table></td>");
		sb.append("<td width=35 align=right><font color=\"FF6666\" name=\"hs9\">").append(hpPct).append("%</font></td>");
		sb.append("</tr></table></td></tr>");
		sb.append("<tr><td height=3></td></tr>");

		// Barra CP
		sb.append("<tr><td>");
		sb.append("<table width=258 cellpadding=0 cellspacing=0><tr>");
		sb.append("<td width=25><font color=\"888888\" name=\"hs9\">CP</font></td>");
		sb.append("<td width=195 bgcolor=\"003300\"><table width=").append((int)(1.95 * cpPct)).append(" height=10 bgcolor=\"33CC33\"><tr><td></td></tr></table></td>");
		sb.append("<td width=35 align=right><font color=\"66FF66\" name=\"hs9\">").append(cpPct).append("%</font></td>");
		sb.append("</tr></table></td></tr>");
		sb.append("<tr><td height=3></td></tr>");

		// Barra MP
		sb.append("<tr><td>");
		sb.append("<table width=258 cellpadding=0 cellspacing=0><tr>");
		sb.append("<td width=25><font color=\"888888\" name=\"hs9\">MP</font></td>");
		sb.append("<td width=195 bgcolor=\"000033\"><table width=").append((int)(1.95 * mpPct)).append(" height=10 bgcolor=\"3366FF\"><tr><td></td></tr></table></td>");
		sb.append("<td width=35 align=right><font color=\"6699FF\" name=\"hs9\">").append(mpPct).append("%</font></td>");
		sb.append("</tr></table></td></tr>");

		sb.append("<tr><td height=10></td></tr>");
		sb.append("<tr><td><img src=\"L2UI.SquareGray\" width=258 height=1></td></tr>");
		sb.append("<tr><td height=8></td></tr>");

		// Auto-use MP
		sb.append("<tr><td align=center><font color=\"AAAAAA\">AUTO-USE MP POTION</font></td></tr>");
		sb.append("<tr><td height=2></td></tr>");
		sb.append("<tr><td align=center><font color=\"888888\" name=\"hs9\">Status: ").append(mpStatus).append("</font></td></tr>");
		sb.append("<tr><td height=4></td></tr>");
		sb.append("<tr><td align=center><font color=\"696969\" name=\"hs9\">Usar potion quando MP cair abaixo de:</font></td></tr>");
		sb.append("<tr><td height=4></td></tr>");
		sb.append("<tr><td align=center>");
		sb.append("<table cellpadding=0 cellspacing=2><tr>");
		for (int pct : new int[]{20, 30, 40, 50, 60})
		{
			String back = (mpThreshold == pct) ? "L2UI_CT1.Button_DF_Down" : "L2UI_CT1.Button_DF";
			sb.append("<td><button value=\"").append(pct).append("%\" action=\"bypass _bbsika_setmp_").append(pct).append("\" width=42 height=20 back=\"").append(back).append("\" fore=\"L2UI_CT1.Button_DF\"></td>");
		}
		sb.append("</tr></table></td></tr>");
		sb.append("<tr><td height=4></td></tr>");
		sb.append("<tr><td align=center><button value=\"Desativar\" action=\"bypass _bbsika_disablemp\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
		sb.append("</table>");
		sb.append("</td>");

		sb.append("</tr></table>"); // fim das 2 colunas

		sb.append("</td></tr>");
		sb.append("<tr><td height=8></td></tr>");
		sb.append("</table>"); // fim content
		sb.append("</td></tr></table>"); // fim layout principal
		sb.append("</body></html>");

		CommunityBoardHandler.separateAndSend(sb.toString(), player);
	}
}
