package net.sf.l2jdev.gameserver.network.serverpackets.enchant.challengepoint;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.ChallengePointInfoHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExEnchantChallengePointInfo extends ServerPacket
{
	private final ChallengePointInfoHolder[] _challengeinfo;

	public ExEnchantChallengePointInfo(Player player)
	{
		this._challengeinfo = player.getChallengeInfo().initializeChallengePoints();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_CHALLENGE_POINT_INFO.writeId(this, buffer);
		buffer.writeInt(this._challengeinfo.length);

		for (ChallengePointInfoHolder info : this._challengeinfo)
		{
			buffer.writeInt(info.getPointGroupId());
			buffer.writeInt(info.getChallengePoint());
			buffer.writeInt(info.getTicketPointOpt1());
			buffer.writeInt(info.getTicketPointOpt2());
			buffer.writeInt(info.getTicketPointOpt3());
			buffer.writeInt(info.getTicketPointOpt4());
			buffer.writeInt(info.getTicketPointOpt5());
			buffer.writeInt(info.getTicketPointOpt6());
		}
	}
}
