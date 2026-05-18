package net.sf.l2jdev.gameserver.network.serverpackets.castlewar;

import java.util.Map;
import java.util.Map.Entry;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.siege.MercenaryPledgeHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class MercenaryPledgeMemberList extends ServerPacket
{
	private final int _castleId;
	private final int _clanId;
	private final Map<Integer, MercenaryPledgeHolder> _mercenaries;

	public MercenaryPledgeMemberList(int castleId, int clanId)
	{
		this._castleId = castleId;
		this._clanId = clanId;
		this._mercenaries = ClanTable.getInstance().getClan(this._clanId).getMapMercenary();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_MERCENARY_MEMBER_LIST.writeId(this, buffer);
		buffer.writeInt(this._castleId);
		buffer.writeInt(this._clanId);
		buffer.writeInt(this._mercenaries.size());

		for (Entry<Integer, MercenaryPledgeHolder> entry : this._mercenaries.entrySet())
		{
			Player player = World.getInstance().getPlayer(entry.getKey());
			MercenaryPledgeHolder mercenary = entry.getValue();
			buffer.writeInt(entry.getKey() == mercenary.getPlayerId());
			if (player == null)
			{
				buffer.writeInt(0);
			}
			else
			{
				buffer.writeInt(player.isOnline());
			}

			buffer.writeSizedString(mercenary.getName());
			buffer.writeInt(mercenary.getClassId());
		}
	}
}
