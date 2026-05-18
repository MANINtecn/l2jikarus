package net.sf.l2jdev.gameserver.network.serverpackets.balok;

import java.util.concurrent.TimeUnit;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.GlobalVariablesManager;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class BalrogWarHud extends ServerPacket
{
	private final int _state;
	private final int _stage;
	private final int _time;

	public BalrogWarHud(int state, int stage)
	{
		this._state = state;
		this._stage = stage;
		long remainTime = GlobalVariablesManager.getInstance().getLong("BALOK_REMAIN_TIME", 0L);
		long currentTime = System.currentTimeMillis();
		this._time = (int) TimeUnit.MILLISECONDS.toSeconds(remainTime - currentTime);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BALROGWAR_HUD.writeId(this, buffer);
		buffer.writeInt(this._state);
		buffer.writeInt(this._stage);
		buffer.writeInt(this._time);
	}
}
