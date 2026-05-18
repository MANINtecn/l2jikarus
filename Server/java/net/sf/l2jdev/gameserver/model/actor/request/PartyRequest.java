package net.sf.l2jdev.gameserver.model.actor.request;

import java.util.Objects;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.Party;

public class PartyRequest extends AbstractRequest
{
	private final Player _targetPlayer;
	private final Party _party;

	public PartyRequest(Player player, Player targetPlayer, Party party)
	{
		super(player);
		Objects.requireNonNull(targetPlayer);
		Objects.requireNonNull(party);
		this._targetPlayer = targetPlayer;
		this._party = party;
	}

	public Player getTargetPlayer()
	{
		return this._targetPlayer;
	}

	public Party getParty()
	{
		return this._party;
	}

	@Override
	public boolean isUsing(int objectId)
	{
		return false;
	}

	@Override
	public void onTimeout()
	{
		super.onTimeout();
		this.getPlayer().removeRequest(this.getClass());
		this._targetPlayer.removeRequest(this.getClass());
	}
}
