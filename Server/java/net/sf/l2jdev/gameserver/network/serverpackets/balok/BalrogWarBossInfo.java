package net.sf.l2jdev.gameserver.network.serverpackets.balok;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.BattleWithBalokManager;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class BalrogWarBossInfo extends ServerPacket
{
	private final int _bossState1;
	private final int _bossState2;
	private final int _bossState3;
	private final int _bossState4;
	private final int _bossState5;
	private final int _finalBossId;
	private final int _finalState;
	private final long _globalpoints;
	private final int _globalstage;

	public BalrogWarBossInfo(int balokid, int balokstatus, int boss1, int boss2, int boss3, int boss4, int boss5)
	{
		this._finalBossId = balokid + 1000000;
		this._finalState = balokstatus;
		this._bossState1 = boss1;
		this._bossState2 = boss2;
		this._bossState3 = boss3;
		this._bossState4 = boss4;
		this._bossState5 = boss5;
		this._globalpoints = BattleWithBalokManager.getInstance().getGlobalPoints();
		this._globalstage = BattleWithBalokManager.getInstance().getGlobalStage();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BALROGWAR_BOSSINFO.writeId(this, buffer);
		if (this._globalpoints < 320000L && this._globalstage <= 2)
		{
			buffer.writeInt(1);
			buffer.writeInt(1);
			buffer.writeInt(1);
			buffer.writeInt(1);
			buffer.writeInt(1);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
		}
		else
		{
 
			int bossId4 = 0;
			int bossId5 = 0;
			if (this._globalpoints >= 800000L && this._globalstage >= 3)
			{
				bossId4 = 1025959;
				bossId5 = 1025960;
			}

			buffer.writeInt(1025956);
			buffer.writeInt(1025957);
			buffer.writeInt(1025958);
			buffer.writeInt(bossId4);
			buffer.writeInt(bossId5);
			buffer.writeInt(this._bossState1);
			buffer.writeInt(this._bossState2);
			buffer.writeInt(this._bossState3);
			buffer.writeInt(this._bossState4);
			buffer.writeInt(this._bossState5);
			buffer.writeInt(this._finalBossId);
			buffer.writeInt(this._finalState);
		}
	}
}
