package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.ServerConfig;
import net.sf.l2jdev.gameserver.config.custom.WalkerBotProtectionConfig;
import net.sf.l2jdev.gameserver.handler.ChatHandler;
import net.sf.l2jdev.gameserver.handler.IChatHandler;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.effects.EffectFlag;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerChat;
import net.sf.l2jdev.gameserver.model.events.returns.ChatFilterReturn;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2jdev.gameserver.network.Disconnection;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.ChatType;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.LeaveWorld;

public class Say2 extends ClientPacket
{
	private static Logger LOGGER_CHAT = Logger.getLogger("chat");
	private static final String[] WALKER_COMMAND_LIST = new String[]
	{
		"USESKILL",
		"USEITEM",
		"BUYITEM",
		"SELLITEM",
		"SAVEITEM",
		"LOADITEM",
		"MSG",
		"DELAY",
		"LABEL",
		"JMP",
		"CALL",
		"RETURN",
		"MOVETO",
		"NPCSEL",
		"NPCDLG",
		"DLGSEL",
		"CHARSTATUS",
		"POSOUTRANGE",
		"POSINRANGE",
		"GOHOME",
		"SAY",
		"EXIT",
		"PAUSE",
		"STRINDLG",
		"STRNOTINDLG",
		"CHANGEWAITTYPE",
		"FORCEATTACK",
		"ISMEMBER",
		"REQUESTJOINPARTY",
		"REQUESTOUTPARTY",
		"QUITPARTY",
		"MEMBERSTATUS",
		"CHARBUFFS",
		"ITEMCOUNT",
		"FOLLOWTELEPORT"
	};
	private String _text;
	private int _type;
	private String _target;
	private boolean _shareLocation;

