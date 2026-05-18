package net.sf.l2jdev.gameserver.model.olympiad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.OlympiadConfig;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.olympiad.OnOlympiadMatchResult;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.olympiad.ExOlympiadMatchResult;
import net.sf.l2jdev.gameserver.network.serverpackets.olympiad.ExOlympiadUserInfo;

public abstract class OlympiadGameNormal extends AbstractOlympiadGame
{
	protected int _damageP1 = 0;
	protected int _damageP2 = 0;
	protected Participant _playerOne;
	protected Participant _playerTwo;

	protected OlympiadGameNormal(int id, Participant[] opponents)
	{
		super(id);
		this._playerOne = opponents[0];
		this._playerTwo = opponents[1];
		this._playerOne.getPlayer().setOlympiadGameId(id);
		this._playerTwo.getPlayer().setOlympiadGameId(id);
	}

	protected static Participant[] createListOfParticipants(Set<Integer> set)
	{
		if (set != null && !set.isEmpty() && set.size() >= 2)
		{
			int playerOneObjectId = 0;
			int playerTwoObjectId = 0;
			Player playerOne = null;
			Player playerTwo = null;

			while (set.size() > 1)
			{
				int random = Rnd.get(set.size());
				Iterator<Integer> iter = set.iterator();

				while (iter.hasNext())
				{
					playerOneObjectId = iter.next();
					if (--random < 0)
					{
						iter.remove();
						break;
					}
				}

				playerOne = World.getInstance().getPlayer(playerOneObjectId);
				if (playerOne != null && playerOne.isOnline())
				{
					random = Rnd.get(set.size());
					iter = set.iterator();

					while (iter.hasNext())
					{
						playerTwoObjectId = iter.next();
						if (--random < 0)
						{
							iter.remove();
							break;
						}
					}

					playerTwo = World.getInstance().getPlayer(playerTwoObjectId);
					if (playerTwo != null && playerTwo.isOnline())
					{
						return new Participant[]
						{
							new Participant(playerOne, 1),
							new Participant(playerTwo, 2)
						};
					}

					set.add(playerOneObjectId);
				}
			}

			return null;
		}
		return null;
	}

	@Override
	public boolean containsParticipant(int playerId)
	{
		return this._playerOne != null && this._playerOne.getObjectId() == playerId || this._playerTwo != null && this._playerTwo.getObjectId() == playerId;
	}

	@Override
	public void sendOlympiadInfo(Creature creature)
	{
		creature.sendPacket(new ExOlympiadUserInfo(this._playerOne));
		creature.sendPacket(new ExOlympiadUserInfo(this._playerTwo));
	}

	@Override
	public void broadcastOlympiadInfo(OlympiadStadium stadium)
	{
		stadium.broadcastPacket(new ExOlympiadUserInfo(this._playerOne));
		stadium.broadcastPacket(new ExOlympiadUserInfo(this._playerTwo));
	}

	@Override
	protected void broadcastPacket(ServerPacket packet)
	{
		if (this._playerOne.updatePlayer())
		{
			this._playerOne.getPlayer().sendPacket(packet);
		}

		if (this._playerTwo.updatePlayer())
		{
			this._playerTwo.getPlayer().sendPacket(packet);
		}
	}

	@Override
	protected final boolean portPlayersToArena(List<Location> spawns, Instance instance)
	{
		boolean result = true;

		try
		{
			result &= portPlayerToArena(this._playerOne, spawns.get(0), this._stadiumId, instance, OlympiadMode.BLUE);
			return result & portPlayerToArena(this._playerTwo, spawns.get(spawns.size() / 2), this._stadiumId, instance, OlympiadMode.RED);
		}
		catch (Exception var5)
		{
			LOGGER.log(Level.WARNING, "", var5);
			return false;
		}
	}

	@Override
	protected boolean needBuffers()
	{
		return true;
	}

	@Override
	protected void removals()
	{
		if (!this._aborted)
		{
			this.removals(this._playerOne.getPlayer(), true);
			this.removals(this._playerTwo.getPlayer(), true);
		}
	}

