package net.sf.l2jdev.gameserver.network.serverpackets.friend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class FriendList extends ServerPacket
{
	private final List<FriendList.FriendInfo> _info = new LinkedList<>();

	public FriendList(Player player)
	{
		for (int objId : player.getFriendList())
		{
			String name = CharInfoTable.getInstance().getNameById(objId);
			Player player1 = World.getInstance().getPlayer(objId);
			boolean online = false;
			int classid = 0;
			int level = 0;
			if (player1 == null)
			{
				try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT char_name, online, classid, level FROM characters WHERE charId = ?");)
				{
					statement.setInt(1, objId);

					try (ResultSet rset = statement.executeQuery())
					{
						if (rset.next())
						{
							this._info.add(new FriendList.FriendInfo(objId, rset.getString(1), rset.getInt(2) == 1, rset.getInt(3), rset.getInt(4)));
						}
					}
				}
				catch (Exception var20)
				{
				}
			}
			else
			{
				if (player1.isOnline())
				{
					online = true;
				}

				classid = player1.getPlayerClass().getId();
				level = player1.getLevel();
				this._info.add(new FriendList.FriendInfo(objId, name, online, classid, level));
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.FRIEND_LIST.writeId(this, buffer);
		buffer.writeInt(this._info.size());

		for (FriendList.FriendInfo info : this._info)
		{
			buffer.writeInt(info._objId);
			buffer.writeString(info._name);
			buffer.writeInt(info._online);
			buffer.writeInt(info._online ? info._objId : 0);
			buffer.writeInt(info._classid);
			buffer.writeInt(info._level);
		}
	}

	private static class FriendInfo
	{
		int _objId;
		String _name;
		boolean _online;
		int _classid;
		int _level;

		public FriendInfo(int objId, String name, boolean online, int classid, int level)
		{
			this._objId = objId;
			this._name = name;
			this._online = online;
			this._classid = classid;
			this._level = level;
		}
	}
}
