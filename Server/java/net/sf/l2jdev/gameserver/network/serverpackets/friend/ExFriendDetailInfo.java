package net.sf.l2jdev.gameserver.network.serverpackets.friend;

import java.util.Calendar;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExFriendDetailInfo extends ServerPacket
{
	private final int _objectId;
	private final Player _friend;
	private final String _name;
	private final int _lastAccess;
	private final boolean _isOnline;
	private final int _friendObjectId;
	private final int _level;
	private final int _classId;
	private final int _clanId;
	private final int _clanCrestId;
	private final String _clanName;
	private final int _allyId;
	private final int _allyCrestId;
	private final String _allyName;
	private final Calendar _createDate;
	private final int _lastAccessDelay;
	private final String _friendMemo;

	public ExFriendDetailInfo(Player player, String name)
	{
		this._objectId = player.getObjectId();
		this._name = name;
		this._friend = World.getInstance().getPlayer(this._name);
		this._lastAccess = this._friend == null || this._friend.isBlocked(player) ? 0 : (this._friend.isOnline() ? (int) System.currentTimeMillis() : (int) (System.currentTimeMillis() - this._friend.getLastAccess()) / 1000);
		CharInfoTable charInfoTable = CharInfoTable.getInstance();
		if (this._friend == null)
		{
			int charId = charInfoTable.getIdByName(this._name);
			this._isOnline = false;
			this._friendObjectId = charId;
			this._level = charInfoTable.getLevelById(charId);
			this._classId = charInfoTable.getClassIdById(charId);
			Clan clan = ClanTable.getInstance().getClan(charInfoTable.getClanIdById(charId));
			if (clan != null)
			{
				this._clanId = clan.getId();
				this._clanCrestId = clan.getCrestId();
				this._clanName = clan.getName();
				this._allyId = clan.getAllyId();
				this._allyCrestId = clan.getAllyCrestId();
				this._allyName = clan.getAllyName();
			}
			else
			{
				this._clanId = 0;
				this._clanCrestId = 0;
				this._clanName = "";
				this._allyId = 0;
				this._allyCrestId = 0;
				this._allyName = "";
			}

			this._createDate = charInfoTable.getCharacterCreationDate(charId);
			this._lastAccessDelay = charInfoTable.getLastAccessDelay(charId);
			this._friendMemo = charInfoTable.getFriendMemo(this._objectId, charId);
		}
		else
		{
			this._isOnline = this._friend.isOnlineInt() == 1;
			this._friendObjectId = this._friend.getObjectId();
			this._level = this._friend.getLevel();
			this._classId = this._friend.getPlayerClass().getId();
			this._clanId = this._friend.getClanId();
			this._clanCrestId = this._friend.getClanCrestId();
			Clan clan = this._friend.getClan();
			this._clanName = clan != null ? clan.getName() : "";
			this._allyId = this._friend.getAllyId();
			this._allyCrestId = this._friend.getAllyCrestId();
			this._allyName = clan != null ? clan.getAllyName() : "";
			this._createDate = this._friend.getCreateDate();
			this._lastAccessDelay = this._lastAccess;
			this._friendMemo = charInfoTable.getFriendMemo(this._objectId, this._friend.getObjectId());
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_FRIEND_DETAIL_INFO.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeString(this._name);
		buffer.writeInt(this._isOnline);
		buffer.writeInt(this._friendObjectId);
		buffer.writeShort(this._level);
		buffer.writeShort(this._classId);
		buffer.writeInt(this._clanId);
		buffer.writeInt(this._clanCrestId);
		buffer.writeString(this._clanName);
		buffer.writeInt(this._allyId);
		buffer.writeInt(this._allyCrestId);
		buffer.writeString(this._allyName);
		buffer.writeByte(this._createDate.get(2) + 1);
		buffer.writeByte(this._createDate.get(5));
		buffer.writeInt(this._lastAccessDelay);
		buffer.writeString(this._friendMemo);
	}
}
