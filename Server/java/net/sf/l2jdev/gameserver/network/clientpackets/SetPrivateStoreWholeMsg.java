package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPrivateStoreSetWholeMsg;

public class SetPrivateStoreWholeMsg extends ClientPacket
{
 
	private String _msg;

	@Override
	protected void readImpl()
	{
		this._msg = this.readString();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null && player.getSellList() != null)
		{
			if (this._msg != null && this._msg.length() > 29)
			{
				PunishmentManager.handleIllegalPlayerAction(player, player + " tried to overflow private store whole message", GeneralConfig.DEFAULT_PUNISH);
			}
			else
			{
				player.getSellList().setTitle(this._msg);
				player.sendPacket(new ExPrivateStoreSetWholeMsg(player));
			}
		}
	}
}
