package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.PackageSendableList;

public class RequestPackageSendableItemList extends ClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new PackageSendableList(1, player, this._objectId));
			player.sendPacket(new PackageSendableList(2, player, this._objectId));
		}
	}
}
