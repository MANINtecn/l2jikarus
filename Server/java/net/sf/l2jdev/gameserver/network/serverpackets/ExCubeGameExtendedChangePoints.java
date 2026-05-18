package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExCubeGameExtendedChangePoints extends ServerPacket
{
	private final int _timeLeft;
	private final int _bluePoints;
	private final int _redPoints;
	private final boolean _isRedTeam;
	private final Player _player;
	private final int _playerPoints;

	public ExCubeGameExtendedChangePoints(int timeLeft, int bluePoints, int redPoints, boolean isRedTeam, Player player, int playerPoints)
	{
		this._timeLeft = timeLeft;
		this._bluePoints = bluePoints;
		this._redPoints = redPoints;
		this._isRedTeam = isRedTeam;
		this._player = player;
		this._playerPoints = playerPoints;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BLOCK_UPSET_STATE.writeId(this, buffer);
		buffer.writeInt(0);
		buffer.writeInt(this._timeLeft);
		buffer.writeInt(this._bluePoints);
		buffer.writeInt(this._redPoints);
		buffer.writeInt(this._isRedTeam);
		buffer.writeInt(this._player.getObjectId());
		buffer.writeInt(this._playerPoints);
	}
}
