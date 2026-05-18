package net.sf.l2jdev.gameserver.model.actor.tasks.player;

import java.util.Objects;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.SayuneRequest;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;
import net.sf.l2jdev.gameserver.network.serverpackets.sayune.ExNotifyFlyMoveStart;

public class FlyMoveStartTask implements Runnable
{
	private final Player _player;
	private final ZoneType _zone;

	public FlyMoveStartTask(ZoneType zone, Player player)
	{
		Objects.requireNonNull(zone);
		Objects.requireNonNull(player);
		this._player = player;
		this._zone = zone;
	}

	@Override
	public void run()
	{
		if (this._zone.isCharacterInZone(this._player))
		{
			if (!this._player.hasRequest(SayuneRequest.class))
			{
				this._player.sendPacket(ExNotifyFlyMoveStart.STATIC_PACKET);
				ThreadPool.schedule(this, 1000L);
			}
		}
	}
}
