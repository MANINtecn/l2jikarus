package net.sf.l2jdev.gameserver.model.olympiad;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.OlympiadConfig;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class OlympiadGameTask implements Runnable
{
	protected static final Logger LOGGER = Logger.getLogger(OlympiadGameTask.class.getName());
	private static final int[] TELEPORT_TO_ARENA_TIMES = new int[]
	{
		120,
		60,
		30,
		15,
		10,
		5,
		4,
		3,
		2,
		1,
		0
	};
	private static final int[] BATTLE_START_TIME_FIRST = new int[]
	{
		60,
		55,
		50,
		40,
		30,
		20,
		10,
		0
	};
	private static final int[] BATTLE_START_TIME_SECOND = new int[]
	{
		10,
		5,
		4,
		3,
		2,
		1,
		0
	};
	private static final int[] BATTLE_END_TIME_SECOND = new int[]
	{
		120,
		60,
		30,
		10,
		5
	};
	private static final int[] TELEPORT_TO_TOWN_TIMES = new int[]
	{
		40,
		30,
		20,
		10,
		5,
		4,
		3,
		2,
		1,
		0
	};
	private final OlympiadStadium _stadium;
	private AbstractOlympiadGame _game;
	private OlympiadGameState _state = OlympiadGameState.IDLE;
	private boolean _needAnnounce = false;
	private int _countDown = 0;

	public OlympiadGameTask(OlympiadStadium stadium)
	{
		this._stadium = stadium;
		this._stadium.registerTask(this);
	}

	public boolean isRunning()
	{
		return this._state != OlympiadGameState.IDLE;
	}

	public boolean isGameStarted()
	{
		return this._state.ordinal() >= OlympiadGameState.GAME_STARTED.ordinal() && this._state.ordinal() <= OlympiadGameState.CLEANUP.ordinal();
	}

	public boolean isBattleStarted()
	{
		return this._state == OlympiadGameState.BATTLE_IN_PROGRESS;
	}

	public boolean isBattleFinished()
	{
		return this._state == OlympiadGameState.TELEPORT_TO_TOWN;
	}

	public boolean needAnnounce()
	{
		if (this._needAnnounce)
		{
			this._needAnnounce = false;
			return true;
		}
		return false;
	}

	public OlympiadStadium getStadium()
	{
		return this._stadium;
	}

	public AbstractOlympiadGame getGame()
	{
		return this._game;
	}

	public void attachGame(AbstractOlympiadGame game)
	{
		if (game != null && this._state != OlympiadGameState.IDLE)
		{
			LOGGER.warning("Attempt to overwrite non-finished game in state " + this._state);
		}
		else
		{
			this._game = game;
			this._state = OlympiadGameState.BEGIN;
			this._needAnnounce = false;
			ThreadPool.execute(this);
		}
	}

	@Override
	public void run()
	{
		try
		{
			int delay;
			delay = 1;
			label72:
			switch (this._state)
			{
				case BEGIN:
					this._state = OlympiadGameState.TELEPORT_TO_ARENA;
					this._countDown = OlympiadConfig.OLYMPIAD_WAIT_TIME;
					break;
				case TELEPORT_TO_ARENA:
					if (this._countDown > 0)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_WILL_BE_TAKEN_TO_THE_OLYMPIC_STADIUM_IN_S1_SEC);
						sm.addInt(this._countDown);
						this._game.broadcastPacket(sm);
					}

					if (this._countDown == 1)
					{
						this._game.untransformPlayers();
					}

					delay = this.getDelay(TELEPORT_TO_ARENA_TIMES);
					if (this._countDown <= 0)
					{
						this._state = OlympiadGameState.GAME_STARTED;
					}
					break;
				case GAME_STARTED:
					if (!this.startGame())
					{
						this._state = OlympiadGameState.GAME_STOPPED;
					}
					else
					{
						this._state = OlympiadGameState.BATTLE_COUNTDOWN_FIRST;
						this._countDown = BATTLE_START_TIME_FIRST[0];
						this._stadium.updateZoneInfoForObservers();
						delay = 5;
					}
					break;
				case BATTLE_COUNTDOWN_FIRST:
					if (this._countDown > 0)
					{
						if (this._countDown == 55)
						{
							this._game.healPlayers();
						}
						else
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.THE_MATCH_BEGINS_IN_S1_SEC);
							sm.addInt(this._countDown);
							this._stadium.broadcastPacket(sm);
						}
					}

					delay = this.getDelay(BATTLE_START_TIME_FIRST);
					if (this._countDown <= 0)
					{
						this._game.makePlayersInvul();
						this._game.resetDamage();
						this._stadium.openDoors();
						this._state = OlympiadGameState.BATTLE_COUNTDOWN_SECOND;
						this._countDown = BATTLE_START_TIME_SECOND[0];
						delay = this.getDelay(BATTLE_START_TIME_SECOND);
					}
					break;
				case BATTLE_COUNTDOWN_SECOND:
					if (this._countDown > 0)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.THE_MATCH_BEGINS_IN_S1_SEC);
						sm.addInt(this._countDown);
						this._stadium.broadcastPacket(sm);
					}

					delay = this.getDelay(BATTLE_START_TIME_SECOND);
					if (this._countDown <= 0)
					{
						this._state = OlympiadGameState.BATTLE_STARTED;
						this._game.removePlayersInvul();
						this._stadium.broadcastPacket(new SystemMessage(SystemMessageId.HIDDEN_MSG_START_OLYMPIAD));
					}
					break;
				case BATTLE_STARTED:
					this._countDown = 0;
					this._state = OlympiadGameState.BATTLE_IN_PROGRESS;
					if (!this.startBattle())
					{
						this._state = OlympiadGameState.GAME_STOPPED;
					}
					break;
				case BATTLE_IN_PROGRESS:
					this._countDown += 1000;
					int remaining = (int) ((OlympiadConfig.OLYMPIAD_BATTLE - this._countDown) / 1000L);
					int[] var3 = BATTLE_END_TIME_SECOND;
					int var4 = var3.length;
					int var5 = 0;

					while (true)
					{
						if (var5 < var4)
						{
							int announceTime = var3[var5];
							if (announceTime != remaining)
							{
								var5++;
								continue;
							}

							SystemMessage sm = new SystemMessage(SystemMessageId.THE_GAME_ENDS_IN_S1_SEC);
							sm.addInt(announceTime);
							this._stadium.broadcastPacket(sm);
						}

						if (this.checkBattle() || this._countDown > OlympiadConfig.OLYMPIAD_BATTLE)
						{
							this._state = OlympiadGameState.GAME_STOPPED;
						}
						break label72;
					}
				case GAME_STOPPED:
					this._state = OlympiadGameState.TELEPORT_TO_TOWN;
					this._countDown = TELEPORT_TO_TOWN_TIMES[0];
					this.stopGame();
					delay = this.getDelay(TELEPORT_TO_TOWN_TIMES);
					break;
				case TELEPORT_TO_TOWN:
					if (this._countDown > 0)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_WILL_BE_MOVED_BACK_TO_TOWN_IN_S1_SECOND_S);
						sm.addInt(this._countDown);
						this._game.broadcastPacket(sm);
					}

					delay = this.getDelay(TELEPORT_TO_TOWN_TIMES);
					if (this._countDown <= 0)
					{
						this._state = OlympiadGameState.CLEANUP;
					}
					break;
				case CLEANUP:
					this.cleanupGame();
					this._state = OlympiadGameState.IDLE;
					this._game = null;
					return;
			}

			ThreadPool.schedule(this, delay * 1000);
		}
		catch (Exception var8)
		{
			switch (this._state)
			{
				case GAME_STOPPED:
				case TELEPORT_TO_TOWN:
				case CLEANUP:
				case IDLE:
					LOGGER.warning("Unable to return players back in town, exception: " + var8.getMessage());
					this._state = OlympiadGameState.IDLE;
					this._game = null;
					return;
				default:
					LOGGER.log(Level.WARNING, "Exception in " + this._state + ", trying to port players back: " + var8.getMessage(), var8);
					this._state = OlympiadGameState.GAME_STOPPED;
					ThreadPool.schedule(this, 1000L);
			}
		}
	}

	private int getDelay(int[] times)
	{
		for (int i = 0; i < times.length - 1; i++)
		{
			int time = times[i];
			if (time < this._countDown)
			{
				int delay = this._countDown - time;
				this._countDown = time;
				return delay;
			}
		}

		this._countDown = -1;
		return 1;
	}

	private boolean startGame()
	{
		try
		{
			if (this._game.checkDefaulted())
			{
				return false;
			}
			this._stadium.closeDoors();
			if (this._game.needBuffers())
			{
				this._stadium.spawnBuffers();
			}

			if (!this._game.portPlayersToArena(this._stadium.getZone().getSpawns(), this._stadium.getInstance()))
			{
				return false;
			}
			this._game.removals();
			this._needAnnounce = true;
			OlympiadGameManager.getInstance().startBattle();
			return true;
		}
		catch (Exception var2)
		{
			LOGGER.log(Level.WARNING, var2.getMessage(), var2);
			return false;
		}
	}

	private boolean startBattle()
	{
		try
		{
			if (this._game.needBuffers())
			{
				this._stadium.deleteBuffers();
			}

			if (this._game.checkBattleStatus() && this._game.makeCompetitionStart())
			{
				this._game.broadcastOlympiadInfo(this._stadium);
				this._stadium.broadcastPacket(new SystemMessage(SystemMessageId.THE_MATCH_HAS_BEGUN_FIGHT));
				this._stadium.updateZoneStatusForCharactersInside();
				return true;
			}
		}
		catch (Exception var2)
		{
			LOGGER.log(Level.WARNING, var2.getMessage(), var2);
		}

		return false;
	}

	private boolean checkBattle()
	{
		try
		{
			return this._game.haveWinner();
		}
		catch (Exception var2)
		{
			LOGGER.log(Level.WARNING, var2.getMessage(), var2);
			return true;
		}
	}

	private void stopGame()
	{
		try
		{
			this._game.validateWinner(this._stadium);
		}
		catch (Exception var5)
		{
			LOGGER.log(Level.WARNING, var5.getMessage(), var5);
		}

		try
		{
			this._game.cleanEffects();
		}
		catch (Exception var4)
		{
			LOGGER.log(Level.WARNING, var4.getMessage(), var4);
		}

		try
		{
			this._game.makePlayersInvul();
		}
		catch (Exception var3)
		{
			LOGGER.log(Level.WARNING, var3.getMessage(), var3);
		}

		try
		{
			this._stadium.updateZoneStatusForCharactersInside();
		}
		catch (Exception var2)
		{
			LOGGER.log(Level.WARNING, var2.getMessage(), var2);
		}
	}

	private void cleanupGame()
	{
		try
		{
			this._game.removePlayersInvul();
		}
		catch (Exception var6)
		{
			LOGGER.log(Level.WARNING, var6.getMessage(), var6);
		}

		try
		{
			this._game.playersStatusBack();
		}
		catch (Exception var5)
		{
			LOGGER.log(Level.WARNING, var5.getMessage(), var5);
		}

		try
		{
			this._game.portPlayersBack();
		}
		catch (Exception var4)
		{
			LOGGER.log(Level.WARNING, var4.getMessage(), var4);
		}

		try
		{
			this._game.clearPlayers();
		}
		catch (Exception var3)
		{
			LOGGER.log(Level.WARNING, var3.getMessage(), var3);
		}

		try
		{
			this._stadium.closeDoors();
		}
		catch (Exception var2)
		{
			LOGGER.log(Level.WARNING, var2.getMessage(), var2);
		}
	}
}
