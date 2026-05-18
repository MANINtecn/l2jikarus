package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.taskmanagers.GameTimeTaskManager;

public class CharSelected extends ServerPacket
{
	private final Player _player;
	private final int _sessionId;

	public CharSelected(Player player, int sessionId)
	{
		this._player = player;
		this._sessionId = sessionId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.CHARACTER_SELECTED.writeId(this, buffer);
		buffer.writeString(this._player.getName());
		buffer.writeInt(this._player.getObjectId());
		buffer.writeString(this._player.getTitle());
		buffer.writeInt(this._sessionId);
		buffer.writeInt(this._player.getClanId());
		buffer.writeInt(0);
		buffer.writeInt(this._player.getAppearance().isFemale());
		buffer.writeInt(this._player.getRace().ordinal());
		if (this._player.isSamurai())
		{
			buffer.writeInt(260);
		}
		else
		{
			buffer.writeInt(this._player.getPlayerClass().getId());
		}

		buffer.writeInt(1);
		buffer.writeInt(this._player.getX());
		buffer.writeInt(this._player.getY());
		buffer.writeInt(this._player.getZ());
		buffer.writeDouble(this._player.getCurrentHp());
		buffer.writeDouble(this._player.getCurrentMp());
		buffer.writeLong(this._player.getSp());
		buffer.writeLong(this._player.getExp());
		buffer.writeInt(this._player.getLevel());
		buffer.writeInt(this._player.getReputation());
		buffer.writeInt(this._player.getPkKills());
		buffer.writeInt(GameTimeTaskManager.getInstance().getGameTime() % 1440);
		buffer.writeInt(0);
		buffer.writeInt(this._player.getPlayerClass().getId());
		buffer.writeBytes();
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeBytes();
		buffer.writeInt(0);
	}
}
