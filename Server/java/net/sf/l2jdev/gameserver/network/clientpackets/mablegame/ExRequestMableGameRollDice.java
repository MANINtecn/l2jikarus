package net.sf.l2jdev.gameserver.network.clientpackets.mablegame;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.data.xml.MableGameData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.mablegame.ExMableGameDiceResult;
import net.sf.l2jdev.gameserver.network.serverpackets.mablegame.ExMableGamePrison;

public class ExRequestMableGameRollDice extends ClientPacket
{
	private byte _diceType;

	@Override
	public void readImpl()
	{
		this._diceType = this.readByte();
	}

	@Override
	public void runImpl()
	{
		if (MableGameData.getInstance().isEnabled())
		{
			Player player = this.getClient().getPlayer();
			if (player != null)
			{
				MableGameData data = MableGameData.getInstance();
				MableGameData.MableGamePlayerState playerState = data.getPlayerState(player.getAccountName());
				playerState.setMoved(false);
				if (playerState.getCurrentCellId() < data.getHighestCellId())
				{
					if (this._diceType == 0)
					{
						if (playerState.getRemainCommonDice() <= 0)
						{
							return;
						}

						playerState.setRemainCommonDice(playerState.getRemainCommonDice() - 1);
					}

					if (player.destroyItemByItemId(ItemProcessType.FEE, this._diceType == 1 ? 93886 : 93885, 1L, player, true))
					{
						int dice = this._diceType == 1 ? Rnd.get(5, 6) : Rnd.get(1, 6);
						boolean diceChanged = false;
						if (playerState.getRemainingPrisonRolls() > 0)
						{
							if (dice >= 5 && dice <= 6)
							{
								playerState.setRemainingPrisonRolls(0);
								player.sendPacket(new ExMableGameDiceResult(dice, playerState.getCurrentCellId() + 1, data.getCellById(playerState.getCurrentCellId() + 1).getColor().getClientId(), playerState.getRemainCommonDice()));
								dice = 1;
								diceChanged = true;
							}
							else
							{
								playerState.setRemainingPrisonRolls(playerState.getRemainingPrisonRolls() - 1);
								if (playerState.getRemainingPrisonRolls() > 0)
								{
									player.sendPacket(new ExMableGameDiceResult(dice, playerState.getCurrentCellId(), data.getCellById(playerState.getCurrentCellId()).getColor().getClientId(), playerState.getRemainCommonDice()));
									player.sendPacket(new ExMableGamePrison(5, 6, playerState.getRemainingPrisonRolls()));
									return;
								}

								player.sendPacket(new ExMableGameDiceResult(dice, playerState.getCurrentCellId() + 1, data.getCellById(playerState.getCurrentCellId() + 1).getColor().getClientId(), playerState.getRemainCommonDice()));
								dice = 1;
								diceChanged = true;
							}
						}

						int newCellId = Math.min(playerState.getCurrentCellId() + dice, data.getHighestCellId());
						playerState.setCurrentCellId(newCellId);
						MableGameData.MableGameCell newCell = data.getCellById(newCellId);
						if (!diceChanged)
						{
							player.sendPacket(new ExMableGameDiceResult(dice, newCellId, newCell.getColor().getClientId(), playerState.getRemainCommonDice()));
						}

						playerState.handleCell(player, newCell);
					}
				}
			}
		}
	}
}
