package net.sf.l2jdev.gameserver.network.serverpackets.crossevent;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.events.CrossEventManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCrossEventNoti extends ServerPacket
{
	private final int _coupons;
	private final int _advancedRewards;

	public ExCrossEventNoti(Player player)
	{
		this._coupons = (int) player.getInventory().getItemByItemId(CrossEventManager.getInstance().getTicketId()).getCount();
		this._advancedRewards = player.getCrossRewardsCount();
	}

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CROSS_EVENT_NOTI.writeId(this, buffer);
		buffer.writeInt(this._coupons);
		buffer.writeInt(this._advancedRewards);
	}
}
