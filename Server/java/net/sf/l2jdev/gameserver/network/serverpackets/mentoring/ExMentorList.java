package net.sf.l2jdev.gameserver.network.serverpackets.mentoring;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.MentorManager;
import net.sf.l2jdev.gameserver.model.Mentee;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExMentorList extends ServerPacket
{
	private final int _type;
	private final Collection<Mentee> _mentees;

	public ExMentorList(Player player)
	{
		if (player.isMentor())
		{
			this._type = 1;
			this._mentees = MentorManager.getInstance().getMentees(player.getObjectId());
		}
		else if (player.isMentee())
		{
			this._type = 2;
			this._mentees = Arrays.asList(MentorManager.getInstance().getMentor(player.getObjectId()));
		}
		else
		{
			this._mentees = Collections.emptyList();
			this._type = 0;
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MENTOR_LIST.writeId(this, buffer);
		buffer.writeInt(this._type);
		buffer.writeInt(0);
		buffer.writeInt(this._mentees.size());

		for (Mentee mentee : this._mentees)
		{
			buffer.writeInt(mentee.getObjectId());
			buffer.writeString(mentee.getName());
			buffer.writeInt(mentee.getClassId());
			buffer.writeInt(mentee.getLevel());
			buffer.writeInt(mentee.isOnlineInt());
		}
	}
}
