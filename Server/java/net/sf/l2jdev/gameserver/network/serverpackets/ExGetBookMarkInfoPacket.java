package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.TeleportBookmark;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExGetBookMarkInfoPacket extends ServerPacket
{
	private final Player _player;

	public ExGetBookMarkInfoPacket(Player player)
	{
		this._player = player;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_USER_BOOKMARK.writeId(this, buffer);
		buffer.writeInt(0);
		buffer.writeInt(this._player.getBookMarkSlot());
		buffer.writeInt(this._player.getTeleportBookmarks().size());

		for (TeleportBookmark tpbm : this._player.getTeleportBookmarks())
		{
			buffer.writeInt(tpbm.getId());
			buffer.writeInt(tpbm.getX());
			buffer.writeInt(tpbm.getY());
			buffer.writeInt(tpbm.getZ());
			buffer.writeString(tpbm.getName());
			buffer.writeInt(tpbm.getIcon());
			buffer.writeString(tpbm.getTag());
		}
	}
}
