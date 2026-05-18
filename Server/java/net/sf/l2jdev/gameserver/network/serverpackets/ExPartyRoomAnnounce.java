package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.ServerConfig;
import net.sf.l2jdev.gameserver.data.enums.CharacterStyleCategoryType;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingRoom;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExPartyRoomAnnounce extends ServerPacket
{
	private final MatchingRoom _room;

	public ExPartyRoomAnnounce(Player player)
	{
		this._room = player.getMatchingRoom();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PARTY_ROOM_ANNOUNCE.writeId(this, buffer);
		if (this._room != null)
		{
			Player leader = this._room.getLeader();
			int minLevel = this._room.getMinLevel();
			int maxLevel = this._room.getMaxLevel();
			int memberCount = this._room.getMembersCount();
			buffer.writeSizedString(leader.getName());
			buffer.writeSizedString(this._room.getTitle());
			buffer.writeInt(ServerConfig.SERVER_ID);
			buffer.writeInt(ServerConfig.SERVER_ID);
			buffer.writeInt(this._room.getId());
			buffer.writeInt(minLevel);
			buffer.writeInt(maxLevel);
			buffer.writeInt(memberCount);
			buffer.writeInt(leader.getPledgeClass());
			int castleId = 0;
			if (leader.getClan() != null)
			{
				castleId = leader.getClan().getCastleId();
			}

			buffer.writeInt(castleId);
			buffer.writeInt(0);
			buffer.writeInt(leader.getVariables().getInt("ACTIVE_CHARACTER_STYLE_" + CharacterStyleCategoryType.CHAT_BACKGROUND, 0));
		}
	}
}
