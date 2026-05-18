package net.sf.l2jdev.gameserver.model;

import java.util.Arrays;
import java.util.stream.Collectors;

import net.sf.l2jdev.gameserver.model.actor.Player;

public class ClientSettings
{
	private final Player _player;
	private boolean _announceDisabled;
	private boolean _partyRequestRestrictedFromOthers;
	private boolean _partyRequestRestrictedFromClan;
	private boolean _partyRequestRestrictedFromFriends;
	private boolean _friendRequestRestrictedFromOthers;
	private boolean _friendRequestRestrictedFromClan;
	private int _partyContributionType;

	public ClientSettings(Player player)
	{
		this._player = player;
		String variable = this._player.getVariables().getString("CLIENT_SETTINGS", "");
		StatSet settings = variable.isEmpty() ? new StatSet() : new StatSet(Arrays.stream(variable.split(",")).map(entry -> entry.split("=")).collect(Collectors.toMap(entry -> entry[0].replace("{", "").replace(" ", ""), entry -> entry[1].replace("}", "").replace(" ", ""))));
		this._announceDisabled = settings.getBoolean("ANNOUNCE_DISABLED", false);
		this._partyRequestRestrictedFromOthers = settings.getBoolean("PARTY_REQUEST_RESTRICTED_FROM_OTHERS", false);
		this._partyRequestRestrictedFromClan = settings.getBoolean("PARTY_REQUEST_RESTRICTED_FROM_CLAN", false);
		this._partyRequestRestrictedFromFriends = settings.getBoolean("PARTY_REQUEST_RESTRICTED_FROM_FRIENDS", false);
		this._friendRequestRestrictedFromOthers = settings.getBoolean("FRIENDS_REQUEST_RESTRICTED_FROM_OTHERS", false);
		this._friendRequestRestrictedFromClan = settings.getBoolean("FRIENDS_REQUEST_RESTRICTED_FROM_CLAN", false);
		this._partyContributionType = settings.getInt("PARTY_CONTRIBUTION_TYPE", 0);
	}

	public void storeSettings()
	{
		StatSet settings = new StatSet();
		settings.set("ANNOUNCE_DISABLED", this._announceDisabled);
		settings.set("PARTY_REQUEST_RESTRICTED_FROM_OTHERS", this._partyRequestRestrictedFromOthers);
		settings.set("PARTY_REQUEST_RESTRICTED_FROM_CLAN", this._partyRequestRestrictedFromClan);
		settings.set("PARTY_REQUEST_RESTRICTED_FROM_FRIENDS", this._partyRequestRestrictedFromFriends);
		settings.set("FRIENDS_REQUEST_RESTRICTED_FROM_OTHERS", this._friendRequestRestrictedFromOthers);
		settings.set("FRIENDS_REQUEST_RESTRICTED_FROM_CLAN", this._friendRequestRestrictedFromClan);
		settings.set("PARTY_CONTRIBUTION_TYPE", this._partyContributionType);
		this._player.getVariables().set("CLIENT_SETTINGS", settings.getSet());
	}

	public boolean isAnnounceDisabled()
	{
		return this._announceDisabled;
	}

	public void setAnnounceEnabled(boolean enabled)
	{
		this._announceDisabled = enabled;
		this.storeSettings();
	}

	public boolean isPartyRequestRestrictedFromOthers()
	{
		return this._partyRequestRestrictedFromOthers;
	}

	public void setPartyRequestRestrictedFromOthers(boolean partyRequestRestrictedFromOthers)
	{
		this._partyRequestRestrictedFromOthers = partyRequestRestrictedFromOthers;
	}

	public boolean isPartyRequestRestrictedFromClan()
	{
		return this._partyRequestRestrictedFromClan;
	}

	public void setPartyRequestRestrictedFromClan(boolean partyRequestRestrictedFromClan)
	{
		this._partyRequestRestrictedFromClan = partyRequestRestrictedFromClan;
	}

	public boolean isPartyRequestRestrictedFromFriends()
	{
		return this._partyRequestRestrictedFromFriends;
	}

	public void setPartyRequestRestrictedFromFriends(boolean partyRequestRestrictedFromFriends)
	{
		this._partyRequestRestrictedFromFriends = partyRequestRestrictedFromFriends;
	}

	public boolean isFriendRequestRestrictedFromOthers()
	{
		return this._friendRequestRestrictedFromOthers;
	}

	public void setFriendRequestRestrictedFromOthers(boolean friendRequestRestrictedFromOthers)
	{
		this._friendRequestRestrictedFromOthers = friendRequestRestrictedFromOthers;
	}

	public boolean isFriendRequestRestrictedFromClan()
	{
		return this._friendRequestRestrictedFromClan;
	}

	public void setFriendRequestRestrictionFromClan(boolean friendRequestRestrictedFromClan)
	{
		this._friendRequestRestrictedFromClan = friendRequestRestrictedFromClan;
	}

	public int getPartyContributionType()
	{
		return this._partyContributionType;
	}

	public void setPartyContributionType(int partyContributionType)
	{
		this._partyContributionType = partyContributionType;
		this.storeSettings();
	}
}
