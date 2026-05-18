package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;

public class RequestTargetActionMenu extends ClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
		this.readShort();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (!this.getClient().getFloodProtectors().canPerformPlayerAction())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (player.isTargetingDisabled())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				for (WorldObject object : World.getInstance().getVisibleObjects(player, WorldObject.class))
				{
					if (this._objectId == object.getObjectId())
					{
						if (object.isTargetable() && object.isAutoAttackable(player))
						{
							player.setTarget(object);
						}

						return;
					}
				}

				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
}
