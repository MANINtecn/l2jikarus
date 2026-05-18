package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.serverpackets.ExRpItemLink;

public class RequestExRqItemLink extends ClientPacket
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
			WorldObject object = World.getInstance().findObject(this._objectId);
			if (object != null && object.isItem())
			{
				Item item = (Item) object;
				if (item.isPublished())
				{
					player.sendPacket(new ExRpItemLink(item));
				}
			}
		}
	}
}
