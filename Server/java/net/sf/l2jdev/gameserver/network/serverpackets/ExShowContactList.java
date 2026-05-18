package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Set;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExShowContactList extends ServerPacket
{
	private final Set<String> _contacts;

	public ExShowContactList(Player player)
	{
		this._contacts = player.getContactList().getAllContacts();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_POST_FRIEND.writeId(this, buffer);
		buffer.writeInt(this._contacts.size());
		this._contacts.forEach(buffer::writeString);
	}
}
