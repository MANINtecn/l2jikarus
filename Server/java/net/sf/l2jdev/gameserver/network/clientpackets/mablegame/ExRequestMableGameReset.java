package net.sf.l2jdev.gameserver.network.clientpackets.mablegame;

import net.sf.l2jdev.gameserver.data.xml.MableGameData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.mablegame.ExMableGameShowPlayerState;

public class ExRequestMableGameReset extends ClientPacket
{
	@Override
	public void readImpl()
	{
		this.readInt();
	}

	@Override
	public void runImpl()
	{
		MableGameData data = MableGameData.getInstance();
		if (data.isEnabled())
		{
			Player player = this.getClient().getPlayer();
			if (player != null)
			{
				MableGameData.MableGamePlayerState playerState = data.getPlayerState(player.getAccountName());
				if (playerState.getCurrentCellId() >= data.getHighestCellId() && playerState.getRound() != data.getDailyAvailableRounds())
				{
					for (ItemHolder itemHolder : data.getResetItems())
					{
						if (player.getInventory().getInventoryItemCount(itemHolder.getId(), -1) < itemHolder.getCount())
						{
							player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT);
							return;
						}
					}

					for (ItemHolder itemHolderx : data.getResetItems())
					{
						if (!player.destroyItemByItemId(ItemProcessType.FEE, itemHolderx.getId(), itemHolderx.getCount(), player, true))
						{
							player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT);
							return;
						}
					}

					playerState.setRound(playerState.getRound() + 1);
					playerState.setCurrentCellId(1);
					playerState.setRemainCommonDice(data.getCommonDiceLimit());
					player.sendPacket(new ExMableGameShowPlayerState(player));
				}
			}
		}
	}
}
