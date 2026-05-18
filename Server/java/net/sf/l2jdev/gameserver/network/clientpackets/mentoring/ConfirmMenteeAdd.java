package net.sf.l2jdev.gameserver.network.clientpackets.mentoring;

import java.sql.Connection;
import java.sql.PreparedStatement;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.managers.MentorManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerMenteeAdd;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.mentoring.ExMentorList;

public class ConfirmMenteeAdd extends ClientPacket
{
	private int _confirmed;
	private String _mentor;

	@Override
	protected void readImpl()
	{
		this._confirmed = this.readInt();
		this._mentor = this.readString();
	}

	@Override
	protected void runImpl()
	{
		Player mentee = this.getPlayer();
		if (mentee != null)
		{
			Player mentor = World.getInstance().getPlayer(this._mentor);
			if (mentor != null)
			{
				if (this._confirmed == 0)
				{
					mentee.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_DECLINED_S1_S_MENTORING_OFFER).addString(mentor.getName()));
					mentor.sendPacket(new SystemMessage(SystemMessageId.S1_HAS_DECLINED_BECOMING_YOUR_MENTEE).addString(mentee.getName()));
				}
				else if (validate(mentor, mentee))
				{
					try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO character_mentees (charId, mentorId) VALUES (?, ?)");)
					{
						statement.setInt(1, mentee.getObjectId());
						statement.setInt(2, mentor.getObjectId());
						statement.execute();
						MentorManager.getInstance().addMentor(mentor.getObjectId(), mentee.getObjectId());
						if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_MENTEE_ADD, mentor))
						{
							EventDispatcher.getInstance().notifyEventAsync(new OnPlayerMenteeAdd(mentor, mentee), mentor);
						}

						mentor.sendPacket(new SystemMessage(SystemMessageId.FROM_NOW_ON_S1_WILL_BE_YOUR_MENTEE).addString(mentee.getName()));
						mentor.sendPacket(new ExMentorList(mentor));
						mentee.sendPacket(new SystemMessage(SystemMessageId.FROM_NOW_ON_S1_WILL_BE_YOUR_MENTOR).addString(mentor.getName()));
						mentee.sendPacket(new ExMentorList(mentee));
					}
					catch (Exception var11)
					{
						PacketLogger.warning(this.getClass().getSimpleName() + ": " + var11.getMessage());
					}
				}
			}
		}
	}

	public static boolean validate(Player mentor, Player mentee)
	{
		return false;
	}
}
