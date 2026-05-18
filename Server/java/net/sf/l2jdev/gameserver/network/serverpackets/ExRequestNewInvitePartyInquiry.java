package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.RankManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.ChatType;

public class ExRequestNewInvitePartyInquiry extends ServerPacket
{
	private final int _reqType;
	private final ChatType _sayType;
	private final int _charRankGrade;
	private final int _pledgeCastleDBID;
	private final int _userID;
	private final Player _player;

	public ExRequestNewInvitePartyInquiry(Player player, int reqType, ChatType sayType)
	{
		this._player = player;
		this._userID = this._player.getObjectId();
		this._reqType = reqType;
		this._sayType = sayType;
		int rank = RankManager.getInstance().getPlayerGlobalRank(player);
		this._charRankGrade = rank == 1 ? 1 : (rank <= 30 ? 2 : (rank <= 100 ? 3 : 0));
		int castle = 0;
		Clan clan = this._player.getClan();
		if (clan != null)
		{
			castle = clan.getCastleId();
		}

		this._pledgeCastleDBID = castle;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_REQUEST_INVITE_PARTY.writeId(this, buffer);
		buffer.writeSizedString(this._player.getName());
		buffer.writeByte(this._reqType);
		buffer.writeByte(this._sayType.ordinal());
		buffer.writeByte(this._charRankGrade);
		buffer.writeByte(this._pledgeCastleDBID);
		buffer.writeByte(this._player.isInTimedHuntingZone() || this._player.isInSiege() || this._player.isRegisteredOnEvent());
		buffer.writeInt(0);
		buffer.writeInt(this._userID);
	}
}
