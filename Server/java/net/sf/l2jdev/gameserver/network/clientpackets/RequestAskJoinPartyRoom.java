package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExAskJoinPartyRoom;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestAskJoinPartyRoom extends ClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		this._name = this.readString();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Player target = World.getInstance().getPlayer(this._name);
			if (target != null)
			{
				if (!target.isProcessingRequest())
				{
					player.onTransactionRequest(target);
					target.sendPacket(new ExAskJoinPartyRoom(player));
				}
				else
				{
					player.sendPacket(new SystemMessage(SystemMessageId.C1_IS_ON_ANOTHER_TASK_PLEASE_TRY_AGAIN_LATER).addPcName(target));
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE);
			}
		}
	}
}
