package net.sf.l2jdev.gameserver.model.actor.holders.player;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.enums.Movie;

public class MovieHolder
{
	private final Movie _movie;
	private final List<Player> _players;
	private final Collection<Player> _votedPlayers = ConcurrentHashMap.newKeySet();

	public MovieHolder(List<Player> players, Movie movie)
	{
		this._players = players;
		this._movie = movie;
		this._players.forEach(p -> p.playMovie(this));
	}

	public Movie getMovie()
	{
		return this._movie;
	}

	public void playerEscapeVote(Player player)
	{
		if (!this._votedPlayers.contains(player) && this._players.contains(player) && this._movie.isEscapable())
		{
			this._votedPlayers.add(player);
			if (this._votedPlayers.size() * 100 / this._players.size() >= 50)
			{
				this._players.forEach(Player::stopMovie);
			}
		}
	}

	public List<Player> getPlayers()
	{
		return this._players;
	}

	public Collection<Player> getVotedPlayers()
	{
		return this._votedPlayers;
	}
}
