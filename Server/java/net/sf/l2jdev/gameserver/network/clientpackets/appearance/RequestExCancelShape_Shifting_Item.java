package net.sf.l2jdev.gameserver.network.clientpackets.appearance;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.ShapeShiftingItemRequest;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.appearance.ExShapeShiftingResult;

public class RequestExCancelShape_Shifting_Item extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.removeRequest(ShapeShiftingItemRequest.class);
			player.sendPacket(ExShapeShiftingResult.FAILED);
		}
	}
}
