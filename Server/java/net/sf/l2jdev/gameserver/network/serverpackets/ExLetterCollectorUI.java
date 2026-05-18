package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.managers.events.LetterCollectorManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExLetterCollectorUI extends ServerPacket
{
	private final int _minimumLevel;

	public ExLetterCollectorUI(Player player)
	{
		this._minimumLevel = player.getLevel() <= LetterCollectorManager.getInstance().getMaxLevel() ? LetterCollectorManager.getInstance().getMinLevel() : PlayerConfig.PLAYER_MAXIMUM_LEVEL;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_LETTER_COLLECTOR_UI_LAUNCHER.writeId(this, buffer);
		buffer.writeByte(1);
		buffer.writeInt(this._minimumLevel);
	}
}
