package net.sf.l2jdev.gameserver.network.clientpackets.friend;

import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.L2FriendSay;

public class RequestSendFriendMsg extends ClientPacket
{
	private static Logger LOGGER_CHAT = Logger.getLogger("chat");
	private String _message;
	private String _reciever;

	@Override
	protected void readImpl()
	{
		this._message = this.readString();
		this._reciever = this.readString();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._message != null && !this._message.isEmpty() && this._message.length() <= 300)
			{
				Player targetPlayer = World.getInstance().getPlayer(this._reciever);
				if (targetPlayer != null && targetPlayer.getFriendList().contains(player.getObjectId()))
				{
					if (GeneralConfig.LOG_CHAT)
					{
						StringBuilder sb = new StringBuilder();
						sb.append("PRIV_MSG [");
						sb.append(player);
						sb.append(" to ");
						sb.append(targetPlayer);
						sb.append("] ");
						sb.append(this._message);
						LOGGER_CHAT.info(sb.toString());
					}

					targetPlayer.sendPacket(new L2FriendSay(player.getName(), this._reciever, this._message));
				}
				else
				{
					player.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE);
				}
			}
		}
	}
}
