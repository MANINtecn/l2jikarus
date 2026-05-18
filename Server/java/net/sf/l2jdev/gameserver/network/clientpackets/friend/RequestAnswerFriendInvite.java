package net.sf.l2jdev.gameserver.network.clientpackets.friend;

import java.sql.Connection;
import java.sql.PreparedStatement;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.friend.FriendAddRequestResult;

public class RequestAnswerFriendInvite extends ClientPacket
{
	private int _response;

	@Override
	protected void readImpl()
	{
		this.readByte();
		this._response = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Player requestor = player.getActiveRequester();
			if (requestor != null)
			{
				if (player == requestor)
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIEND_LIST);
				}
				else if (!player.getFriendList().contains(requestor.getObjectId()) && !requestor.getFriendList().contains(player.getObjectId()))
				{
					if (this._response == 1)
					{
						try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO character_friends (charId, friendId) VALUES (?, ?), (?, ?)");)
						{
							statement.setInt(1, requestor.getObjectId());
							statement.setInt(2, player.getObjectId());
							statement.setInt(3, player.getObjectId());
							statement.setInt(4, requestor.getObjectId());
							statement.execute();
							SystemMessage msg = new SystemMessage(SystemMessageId.THAT_PERSON_HAS_BEEN_SUCCESSFULLY_ADDED_TO_YOUR_FRIEND_LIST);
							requestor.sendPacket(msg);
							msg = new SystemMessage(SystemMessageId.S1_HAS_BEEN_ADDED_TO_YOUR_FRIEND_LIST);
							msg.addString(player.getName());
							requestor.sendPacket(msg);
							requestor.getFriendList().add(player.getObjectId());
							msg = new SystemMessage(SystemMessageId.S1_HAS_BEEN_ADDED_TO_YOUR_FRIEND_LIST_2);
							msg.addString(requestor.getName());
							player.sendPacket(msg);
							player.getFriendList().add(requestor.getObjectId());
							player.sendPacket(new FriendAddRequestResult(requestor, 1));
							requestor.sendPacket(new FriendAddRequestResult(player, 1));
						}
						catch (Exception var11)
						{
							PacketLogger.warning("Could not add friend objectid: " + var11.getMessage());
						}
					}
					else
					{
						requestor.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_ADD_A_FRIEND_TO_YOUR_FRIENDS_LIST));
					}

					player.setActiveRequester(null);
					requestor.onTransactionResponse();
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_ALREADY_ON_YOUR_FRIEND_LIST);
					sm.addString(player.getName());
					requestor.sendPacket(sm);
				}
			}
		}
	}
}
