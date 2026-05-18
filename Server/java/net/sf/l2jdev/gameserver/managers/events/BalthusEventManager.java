package net.sf.l2jdev.gameserver.managers.events;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.managers.MailManager;
import net.sf.l2jdev.gameserver.model.Message;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemChanceHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.Mail;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.MailType;
import net.sf.l2jdev.gameserver.network.serverpackets.ExItemAnnounce;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.balthusevent.ExBalthusEvent;
import net.sf.l2jdev.gameserver.network.serverpackets.balthusevent.ExBalthusEventJackpotUser;

public class BalthusEventManager
{
	private static final Logger LOGGER = Logger.getLogger(BalthusEventManager.class.getName());
	private final Set<Player> _players = ConcurrentHashMap.newKeySet();
	private final Map<Entry<Integer, Integer>, Collection<BalthusEventManager.BalthusItemHolder>> _rewards = new HashMap<>();
	private BalthusEventManager.BalthusItemHolder _rewardItem = null;
	private boolean _isRunning = false;
	private boolean _participationRewardOnRedeem = false;
	private int _minimumLevel = 65;
	private int _maxRollPerHour = 5;
	private boolean _announceWinnerByExItemAnnounce = true;
	private boolean _useSystemMessageToAnnounce = true;
	private boolean _showAnnounceWithName = true;
	private ItemHolder _consolation = new ItemHolder(49783, 100L);
	private ItemHolder _dailySupplyItem = new ItemHolder(49782, 1L);
	private ItemHolder _dailySupplyFeeItem = new ItemHolder(57, 1L);
	private Entry<Integer, Integer> _winnerCount = new SimpleEntry<>(1, 1);
	private boolean _participationRewardToWinner = false;
	private Map<String, Entry<String, String>> _mail = new HashMap<>();
	private final AtomicInteger _currentRoll = new AtomicInteger(0);
	private int _currProgress = -1;
	private long[] _timeForRoll = null;
	private ScheduledFuture<?> _cycleConcurrentScheduling = null;
	private Date _eventStartDate;
	private Date _eventEndDate;

