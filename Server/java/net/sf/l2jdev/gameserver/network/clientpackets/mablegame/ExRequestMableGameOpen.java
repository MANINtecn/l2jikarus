package net.sf.l2jdev.gameserver.network.clientpackets.mablegame;

import net.sf.l2jdev.gameserver.data.xml.MableGameData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.mablegame.ExMableGamePrison;
import net.sf.l2jdev.gameserver.network.serverpackets.mablegame.ExMableGameShowPlayerState;

public class ExRequestMableGameOpen extends ClientPacket
{
	@Override
	public void readImpl()
	{
	}

	@Override
	public void runImpl()
	{
		if (MableGameData.getInstance().isEnabled())
		{
			Player player = this.getClient().getPlayer();
			if (player != null)
			{
				player.sendPacket(new ExMableGameShowPlayerState(player));
				MableGameData.MableGamePlayerState playerState = MableGameData.getInstance().getPlayerState(player.getAccountName());
				if (playerState.getRemainingPrisonRolls() > 0)
				{
					player.sendPacket(new ExMableGamePrison(5, 6, playerState.getRemainingPrisonRolls()));
				}
			}
		}
	}
}
