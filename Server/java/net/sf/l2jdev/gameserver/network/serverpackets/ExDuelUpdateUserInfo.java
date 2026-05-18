package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExDuelUpdateUserInfo extends ServerPacket
{
	private final Player _player;

	public ExDuelUpdateUserInfo(Player player)
	{
		this._player = player;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_DUEL_UPDATE_USER_INFO.writeId(this, buffer);
		buffer.writeString(this._player.getName());
		buffer.writeInt(this._player.getObjectId());
		buffer.writeInt(this._player.getPlayerClass().getId());
		buffer.writeInt(this._player.getLevel());
		buffer.writeInt((int) this._player.getCurrentHp());
		buffer.writeInt((int) this._player.getMaxHp());
		buffer.writeInt((int) this._player.getCurrentMp());
		buffer.writeInt(this._player.getMaxMp());
		buffer.writeInt((int) this._player.getCurrentCp());
		buffer.writeInt(this._player.getMaxCp());
	}
}
