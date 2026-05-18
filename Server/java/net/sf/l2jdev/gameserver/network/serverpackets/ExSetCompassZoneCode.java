package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExSetCompassZoneCode extends ServerPacket
{
	public static final int ALTEREDZONE = 7;
	public static final int SIEGEWARZONE = 10;
	public static final int PEACEZONE = 11;
	public static final int SEVENSIGNSZONE = 12;
	public static final int NOPVPZONE = 13;
	public static final int PVPZONE = 14;
	public static final int GENERALZONE = 15;
	private final int _zoneType;

	public ExSetCompassZoneCode(int value)
	{
		this._zoneType = value;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SET_COMPASS_ZONE_CODE.writeId(this, buffer);
		buffer.writeInt(this._zoneType);
	}
}
