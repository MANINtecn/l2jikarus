package net.sf.l2jdev.gameserver.model.actor.request;

import java.util.List;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.adenadistribution.ExDivideAdenaCancel;

public class AdenaDistributionRequest extends AbstractRequest
{
	private final Player _distributor;
	private final List<Player> _players;
	private final int _adenaObjectId;
	private final long _adenaCount;

	public AdenaDistributionRequest(Player player, Player distributor, List<Player> players, int adenaObjectId, long adenaCount)
	{
		super(player);
		this._distributor = distributor;
		this._adenaObjectId = adenaObjectId;
		this._players = players;
		this._adenaCount = adenaCount;
	}

	public Player getDistributor()
	{
		return this._distributor;
	}

	public List<Player> getPlayers()
	{
		return this._players;
	}

	public int getAdenaObjectId()
	{
		return this._adenaObjectId;
	}

	public long getAdenaCount()
	{
		return this._adenaCount;
	}

	@Override
	public boolean isUsing(int objectId)
	{
		return objectId == this._adenaObjectId;
	}

	@Override
	public void onTimeout()
	{
		super.onTimeout();
		this._players.forEach(p -> {
			p.removeRequest(AdenaDistributionRequest.class);
			p.sendPacket(ExDivideAdenaCancel.STATIC_PACKET);
		});
	}
}
