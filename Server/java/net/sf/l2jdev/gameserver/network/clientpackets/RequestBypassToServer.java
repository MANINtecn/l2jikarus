package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.StringTokenizer;

import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.commons.util.TraceUtil;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.custom.PremiumSystemConfig;
import net.sf.l2jdev.gameserver.data.xml.MultisellData;
import net.sf.l2jdev.gameserver.handler.AdminCommandHandler;
import net.sf.l2jdev.gameserver.handler.BypassHandler;
import net.sf.l2jdev.gameserver.handler.CommunityBoardHandler;
import net.sf.l2jdev.gameserver.handler.IBypassHandler;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcManorBypass;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcMenuSelect;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerBypass;
import net.sf.l2jdev.gameserver.model.events.returns.TerminateReturn;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.olympiad.Hero;
import net.sf.l2jdev.gameserver.network.Disconnection;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.LeaveWorld;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public class RequestBypassToServer extends ClientPacket
{
	private static final String[] _possibleNonHtmlCommands = new String[]
	{
		"_bbs",
		"bbs",
		"_mail",
		"_friend",
		"_match",
		"_diary",
		"_olympiad?command",
		"menu_select",
		"manor_menu_select",
		"pccafe"
	};
	private String _command;

	@Override
	protected void readImpl()
	{
		this._command = this.readString();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._command.isEmpty())
			{
				PacketLogger.warning(player + " sent empty bypass!");
				Disconnection.of(this.getClient(), player).storeAndDeleteWith(LeaveWorld.STATIC_PACKET);
			}
			else
			{
				boolean requiresBypassValidation = true;

				for (String possibleNonHtmlCommand : _possibleNonHtmlCommands)
				{
					if (this._command.startsWith(possibleNonHtmlCommand))
					{
						requiresBypassValidation = false;
						break;
					}
				}

				int bypassOriginId = 0;
				if (requiresBypassValidation)
				{
					bypassOriginId = player.validateHtmlAction(this._command);
					if ((bypassOriginId == -1) || (bypassOriginId > 0 && !LocationUtil.isInsideRangeOfObjectId(player, bypassOriginId, 250)))
					{
						return;
					}
				}

				if (this.getClient().getFloodProtectors().canUseServerBypass())
				{
					if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_BYPASS, player))
					{
						TerminateReturn terminateReturn = EventDispatcher.getInstance().notifyEvent(new OnPlayerBypass(player, this._command), player, TerminateReturn.class);
						if (terminateReturn != null && terminateReturn.terminate())
						{
							return;
						}
					}

					try
					{
						if (this._command.startsWith("admin_"))
						{
							AdminCommandHandler.getInstance().onCommand(player, this._command, true);
						}
						else if (CommunityBoardHandler.getInstance().isCommunityBoardCommand(this._command))
						{
							CommunityBoardHandler.getInstance().handleParseCommand(this._command, player);
						}
						else if (this._command.equals("come_here") && player.isGM())
						{
							this.comeHere(player);
						}
						else if (this._command.startsWith("npc_"))
						{
							int endOfId = this._command.indexOf(95, 5);
							String id;
							if (endOfId > 0)
							{
								id = this._command.substring(4, endOfId);
							}
							else
							{
								id = this._command.substring(4);
							}

							if (StringUtil.isNumeric(id))
							{
								WorldObject object = World.getInstance().findObject(Integer.parseInt(id));
								if (object != null && object.isNpc() && endOfId > 0 && player.isInsideRadius2D(object, 250))
								{
									object.asNpc().onBypassFeedback(player, this._command.substring(endOfId + 1));
								}
							}

							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
						else if (this._command.startsWith("item_"))
						{
							int endOfIdx = this._command.indexOf(95, 5);
							String idx;
							if (endOfIdx > 0)
							{
								idx = this._command.substring(5, endOfIdx);
							}
							else
							{
								idx = this._command.substring(5);
							}

							try
							{
								Item item = player.getInventory().getItemByObjectId(Integer.parseInt(idx));
								if (item != null && endOfIdx > 0)
								{
									item.onBypassFeedback(player, this._command.substring(endOfIdx + 1));
								}

								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
							catch (NumberFormatException var10)
							{
								PacketLogger.warning("NFE for command [" + this._command + "] " + var10.getMessage());
							}
						}
						else if (this._command.startsWith("_match"))
						{
							String params = this._command.substring(this._command.indexOf(63) + 1);
							StringTokenizer st = new StringTokenizer(params, "&");
							int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
							int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
							int heroid = Hero.getInstance().getHeroByClass(heroclass);
							if (heroid > 0)
							{
								Hero.getInstance().showHeroFights(player, heroclass, heroid, heropage);
							}
						}
						else if (this._command.startsWith("_diary"))
						{
							String params = this._command.substring(this._command.indexOf(63) + 1);
							StringTokenizer st = new StringTokenizer(params, "&");
							int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
							int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
							int heroid = Hero.getInstance().getHeroByClass(heroclass);
							if (heroid > 0)
							{
								Hero.getInstance().showHeroDiary(player, heroclass, heroid, heropage);
							}
						}
						else if (this._command.startsWith("_olympiad?command"))
						{
							int arenaId = Integer.parseInt(this._command.split("=")[2]);
							IBypassHandler handler = BypassHandler.getInstance().getHandler("arenachange");
							if (handler != null)
							{
								handler.onCommand("arenachange " + (arenaId - 1), player, null);
							}
						}
						else if (this._command.startsWith("menu_select"))
						{
							Npc lastNpc = player.getLastFolkNPC();
							if (lastNpc != null && lastNpc.canInteract(player) && EventDispatcher.getInstance().hasListener(EventType.ON_NPC_MENU_SELECT, lastNpc))
							{
								String[] split = this._command.substring(this._command.indexOf(63) + 1).split("&");
								int ask = Integer.parseInt(split[0].split("=")[1]);
								int reply = Integer.parseInt(split[1].split("=")[1]);
								EventDispatcher.getInstance().notifyEventAsync(new OnNpcMenuSelect(player, lastNpc, ask, reply), lastNpc);
							}
						}
						else if (this._command.startsWith("manor_menu_select"))
						{
							Npc lastNpc = player.getLastFolkNPC();
							if (GeneralConfig.ALLOW_MANOR && lastNpc != null && lastNpc.canInteract(player) && EventDispatcher.getInstance().hasListener(EventType.ON_NPC_MANOR_BYPASS, lastNpc))
							{
								String[] split = this._command.substring(this._command.indexOf(63) + 1).split("&");
								int ask = Integer.parseInt(split[0].split("=")[1]);
								int state = Integer.parseInt(split[1].split("=")[1]);
								boolean time = split[2].split("=")[1].equals("1");
								EventDispatcher.getInstance().notifyEventAsync(new OnNpcManorBypass(player, lastNpc, ask, state, time), lastNpc);
							}
						}
						else if (this._command.startsWith("pccafe"))
						{
							if (!PremiumSystemConfig.PC_CAFE_ENABLED)
							{
								return;
							}

							int multisellId = Integer.parseInt(this._command.substring(10).trim());
							MultisellData.getInstance().separateAndSend(multisellId, player, null, false);
						}
						else
						{
							IBypassHandler handler = BypassHandler.getInstance().getHandler(this._command);
							if (handler != null)
							{
								if (bypassOriginId > 0)
								{
									WorldObject bypassOrigin = World.getInstance().findObject(bypassOriginId);
									if (bypassOrigin != null && bypassOrigin.isCreature())
									{
										handler.onCommand(this._command, player, bypassOrigin.asCreature());
									}
									else
									{
										handler.onCommand(this._command, player, null);
									}
								}
								else
								{
									handler.onCommand(this._command, player, null);
								}
							}
							else
							{
								PacketLogger.warning(this.getClient() + " sent not handled RequestBypassToServer: [" + this._command + "]");
							}
						}
					}
					catch (Exception var11)
					{
						PacketLogger.warning("Exception processing bypass from " + player + ": " + this._command + " " + var11.getMessage());
						PacketLogger.warning(TraceUtil.getStackTrace(var11));
						if (player.isGM())
						{
							StringBuilder sb = new StringBuilder(200);
							sb.append("<html><body>");
							sb.append("Bypass error: " + var11 + "<br1>");
							sb.append("Bypass command: " + this._command + "<br1>");
							sb.append("StackTrace:<br1>");

							for (StackTraceElement ste : var11.getStackTrace())
							{
								sb.append(ste + "<br1>");
							}

							sb.append("</body></html>");
							NpcHtmlMessage msg = new NpcHtmlMessage(0, 1, sb.toString());
							msg.disableValidation();
							player.sendPacket(msg);
						}
					}
				}
			}
		}
	}

	protected void comeHere(Player player)
	{
		WorldObject obj = player.getTarget();
		if (obj != null)
		{
			if (obj.isNpc())
			{
				Npc temp = obj.asNpc();
				temp.setTarget(player);
				temp.getAI().setIntention(Intention.MOVE_TO, player.getLocation());
			}
		}
	}
}
