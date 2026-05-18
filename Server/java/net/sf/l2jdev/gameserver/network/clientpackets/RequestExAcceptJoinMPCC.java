package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.CommandChannel;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestExAcceptJoinMPCC extends ClientPacket
{
	private int _response;

	@Override
	protected void readImpl()
	{
		this._response = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Player requestor = player.getActiveRequester();
			if (requestor == null)
			{
				return;
			}

			if (this._response == 1)
			{
				boolean newCc = false;
				if (!requestor.getParty().isInCommandChannel())
				{
					new CommandChannel(requestor);
					SystemMessage sm = new SystemMessage(SystemMessageId.THE_COMMAND_CHANNEL_HAS_BEEN_FORMED);
					requestor.sendPacket(sm);
					newCc = true;
				}

				requestor.getParty().getCommandChannel().addParty(player.getParty());
				if (!newCc)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_JOINED_THE_COMMAND_CHANNEL);
					player.sendPacket(sm);
				}
			}
			else
			{
				requestor.sendMessage("The player declined to join your Command Channel.");
			}

			player.setActiveRequester(null);
			requestor.onTransactionResponse();
		}
	}
}
