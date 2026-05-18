package net.sf.l2jdev.gameserver.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.custom.FakePlayersConfig;
import net.sf.l2jdev.gameserver.data.SpawnTable;
import net.sf.l2jdev.gameserver.data.holders.FakePlayerChatHolder;
import net.sf.l2jdev.gameserver.data.xml.FakePlayerData;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.enums.ChatType;
import net.sf.l2jdev.gameserver.network.serverpackets.CreatureSay;
import org.w3c.dom.Document;

public class FakePlayerChatManager implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(FakePlayerChatManager.class.getName());
	private static final List<FakePlayerChatHolder> MESSAGES = new ArrayList<>();
	public static final int MIN_DELAY = 5000;
	public static final int MAX_DELAY = 15000;

	protected FakePlayerChatManager()
	{
		this.load();
	}

	@Override
	public void load()
	{
		if (FakePlayersConfig.FAKE_PLAYERS_ENABLED)
		{
			FakePlayerData.getInstance().report();
			if (FakePlayersConfig.FAKE_PLAYER_CHAT)
			{
				MESSAGES.clear();
				this.parseDatapackFile("data/FakePlayerChatData.xml");
				LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + MESSAGES.size() + " chat templates.");
			}
		}
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "fakePlayerChat", fakePlayerChatNode -> {
			StatSet set = new StatSet(this.parseAttributes(fakePlayerChatNode));
			MESSAGES.add(new FakePlayerChatHolder(set.getString("fpcName"), set.getString("searchMethod"), set.getString("searchText"), set.getString("answers")));
		}));
	}

	public void manageChat(Player player, String fpcName, String message)
	{
		ThreadPool.schedule(() -> this.manageResponce(player, fpcName, message), Rnd.get(5000, 15000));
	}

	public void manageChat(Player player, String fpcName, String message, int minDelay, int maxDelay)
	{
		ThreadPool.schedule(() -> this.manageResponce(player, fpcName, message), Rnd.get(minDelay, maxDelay));
	}

	private void manageResponce(Player player, String fpcName, String message)
	{
		if (player != null)
		{
			String text = message.toLowerCase();
			if (text.contains("can you see me"))
			{
				Spawn spawn = SpawnTable.getInstance().getAnySpawn(FakePlayerData.getInstance().getNpcIdByName(fpcName));
				if (spawn != null)
				{
					Npc npc = spawn.getLastSpawn();
					if (npc != null)
					{
						if (npc.calculateDistance2D(player) < 3000.0)
						{
							if (GeoEngine.getInstance().canSeeTarget(npc, player) && !player.isInvisible())
							{
								this.sendChat(player, fpcName, Rnd.nextBoolean() ? "i am not blind" : (Rnd.nextBoolean() ? "of course i can" : "yes"));
							}
							else
							{
								this.sendChat(player, fpcName, Rnd.nextBoolean() ? "i know you are around" : (Rnd.nextBoolean() ? "not at the moment :P" : "no, where are you?"));
							}
						}
						else
						{
							this.sendChat(player, fpcName, Rnd.nextBoolean() ? "nope, can't see you" : (Rnd.nextBoolean() ? "nope" : "no"));
						}

						return;
					}
				}
			}

			for (FakePlayerChatHolder chatHolder : MESSAGES)
			{
				if (chatHolder.getFpcName().equals(fpcName) || chatHolder.getFpcName().equals("ALL"))
				{
					String var7 = chatHolder.getSearchMethod();
					switch (var7)
					{
						case "EQUALS":
							if (text.equals(chatHolder.getSearchText().get(0)))
							{
								this.sendChat(player, fpcName, chatHolder.getAnswers().get(Rnd.get(chatHolder.getAnswers().size())));
							}
							break;
						case "STARTS_WITH":
							if (text.startsWith(chatHolder.getSearchText().get(0)))
							{
								this.sendChat(player, fpcName, chatHolder.getAnswers().get(Rnd.get(chatHolder.getAnswers().size())));
							}
							break;
						case "CONTAINS":
							boolean allFound = true;

							for (String word : chatHolder.getSearchText())
							{
								if (!text.contains(word))
								{
									allFound = false;
								}
							}

							if (allFound)
							{
								this.sendChat(player, fpcName, chatHolder.getAnswers().get(Rnd.get(chatHolder.getAnswers().size())));
							}
					}
				}
			}
		}
	}

	public void sendChat(Player player, String fpcName, String message)
	{
		Spawn spawn = SpawnTable.getInstance().getAnySpawn(FakePlayerData.getInstance().getNpcIdByName(fpcName));
		if (spawn != null)
		{
			Npc npc = spawn.getLastSpawn();
			if (npc != null)
			{
				player.sendPacket(new CreatureSay(npc, ChatType.WHISPER, fpcName, message));
			}
		}
	}

	public static FakePlayerChatManager getInstance()
	{
		return FakePlayerChatManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final FakePlayerChatManager INSTANCE = new FakePlayerChatManager();
	}
}