	@Override
	protected final boolean makeCompetitionStart()
	{
		if (!super.makeCompetitionStart())
		{
			return false;
		}
		else if (this._playerOne.getPlayer() != null && this._playerTwo.getPlayer() != null)
		{
			this._playerOne.getPlayer().setOlympiadStart(true);
			this._playerOne.getPlayer().updateEffectIcons();
			this._playerTwo.getPlayer().setOlympiadStart(true);
			this._playerTwo.getPlayer().updateEffectIcons();
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	protected void cleanEffects()
	{
		if (this._playerOne.getPlayer() != null && !this._playerOne.isDefaulted() && !this._playerOne.isDisconnected() && this._playerOne.getPlayer().getOlympiadGameId() == this._stadiumId)
		{
			this.cleanEffects(this._playerOne.getPlayer());
		}

		if (this._playerTwo.getPlayer() != null && !this._playerTwo.isDefaulted() && !this._playerTwo.isDisconnected() && this._playerTwo.getPlayer().getOlympiadGameId() == this._stadiumId)
		{
			this.cleanEffects(this._playerTwo.getPlayer());
		}
	}

	@Override
	protected void portPlayersBack()
	{
		if (this._playerOne.getPlayer() != null && !this._playerOne.isDefaulted() && !this._playerOne.isDisconnected())
		{
			this.portPlayerBack(this._playerOne.getPlayer());
		}

		if (this._playerTwo.getPlayer() != null && !this._playerTwo.isDefaulted() && !this._playerTwo.isDisconnected())
		{
			this.portPlayerBack(this._playerTwo.getPlayer());
		}
	}

	@Override
	protected void playersStatusBack()
	{
		if (this._playerOne.getPlayer() != null && !this._playerOne.isDefaulted() && !this._playerOne.isDisconnected() && this._playerOne.getPlayer().getOlympiadGameId() == this._stadiumId)
		{
			this.playerStatusBack(this._playerOne.getPlayer());
		}

		if (this._playerTwo.getPlayer() != null && !this._playerTwo.isDefaulted() && !this._playerTwo.isDisconnected() && this._playerTwo.getPlayer().getOlympiadGameId() == this._stadiumId)
		{
			this.playerStatusBack(this._playerTwo.getPlayer());
		}
	}

	@Override
	protected void clearPlayers()
	{
		this._playerOne.setPlayer(null);
		this._playerOne = null;
		this._playerTwo.setPlayer(null);
		this._playerTwo = null;
	}

	@Override
	protected void handleDisconnect(Player player)
	{
		if (player.getObjectId() == this._playerOne.getObjectId())
		{
			this._playerOne.setDisconnected(true);
		}
		else if (player.getObjectId() == this._playerTwo.getObjectId())
		{
			this._playerTwo.setDisconnected(true);
		}
	}

	@Override
	protected final boolean checkBattleStatus()
	{
		if (this._aborted)
		{
			return false;
		}
		return this._playerOne.getPlayer() == null || this._playerOne.isDisconnected() ? false : this._playerTwo.getPlayer() != null && !this._playerTwo.isDisconnected();
	}

	@Override
	protected final boolean haveWinner()
	{
		if (!this.checkBattleStatus())
		{
			return true;
		}
		boolean playerOneLost = true;

		try
		{
			if (this._playerOne.getPlayer().getOlympiadGameId() == this._stadiumId)
			{
				playerOneLost = this._playerOne.getPlayer().isDead();
			}
		}
		catch (Exception var5)
		{
			playerOneLost = true;
		}

		boolean playerTwoLost = true;

		try
		{
			if (this._playerTwo.getPlayer().getOlympiadGameId() == this._stadiumId)
			{
				playerTwoLost = this._playerTwo.getPlayer().isDead();
			}
		}
		catch (Exception var4)
		{
			playerTwoLost = true;
		}

		return playerOneLost || playerTwoLost;
	}

	@Override
	protected void validateWinner(OlympiadStadium stadium)
	{
		if (!this._aborted)
		{
			ExOlympiadMatchResult resultP1 = null;
			ExOlympiadMatchResult resultP2 = null;
			boolean tie = false;
			int winside = 0;
			List<OlympiadInfo> list1 = new ArrayList<>(1);
			List<OlympiadInfo> list2 = new ArrayList<>(1);
			boolean _pOneCrash = this._playerOne.getPlayer() == null || this._playerOne.isDisconnected();
			boolean _pTwoCrash = this._playerTwo.getPlayer() == null || this._playerTwo.isDisconnected();
			int playerOnePoints = this._playerOne.getStats().getInt("olympiad_points");
			int playerTwoPoints = this._playerTwo.getStats().getInt("olympiad_points");
			int pointDiff = Math.min(playerOnePoints, playerTwoPoints) / this.getDivider();
			if (pointDiff <= 0)
			{
				pointDiff = 1;
			}
			else if (pointDiff > OlympiadConfig.OLYMPIAD_MAX_POINTS)
			{
				pointDiff = OlympiadConfig.OLYMPIAD_MAX_POINTS;
			}

			if (this._playerOne.isDefaulted() || this._playerTwo.isDefaulted())
			{
				try
				{
					if (this._playerOne.isDefaulted())
					{
						try
						{
							int points = Math.min(playerOnePoints / 3, OlympiadConfig.OLYMPIAD_MAX_POINTS);
							this.removePointsFromParticipant(this._playerOne, points);
							list1.add(new OlympiadInfo(this._playerOne.getName(), this._playerOne.getClanName(), this._playerOne.getClanId(), this._playerOne.getBaseClass(), this._damageP1, playerOnePoints - points, -points));
							winside = 2;
							if (OlympiadConfig.OLYMPIAD_LOG_FIGHTS)
							{
								LOGGER_OLYMPIAD.info(this._playerOne.getName() + " default," + this._playerOne + "," + this._playerTwo + ",0,0,0,0," + points + "," + this.getType());
							}
						}
						catch (Exception var24)
						{
							LOGGER.log(Level.WARNING, "Exception on validateWinner(): " + var24.getMessage(), var24);
						}
					}

					if (this._playerTwo.isDefaulted())
					{
						try
						{
							int points = Math.min(playerTwoPoints / 3, OlympiadConfig.OLYMPIAD_MAX_POINTS);
							this.removePointsFromParticipant(this._playerTwo, points);
							list2.add(new OlympiadInfo(this._playerTwo.getName(), this._playerTwo.getClanName(), this._playerTwo.getClanId(), this._playerTwo.getBaseClass(), this._damageP2, playerTwoPoints - points, -points));
							if (winside == 2)
							{
								tie = true;
							}
							else
							{
								winside = 1;
							}

							if (OlympiadConfig.OLYMPIAD_LOG_FIGHTS)
							{
								LOGGER_OLYMPIAD.info(this._playerTwo.getName() + " default," + this._playerOne + "," + this._playerTwo + ",0,0,0,0," + points + "," + this.getType());
							}
						}
						catch (Exception var23)
						{
							LOGGER.log(Level.WARNING, "Exception on validateWinner(): " + var23.getMessage(), var23);
						}
					}

					if (winside == 1)
					{
						resultP1 = new ExOlympiadMatchResult(tie, winside, list1, list2, 0, 0, 0, list1.get(0).getCurrentPoints(), list1.get(0).getDiffPoints());
					}
					else
					{
						resultP1 = new ExOlympiadMatchResult(tie, winside, list2, list1, 0, 0, 0, list1.get(0).getCurrentPoints(), list1.get(0).getDiffPoints());
					}

					if (winside == 1)
					{
						resultP2 = new ExOlympiadMatchResult(tie, winside, list1, list2, 0, 0, 0, list2.get(0).getCurrentPoints(), list2.get(0).getDiffPoints());
					}
					else
					{
						resultP2 = new ExOlympiadMatchResult(tie, winside, list2, list1, 0, 0, 0, list2.get(0).getCurrentPoints(), list2.get(0).getDiffPoints());
					}

					if (this._playerOne != null)
					{
						this._playerOne.getPlayer().sendPacket(resultP1);
					}

					if (this._playerTwo != null)
					{
						this._playerTwo.getPlayer().sendPacket(resultP2);
					}
				}
				catch (Exception var25)
				{
					LOGGER.log(Level.WARNING, "Exception on validateWinner(): " + var25.getMessage(), var25);
				}
			}
			else if (!_pOneCrash && !_pTwoCrash)
			{
				try
				{
					String winner = "draw";
					long _fightTime = System.currentTimeMillis() - this._startTime;
					double playerOneHp = 0.0;
					if (this._playerOne.getPlayer() != null && !this._playerOne.getPlayer().isDead())
					{
						playerOneHp = this._playerOne.getPlayer().getCurrentHp() + this._playerOne.getPlayer().getCurrentCp();
						if (playerOneHp < 0.5)
						{
							playerOneHp = 0.0;
						}
					}

					double playerTwoHp = 0.0;
					if (this._playerTwo.getPlayer() != null && !this._playerTwo.getPlayer().isDead())
					{
						playerTwoHp = this._playerTwo.getPlayer().getCurrentHp() + this._playerTwo.getPlayer().getCurrentCp();
						if (playerTwoHp < 0.5)
						{
							playerTwoHp = 0.0;
						}
					}

					this._playerOne.updatePlayer();
					this._playerTwo.updatePlayer();
					if (this._playerOne.getPlayer() != null && this._playerOne.getPlayer().isOnline() || this._playerTwo.getPlayer() != null && this._playerTwo.getPlayer().isOnline())
					{
						if (this._playerTwo.getPlayer() != null && this._playerTwo.getPlayer().isOnline() && (playerTwoHp != 0.0 || playerOneHp == 0.0) && (this._damageP1 <= this._damageP2 || playerTwoHp == 0.0 || playerOneHp == 0.0))
						{
							if (this._playerOne.getPlayer() == null || !this._playerOne.getPlayer().isOnline() || playerOneHp == 0.0 && playerTwoHp != 0.0 || this._damageP2 > this._damageP1 && playerOneHp != 0.0 && playerTwoHp != 0.0)
							{
								SystemMessage sm = new SystemMessage(SystemMessageId.CONGRATULATIONS_C1_YOU_WIN_THE_MATCH);
								sm.addString(this._playerTwo.getName());
								stadium.broadcastPacket(sm);
								this._playerTwo.updateStat("competitions_won", 1);
								this._playerOne.updateStat("competitions_lost", 1);
								this.addPointsToParticipant(this._playerTwo, pointDiff);
								list2.add(new OlympiadInfo(this._playerTwo.getName(), this._playerTwo.getClanName(), this._playerTwo.getClanId(), this._playerTwo.getBaseClass(), this._damageP2, playerTwoPoints + pointDiff, pointDiff));
								this.removePointsFromParticipant(this._playerOne, pointDiff);
								list1.add(new OlympiadInfo(this._playerOne.getName(), this._playerOne.getClanName(), this._playerOne.getClanId(), this._playerOne.getBaseClass(), this._damageP1, playerOnePoints - pointDiff, -pointDiff));
								winner = this._playerTwo.getName() + " won";
								winside = 2;
								this.saveResults(this._playerOne, this._playerTwo, 2, this._startTime, _fightTime, this.getType());
								rewardParticipant(this._playerTwo.getPlayer(), OlympiadConfig.OLYMPIAD_WINNER_REWARD);
								rewardParticipant(this._playerOne.getPlayer(), OlympiadConfig.OLYMPIAD_LOSER_REWARD);
								if (EventDispatcher.getInstance().hasListener(EventType.ON_OLYMPIAD_MATCH_RESULT, Olympiad.getInstance()))
								{
									EventDispatcher.getInstance().notifyEventAsync(new OnOlympiadMatchResult(this._playerTwo, this._playerOne, this.getType()), Olympiad.getInstance());
								}
							}
							else
							{
								this.saveResults(this._playerOne, this._playerTwo, 0, this._startTime, _fightTime, this.getType());
								SystemMessage sm = new SystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE);
								stadium.broadcastPacket(sm);
								int value = Math.min(playerOnePoints / this.getDivider(), OlympiadConfig.OLYMPIAD_MAX_POINTS);
								this.removePointsFromParticipant(this._playerOne, value);
								list1.add(new OlympiadInfo(this._playerOne.getName(), this._playerOne.getClanName(), this._playerOne.getClanId(), this._playerOne.getBaseClass(), this._damageP1, playerOnePoints - value, -value));
								value = Math.min(playerTwoPoints / this.getDivider(), OlympiadConfig.OLYMPIAD_MAX_POINTS);
								this.removePointsFromParticipant(this._playerTwo, value);
								list2.add(new OlympiadInfo(this._playerTwo.getName(), this._playerTwo.getClanName(), this._playerTwo.getClanId(), this._playerTwo.getBaseClass(), this._damageP2, playerTwoPoints - value, -value));
								tie = true;
							}
						}
						else
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.CONGRATULATIONS_C1_YOU_WIN_THE_MATCH);
							sm.addString(this._playerOne.getName());
							stadium.broadcastPacket(sm);
							this._playerOne.updateStat("competitions_won", 1);
							this._playerTwo.updateStat("competitions_lost", 1);
							this.addPointsToParticipant(this._playerOne, pointDiff);
							list1.add(new OlympiadInfo(this._playerOne.getName(), this._playerOne.getClanName(), this._playerOne.getClanId(), this._playerOne.getBaseClass(), this._damageP1, playerOnePoints + pointDiff, pointDiff));
							this.removePointsFromParticipant(this._playerTwo, pointDiff);
							list2.add(new OlympiadInfo(this._playerTwo.getName(), this._playerTwo.getClanName(), this._playerTwo.getClanId(), this._playerTwo.getBaseClass(), this._damageP2, playerTwoPoints - pointDiff, -pointDiff));
							winner = this._playerOne.getName() + " won";
							winside = 1;
							this.saveResults(this._playerOne, this._playerTwo, 1, this._startTime, _fightTime, this.getType());
							rewardParticipant(this._playerOne.getPlayer(), OlympiadConfig.OLYMPIAD_WINNER_REWARD);
							rewardParticipant(this._playerTwo.getPlayer(), OlympiadConfig.OLYMPIAD_LOSER_REWARD);
							if (EventDispatcher.getInstance().hasListener(EventType.ON_OLYMPIAD_MATCH_RESULT, Olympiad.getInstance()))
							{
								EventDispatcher.getInstance().notifyEventAsync(new OnOlympiadMatchResult(this._playerOne, this._playerTwo, this.getType()), Olympiad.getInstance());
							}
						}
					}
					else
					{
						this._playerOne.updateStat("competitions_drawn", 1);
						this._playerTwo.updateStat("competitions_drawn", 1);
						SystemMessage sm = new SystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE);
						stadium.broadcastPacket(sm);
					}

					this._playerOne.updateStat("competitions_done", 1);
					this._playerTwo.updateStat("competitions_done", 1);
					this._playerOne.updateStat("competitions_done_week", 1);
					this._playerTwo.updateStat("competitions_done_week", 1);
					if (winside == 1)
					{
						resultP1 = new ExOlympiadMatchResult(tie, winside, list1, list2, 2, 2, 2, list1.get(0).getCurrentPoints(), list1.get(0).getDiffPoints());
						resultP2 = new ExOlympiadMatchResult(tie, winside, list1, list2, 2, 2, 2, list2.get(0).getCurrentPoints(), list2.get(0).getDiffPoints());
					}
					else
					{
						resultP1 = new ExOlympiadMatchResult(tie, winside, list2, list1, 3, 3, 3, list1.get(0).getCurrentPoints(), list1.get(0).getDiffPoints());
						resultP2 = new ExOlympiadMatchResult(tie, winside, list2, list1, 3, 3, 3, list2.get(0).getCurrentPoints(), list2.get(0).getDiffPoints());
					}

					if (this._playerOne.getPlayer() != null)
					{
						this._playerOne.getPlayer().sendPacket(resultP1);
					}

					if (this._playerTwo.getPlayer() != null)
					{
						this._playerTwo.getPlayer().sendPacket(resultP2);
					}

					if (OlympiadConfig.OLYMPIAD_LOG_FIGHTS)
					{
						LOGGER_OLYMPIAD.info(winner + "," + this._playerOne.getName() + "," + this._playerOne + "," + this._playerTwo + "," + playerOneHp + "," + playerTwoHp + "," + this._damageP1 + "," + this._damageP2 + "," + pointDiff + "," + this.getType());
					}
				}
				catch (Exception var26)
				{
					LOGGER.log(Level.WARNING, "Exception on validateWinner(): " + var26.getMessage(), var26);
				}
			}
			else
			{
				try
				{
					if (_pTwoCrash && !_pOneCrash)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.CONGRATULATIONS_C1_YOU_WIN_THE_MATCH);
						sm.addString(this._playerOne.getName());
						stadium.broadcastPacket(sm);
						this._playerOne.updateStat("competitions_won", 1);
						this.addPointsToParticipant(this._playerOne, pointDiff);
						list1.add(new OlympiadInfo(this._playerOne.getName(), this._playerOne.getClanName(), this._playerOne.getClanId(), this._playerOne.getBaseClass(), this._damageP1, playerOnePoints + pointDiff, pointDiff));
						this._playerTwo.updateStat("competitions_lost", 1);
						this.removePointsFromParticipant(this._playerTwo, pointDiff);
						list2.add(new OlympiadInfo(this._playerTwo.getName(), this._playerTwo.getClanName(), this._playerTwo.getClanId(), this._playerTwo.getBaseClass(), this._damageP2, playerTwoPoints - pointDiff, -pointDiff));
						winside = 1;
						rewardParticipant(this._playerOne.getPlayer(), OlympiadConfig.OLYMPIAD_WINNER_REWARD);
						if (OlympiadConfig.OLYMPIAD_LOG_FIGHTS)
						{
							LOGGER_OLYMPIAD.info(this._playerTwo.getName() + " crash," + this._playerOne + "," + this._playerTwo + ",0,0,0,0," + pointDiff + "," + this.getType());
						}

						if (EventDispatcher.getInstance().hasListener(EventType.ON_OLYMPIAD_MATCH_RESULT, Olympiad.getInstance()))
						{
							EventDispatcher.getInstance().notifyEventAsync(new OnOlympiadMatchResult(this._playerOne, this._playerTwo, this.getType()), Olympiad.getInstance());
						}
					}
					else if (_pOneCrash && !_pTwoCrash)
					{
						SystemMessage smx = new SystemMessage(SystemMessageId.CONGRATULATIONS_C1_YOU_WIN_THE_MATCH);
						smx.addString(this._playerTwo.getName());
						stadium.broadcastPacket(smx);
						this._playerTwo.updateStat("competitions_won", 1);
						this.addPointsToParticipant(this._playerTwo, pointDiff);
						list2.add(new OlympiadInfo(this._playerTwo.getName(), this._playerTwo.getClanName(), this._playerTwo.getClanId(), this._playerTwo.getBaseClass(), this._damageP2, playerTwoPoints + pointDiff, pointDiff));
						this._playerOne.updateStat("competitions_lost", 1);
						this.removePointsFromParticipant(this._playerOne, pointDiff);
						list1.add(new OlympiadInfo(this._playerOne.getName(), this._playerOne.getClanName(), this._playerOne.getClanId(), this._playerOne.getBaseClass(), this._damageP1, playerOnePoints - pointDiff, -pointDiff));
						winside = 2;
						rewardParticipant(this._playerTwo.getPlayer(), OlympiadConfig.OLYMPIAD_WINNER_REWARD);
						if (OlympiadConfig.OLYMPIAD_LOG_FIGHTS)
						{
							LOGGER_OLYMPIAD.info(this._playerOne.getName() + " crash," + this._playerOne + "," + this._playerTwo + ",0,0,0,0," + pointDiff + "," + this.getType());
						}

						if (EventDispatcher.getInstance().hasListener(EventType.ON_OLYMPIAD_MATCH_RESULT, Olympiad.getInstance()))
						{
							EventDispatcher.getInstance().notifyEventAsync(new OnOlympiadMatchResult(this._playerTwo, this._playerOne, this.getType()), Olympiad.getInstance());
						}
					}
					else if (_pOneCrash && _pTwoCrash)
					{
						stadium.broadcastPacket(new SystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE));
						this._playerOne.updateStat("competitions_lost", 1);
						this.removePointsFromParticipant(this._playerOne, pointDiff);
						list1.add(new OlympiadInfo(this._playerOne.getName(), this._playerOne.getClanName(), this._playerOne.getClanId(), this._playerOne.getBaseClass(), this._damageP1, playerOnePoints - pointDiff, -pointDiff));
						this._playerTwo.updateStat("competitions_lost", 1);
						this.removePointsFromParticipant(this._playerTwo, pointDiff);
						list2.add(new OlympiadInfo(this._playerTwo.getName(), this._playerTwo.getClanName(), this._playerTwo.getClanId(), this._playerTwo.getBaseClass(), this._damageP2, playerTwoPoints - pointDiff, -pointDiff));
						tie = true;
						if (OlympiadConfig.OLYMPIAD_LOG_FIGHTS)
						{
							LOGGER_OLYMPIAD.info("both crash," + this._playerOne.getName() + "," + this._playerOne + ",0,0,0,0," + this._playerTwo + "," + pointDiff + "," + this.getType());
						}
					}

					this._playerOne.updateStat("competitions_done", 1);
					this._playerTwo.updateStat("competitions_done", 1);
					this._playerOne.updateStat("competitions_done_week", 1);
					this._playerTwo.updateStat("competitions_done_week", 1);
					if (winside == 1)
					{
						resultP1 = new ExOlympiadMatchResult(false, winside, list1, list2, 0, 0, 0, list1.get(0).getCurrentPoints(), list1.get(0).getDiffPoints());
					}
					else
					{
						resultP1 = new ExOlympiadMatchResult(false, winside, list2, list1, 0, 0, 0, list1.get(0).getCurrentPoints(), list1.get(0).getDiffPoints());
					}

					if (winside == 1)
					{
						resultP2 = new ExOlympiadMatchResult(false, winside, list1, list2, 0, 0, 0, list2.get(0).getCurrentPoints(), list2.get(0).getDiffPoints());
					}
					else
					{
						resultP2 = new ExOlympiadMatchResult(false, winside, list2, list1, 0, 0, 0, list2.get(0).getCurrentPoints(), list2.get(0).getDiffPoints());
					}

					if (this._playerOne != null)
					{
						this._playerOne.getPlayer().sendPacket(resultP1);
					}

					if (this._playerTwo != null)
					{
						this._playerTwo.getPlayer().sendPacket(resultP2);
					}

					if (EventDispatcher.getInstance().hasListener(EventType.ON_OLYMPIAD_MATCH_RESULT, Olympiad.getInstance()))
					{
						EventDispatcher.getInstance().notifyEventAsync(new OnOlympiadMatchResult(null, this._playerOne, this.getType()), Olympiad.getInstance());
						EventDispatcher.getInstance().notifyEventAsync(new OnOlympiadMatchResult(null, this._playerTwo, this.getType()), Olympiad.getInstance());
					}
				}
				catch (Exception var27)
				{
					LOGGER.log(Level.WARNING, "Exception on validateWinner(): " + var27.getMessage(), var27);
				}
			}
		}
	}

	@Override
	protected void addDamage(Player player, int damage)
	{
		Player player1 = this._playerOne.getPlayer();
		Player player2 = this._playerTwo.getPlayer();
		if (player1 != null && player2 != null)
		{
			if (player == player1)
			{
				if (!player2.isInvul() && !player2.isHpBlocked())
				{
					this._damageP1 += damage;
				}
			}
			else if (player == player2 && !player1.isInvul() && !player1.isHpBlocked())
			{
				this._damageP2 += damage;
			}
		}
	}

	@Override
	public String[] getPlayerNames()
	{
		return new String[]
		{
			this._playerOne.getName(),
			this._playerTwo.getName()
		};
	}

	@Override
	public boolean checkDefaulted()
	{
		this._playerOne.updatePlayer();
		this._playerTwo.updatePlayer();
		SystemMessage reason = checkDefaulted(this._playerOne.getPlayer());
		if (reason != null)
		{
			this._playerOne.setDefaulted(true);
			if (this._playerTwo.getPlayer() != null)
			{
				this._playerTwo.getPlayer().sendPacket(reason);
			}
		}

		reason = checkDefaulted(this._playerTwo.getPlayer());
		if (reason != null)
		{
			this._playerTwo.setDefaulted(true);
			if (this._playerOne.getPlayer() != null)
			{
				this._playerOne.getPlayer().sendPacket(reason);
			}
		}

		return this._playerOne.isDefaulted() || this._playerTwo.isDefaulted();
	}

	@Override
	public void resetDamage()
	{
		this._damageP1 = 0;
		this._damageP2 = 0;
	}

	protected void saveResults(Participant one, Participant two, int winner, long startTime, long fightTime, CompetitionType type)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO olympiad_fights (charOneId, charTwoId, charOneClass, charTwoClass, winner, start, time, classed) values(?,?,?,?,?,?,?,?)");)
		{
			statement.setInt(1, one.getObjectId());
			statement.setInt(2, two.getObjectId());
			statement.setInt(3, one.getBaseClass());
			statement.setInt(4, two.getBaseClass());
			statement.setInt(5, winner);
			statement.setLong(6, startTime);
			statement.setLong(7, fightTime);
			statement.setInt(8, type == CompetitionType.CLASSED ? 1 : 0);
			statement.execute();
		}
		catch (SQLException var17)
		{
			LOGGER.log(Level.SEVERE, "SQL exception while saving olympiad fight.", var17);
		}
	}

	@Override
	protected void healPlayers()
	{
		Player player1 = this._playerOne.getPlayer();
		if (player1 != null)
		{
			player1.setCurrentCp(player1.getMaxCp());
			player1.setCurrentHp(player1.getMaxHp());
			player1.setCurrentMp(player1.getMaxMp());
		}

		Player player2 = this._playerTwo.getPlayer();
		if (player2 != null)
		{
			player2.setCurrentCp(player2.getMaxCp());
			player2.setCurrentHp(player2.getMaxHp());
			player2.setCurrentMp(player2.getMaxMp());
		}
	}

	@Override
	protected void untransformPlayers()
	{
		Player player1 = this._playerOne.getPlayer();
		if (player1 != null && player1.isTransformed())
		{
			player1.stopTransformation(true);
		}

		Player player2 = this._playerTwo.getPlayer();
		if (player2 != null && player2.isTransformed())
		{
			player2.stopTransformation(true);
		}
	}

	@Override
	public void makePlayersInvul()
	{
		if (this._playerOne.getPlayer() != null)
		{
			this._playerOne.getPlayer().setInvul(true);
		}

		if (this._playerTwo.getPlayer() != null)
		{
			this._playerTwo.getPlayer().setInvul(true);
		}
	}

	@Override
	public void removePlayersInvul()
	{
		if (this._playerOne.getPlayer() != null)
		{
			this._playerOne.getPlayer().setInvul(false);
		}

		if (this._playerTwo.getPlayer() != null)
		{
			this._playerTwo.getPlayer().setInvul(false);
		}
	}
}
