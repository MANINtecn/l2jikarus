package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class StopPledgeWar extends ServerPacket
{
	private final String _pledgeName;
	private final String _playerName;

	public StopPledgeWar(String pledge, String charName)
	{
		this._pledgeName = pledge;
		this._playerName = charName;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.STOP_PLEDGE_WAR.writeId(this, buffer);
		buffer.writeString(this._pledgeName);
		buffer.writeString(this._playerName);
	}
}
