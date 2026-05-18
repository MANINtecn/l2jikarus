package net.sf.l2jdev.gameserver.network.serverpackets;

import java.time.Instant;
import java.time.ZoneId;
import java.time.zone.ZoneRules;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExEnterWorld extends ServerPacket
{
	private final int _zoneIdOffsetSeconds;
	private final int _epochInSeconds;
	private final int _daylight;

	public ExEnterWorld()
	{
		Instant now = Instant.now();
		this._epochInSeconds = (int) now.getEpochSecond();
		ZoneRules rules = ZoneId.systemDefault().getRules();
		this._zoneIdOffsetSeconds = rules.getStandardOffset(now).getTotalSeconds();
		this._daylight = (int) (rules.getDaylightSavings(now).toMillis() / 1000L);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENTER_WORLD.writeId(this, buffer);
		buffer.writeInt(this._epochInSeconds);
		buffer.writeInt(-this._zoneIdOffsetSeconds);
		buffer.writeInt(this._daylight);
		buffer.writeInt(PlayerConfig.MAX_FREE_TELEPORT_LEVEL);
	}
}
