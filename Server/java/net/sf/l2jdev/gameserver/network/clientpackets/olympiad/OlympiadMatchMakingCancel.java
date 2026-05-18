package net.sf.l2jdev.gameserver.network.clientpackets.olympiad;

import net.sf.l2jdev.gameserver.config.OlympiadConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.olympiad.Olympiad;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.olympiad.ExOlympiadInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.olympiad.ExOlympiadMatchMakingResult;
import net.sf.l2jdev.gameserver.network.serverpackets.olympiad.ExOlympiadRecord;

public class OlympiadMatchMakingCancel extends ClientPacket
{
	private byte _cGameRuleType;

	@Override
	protected void readImpl()
	{
		this._cGameRuleType = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._cGameRuleType == 1)
			{
				OlympiadManager.getInstance().unRegisterNoble(player);
			}

			player.sendPacket(new ExOlympiadMatchMakingResult(this._cGameRuleType, 0));
			if (OlympiadConfig.OLYMPIAD_ENABLED && Olympiad.getInstance().inCompPeriod())
			{
				player.sendPacket(new ExOlympiadInfo(1, Olympiad.getInstance().getRemainingTime()));
			}
			else
			{
				player.sendPacket(new ExOlympiadInfo(0, Olympiad.getInstance().getRemainingTime()));
			}

			player.sendPacket(new ExOlympiadRecord(player, this._cGameRuleType));
		}
	}
}
