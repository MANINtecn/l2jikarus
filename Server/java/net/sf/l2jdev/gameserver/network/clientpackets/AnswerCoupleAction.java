package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExRotation;
import net.sf.l2jdev.gameserver.network.serverpackets.SocialAction;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public class AnswerCoupleAction extends ClientPacket
{
	private int _objectId;
	private int _actionId;
	private int _answer;

	@Override
	protected void readImpl()
	{
		this._actionId = this.readInt();
		this._answer = this.readInt();
		this._objectId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		Player target = World.getInstance().getPlayer(this._objectId);
		if (player != null && target != null)
		{
			if (target.getMultiSocialTarget() == player.getObjectId() && target.getMultiSociaAction() == this._actionId)
			{
				if (this._answer == 0)
				{
					target.sendPacket(SystemMessageId.THE_COUPLE_ACTION_REQUEST_HAS_BEEN_DENIED);
				}
				else if (this._answer == 1)
				{
					int distance = (int) player.calculateDistance2D(target);
					if (distance > 125 || distance < 15 || player.getObjectId() == target.getObjectId())
					{
						player.sendPacket(SystemMessageId.THE_REQUEST_CANNOT_BE_COMPLETED_BECAUSE_THE_TARGET_DOES_NOT_MEET_LOCATION_REQUIREMENTS);
						target.sendPacket(SystemMessageId.THE_REQUEST_CANNOT_BE_COMPLETED_BECAUSE_THE_TARGET_DOES_NOT_MEET_LOCATION_REQUIREMENTS);
						return;
					}

					int heading = LocationUtil.calculateHeadingFrom(player, target);
					player.broadcastPacket(new ExRotation(player.getObjectId(), heading));
					player.setHeading(heading);
					heading = LocationUtil.calculateHeadingFrom(target, player);
					target.setHeading(heading);
					target.broadcastPacket(new ExRotation(target.getObjectId(), heading));
					player.broadcastPacket(new SocialAction(player.getObjectId(), this._actionId));
					target.broadcastPacket(new SocialAction(this._objectId, this._actionId));
				}
				else if (this._answer == -1)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_SET_TO_REFUSE_COUPLE_ACTIONS_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
					sm.addPcName(player);
					target.sendPacket(sm);
				}

				target.setMultiSocialAction(0, 0);
			}
		}
	}
}
