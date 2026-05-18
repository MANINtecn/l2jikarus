package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.PrivateStoreMsgBuy;

public class SetPrivateStoreMsgBuy extends ClientPacket
{
 
	private String _storeMsg;

	@Override
	protected void readImpl()
	{
		this._storeMsg = this.readString();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null && player.getBuyList() != null)
		{
			if (this._storeMsg != null && this._storeMsg.length() > 29)
			{
				PunishmentManager.handleIllegalPlayerAction(player, player + " tried to overflow private store buy message", GeneralConfig.DEFAULT_PUNISH);
			}
			else
			{
				player.getBuyList().setTitle(this._storeMsg);
				player.sendPacket(new PrivateStoreMsgBuy(player));
			}
		}
	}
}
