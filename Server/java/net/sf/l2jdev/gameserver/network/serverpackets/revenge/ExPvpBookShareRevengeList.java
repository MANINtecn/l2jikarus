package net.sf.l2jdev.gameserver.network.serverpackets.revenge;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.RevengeHistoryManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.holders.RevengeHistoryHolder;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPvpBookShareRevengeList extends ServerPacket
{
	private final Collection<RevengeHistoryHolder> _history;

	public ExPvpBookShareRevengeList(Player player)
	{
		this._history = RevengeHistoryManager.getInstance().getHistory(player);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PVPBOOK_SHARE_REVENGE_LIST.writeId(this, buffer);
		if (this._history == null)
		{
			buffer.writeByte(1);
			buffer.writeByte(1);
			buffer.writeInt(0);
		}
		else
		{
			buffer.writeByte(1);
			buffer.writeByte(1);
			buffer.writeInt(this._history.size());

			for (RevengeHistoryHolder holder : this._history)
			{
				buffer.writeInt(holder.getType().ordinal());
				buffer.writeInt((int) (holder.getKillTime() / 1000L));
				buffer.writeInt(holder.getShowLocationRemaining());
				buffer.writeInt(holder.getTeleportRemaining());
				buffer.writeInt(holder.getSharedTeleportRemaining());
				buffer.writeInt(0);
				buffer.writeSizedString(holder.getVictimName());
				buffer.writeSizedString(holder.getVictimClanName());
				buffer.writeInt(holder.getVictimLevel());
				buffer.writeInt(holder.getVictimRaceId());
				buffer.writeInt(holder.getVictimClassId());
				buffer.writeInt(0);
				buffer.writeSizedString(holder.getKillerName());
				buffer.writeSizedString(holder.getKillerClanName());
				buffer.writeInt(holder.getKillerLevel());
				buffer.writeInt(holder.getKillerRaceId());
				buffer.writeInt(holder.getKillerClassId());
				Player killer = World.getInstance().getPlayer(holder.getKillerName());
				buffer.writeInt(killer != null && killer.isOnline() ? 2 : 0);
				buffer.writeInt(0);
				buffer.writeInt((int) (holder.getShareTime() / 1000L));
			}
		}
	}
}
