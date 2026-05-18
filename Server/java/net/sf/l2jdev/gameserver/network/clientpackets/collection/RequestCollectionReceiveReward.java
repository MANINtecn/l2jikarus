package net.sf.l2jdev.gameserver.network.clientpackets.collection;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.collection.ExCollectionReceiveReward;

public class RequestCollectionReceiveReward extends ClientPacket
{
	private int _collectionId;

	@Override
	protected void readImpl()
	{
		this._collectionId = this.readShort();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getClient().getPlayer();
		if (player != null)
		{
			player.sendPacket(new ExCollectionReceiveReward(this._collectionId, true));
		}
	}
}
