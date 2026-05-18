package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerChangeToAwakenedClass;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;

public class RequestChangeToAwakenedClass extends ClientPacket
{
	private boolean _change;

	@Override
	protected void readImpl()
	{
		this._change = this.readInt() == 1;
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._change)
			{
				if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CHANGE_TO_AWAKENED_CLASS, player))
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnPlayerChangeToAwakenedClass(player), player);
				}
			}
			else
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
}