	public void init()
	{
		Date currentDate = new Date();
		if (this.isValidPeriod() && !currentDate.before(this._eventStartDate) && !currentDate.after(this._eventEndDate))
		{
			Calendar calendar = Calendar.getInstance();
			calendar.set(12, 0);
			calendar.set(13, 0);
			this.initRollTime(calendar.getTimeInMillis());
			if (this._currentRoll.get() > this._maxRollPerHour)
			{
				calendar.add(11, 1);
				this.initRollTime(calendar.getTimeInMillis());
			}

			this.rollNextReward();
			this.initNextRoll();
			this._isRunning = true;
		}
		else
		{
			if (!currentDate.after(this._eventEndDate))
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": Balthus event cannot be started because the event period is not valid. Trying to create a thread to run the event;");
				long delay = this._eventStartDate.getTime() - System.currentTimeMillis();
				if (delay > 0L)
				{
					if (this._cycleConcurrentScheduling != null)
					{
						this._cycleConcurrentScheduling.cancel(true);
					}

					this._cycleConcurrentScheduling = ThreadPool.schedule(this::init, delay);
				}
				else
				{
					LOGGER.warning(this.getClass().getSimpleName() + ": Balthus event start time has already passed. Event cannot be started.");
				}
			}
		}
	}

	public void initParameters(StatSet params)
	{
		this._participationRewardOnRedeem = params.getBoolean("give_coin_only_if_redeem", true);
		this._minimumLevel = params.getInt("minLevel", 65);
		this._maxRollPerHour = Math.min(40, Math.max(1, params.getInt("try_to_roll_per_hour", 5)));
		this._announceWinnerByExItemAnnounce = params.getBoolean("announce_by_ex_item_announce", true);
		this._useSystemMessageToAnnounce = params.getBoolean("announce_by_system_message", true);
		this._showAnnounceWithName = params.getBoolean("show_name_in_announce", true);
		this._consolation = new ItemHolder(params.getInt("coin_id", 49783), params.getLong("coin_count", 100L));
		this._dailySupplyItem = params.getBoolean("daily_supply_enabled", true) ? new ItemHolder(params.getInt("daily_supply_id", 49782), params.getLong("daily_supply_count", 1L)) : null;
		this._dailySupplyFeeItem = params.getBoolean("daily_supply_fee_enabled", true) ? new ItemHolder(params.getInt("daily_supply_fee_id", 57), params.getLong("daily_supply_fee_count", 1L)) : null;
		this._winnerCount = new SimpleEntry<>(params.getInt("min_winner", 1), params.getInt("max_winner", 1));
		this._participationRewardToWinner = params.getBoolean("give_participation_reward_to_winner", false);
		Map<String, Entry<String, String>> mailContent = new HashMap<>();
		Set<String> availableLang = new HashSet<>();
		Map<String, String> subject = new HashMap<>();
		Map<String, String> content = new HashMap<>();

		for (Entry<String, Object> entry : params.getSet().entrySet())
		{
			String key = entry.getKey();
			if (key.startsWith("mailSubject_"))
			{
				String value = String.valueOf(entry.getValue());
				String lang = key.substring("mailSubject_".length());
				subject.put(lang, value);
				availableLang.add(lang);
			}

			if (key.startsWith("mailContent_"))
			{
				String value = String.valueOf(entry.getValue());
				String lang = key.substring("mailContent_".length());
				content.put(lang, value);
				availableLang.add(lang);
			}
		}

		for (String lang : availableLang)
		{
			String subject2 = subject.getOrDefault(lang, "");
			String content2 = content.getOrDefault(lang, "");
			mailContent.put(lang, new SimpleEntry<>(subject2, content2));
		}

		this._mail = mailContent;
	}

	public void setEventPeriod(Date startDate, Date endDate)
	{
		this._eventStartDate = startDate;
		this._eventEndDate = endDate;
	}

	public Date getEventStartDate()
	{
		return this._eventStartDate;
	}

	public Date getEventEndDate()
	{
		return this._eventEndDate;
	}

	private boolean isValidPeriod()
	{
		return this._eventStartDate != null && this._eventEndDate != null && this._eventStartDate.before(this._eventEndDate);
	}

	public void addRewards(Entry<Integer, Integer> period, int itemId, long itemCount, double chanceToObtain, int enchantLevel, double chanceToNextGame, boolean redeemInAnyCase)
	{
		this._rewards.computeIfAbsent(period, _ -> new ArrayList<>()).add(new BalthusEventManager.BalthusItemHolder(itemId, itemCount, chanceToObtain, enchantLevel, chanceToNextGame, redeemInAnyCase));
	}

	private void initRollTime(long millis)
	{
		long currentTime = System.currentTimeMillis();
		this._timeForRoll = this.calculateTimeForRolls(millis);

		for (int attempt = 1; attempt <= this._maxRollPerHour; attempt++)
		{
			int rollIndex = this._currentRoll.incrementAndGet();
			if (this._timeForRoll[rollIndex] > currentTime)
			{
				break;
			}
		}
	}

	private void initNextRoll()
	{
		if (!this.isValidPeriod())
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Event period is not valid. Event end.");
		}
		else
		{
			if (this._currentRoll.get() > this._maxRollPerHour)
			{
				this.rollNextReward();
				this._isRunning = true;
				this._currentRoll.set(0);
				this._timeForRoll = this.calculateTimeForRolls(System.currentTimeMillis());
				this._currProgress = -1;
			}

			this._currentRoll.addAndGet(1);
			long timeForRoll = Math.max(1L, this._timeForRoll[this._currentRoll.get()] - System.currentTimeMillis());
			if (this._cycleConcurrentScheduling != null)
			{
				this._cycleConcurrentScheduling.cancel(true);
			}

			this._cycleConcurrentScheduling = ThreadPool.schedule(this::redeemReward, timeForRoll);

			for (Player player : World.getInstance().getPlayers())
			{
				if (player != null && !player.isInOfflineMode() && player.getClient() != null)
				{
					player.sendPacket(new ExBalthusEvent(player));
				}
			}
		}
	}

	private void redeemReward()
	{
		if (!this._isRunning)
		{
			this.initNextRoll();
		}
		else
		{
			if (this._rewardItem == null)
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": No reward, careful!");
			}

			if ((!this._rewardItem.isRedeemInAnyCase() || this._currentRoll.get() < this._maxRollPerHour) && this._rewardItem.getChance() <= 2.0)
			{
				this.initNextRoll();
			}
			else
			{
				Set<Player> winners = this.selectWinnerFromParticipators();
				Item itemToWinner = winners != null ? this.sendMessageToWinner(winners) : null;
				if (winners != null)
				{
					for (Player winner : winners)
					{
						LOGGER.warning(this.getClass().getSimpleName() + ": " + winner + " win " + itemToWinner + " in hour " + Calendar.getInstance().get(11));
					}
				}

				if (winners != null || this._currentRoll.get() >= this._maxRollPerHour)
				{
					this._currProgress = this._currentRoll.get();
					this._isRunning = false;
					this._currentRoll.set(this._maxRollPerHour + 1);
					this.sendParticipatorsCoin(winners, itemToWinner);
					if (this._cycleConcurrentScheduling != null)
					{
						this._cycleConcurrentScheduling.cancel(true);
					}

					this._cycleConcurrentScheduling = ThreadPool.schedule(this::initNextRoll, Math.max(1L, this._timeForRoll[this._maxRollPerHour + 1] - System.currentTimeMillis()));
				}
			}
		}
	}

	private Set<Player> selectWinnerFromParticipators()
	{
		int winnersCount = Math.min(Rnd.get(this._winnerCount.getKey(), this._winnerCount.getValue()), this._players.size());
		List<Player> participators = new ArrayList<>(this._players);
		Collections.shuffle(participators);
		Set<Player> winners = winnersCount <= 0 ? null : new HashSet<>();

		for (Player participator : participators)
		{
			if (winners == null || winnersCount <= winners.size())
			{
				break;
			}

			if (participator != null && !participator.isInOfflineMode() && participator.getClient() != null && participator.getLevel() >= this._minimumLevel)
			{
				winners.add(participator);
			}
		}

		return winners;
	}

	private void sendParticipatorsCoin(Set<Player> winners, Item itemToWinner)
	{
		Set<ServerPacket> packetsForSend = new HashSet<>();
		if (winners != null)
		{
			for (Player winner : winners)
			{
				if (this._announceWinnerByExItemAnnounce)
				{
					packetsForSend.add(new ExItemAnnounce(winner, itemToWinner, 5));
				}

				if (this._useSystemMessageToAnnounce)
				{
					SystemMessage messageAnnounce = null;
					if (this._showAnnounceWithName)
					{
						messageAnnounce = new SystemMessage(SystemMessageId.BALTHUS_KNIGHTS_HAVE_GIVEN_THE_GRAND_PRIZE_AWAY_S2_THE_WINNER_S1);
						messageAnnounce.addPcName(winner);
						messageAnnounce.addItemName(itemToWinner);
					}
					else
					{
						messageAnnounce = new SystemMessage(SystemMessageId.THE_SECRET_SUPPLIES_OF_THE_BALTHUS_KNIGHTS_ARRIVED_SOMEONE_RECEIVED_S1);
						messageAnnounce.addItemName(itemToWinner);
					}

					packetsForSend.add(messageAnnounce);
				}
			}
		}

		if (!this._participationRewardOnRedeem || winners != null)
		{
			for (Player player : World.getInstance().getPlayers())
			{
				if (player != null && !player.isInOfflineMode() && player.getClient() != null)
				{
					if (winners != null)
					{
						player.sendPacket(new ExBalthusEventJackpotUser());
					}

					for (ServerPacket packet : packetsForSend)
					{
						player.sendPacket(packet);
					}

					if (this._players.contains(player) && (winners == null || !winners.contains(player) || this._participationRewardToWinner))
					{
						player.getVariables().increaseLong("BALTHUS_REWARD", 0L, this._consolation.getCount());
						player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_SIBI_S_COIN_X_S1).addInt((int) this._consolation.getCount()));
					}

					player.sendPacket(new ExBalthusEvent(player));
				}
			}
		}
	}

	private Item sendMessageToWinner(Set<Player> winners)
	{
		Item returnItem = null;

		for (Player winner : winners)
		{
			String lang = winner.getLang();
			String subject = lang != null && this._mail.containsKey(lang) ? this._mail.get(lang).getKey() : this._mail.get("en").getKey();
			String content = lang != null && this._mail.containsKey(lang) ? this._mail.get(lang).getValue() : this._mail.get("en").getValue();
			Message msg = new Message(winner.getObjectId(), subject, content, MailType.NEWS_INFORMER);
			Mail attachments = msg.createAttachments();
			returnItem = attachments.addItem(ItemProcessType.REWARD, this._rewardItem.getId(), this._rewardItem.getCount(), null, null);
			if (returnItem != null)
			{
				returnItem.setEnchantLevel(this._rewardItem.getEnchantmentLevel());
			}

			MailManager.getInstance().sendMessage(msg);
		}

		return returnItem;
	}

	public void rollNextReward()
	{
		int hour = Calendar.getInstance().get(11);

		label34:
		for (Entry<Entry<Integer, Integer>, Collection<BalthusEventManager.BalthusItemHolder>> entry : this._rewards.entrySet())
		{
			Entry<Integer, Integer> period = entry.getKey();
			if (period.getKey() <= hour && period.getValue() >= hour)
			{
				Collection<BalthusEventManager.BalthusItemHolder> value = entry.getValue();
				double rnd = Rnd.get(0.0, 100.0);
				double val = 0.0;

				for (BalthusEventManager.BalthusItemHolder reward : value)
				{
					val += reward.getChanceToNextGame();
					if (rnd <= val)
					{
						this._rewardItem = reward;
						break label34;
					}
				}
				break;
			}
		}

		if (this._rewardItem == null)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": No reward, careful!");
		}
		else
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Reward for next " + hour + " hour " + this._rewardItem.toString());
		}
	}

	private long[] calculateTimeForRolls(long requestedTime)
	{
		long[] rollTime = new long[this._maxRollPerHour + 2];
		rollTime[0] = requestedTime;

		for (int i = 1; i <= this._maxRollPerHour; i++)
		{
			rollTime[i] = rollTime[i - 1] + Rnd.get(TimeUnit.MINUTES.toMillis(1L), TimeUnit.MINUTES.toMillis(60 / this._maxRollPerHour));
		}

		rollTime[this._maxRollPerHour + 1] = rollTime[0] + TimeUnit.MINUTES.toMillis(60L);
		return rollTime;
	}

	public void addPlayerToList(Player player)
	{
		this._players.add(player);
		player.sendPacket(new ExBalthusEvent(player));
	}

	public void removePlayerFromList(Player player)
	{
		this._players.remove(player);
		player.sendPacket(new ExBalthusEvent(player));
	}

	public int getMinimumLevel()
	{
		return this._minimumLevel;
	}

	public ItemHolder getConsolation()
	{
		return this._consolation;
	}

	public ItemHolder getDailySupplyItem()
	{
		return this._dailySupplyItem;
	}

	public ItemHolder getDailySupplyFeeItem()
	{
		return this._dailySupplyFeeItem;
	}

	public ItemHolder getCurrRewardItem()
	{
		return this._rewardItem;
	}

	public int getCurrentProgress()
	{
		return this._currProgress == -1 ? this._currentRoll.get() : this._currProgress;
	}

	public boolean isPlayerParticipant(Player player)
	{
		return this._players.contains(player) && player.getLevel() > this._minimumLevel;
	}

	public boolean isRunning()
	{
		return this._isRunning;
	}

	public static BalthusEventManager getInstance()
	{
		return BalthusEventManager.SingletonHolder.INSTANCE;
	}

	private static class BalthusItemHolder extends ItemChanceHolder
	{
		final double _chanceToNextGame;
		final boolean _redeemInAnyCase;

		public BalthusItemHolder(int itemId, long itemCount, double chanceToObtain, int enchantLevel, double chanceToNextGame, boolean redeemInAnyCase)
		{
			super(itemId, chanceToObtain, itemCount, (byte) enchantLevel);
			this._chanceToNextGame = chanceToNextGame;
			this._redeemInAnyCase = redeemInAnyCase;
		}

		public double getChanceToNextGame()
		{
			return this._chanceToNextGame;
		}

		public boolean isRedeemInAnyCase()
		{
			return this._redeemInAnyCase;
		}
	}

	private static class SingletonHolder
	{
		protected static final BalthusEventManager INSTANCE = new BalthusEventManager();
	}
}
