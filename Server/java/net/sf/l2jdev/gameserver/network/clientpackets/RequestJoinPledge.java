package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.data.xml.FakePlayerData;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.AskJoinPledge;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestJoinPledge extends ClientPacket
{
	private int _target;
	private int _pledgeType;

	@Override
	protected void readImpl()
	{
		this._target = this.readInt();
		this._pledgeType = this.readInt();
	}

	protected void scheduleDeny(Player player, String name)
	{
		if (player != null)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_DID_NOT_RESPOND_INVITATION_TO_THE_CLAN_HAS_BEEN_CANCELLED);
			sm.addString(name);
			player.sendPacket(sm);
			player.onTransactionResponse();
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Clan clan = player.getClan();
			if (clan != null)
			{
				if (player.getTarget() != null && FakePlayerData.getInstance().isTalkable(player.getTarget().getName()))
				{
					if (player.getTarget().asNpc().getTemplate().getFakePlayerInfo().getClanId() > 0)
					{
						player.sendPacket(SystemMessageId.THIS_CHARACTER_IS_A_MEMBER_OF_ANOTHER_CLAN);
					}
					else if (!player.isProcessingRequest())
					{
						ThreadPool.schedule(() -> this.scheduleDeny(player, player.getTarget().getName()), 10000L);
						player.blockRequest();
					}
					else
					{
						SystemMessage msg = new SystemMessage(SystemMessageId.C1_IS_ON_ANOTHER_TASK_PLEASE_TRY_AGAIN_LATER);
						msg.addString(player.getTarget().getName());
						player.sendPacket(msg);
					}
				}
				else
				{
					Player target = World.getInstance().getPlayer(this._target);
					if (target == null)
					{
						player.sendPacket(SystemMessageId.THE_TARGET_CANNOT_BE_INVITED);
					}
					else if (clan.checkClanJoinCondition(player, target, this._pledgeType))
					{
						if (player.getRequest().setRequest(target, this))
						{
							String pledgeName = player.getClan().getName();
							target.sendPacket(new AskJoinPledge(player, pledgeName));
						}
					}
				}
			}
		}
	}

	public int getPledgeType()
	{
		return this._pledgeType;
	}
}
