package net.sf.l2jdev.gameserver.network.serverpackets.ranking;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.ServerConfig;
import net.sf.l2jdev.gameserver.managers.RankManager;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExRankingCharRankers extends ServerPacket
{
	private final Player _player;
	private final int _group;
	private final int _scope;
	private final int _race;
	private final int _class;
	private final Map<Integer, StatSet> _playerList;
	private final Map<Integer, StatSet> _snapshotList;

	public ExRankingCharRankers(Player player, int group, int scope, int race, int baseclass)
	{
		this._player = player;
		this._group = group;
		this._scope = scope;
		this._race = race;
		this._class = baseclass;
		this._playerList = RankManager.getInstance().getRankList();
		this._snapshotList = RankManager.getInstance().getSnapshotList();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RANKING_CHAR_RANKERS.writeId(this, buffer);
		buffer.writeByte(this._group);
		buffer.writeByte(this._scope);
		buffer.writeInt(this._race);
		buffer.writeInt(this._player.getPlayerClass().getId());
		if (!this._playerList.isEmpty())
		{
			switch (this._group)
			{
				case 0:
					if (this._scope == 0)
					{
						int count = this._playerList.size() > 150 ? 150 : this._playerList.size();
						buffer.writeInt(count);

						for (Integer idxxxxxxxxxx : this._playerList.keySet())
						{
							StatSet player = this._playerList.get(idxxxxxxxxxx);
							buffer.writeSizedString(player.getString("name"));
							buffer.writeSizedString(player.getString("clanName"));
							buffer.writeInt(ServerConfig.SERVER_ID);
							buffer.writeInt(player.getInt("level"));
							buffer.writeInt(player.getInt("classId"));
							buffer.writeInt(player.getInt("race"));
							buffer.writeInt(idxxxxxxxxxx);
							if (!this._snapshotList.isEmpty())
							{
								for (Integer id2xxxxxxx : this._snapshotList.keySet())
								{
									StatSet snapshot = this._snapshotList.get(id2xxxxxxx);
									if (player.getInt("charId") == snapshot.getInt("charId"))
									{
										buffer.writeInt(id2xxxxxxx);
										buffer.writeInt(snapshot.getInt("raceRank", 0));
										buffer.writeInt(snapshot.getInt("classRank", 0));
									}
								}
							}
							else
							{
								buffer.writeInt(idxxxxxxxxxx);
								buffer.writeInt(0);
								buffer.writeInt(0);
							}
						}
					}
					else
					{
						boolean found = false;

						for (Integer idxxxxxxxxxxx : this._playerList.keySet())
						{
							StatSet player = this._playerList.get(idxxxxxxxxxxx);
							if (player.getInt("charId") == this._player.getObjectId())
							{
								found = true;
								int first = idxxxxxxxxxxx > 10 ? idxxxxxxxxxxx - 9 : 1;
								int last = this._playerList.size() >= idxxxxxxxxxxx + 10 ? idxxxxxxxxxxx + 10 : idxxxxxxxxxxx + (this._playerList.size() - idxxxxxxxxxxx);
								if (first == 1)
								{
									buffer.writeInt(last - (first - 1));
								}
								else
								{
									buffer.writeInt(last - first);
								}

								for (int id2xxxxxxxx = first; id2xxxxxxxx <= last; id2xxxxxxxx++)
								{
									StatSet plr = this._playerList.get(id2xxxxxxxx);
									buffer.writeSizedString(plr.getString("name"));
									buffer.writeSizedString(plr.getString("clanName"));
									buffer.writeInt(ServerConfig.SERVER_ID);
									buffer.writeInt(plr.getInt("level"));
									buffer.writeInt(plr.getInt("classId"));
									buffer.writeInt(plr.getInt("race"));
									buffer.writeInt(id2xxxxxxxx);
									if (!this._snapshotList.isEmpty())
									{
										for (Integer id3 : this._snapshotList.keySet())
										{
											StatSet snapshot = this._snapshotList.get(id3);
											if (player.getInt("charId") == snapshot.getInt("charId"))
											{
												buffer.writeInt(id3);
												buffer.writeInt(snapshot.getInt("raceRank", 0));
												buffer.writeInt(snapshot.getInt("classRank", 0));
											}
										}
									}
								}
							}
						}

						if (!found)
						{
							buffer.writeInt(0);
						}
					}
					break;
				case 1:
					if (this._scope == 0)
					{
						int count = 0;

						for (int ix = 1; ix <= this._playerList.size(); ix++)
						{
							StatSet player = this._playerList.get(ix);
							if (this._race == player.getInt("race"))
							{
								count++;
							}
						}

						buffer.writeInt(count > 100 ? 100 : count);
						int ixx = 1;

						for (Integer idxxxxxxx : this._playerList.keySet())
						{
							StatSet player = this._playerList.get(idxxxxxxx);
							if (this._race == player.getInt("race"))
							{
								buffer.writeSizedString(player.getString("name"));
								buffer.writeSizedString(player.getString("clanName"));
								buffer.writeInt(ServerConfig.SERVER_ID);
								buffer.writeInt(player.getInt("level"));
								buffer.writeInt(player.getInt("classId"));
								buffer.writeInt(player.getInt("race"));
								buffer.writeInt(ixx);
								if (this._snapshotList.isEmpty())
								{
									buffer.writeInt(ixx);
									buffer.writeInt(ixx);
									buffer.writeInt(ixx);
								}
								else
								{
									Map<Integer, StatSet> snapshotRaceList = new ConcurrentHashMap<>();
									int j = 1;

									for (Integer id2xxxxx : this._snapshotList.keySet())
									{
										StatSet snapshot = this._snapshotList.get(id2xxxxx);
										if (this._race == snapshot.getInt("race"))
										{
											snapshotRaceList.put(j, this._snapshotList.get(id2xxxxx));
											j++;
										}
									}

									for (Integer id2xxxxxx : snapshotRaceList.keySet())
									{
										StatSet snapshot = snapshotRaceList.get(id2xxxxxx);
										if (player.getInt("charId") == snapshot.getInt("charId"))
										{
											buffer.writeInt(id2xxxxxx);
											buffer.writeInt(snapshot.getInt("raceRank", 0));
											buffer.writeInt(snapshot.getInt("classRank", 0));
										}
									}
								}

								ixx++;
							}
						}
					}
					else
					{
						boolean found = false;
						Map<Integer, StatSet> raceList = new ConcurrentHashMap<>();
						int ixx = 1;

						for (Integer idxxxxxxxx : this._playerList.keySet())
						{
							StatSet set = this._playerList.get(idxxxxxxxx);
							if (this._player.getRace().ordinal() == set.getInt("race"))
							{
								raceList.put(ixx, this._playerList.get(idxxxxxxxx));
								ixx++;
							}
						}

						for (Integer idxxxxxxxxx : raceList.keySet())
						{
							StatSet player = raceList.get(idxxxxxxxxx);
							if (player.getInt("charId") == this._player.getObjectId())
							{
								found = true;
								int first = idxxxxxxxxx > 10 ? idxxxxxxxxx - 9 : 1;
								int last = raceList.size() >= idxxxxxxxxx + 10 ? idxxxxxxxxx + 10 : idxxxxxxxxx + (raceList.size() - idxxxxxxxxx);
								if (first == 1)
								{
									buffer.writeInt(last - (first - 1));
								}
								else
								{
									buffer.writeInt(last - first);
								}

								for (int id2xxxxxxx = first; id2xxxxxxx <= last; id2xxxxxxx++)
								{
									StatSet plr = raceList.get(id2xxxxxxx);
									buffer.writeSizedString(plr.getString("name"));
									buffer.writeSizedString(plr.getString("clanName"));
									buffer.writeInt(ServerConfig.SERVER_ID);
									buffer.writeInt(plr.getInt("level"));
									buffer.writeInt(plr.getInt("classId"));
									buffer.writeInt(plr.getInt("race"));
									buffer.writeInt(id2xxxxxxx);
									buffer.writeInt(id2xxxxxxx);
									buffer.writeInt(id2xxxxxxx);
									buffer.writeInt(id2xxxxxxx);
								}
							}
						}

						if (!found)
						{
							buffer.writeInt(0);
						}
					}
					break;
				case 2:
					Clan clanx = this._player.getClan();
					if (clanx != null)
					{
						Map<Integer, StatSet> clanList = new ConcurrentHashMap<>();
						int ix = 1;

						for (Integer idxxxxx : this._playerList.keySet())
						{
							StatSet set = this._playerList.get(idxxxxx);
							if (clanx.getName().equals(set.getString("clanName")))
							{
								clanList.put(ix, this._playerList.get(idxxxxx));
								ix++;
							}
						}

						buffer.writeInt(clanList.size());

						for (Integer idxxxxxx : clanList.keySet())
						{
							StatSet player = clanList.get(idxxxxxx);
							buffer.writeSizedString(player.getString("name"));
							buffer.writeSizedString(player.getString("clanName"));
							buffer.writeInt(ServerConfig.SERVER_ID);
							buffer.writeInt(player.getInt("level"));
							buffer.writeInt(player.getInt("classId"));
							buffer.writeInt(player.getInt("race"));
							buffer.writeInt(idxxxxxx);
							if (!this._snapshotList.isEmpty())
							{
								for (Integer id2xxxx : this._snapshotList.keySet())
								{
									StatSet snapshot = this._snapshotList.get(id2xxxx);
									if (player.getInt("charId") == snapshot.getInt("charId"))
									{
										buffer.writeInt(id2xxxx);
										buffer.writeInt(snapshot.getInt("raceRank", 0));
										buffer.writeInt(snapshot.getInt("classRank", 0));
									}
								}
							}
							else
							{
								buffer.writeInt(idxxxxxx);
								buffer.writeInt(0);
								buffer.writeInt(0);
							}
						}
					}
					else
					{
						buffer.writeInt(0);
					}
					break;
				case 3:
					if (!this._player.getFriendList().isEmpty())
					{
						Set<Integer> friendList = ConcurrentHashMap.newKeySet();
						int count = 1;

						for (int idxxx : this._player.getFriendList())
						{
							for (Integer id2xx : this._playerList.keySet())
							{
								StatSet temp = this._playerList.get(id2xx);
								if (temp.getInt("charId") == idxxx)
								{
									friendList.add(temp.getInt("charId"));
									count++;
								}
							}
						}

						friendList.add(this._player.getObjectId());
						buffer.writeInt(count);

						for (int idxxx : this._playerList.keySet())
						{
							StatSet player = this._playerList.get(idxxx);
							if (friendList.contains(player.getInt("charId")))
							{
								buffer.writeSizedString(player.getString("name"));
								buffer.writeSizedString(player.getString("clanName"));
								buffer.writeInt(ServerConfig.SERVER_ID);
								buffer.writeInt(player.getInt("level"));
								buffer.writeInt(player.getInt("classId"));
								buffer.writeInt(player.getInt("race"));
								buffer.writeInt(idxxx);
								if (!this._snapshotList.isEmpty())
								{
									for (Integer id2xxx : this._snapshotList.keySet())
									{
										StatSet snapshot = this._snapshotList.get(id2xxx);
										if (player.getInt("charId") == snapshot.getInt("charId"))
										{
											buffer.writeInt(id2xxx);
											buffer.writeInt(snapshot.getInt("raceRank", 0));
											buffer.writeInt(snapshot.getInt("classRank", 0));
										}
									}
								}
								else
								{
									buffer.writeInt(idxxx);
									buffer.writeInt(0);
									buffer.writeInt(0);
								}
							}
						}
					}
					else
					{
						buffer.writeInt(1);
						buffer.writeSizedString(this._player.getName());
						Clan clan = this._player.getClan();
						if (clan != null)
						{
							buffer.writeSizedString(clan.getName());
						}
						else
						{
							buffer.writeSizedString("");
						}

						buffer.writeInt(ServerConfig.SERVER_ID);
						buffer.writeInt(this._player.getStat().getBaseLevel());
						buffer.writeInt(this._player.getBaseClass());
						buffer.writeInt(this._player.getRace().ordinal());
						buffer.writeInt(1);
						if (!this._snapshotList.isEmpty())
						{
							for (Integer idxxxx : this._snapshotList.keySet())
							{
								StatSet snapshot = this._snapshotList.get(idxxxx);
								if (this._player.getObjectId() == snapshot.getInt("charId"))
								{
									buffer.writeInt(idxxxx);
									buffer.writeInt(snapshot.getInt("raceRank", 0));
									buffer.writeInt(snapshot.getInt("classRank", 0));
								}
							}
						}
						else
						{
							buffer.writeInt(0);
							buffer.writeInt(0);
							buffer.writeInt(0);
						}
					}
					break;
				case 4:
					if (this._scope == 0)
					{
						int count = 0;

						for (int i = 1; i <= this._playerList.size(); i++)
						{
							StatSet player = this._playerList.get(i);
							if (this._class == player.getInt("classId"))
							{
								count++;
							}
						}

						buffer.writeInt(count > 100 ? 100 : count);
						int ix = 1;

						for (Integer id : this._playerList.keySet())
						{
							StatSet player = this._playerList.get(id);
							if (this._class == player.getInt("classId"))
							{
								buffer.writeSizedString(player.getString("name"));
								buffer.writeSizedString(player.getString("clanName"));
								buffer.writeInt(ServerConfig.SERVER_ID);
								buffer.writeInt(player.getInt("level"));
								buffer.writeInt(player.getInt("classId"));
								buffer.writeInt(player.getInt("race"));
								buffer.writeInt(ix);
								if (this._snapshotList.size() <= 0)
								{
									buffer.writeInt(ix);
									buffer.writeInt(ix);
									buffer.writeInt(ix);
								}
								else
								{
									Map<Integer, StatSet> snapshotClassList = new ConcurrentHashMap<>();
									int j = 1;

									for (Integer id2 : this._snapshotList.keySet())
									{
										StatSet snapshot = this._snapshotList.get(id2);
										if (this._class == snapshot.getInt("classId"))
										{
											snapshotClassList.put(j, this._snapshotList.get(id2));
											j++;
										}
									}

									for (Integer id2x : snapshotClassList.keySet())
									{
										StatSet snapshot = snapshotClassList.get(id2x);
										if (player.getInt("charId") == snapshot.getInt("charId"))
										{
											buffer.writeInt(id2x);
											buffer.writeInt(snapshot.getInt("raceRank", 0));
											buffer.writeInt(snapshot.getInt("classRank", 0));
										}
									}
								}

								ix++;
							}
						}
					}
					else
					{
						boolean found = false;
						Map<Integer, StatSet> classList = new ConcurrentHashMap<>();
						int ix = 1;

						for (Integer idx : this._playerList.keySet())
						{
							StatSet set = this._playerList.get(idx);
							if (this._player.getBaseClass() == set.getInt("classId"))
							{
								classList.put(ix, this._playerList.get(idx));
								ix++;
							}
						}

						for (Integer idxx : classList.keySet())
						{
							StatSet player = classList.get(idxx);
							if (player.getInt("charId") == this._player.getObjectId())
							{
								found = true;
								int first = idxx > 10 ? idxx - 9 : 1;
								int last = classList.size() >= idxx + 10 ? idxx + 10 : idxx + (classList.size() - idxx);
								if (first == 1)
								{
									buffer.writeInt(last - (first - 1));
								}
								else
								{
									buffer.writeInt(last - first);
								}

								for (int id2xx = first; id2xx <= last; id2xx++)
								{
									StatSet plr = classList.get(id2xx);
									buffer.writeSizedString(plr.getString("name"));
									buffer.writeSizedString(plr.getString("clanName"));
									buffer.writeInt(ServerConfig.SERVER_ID);
									buffer.writeInt(plr.getInt("level"));
									buffer.writeInt(plr.getInt("classId"));
									buffer.writeInt(plr.getInt("race"));
									buffer.writeInt(id2xx);
									buffer.writeInt(id2xx);
									buffer.writeInt(id2xx);
									buffer.writeInt(id2xx);
								}
							}
						}

						if (!found)
						{
							buffer.writeInt(0);
						}
					}
			}
		}
		else
		{
			buffer.writeInt(0);
		}
	}
}
