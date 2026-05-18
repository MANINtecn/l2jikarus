package net.sf.l2jdev.gameserver.model.actor.tasks.player;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.custom.OfflineTradeConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class FameTask implements Runnable
{
	private final Player _player;
	private final int _value;

	public FameTask(Player player, int value)
	{
		this._player = player;
		this._value = value;
	}

	@Override
	public void run()
	{
		if (this._player != null && (!this._player.isDead() || PlayerConfig.FAME_FOR_DEAD_PLAYERS))
		{
			if (this._player.getClient() != null && !this._player.getClient().isDetached() || OfflineTradeConfig.OFFLINE_FAME)
			{
				this._player.setFame(this._player.getFame() + this._value);
				SystemMessage sm = new SystemMessage(SystemMessageId.PERSONAL_REPUTATION_S1);
				sm.addInt(this._value);
				this._player.sendPacket(sm);
				this._player.updateUserInfo();
			}
		}
	}
}
