package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.ChatType;
import net.sf.l2jdev.gameserver.network.serverpackets.ExRequestNewInvitePartyInquiry;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class RequestNewInvitePartyInquiry extends ClientPacket
{
	private int _reqType;
	private ChatType _sayType;

	@Override
	protected void readImpl()
	{
		this._reqType = this.readByte();
		int chatTypeValue = this.readByte();
		switch (chatTypeValue)
		{
			case 0:
				this._sayType = ChatType.GENERAL;
				break;
			case 1:
				this._sayType = ChatType.SHOUT;
			case 2:
			case 5:
			case 6:
			case 7:
			default:
				break;
			case 3:
				this._sayType = ChatType.PARTY;
				break;
			case 4:
				this._sayType = ChatType.CLAN;
				break;
			case 8:
				this._sayType = ChatType.TRADE;
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (player.isChatBanned())
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_ALLOWED_TO_CHAT_WITH_A_CONTACT_WHILE_A_CHATTING_BLOCK_IS_IMPOSED);
			}
			else if (this.getClient().getFloodProtectors().canSendMail())
			{
				if (GeneralConfig.JAIL_DISABLE_CHAT && player.isJailed() && !player.isGM())
				{
					player.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
				}
				else if (player.isInOlympiadMode())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_CHAT_WHILE_PARTICIPATING_IN_THE_OLYMPIAD);
				}
				else if (this._sayType == ChatType.GENERAL || this._sayType == ChatType.TRADE || this._sayType == ChatType.SHOUT || this._sayType == ChatType.CLAN || this._sayType == ChatType.ALLIANCE)
				{
					switch (this._reqType)
					{
						case 0:
							if (player.isInParty())
							{
								return;
							}
							break;
						case 1:
							Party party = player.getParty();
							if (party == null || !party.isLeader(player) || party.getCommandChannel() != null)
							{
								return;
							}
					}

					switch (this._sayType)
					{
						case SHOUT:
							if (player.inObserverMode())
							{
								player.sendPacket(SystemMessageId.YOU_CANNOT_CHAT_WHILE_IN_THE_SPECTATOR_MODE);
								return;
							}

							Broadcast.toAllOnlinePlayers(new ExRequestNewInvitePartyInquiry(player, this._reqType, this._sayType));
							break;
						case TRADE:
							if (player.inObserverMode())
							{
								player.sendPacket(SystemMessageId.YOU_CANNOT_CHAT_WHILE_IN_THE_SPECTATOR_MODE);
								return;
							}

							Broadcast.toAllOnlinePlayers(new ExRequestNewInvitePartyInquiry(player, this._reqType, this._sayType));
							break;
						case GENERAL:
							if (player.inObserverMode())
							{
								player.sendPacket(SystemMessageId.YOU_CANNOT_CHAT_WHILE_IN_THE_SPECTATOR_MODE);
								return;
							}

							ExRequestNewInvitePartyInquiry msg = new ExRequestNewInvitePartyInquiry(player, this._reqType, this._sayType);
							player.sendPacket(msg);
							World.getInstance().forEachVisibleObjectInRange(player, Player.class, PlayerConfig.ALT_PARTY_RANGE, nearby -> nearby.sendPacket(msg));
							break;
						case CLAN:
							Clan clan = player.getClan();
							if (clan == null)
							{
								player.sendPacket(SystemMessageId.YOU_ARE_NOT_IN_A_CLAN);
								return;
							}

							clan.broadcastToOnlineMembers(new ExRequestNewInvitePartyInquiry(player, this._reqType, this._sayType));
							break;
						case ALLIANCE:
							if (player.getClan() == null || player.getClan() != null && player.getClan().getAllyId() == 0)
							{
								player.sendPacket(SystemMessageId.YOU_ARE_NOT_IN_AN_ALLIANCE);
								return;
							}

							player.getClan().broadcastToOnlineAllyMembers(new ExRequestNewInvitePartyInquiry(player, this._reqType, this._sayType));
					}
				}
			}
		}
	}
}
