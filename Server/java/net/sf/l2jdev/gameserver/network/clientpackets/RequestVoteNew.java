package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.xml.FakePlayerData;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExVoteSystemInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestVoteNew extends ClientPacket
{
	private int _targetId;

	@Override
	protected void readImpl()
	{
		this._targetId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			WorldObject object = player.getTarget();
			if (object instanceof Player)
			{
				Player target = object.asPlayer();
				if (target.getObjectId() == this._targetId)
				{
					if (target == player)
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_RECOMMEND_YOURSELF);
					}
					else if (player.getRecomLeft() <= 0)
					{
						player.sendPacket(SystemMessageId.YOU_HAVE_NO_RECOMMENDATIONS_AT_THE_MOMENT);
					}
					else if (target.getRecomHave() >= 255)
					{
						player.sendPacket(SystemMessageId.THE_TARGET_CANNOT_RECEIVE_ANY_MORE_RECOMMENDATIONS_FROM_YOU);
					}
					else
					{
						player.giveRecom(target);
						SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_RECEIVED_A_RECOMMENDATION_FROM_YOU_RECOMMENDATIONS_LEFT_S2);
						sm.addPcName(target);
						sm.addInt(player.getRecomLeft());
						player.sendPacket(sm);
						sm = new SystemMessage(SystemMessageId.C1_GIVES_YOU_A_RECOMMENDATION);
						sm.addPcName(player);
						target.sendPacket(sm);
						player.updateUserInfo();
						target.broadcastUserInfo();
						player.sendPacket(new ExVoteSystemInfo(player));
						target.sendPacket(new ExVoteSystemInfo(target));
					}
				}
			}
			else
			{
				if (object == null)
				{
					player.sendPacket(SystemMessageId.SELECT_YOUR_TARGET);
				}
				else if (object.isFakePlayer() && FakePlayerData.getInstance().isTalkable(object.getName()))
				{
					if (player.getRecomLeft() <= 0)
					{
						player.sendPacket(SystemMessageId.YOU_HAVE_NO_RECOMMENDATIONS_AT_THE_MOMENT);
						return;
					}

					SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_RECEIVED_A_RECOMMENDATION_FROM_YOU_RECOMMENDATIONS_LEFT_S2);
					sm.addString(FakePlayerData.getInstance().getProperName(object.getName()));
					sm.addInt(player.getRecomLeft());
					player.sendPacket(sm);
					player.setRecomLeft(player.getRecomLeft() - 1);
					player.updateUserInfo();
					player.sendPacket(new ExVoteSystemInfo(player));
				}
				else
				{
					player.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
				}
			}
		}
	}
}
