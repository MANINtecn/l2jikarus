package net.sf.l2jdev.gameserver.network.serverpackets.friend;

import java.util.LinkedList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class L2FriendList extends ServerPacket
{
	private final List<L2FriendList.FriendInfo> _info = new LinkedList<>();

	public L2FriendList(Player player)
	{
		for (int objId : player.getFriendList())
		{
			String name = CharInfoTable.getInstance().getNameById(objId);
			Player player1 = World.getInstance().getPlayer(objId);
			boolean online = false;
			int level = 0;
			int classId = 0;
			if (player1 != null)
			{
				online = true;
				level = player1.getLevel();
				classId = player1.getPlayerClass().getId();
			}
			else
			{
				level = CharInfoTable.getInstance().getLevelById(objId);
				classId = CharInfoTable.getInstance().getClassIdById(objId);
			}

			this._info.add(new L2FriendList.FriendInfo(objId, name, online, level, classId));
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.L2_FRIEND_LIST.writeId(this, buffer);
		buffer.writeInt(this._info.size());

		for (L2FriendList.FriendInfo info : this._info)
		{
			buffer.writeInt(info._objId);
			buffer.writeString(info._name);
			buffer.writeInt(info._online);
			buffer.writeInt(info._online ? info._objId : 0);
			buffer.writeInt(info._level);
			buffer.writeInt(info._classId);
			buffer.writeShort(0);
		}
	}

	private static class FriendInfo
	{
		int _objId;
		String _name;
		int _level;
		int _classId;
		boolean _online;

		public FriendInfo(int objId, String name, boolean online, int level, int classId)
		{
			this._objId = objId;
			this._name = name;
			this._online = online;
			this._level = level;
			this._classId = classId;
		}
	}
}
