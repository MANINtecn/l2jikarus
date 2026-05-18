package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExShowBeautyMenu extends ServerPacket
{
	public static final int MODIFY_APPEARANCE = 0;
	public static final int RESTORE_APPEARANCE = 1;
	private final Player _player;
	private final int _type;

	public ExShowBeautyMenu(Player player, int type)
	{
		this._player = player;
		this._type = type;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_BEAUTY_MENU.writeId(this, buffer);
		buffer.writeInt(this._type);
		buffer.writeInt(this._player.getVisualHair());
		buffer.writeInt(this._player.getVisualHairColor());
		buffer.writeInt(this._player.getVisualFace());
	}
}
