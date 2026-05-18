package net.sf.l2jdev.gameserver.network.clientpackets.mablegame;

import net.sf.l2jdev.gameserver.data.xml.MableGameData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class ExRequestMableGamePopupOk extends ClientPacket
{
	@Override
	public void readImpl()
	{
		this.readByte();
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
				int pendingCellId = playerState.getPendingCellIdPopup();
				if (pendingCellId >= 1)
				{
					MableGameData.MableGameCell cell = data.getCellById(pendingCellId);
					if (cell != null)
					{
						playerState.setCurrentCellId(pendingCellId);
						playerState.setPendingCellIdPopup(-1);
						switch (cell.getColor())
						{
							case LIGHT_BLUE:
							case GREEN:
							case RED:
							case BURNING_RED:
								if (playerState.getPendingReward() != null)
								{
									player.addItem(ItemProcessType.REWARD, playerState.getPendingReward(), player, true);
									playerState.setPendingReward(null);
								}
								else if (playerState.isMoved())
								{
									playerState.handleCell(player, cell);
								}
							case YELLOW:
								break;
							case PURPLE:
								if (playerState.isMoved())
								{
									playerState.handleCell(player, cell);
								}
								break;
							default:
								PacketLogger.warning(this.getClass().getSimpleName() + ": Unhandled Cell Id:" + cell.getId() + " Color:" + cell.getColor());
						}
					}
				}
			}
		}
	}
}
