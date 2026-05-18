package net.sf.l2jdev.gameserver.network.serverpackets.olympiad;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadInfo;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExOlympiadMatchResult extends ServerPacket
{
	private final boolean _tie;
	private int _winTeam;
	private int _loseTeam = 2;
	private final List<OlympiadInfo> _winnerList;
	private final List<OlympiadInfo> _loserList;
	private final int _round1winner;
	private final int _round2winner;
	private final int _round3winner;
	private final int _currentPoints;
	private final int _diffPoints;

	public ExOlympiadMatchResult(boolean tie, int winTeam, List<OlympiadInfo> winnerList, List<OlympiadInfo> loserList, int round1winner, int round2winner, int round3winner, int currentPoints, int diffPoints)
	{
		this._tie = tie;
		this._winTeam = winTeam;
		this._winnerList = winnerList;
		this._loserList = loserList;
		if (this._winTeam == 2)
		{
			this._loseTeam = 1;
		}
		else if (this._winTeam == 0)
		{
			this._winTeam = 1;
		}

		this._round1winner = round1winner;
		this._round2winner = round2winner;
		this._round3winner = round3winner;
		this._currentPoints = currentPoints;
		this._diffPoints = diffPoints;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_GFX_OLYMPIAD.writeId(this, buffer);
		buffer.writeInt(1);
		buffer.writeInt(this._tie);
		buffer.writeString(this._winnerList.get(0).getName());
		buffer.writeInt(this._winTeam);
		buffer.writeInt(this._winnerList.size());

		for (OlympiadInfo info : this._winnerList)
		{
			buffer.writeString(info.getName());
			buffer.writeString(info.getClanName());
			buffer.writeInt(info.getClanId());
			buffer.writeInt(info.getClassId());
			buffer.writeLong(info.getDamage());
			buffer.writeInt(this._currentPoints);
			buffer.writeInt(this._diffPoints);
			buffer.writeInt(0);
		}

		buffer.writeInt(this._loseTeam);
		buffer.writeInt(this._loserList.size());

		for (OlympiadInfo info : this._loserList)
		{
			buffer.writeString(info.getName());
			buffer.writeString(info.getClanName());
			buffer.writeInt(info.getClanId());
			buffer.writeInt(info.getClassId());
			buffer.writeLong(info.getDamage());
			buffer.writeInt(this._currentPoints);
			buffer.writeInt(this._diffPoints);
			buffer.writeInt(0);
		}

		buffer.writeByte(this._round1winner);
		buffer.writeByte(this._round2winner);
		buffer.writeByte(this._round3winner);
		buffer.writeInt(15);
		buffer.writeInt(0);
	}
}
