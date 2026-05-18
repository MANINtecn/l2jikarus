package net.sf.l2jdev.gameserver.model.clan.entry;

import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.clan.Clan;

public class PledgeRecruitInfo
{
	private int _clanId;
	private int _karma;
	private String _information;
	private String _detailedInformation;
	private final Clan _clan;
	private final int _applicationType;
	private final int _recruitType;

	public PledgeRecruitInfo(int clanId, int karma, String information, String detailedInformation, int applicationType, int recruitType)
	{
		this._clanId = clanId;
		this._karma = karma;
		this._information = information;
		this._detailedInformation = detailedInformation;
		this._clan = ClanTable.getInstance().getClan(clanId);
		this._applicationType = applicationType;
		this._recruitType = recruitType;
	}

	public int getClanId()
	{
		return this._clanId;
	}

	public void setClanId(int clanId)
	{
		this._clanId = clanId;
	}

	public String getClanName()
	{
		return this._clan.getName();
	}

	public String getClanLeaderName()
	{
		return this._clan.getLeaderName();
	}

	public int getClanLevel()
	{
		return this._clan.getLevel();
	}

	public int getKarma()
	{
		return this._karma;
	}

	public void setKarma(int karma)
	{
		this._karma = karma;
	}

	public String getInformation()
	{
		return this._information;
	}

	public void setInformation(String information)
	{
		this._information = information;
	}

	public String getDetailedInformation()
	{
		return this._detailedInformation;
	}

	public void setDetailedInformation(String detailedInformation)
	{
		this._detailedInformation = detailedInformation;
	}

	public int getApplicationType()
	{
		return this._applicationType;
	}

	public int getRecruitType()
	{
		return this._recruitType;
	}

	public Clan getClan()
	{
		return this._clan;
	}
}
