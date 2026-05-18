package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class RequestRecipeShopMessageSet extends ClientPacket
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
			if (this._name != null && this._name.length() > 29)
			{
				PunishmentManager.handleIllegalPlayerAction(player, player + " tried to overflow recipe shop message", GeneralConfig.DEFAULT_PUNISH);
			}
			else
			{
				player.setStoreName(this._name);
			}
		}
	}
}
