package net.sf.l2jdev.gameserver.network.clientpackets.mablegame;

import net.sf.l2jdev.gameserver.data.xml.MableGameData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class ExRequestMableGameClose extends ClientPacket
{
	@Override
	public void readImpl()
	{
	}

	@Override
	public void runImpl()
	{
		Player player = this.getClient().getPlayer();
		if (player != null)
		{
			MableGameData.MableGamePlayerState playerState = MableGameData.getInstance().getPlayerState(player.getAccountName());
			playerState.setMoved(false);
			playerState.setPendingCellIdPopup(-1);
			playerState.setPendingReward(null);
		}
	}
}
