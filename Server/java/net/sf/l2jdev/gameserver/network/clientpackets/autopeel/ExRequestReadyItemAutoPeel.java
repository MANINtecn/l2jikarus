package net.sf.l2jdev.gameserver.network.clientpackets.autopeel;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.AutoPeelRequest;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.autopeel.ExReadyItemAutoPeel;

public class ExRequestReadyItemAutoPeel extends ClientPacket
{
	private int _itemObjectId;

	@Override
	protected void readImpl()
	{
		this._itemObjectId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Item item = player.getInventory().getItemByObjectId(this._itemObjectId);
			if (item != null && item.isEtcItem() && item.getEtcItem().getExtractableItems() != null && !item.getEtcItem().getExtractableItems().isEmpty())
			{
				player.addRequest(new AutoPeelRequest(player, item));
				player.sendPacket(new ExReadyItemAutoPeel(true, this._itemObjectId));
			}
			else
			{
				player.sendPacket(new ExReadyItemAutoPeel(false, this._itemObjectId));
			}
		}
	}
}
