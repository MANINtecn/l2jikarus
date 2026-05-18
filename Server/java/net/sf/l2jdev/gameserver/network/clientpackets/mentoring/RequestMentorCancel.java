package net.sf.l2jdev.gameserver.network.clientpackets.mentoring;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.managers.MentorManager;
import net.sf.l2jdev.gameserver.model.Mentee;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerMenteeLeft;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerMenteeRemove;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestMentorCancel extends ClientPacket
{
	private int _confirmed;
	private String _name;

	@Override
	protected void readImpl()
	{
		this._confirmed = this.readInt();
		this._name = this.readString();
	}

	@Override
	protected void runImpl()
	{
		if (this._confirmed == 1)
		{
			Player player = this.getPlayer();
			int objectId = CharInfoTable.getInstance().getIdByName(this._name);
			if (player != null)
			{
				if (player.isMentor())
				{
					Mentee mentee = MentorManager.getInstance().getMentee(player.getObjectId(), objectId);
					if (mentee != null)
					{
						MentorManager.getInstance().cancelAllMentoringBuffs(mentee.getPlayer());
						if (MentorManager.getInstance().isAllMenteesOffline(player.getObjectId(), mentee.getObjectId()))
						{
							MentorManager.getInstance().cancelAllMentoringBuffs(player);
						}

						player.sendPacket(new SystemMessage(SystemMessageId.S1_S_MENTORING_CONTRACT_IS_CANCELLED_THE_MENTOR_CANNOT_BOND_WITH_ANOTHER_MENTEE_FOR_2_DAYS).addString(this._name));
						MentorManager.getInstance().setPenalty(player.getObjectId(), PlayerConfig.MENTOR_PENALTY_FOR_MENTEE_LEAVE);
						MentorManager.getInstance().deleteMentor(player.getObjectId(), mentee.getObjectId());
						if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_MENTEE_REMOVE, player))
						{
							EventDispatcher.getInstance().notifyEventAsync(new OnPlayerMenteeRemove(player, mentee), player);
						}
					}
				}
				else if (player.isMentee())
				{
					Mentee mentor = MentorManager.getInstance().getMentor(player.getObjectId());
					if (mentor != null && mentor.getObjectId() == objectId)
					{
						MentorManager.getInstance().cancelAllMentoringBuffs(player);
						if (MentorManager.getInstance().isAllMenteesOffline(mentor.getObjectId(), player.getObjectId()))
						{
							MentorManager.getInstance().cancelAllMentoringBuffs(mentor.getPlayer());
						}

						MentorManager.getInstance().setPenalty(mentor.getObjectId(), PlayerConfig.MENTOR_PENALTY_FOR_MENTEE_LEAVE);
						MentorManager.getInstance().deleteMentor(mentor.getObjectId(), player.getObjectId());
						if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_MENTEE_LEFT, player))
						{
							EventDispatcher.getInstance().notifyEventAsync(new OnPlayerMenteeLeft(mentor, player), player);
						}

						mentor.getPlayer().sendPacket(new SystemMessage(SystemMessageId.S1_S_MENTORING_CONTRACT_IS_CANCELLED_THE_MENTOR_CANNOT_BOND_WITH_ANOTHER_MENTEE_FOR_2_DAYS).addString(this._name));
					}
				}
			}
		}
	}
}
