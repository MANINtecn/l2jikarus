package net.sf.l2jdev.gameserver.network.clientpackets.friend;

import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestFriendList extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(SystemMessageId.FRIENDS_LIST);
			Player friend = null;

			for (int id : player.getFriendList())
			{
				String friendName = CharInfoTable.getInstance().getNameById(id);
				if (friendName != null)
				{
					friend = World.getInstance().getPlayer(friendName);
					SystemMessage sm;
					if (friend != null && friend.isOnline())
					{
						sm = new SystemMessage(SystemMessageId.S1_CURRENTLY_ONLINE);
						sm.addString(friendName);
					}
					else
					{
						sm = new SystemMessage(SystemMessageId.S1_OFFLINE);
						sm.addString(friendName);
					}

					player.sendPacket(sm);
				}
			}

			player.sendPacket(SystemMessageId.EMPTY_3);
		}
	}
}
