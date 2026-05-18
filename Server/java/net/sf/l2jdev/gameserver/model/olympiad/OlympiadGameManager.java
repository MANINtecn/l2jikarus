package net.sf.l2jdev.gameserver.model.olympiad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.zone.type.OlympiadStadiumZone;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class OlympiadGameManager implements Runnable
{
	private static final Logger LOGGER = Logger.getLogger(OlympiadGameManager.class.getName());
	private volatile boolean _battleStarted = false;
	private final List<OlympiadStadium> _tasks;
	private int _delay = 0;

	protected OlympiadGameManager()
	{
		Collection<OlympiadStadiumZone> zones = ZoneManager.getInstance().getAllZones(OlympiadStadiumZone.class);
		if (zones != null && !zones.isEmpty())
		{
			OlympiadStadiumZone[] array = zones.toArray(new OlympiadStadiumZone[zones.size()]);
			this._tasks = new ArrayList<>(80);
			int zonesCount = array.length;

			for (int i = 0; i < 80; i++)
			{
				OlympiadStadium stadium = new OlympiadStadium(array[i % zonesCount], i);
				stadium.registerTask(new OlympiadGameTask(stadium));
				this._tasks.add(stadium);
			}

			LOGGER.info("Olympiad System: Loaded " + this._tasks.size() + " stadiums.");
		}
		else
		{
			throw new Error("No olympiad stadium zones defined !");
		}
	}

	protected final boolean isBattleStarted()
	{
		return this._battleStarted;
	}

	protected void startBattle()
	{
		this._battleStarted = true;
	}

	@Override
	public void run()
	{
		if (!Olympiad.getInstance().isOlympiadEnd())
		{
			if (Olympiad.getInstance().inCompPeriod())
			{
				List<Set<Integer>> readyClassed = OlympiadManager.getInstance().hasEnoughRegisteredClassed();
				boolean readyNonClassed = OlympiadManager.getInstance().hasEnoughRegisteredNonClassed();
				if (readyClassed == null && !readyNonClassed)
				{
					this._delay++;
					if (this._delay >= 10)
					{
						for (Integer id : OlympiadManager.getInstance().getRegisteredNonClassBased())
						{
							if (id != null)
							{
								Player noble = World.getInstance().getPlayer(id);
								if (noble != null)
								{
									noble.sendPacket(new SystemMessage(SystemMessageId.THE_GAMES_MAY_BE_DELAYED_DUE_TO_AN_INSUFFICIENT_NUMBER_OF_PLAYERS_WAITING));
								}
							}
						}

						for (Set<Integer> list : OlympiadManager.getInstance().getRegisteredClassBased().values())
						{
							for (Integer idx : list)
							{
								if (idx != null)
								{
									Player noble = World.getInstance().getPlayer(idx);
									if (noble != null)
									{
										noble.sendPacket(new SystemMessage(SystemMessageId.THE_GAMES_MAY_BE_DELAYED_DUE_TO_AN_INSUFFICIENT_NUMBER_OF_PLAYERS_WAITING));
									}
								}
							}
						}

						this._delay = 0;
					}
				}
				else
				{
					this._delay = 0;

					for (int i = 0; i < this._tasks.size(); i++)
					{
						OlympiadGameTask task = this._tasks.get(i).getTask();
						synchronized (task)
						{
							if (!task.isRunning())
							{
								if (readyClassed != null)
								{
									AbstractOlympiadGame newGame = OlympiadGameClassed.createGame(i, readyClassed);
									if (newGame != null)
									{
										task.attachGame(newGame);
										continue;
									}

									readyClassed = null;
								}

								if (readyNonClassed)
								{
									AbstractOlympiadGame newGame = OlympiadGameNonClassed.createGame(i, OlympiadManager.getInstance().getRegisteredNonClassBased());
									if (newGame != null)
									{
										task.attachGame(newGame);
										continue;
									}

									readyNonClassed = false;
								}
							}
						}

						if (readyClassed == null && !readyNonClassed)
						{
							break;
						}
					}
				}
			}
			else if (this.isAllTasksFinished())
			{
				OlympiadManager.getInstance().clearRegistered();
				this._battleStarted = false;
			}
		}
	}

	public boolean isAllTasksFinished()
	{
		for (OlympiadStadium stadium : this._tasks)
		{
			OlympiadGameTask task = stadium.getTask();
			if (task.isRunning())
			{
				return false;
			}
		}

		return true;
	}

	public OlympiadGameTask getOlympiadTask(int id)
	{
		return id >= 0 && id < this._tasks.size() ? this._tasks.get(id).getTask() : null;
	}

	public int getNumberOfStadiums()
	{
		return this._tasks.size();
	}

	public void notifyCompetitorDamage(Player attacker, int damage)
	{
		if (attacker != null)
		{
			int id = attacker.getOlympiadGameId();
			if (id >= 0 && id < this._tasks.size())
			{
				AbstractOlympiadGame game = this._tasks.get(id).getTask().getGame();
				if (game != null)
				{
					game.addDamage(attacker, damage);
				}
			}
		}
	}

	public List<OlympiadStadium> getTasks()
	{
		return this._tasks;
	}

	public static OlympiadGameManager getInstance()
	{
		return OlympiadGameManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final OlympiadGameManager INSTANCE = new OlympiadGameManager();
	}
}
