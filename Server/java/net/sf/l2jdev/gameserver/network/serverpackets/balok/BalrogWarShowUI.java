package net.sf.l2jdev.gameserver.network.serverpackets.balok;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.BattleWithBalokManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class BalrogWarShowUI extends ServerPacket
{
	private final Player _player;
	private final int _personalPoints;
	private final long _globalPoints;
	private final int _rank;
	private final int _active;
	private final int _reward;

	public BalrogWarShowUI(Player player)
	{
		this._player = player;
		this._personalPoints = BattleWithBalokManager.getInstance().getMonsterPoints(this._player);
		this._globalPoints = BattleWithBalokManager.getInstance().getGlobalPoints();
		this._rank = this._personalPoints < 1 ? 0 : BattleWithBalokManager.getInstance().getPlayerRank(this._player);
		this._active = this._player.getVariables().getInt("BALOK_AVAILABLE_REWARD", 0);
		this._reward = BattleWithBalokManager.getInstance().getReward();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BALROGWAR_SHOW_UI.writeId(this, buffer);
		buffer.writeInt(this._rank);
		buffer.writeInt(this._personalPoints);
		buffer.writeLong(this._globalPoints);
		buffer.writeInt(this._active);
		buffer.writeInt(this._reward);
		buffer.writeLong(1L);
	}
}
