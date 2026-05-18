package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExVoteSystemInfo extends ServerPacket
{
	private final int _recomLeft;
	private final int _recomHave;
	private final int _bonusTime;
	private final int _bonusVal;
	private final int _bonusType;

	public ExVoteSystemInfo(Player player)
	{
		this._recomLeft = player.getRecomLeft();
		this._recomHave = player.getRecomHave();
		this._bonusTime = 0;
		this._bonusVal = 0;
		this._bonusType = 0;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_VOTE_SYSTEM_INFO.writeId(this, buffer);
		buffer.writeInt(this._recomLeft);
		buffer.writeInt(this._recomHave);
		buffer.writeInt(this._bonusTime);
		buffer.writeInt(this._bonusVal);
		buffer.writeInt(this._bonusType);
	}
}
