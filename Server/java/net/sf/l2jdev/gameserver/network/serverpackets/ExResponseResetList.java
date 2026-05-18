package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.appearance.PlayerAppearance;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExResponseResetList extends ServerPacket
{
	private final Player _player;

	public ExResponseResetList(Player player)
	{
		this._player = player;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RESPONSE_RESET_LIST.writeId(this, buffer);
		buffer.writeLong(this._player.getAdena());
		buffer.writeLong(this._player.getBeautyTickets());
		PlayerAppearance appearance = this._player.getAppearance();
		buffer.writeInt(appearance.getHairStyle());
		buffer.writeInt(appearance.getHairColor());
		buffer.writeInt(appearance.getFace());
	}
}
