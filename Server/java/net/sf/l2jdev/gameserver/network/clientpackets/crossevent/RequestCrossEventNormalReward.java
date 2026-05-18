package net.sf.l2jdev.gameserver.network.clientpackets.crossevent;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.managers.events.CrossEventManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.CrossEventHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.CrossEventRegularRewardHolder;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.crossevent.ExCrossEventInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.crossevent.ExCrossEventNormalReward;

public class RequestCrossEventNormalReward extends ClientPacket
{
	private int _vertical = 0;
	private int _horizontal = 0;
	private CrossEventRegularRewardHolder _regularReward = null;

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
			if (CrossEventManager.getInstance().getGameTickets(player) != 0)
			{
				player.destroyItemByItemId(ItemProcessType.FEE, CrossEventManager.getInstance().getTicketId(), 1L, player, false);
				this.getRandomCell(player);
				if (this._regularReward.cellAmount() > 0)
				{
					ItemHolder reward = new ItemHolder(this._regularReward.cellReward(), this._regularReward.cellAmount());
					player.sendPacket(new ExCrossEventNormalReward(this._vertical, this._horizontal, this._regularReward.cellAmount()));
					player.sendPacket(new ExCrossEventInfo(player));
					player.addItem(ItemProcessType.REWARD, reward, player, true);
				}

				CrossEventManager.getInstance().checkAdvancedRewardAvailable(player);
			}
		}
	}

	private void getRandomCell(Player player)
	{
		int randomCell = Rnd.get(0, 15);
		this._regularReward = CrossEventManager.getInstance().getRegularRewardsList().get(randomCell);
		switch (randomCell)
		{
			case 0:
				this._vertical = 0;
				this._horizontal = 0;
				break;
			case 1:
				this._vertical = 0;
				this._horizontal = 1;
				break;
			case 2:
				this._vertical = 0;
				this._horizontal = 2;
				break;
			case 3:
				this._vertical = 0;
				this._horizontal = 3;
				break;
			case 4:
				this._vertical = 1;
				this._horizontal = 0;
				break;
			case 5:
				this._vertical = 1;
				this._horizontal = 1;
				break;
			case 6:
				this._vertical = 1;
				this._horizontal = 2;
				break;
			case 7:
				this._vertical = 1;
				this._horizontal = 3;
				break;
			case 8:
				this._vertical = 2;
				this._horizontal = 0;
				break;
			case 9:
				this._vertical = 2;
				this._horizontal = 1;
				break;
			case 10:
				this._vertical = 2;
				this._horizontal = 2;
				break;
			case 11:
				this._vertical = 2;
				this._horizontal = 3;
				break;
			case 12:
				this._vertical = 3;
				this._horizontal = 0;
				break;
			case 13:
				this._vertical = 3;
				this._horizontal = 1;
				break;
			case 14:
				this._vertical = 3;
				this._horizontal = 2;
				break;
			case 15:
				this._vertical = 3;
				this._horizontal = 3;
		}

		for (CrossEventHolder cell : player.getCrossEventCells())
		{
			if (cell.cellId() == randomCell)
			{
				this.getRandomCell(player);
				return;
			}
		}

		player.getCrossEventCells().add(new CrossEventHolder(randomCell, this._horizontal, this._vertical));
	}
}
