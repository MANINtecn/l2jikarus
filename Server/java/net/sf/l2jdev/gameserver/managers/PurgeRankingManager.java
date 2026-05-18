package net.sf.l2jdev.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.AbstractMap.SimpleEntry;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.model.Message;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.PlayerPurgeHolder;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.itemcontainer.Mail;
import net.sf.l2jdev.gameserver.network.enums.MailType;
import net.sf.l2jdev.gameserver.network.serverpackets.subjugation.ExSubjugationSidebar;

public class PurgeRankingManager
{
	protected static final Logger LOGGER = Logger.getLogger(PurgeRankingManager.class.getName());
	private static final Map<Integer, Map<Integer, StatSet>> _ranking = new HashMap<>();
	public static final String RESTORE_SUBJUGATION = "SELECT *, `points` + `keys` * 1000000 as `total` FROM `character_purge` WHERE `category`=? ORDER BY `total` DESC";
	public static final String DELETE_SUBJUGATION = "DELETE FROM character_purge WHERE charId=? and category=?";

	public PurgeRankingManager()
	{
		this.updateRankingFromDB();
		int nextDate = Calendar.getInstance().get(12);

		while (nextDate % 5 != 0)
		{
			nextDate++;
		}

		ThreadPool.scheduleAtFixedRate(this::updateRankingFromDB, nextDate - Calendar.getInstance().get(12) > 0 ? (nextDate - Calendar.getInstance().get(12)) * 60L * 1000L : (nextDate + 60 - Calendar.getInstance().get(12)) * 60L * 1000L, 300000L);
	}

	private void updateRankingFromDB()
	{
		long lastPurgeRewards = GlobalVariablesManager.getInstance().getLong("PURGE_REWARD_TIME", 0L);
		if (Calendar.getInstance().get(7) == 1 && System.currentTimeMillis() - lastPurgeRewards > 604200000L)
		{
			GlobalVariablesManager.getInstance().set("PURGE_REWARD_TIME", System.currentTimeMillis());

			for (int category = 1; category <= 9; category++)
			{
				if (this.getTop5(category) != null)
				{
					int counter = 0;

					for (Entry<String, Integer> purgeData : this.getTop5(category).entrySet())
					{
						int charId = CharInfoTable.getInstance().getIdByName(purgeData.getKey());
						Message msg = new Message(charId, GeneralConfig.SUBJUGATION_TOPIC_HEADER, GeneralConfig.SUBJUGATION_TOPIC_BODY, MailType.PURGE_REWARD);
						Mail attachment = msg.createAttachments();

						attachment.addItem(ItemProcessType.REWARD, switch (category)
						{
							case 1 -> 95460;
							case 2 -> 95461;
							case 3 -> 95462;
							case 4 -> 95463;
							case 5 -> 95464;
							case 6 -> 95465;
							case 7 -> 96724;
							case 8 -> 97225;
							case 9 -> 95466;
							default -> throw new IllegalStateException("Unexpected value: " + category);
						}, 5 - counter, null, null);
						MailManager.getInstance().sendMessage(msg);

						try (Connection con = DatabaseFactory.getConnection())
						{
							try (PreparedStatement st = con.prepareStatement("DELETE FROM character_purge WHERE charId=? and category=?"))
							{
								st.setInt(1, charId);
								st.setInt(2, category);
								st.execute();
							}
							catch (Exception var18)
							{
								LOGGER.log(Level.SEVERE, "Failed to delete character subjugation info " + charId, var18);
							}
						}
						catch (Exception var20)
						{
							LOGGER.log(Level.SEVERE, "Failed to delete character subjugation info " + charId, var20);
						}

						Player onlinePlayer = World.getInstance().getPlayer(charId);
						if (onlinePlayer != null)
						{
							onlinePlayer.getPurgePoints().clear();
							onlinePlayer.sendPacket(new ExSubjugationSidebar(null, new PlayerPurgeHolder(0, 0, 0)));
						}

						counter++;
					}
				}
			}
		}

		_ranking.clear();

		for (int categoryx = 1; categoryx <= 9; categoryx++)
		{
			this.restoreByCategories(categoryx);
		}
	}

	public void restoreByCategories(int category)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT *, `points` + `keys` * 1000000 as `total` FROM `character_purge` WHERE `category`=? ORDER BY `total` DESC");)
		{
			statement.setInt(1, category);

			try (ResultSet rset = statement.executeQuery())
			{
				int rank = 1;

				Map<Integer, StatSet> rankingInCategory;
				for (rankingInCategory = new HashMap<>(); rset.next(); rank++)
				{
					StatSet set = new StatSet();
					set.set("charId", rset.getInt("charId"));
					set.set("points", rset.getInt("total"));
					rankingInCategory.put(rank, set);
				}

				_ranking.put(category, rankingInCategory);
			}
		}
		catch (Exception var14)
		{
			LOGGER.log(Level.SEVERE, "Could not restore subjugation ranking data", var14);
		}
	}

	public Map<String, Integer> getTop5(int category)
	{
		Map<String, Integer> top5 = new HashMap<>();

		for (int i = 1; i <= 5; i++)
		{
			try
			{
				if (_ranking.get(category) != null)
				{
					StatSet ss = _ranking.get(category).get(i);
					if (ss != null)
					{
						String charName = CharInfoTable.getInstance().getNameById(ss.getInt("charId"));
						int points = ss.getInt("points");
						top5.put(charName, points);
					}
				}
			}
			catch (IndexOutOfBoundsException var7)
			{
			}
		}

		return top5.entrySet().stream().sorted(Entry.<String, Integer> comparingByValue().reversed()).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (_, e2) -> e2, LinkedHashMap::new));
	}

	public SimpleEntry<Integer, Integer> getPlayerRating(int category, int charId)
	{
		if (_ranking.get(category) == null)
		{
			return new SimpleEntry<>(0, 0);
		}
		Optional<Entry<Integer, StatSet>> player = _ranking.get(category).entrySet().stream().filter(it -> it.getValue().getInt("charId") == charId).findFirst();
		if (player.isPresent())
		{
			return player.get().getValue() == null ? new SimpleEntry<>(0, 0) : new SimpleEntry<>(player.get().getKey(), player.get().getValue().getInt("points"));
		}
		return new SimpleEntry<>(0, 0);
	}

	public static PurgeRankingManager getInstance()
	{
		return PurgeRankingManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final PurgeRankingManager INSTANCE = new PurgeRankingManager();
	}
}
