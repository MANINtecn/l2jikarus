package net.sf.l2jdev.gameserver.model.residences;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import net.sf.l2jdev.gameserver.model.clan.Clan;

public class Bidder
{
	private final Clan _clan;
	private final long _bid;
	private final long _time;

	public Bidder(Clan clan, long bid, long time)
	{
		this._clan = clan;
		this._bid = bid;
		this._time = time == 0L ? Instant.now().toEpochMilli() : time;
	}

	public Clan getClan()
	{
		return this._clan;
	}

	public String getClanName()
	{
		return this._clan.getName();
	}

	public String getLeaderName()
	{
		return this._clan.getLeaderName();
	}

	public long getBid()
	{
		return this._bid;
	}

	public long getTime()
	{
		return this._time;
	}

	public String getFormattedTime()
	{
		return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(Instant.ofEpochMilli(this._time).atZone(ZoneId.systemDefault()));
	}
}
