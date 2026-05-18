package net.sf.l2jdev.gameserver.network.clientpackets.mentoring;

import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.mentoring.ExMentorAdd;

public class RequestMenteeAdd extends ClientPacket
{
	private String _target;

	@Override
	protected void readImpl()
	{
		this._target = this.readString();
	}

	@Override
	protected void runImpl()
	{
		Player mentor = this.getPlayer();
		if (mentor != null)
		{
			Player mentee = World.getInstance().getPlayer(this._target);
			if (mentee != null)
			{
				if (ConfirmMenteeAdd.validate(mentor, mentee))
				{
					mentor.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_OFFERED_TO_BECOME_S1_S_MENTOR).addString(mentee.getName()));
					mentee.sendPacket(new ExMentorAdd(mentor));
				}
			}
		}
	}
}
