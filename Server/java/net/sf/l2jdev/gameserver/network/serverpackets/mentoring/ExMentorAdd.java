package net.sf.l2jdev.gameserver.network.serverpackets.mentoring;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExMentorAdd extends ServerPacket
{
	final Player _mentor;

	public ExMentorAdd(Player mentor)
	{
		this._mentor = mentor;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MENTOR_ADD.writeId(this, buffer);
		buffer.writeString(this._mentor.getName());
		buffer.writeInt(this._mentor.getActiveClass());
		buffer.writeInt(this._mentor.getLevel());
	}
}
