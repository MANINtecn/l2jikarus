package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class AnswerTradeRequest extends ClientPacket
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
			if (!player.getAccessLevel().allowTransaction())
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				Player partner = player.getActiveRequester();
				if (partner == null)
				{
					player.sendPacket(new net.sf.l2jdev.gameserver.network.serverpackets.TradeDone(0));
					player.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE);
					player.setActiveRequester(null);
				}
				else if (World.getInstance().getPlayer(partner.getObjectId()) == null)
				{
					player.sendPacket(new net.sf.l2jdev.gameserver.network.serverpackets.TradeDone(0));
					player.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE);
					player.setActiveRequester(null);
				}
				else
				{
					if (this._response == 1 && !partner.isRequestExpired())
					{
						player.startTrade(partner);
					}
					else
					{
						SystemMessage msg = new SystemMessage(SystemMessageId.C1_HAS_DENIED_YOUR_REQUEST_TO_TRADE);
						msg.addString(player.getName());
						partner.sendPacket(msg);
					}

					player.setActiveRequester(null);
					partner.onTransactionResponse();
				}
			}
		}
	}
}