	@Override
	protected void readImpl()
	{
		this._text = this.readString();
		this._type = this.readInt();
		this._shareLocation = this.readByte() == 1;
		if (this._type == ChatType.WHISPER.getClientId())
		{
			this._target = this.readString();
			this._shareLocation = false;
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			ChatType chatType = ChatType.findByClientId(this._type);
			if (chatType == null)
			{
				PacketLogger.warning("Say2: Invalid type: " + this._type + " Player : " + player.getName() + " text: " + this._text);
				player.sendPacket(ActionFailed.STATIC_PACKET);
				Disconnection.of(player).storeAndDeleteWith(LeaveWorld.STATIC_PACKET);
			}
			else if (!player.isMercenary())
			{
				if (this._text.isEmpty())
				{
					PacketLogger.warning(player.getName() + ": sending empty text. Possible packet hack!");
					player.sendPacket(ActionFailed.STATIC_PACKET);
					Disconnection.of(player).storeAndDeleteWith(LeaveWorld.STATIC_PACKET);
				}
				else if (player.isGM() || (this._text.indexOf(8) < 0 || this._text.length() <= 500) && (this._text.indexOf(8) >= 0 || this._text.length() <= 105))
				{
					if (WalkerBotProtectionConfig.L2WALKER_PROTECTION && chatType == ChatType.WHISPER && this.checkBot(this._text))
					{
						PunishmentManager.handleIllegalPlayerAction(player, "Client Emulator Detect: " + player + " using L2Walker.", GeneralConfig.DEFAULT_PUNISH);
					}
					else if (!player.isCursedWeaponEquipped() || chatType != ChatType.TRADE && chatType != ChatType.SHOUT)
					{
						if (player.isChatBanned() && this._text.charAt(0) != '.')
						{
							if (player.isAffected(EffectFlag.CHAT_BLOCK))
							{
								player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_CHATTING_IS_NOT_ALLOWED);
							}
							else if (GeneralConfig.BAN_CHAT_CHANNELS.contains(chatType))
							{
								player.sendPacket(SystemMessageId.IF_YOU_TRY_TO_CHAT_BEFORE_THE_PROHIBITION_IS_REMOVED_THE_PROHIBITION_TIME_WILL_INCREASE_EVEN_FURTHER_S1_SEC_OF_PROHIBITION_IS_LEFT);
							}
						}
						else if (player.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(player))
						{
							player.sendPacket(SystemMessageId.YOU_CANNOT_CHAT_WHILE_PARTICIPATING_IN_THE_OLYMPIAD);
						}
						else if (!player.isJailed() || !GeneralConfig.JAIL_DISABLE_CHAT || chatType != ChatType.WHISPER && chatType != ChatType.SHOUT && chatType != ChatType.TRADE && chatType != ChatType.HERO_VOICE)
						{
							if (chatType == ChatType.PETITION_PLAYER && player.isGM())
							{
								chatType = ChatType.PETITION_GM;
							}

							if (GeneralConfig.LOG_CHAT)
							{
								StringBuilder sb = new StringBuilder();
								sb.append(chatType.name());
								sb.append(" [");
								sb.append(player);
								if (chatType == ChatType.WHISPER)
								{
									sb.append(" to ");
									sb.append(this._target);
									sb.append("] ");
									sb.append(this._text);
									LOGGER_CHAT.info(sb.toString());
								}
								else
								{
									sb.append("] ");
									sb.append(this._text);
									LOGGER_CHAT.info(sb.toString());
								}
							}

							if (this._text.indexOf(8) < 0 || this.parseAndPublishItem(this.getClient(), player))
							{
								if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CHAT, player))
								{
									ChatFilterReturn filter = EventDispatcher.getInstance().notifyEvent(new OnPlayerChat(player, this._target, this._text, chatType), player, ChatFilterReturn.class);
									if (filter != null)
									{
										this._text = filter.getFilteredText();
										chatType = filter.getChatType();
									}
								}

								if (GeneralConfig.USE_SAY_FILTER)
								{
									this.checkText();
								}

								IChatHandler handler = ChatHandler.getInstance().getHandler(chatType);
								if (handler != null)
								{
									handler.onChat(chatType, player, this._target, this._text, this._shareLocation);
								}
								else
								{
									PacketLogger.info("No handler registered for ChatType: " + this._type + " Player: " + this.getClient());
								}
							}
						}
						else
						{
							player.sendMessage("You can not chat with players outside of the jail.");
						}
					}
					else
					{
						player.sendPacket(SystemMessageId.SHOUT_AND_TRADE_CHATTING_CANNOT_BE_USED_WHILE_POSSESSING_A_CURSED_WEAPON);
					}
				}
				else
				{
					player.sendPacket(SystemMessageId.WHEN_A_USER_S_KEYBOARD_INPUT_EXCEEDS_A_CERTAIN_CUMULATIVE_SCORE_A_CHAT_BAN_WILL_BE_APPLIED_THIS_IS_DONE_TO_DISCOURAGE_SPAMMING_PLEASE_AVOID_POSTING_THE_SAME_MESSAGE_MULTIPLE_TIMES_DURING_A_SHORT_PERIOD);
				}
			}
		}
	}

	protected boolean checkBot(String text)
	{
		for (String botCommand : WALKER_COMMAND_LIST)
		{
			if (text.startsWith(botCommand))
			{
				return true;
			}
		}

		return false;
	}

	private void checkText()
	{
		String filteredText = this._text;

		for (String pattern : ServerConfig.FILTER_LIST)
		{
			filteredText = filteredText.replaceAll("(?i)" + pattern, GeneralConfig.CHAT_FILTER_CHARS);
		}

		this._text = filteredText;
	}

	private boolean parseAndPublishItem(GameClient client, Player owner)
	{
		int pos1 = -1;

		while ((pos1 = this._text.indexOf(8, pos1)) > -1)
		{
			int pos = this._text.indexOf("ID=", pos1);
			if (pos == -1)
			{
				return false;
			}

			StringBuilder result = new StringBuilder(9);
			pos += 3;

			while (Character.isDigit(this._text.charAt(pos)))
			{
				result.append(this._text.charAt(pos++));
			}

			int id = Integer.parseInt(result.toString());
			Item item = owner.getInventory().getItemByObjectId(id);
			if (item == null)
			{
				PacketLogger.info(client + " trying publish item which does not own! ID:" + id);
				return false;
			}

			item.publish();
			pos1 = this._text.indexOf(8, pos) + 1;
			if (pos1 == 0)
			{
				PacketLogger.info(client + " sent invalid publish item msg! ID:" + id);
				return false;
			}
		}

		return true;
	}
}
