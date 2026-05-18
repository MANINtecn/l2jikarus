package net.sf.l2jdev.gameserver.managers.events;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.managers.MailManager;
import net.sf.l2jdev.gameserver.model.Message;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.Mail;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.MailType;
import net.sf.l2jdev.gameserver.network.serverpackets.ExItemAnnounce;
import net.sf.l2jdev.gameserver.network.serverpackets.secretshop.ExFestivalBmAllItemInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.secretshop.ExFestivalBmGame;
import net.sf.l2jdev.gameserver.network.serverpackets.secretshop.ExFestivalBmInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.secretshop.ExFestivalBmTopItemInfo;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class SecretShopEventManager
{
	private static final Logger LOGGER = Logger.getLogger(SecretShopEventManager.class.getName());
	public static final int UPDATE_INTERVAL = 5000;
	public static final int CLAN_REWARD_ITEM_ID = 94834;
	public static final int CLAN_REWARD_ITEM_COUNT = 1000;
	public static final int TICKET_AMOUNT_PER_GAME = 1;
	private static final ConcurrentHashMap<Player, SecretShopEventManager.PlayerTicketData> PLAYER_REWARD_QUEUES = new ConcurrentHashMap<>();
	private static final Set<Player> LIST_TO_UPDATE = ConcurrentHashMap.newKeySet();
	private static Map<Integer, List<SecretShopEventManager.SecretShopRewardHolder>> _rewardData = new HashMap<>();
	private static List<SecretShopEventManager.SecretShopRewardHolder> _activeRewards;
	private static boolean _isEventPeriod;
	private static boolean _exchangeEnabled;
	private static long _startTime;
	private static long _endTime;
	private static int _ticketId;
	private static int _startHour;
	private static int _startMinute;
	private static int _endHour;
	private static int _endMinute;
	private ScheduledFuture<?> _updateTask = null;
	private ScheduledFuture<?> _startTask = null;
	private ScheduledFuture<?> _endTask = null;

	protected SecretShopEventManager()
	{
	}

	public void init(Map<Integer, List<SecretShopEventManager.SecretShopRewardHolder>> rewardData, int ticketId, int startHour, int startMinute, int endHour, int endMinute)
	{
		_rewardData = rewardData;
		_ticketId = ticketId;
		_startHour = startHour;
		_startMinute = startMinute;
		_endHour = endHour;
		_endMinute = endMinute;
	}

	private void start()
	{
		if (_isEventPeriod)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Started!");
			_exchangeEnabled = true;
			_endTime = this.getNextEndTime();
			_startTime = 0L;
			this.initRewards(Calendar.getInstance().get(7));
			this.broadcastInfo();
			if (this._updateTask != null)
			{
				this._updateTask.cancel(true);
				this._updateTask = null;
			}

			this._updateTask = ThreadPool.scheduleAtFixedRate(this::updateInfo, 500L, 5000L);
			if (this._endTask != null)
			{
				this._endTask.cancel(false);
				this._endTask = null;
			}

			LOGGER.warning(this.getClass().getSimpleName() + ": Scheduled end in " + _endTime + " ms.");
			ThreadPool.schedule(this::stop, _endTime);
		}
	}

	private void stop()
	{
		_exchangeEnabled = false;
		_startTime = this.getNextStartTime();
		_endTime = 0L;

		for (SecretShopEventManager.SecretShopRewardHolder reward : _activeRewards)
		{
			reward._currentAmount = 0L;
		}

		if (this._updateTask != null)
		{
			this._updateTask.cancel(true);
			this._updateTask = null;
		}

		if (this._endTask != null)
		{
			this._endTask.cancel(false);
			this._endTask = null;
		}

		this.updateInfo();
		LIST_TO_UPDATE.clear();
		this.broadcastInfo();
		LOGGER.warning(this.getClass().getSimpleName() + ": Scheduled start in " + _startTime + " ms.");
		ThreadPool.schedule(this::start, _startTime);
	}

	public void startEvent()
	{
		if (!_rewardData.isEmpty())
		{
			_isEventPeriod = true;
			LOGGER.warning(this.getClass().getSimpleName() + ": Activated");
			if (this._startTask != null)
			{
				this._startTask.cancel(false);
				this._startTask = null;
			}

			if (this._endTask != null)
			{
				this._endTask.cancel(false);
				this._endTask = null;
			}

			if (this._updateTask != null)
			{
				this._updateTask.cancel(true);
				this._updateTask = null;
			}

			LIST_TO_UPDATE.clear();
			if (this.isExchangePeriod())
			{
				this.start();
			}
			else
			{
				_startTime = this.getNextStartTime();
				LOGGER.warning(this.getClass().getSimpleName() + ": Scheduled start in " + _startTime + " ms.");
				this._startTask = ThreadPool.schedule(this::start, _startTime);
			}
		}
	}

	public void stopEvent()
	{
		_isEventPeriod = false;
	}

	public synchronized void exchange(Player player)
	{
		if (_isEventPeriod && this.hasAvailableRewards())
		{
			int ticketAmount = (int) player.getInventory().getInventoryItemCount(_ticketId, -1);
			if (ticketAmount >= 1)
			{
				double totalChance = 0.0;
				double totalPossibleChance = 0.0;

				for (SecretShopEventManager.SecretShopRewardHolder reward : _activeRewards)
				{
					if (reward._currentAmount > 0L)
					{
						totalPossibleChance += reward._chance;
					}
				}

				double chance = Rnd.get(0.0, totalPossibleChance);

				for (SecretShopEventManager.SecretShopRewardHolder rewardx : _activeRewards)
				{
					if (rewardx._currentAmount > 0L)
					{
						totalChance += rewardx._chance;
						if (totalChance >= chance)
						{
							rewardx._currentAmount--;
							player.destroyItemByItemId(ItemProcessType.FEE, _ticketId, 1L, player, true);
							ticketAmount--;
							Item item = player.addItem(ItemProcessType.REWARD, rewardx._id, rewardx._count, player, true);
							player.sendPacket(new ExFestivalBmGame(_ticketId, ticketAmount, 1, rewardx._grade, rewardx._id, (int) rewardx._count, 1));
							if (rewardx.isTopGrade())
							{
								Broadcast.toAllOnlinePlayers(new ExItemAnnounce(player, item, 5));
								this.sendClanReward(player);
								if (this.noTopGradeRewardsRemaining())
								{
									this.show(player, true);
									this.updateInfo();
									this.stop();
									return;
								}

								this.broadcastInfo();
							}
							break;
						}
					}
				}

				this.show(player, true);
			}
		}
	}

	public void show(Player player, boolean show)
	{
		if (_isEventPeriod)
		{
			if (show)
			{
				if (!LIST_TO_UPDATE.contains(player))
				{
					LIST_TO_UPDATE.add(player);
				}

				int ticketAmount = (int) player.getInventory().getInventoryItemCount(_ticketId, -1);
				player.sendPacket(new ExFestivalBmInfo(_ticketId, ticketAmount, 1));
				player.sendPacket(new ExFestivalBmAllItemInfo(this.getSortedRewards()));
			}
			else
			{
				LIST_TO_UPDATE.remove(player);
			}
		}
	}

	private void sendClanReward(Player player)
	{
		if (player != null)
		{
			Clan clan = player.getClan();
			if (clan == null)
			{
				return;
			}

			LOGGER.warning(this.getClass().getSimpleName() + ": Give Clan Reward->: clan=" + clan + " player=" + player);
			clan.getOnlineMembers(player.getObjectId()).forEach(this::sendMail);
		}
		else
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Player is Null!");
		}
	}

	private void sendMail(Player player)
	{
		Message message = new Message(player.getObjectId(), "Secret Shop Clan Reward", "Your clan member won one of the main prize of the event. All (online) clan members receive an additional reward.", MailType.REGULAR);
		Mail attachment = message.createAttachments();
		attachment.addItem(ItemProcessType.REWARD, 94834, 1000L, null, null);
		MailManager.getInstance().sendMessage(message);
	}

	private void initRewards(int currentGame)
	{
		_activeRewards = new ArrayList<>();
		List<SecretShopEventManager.SecretShopRewardHolder> topRewards = new ArrayList<>();
		List<SecretShopEventManager.SecretShopRewardHolder> rewards = new ArrayList<>();

		for (SecretShopEventManager.SecretShopRewardHolder eventReward : _rewardData.get(currentGame))
		{
			SecretShopEventManager.SecretShopRewardHolder reward = new SecretShopEventManager.SecretShopRewardHolder(eventReward);
			switch (reward._grade)
			{
				case 1:
					topRewards.add(reward);
					break;
				case 2:
				case 3:
					rewards.add(reward);
					break;
				default:
					LOGGER.warning(this.getClass().getSimpleName() + ": Incorrect reward grade " + reward._grade);
			}
		}

		_activeRewards.clear();
		Collections.shuffle(topRewards);
		_activeRewards.addAll(topRewards.stream().limit(3L).collect(Collectors.toList()));
		_activeRewards.addAll(rewards);
	}

	private void updateInfo()
	{
		for (Player player : LIST_TO_UPDATE)
		{
			GameClient client = player.getClient();
			if (player.isOnline() && client != null && !client.isDetached())
			{
				this.sendInfo(player);
				player.sendPacket(new ExFestivalBmAllItemInfo(this.getSortedRewards()));
			}
			else
			{
				LIST_TO_UPDATE.remove(player);
			}
		}
	}

	public void sendInfo(Player player)
	{
		if (_isEventPeriod && player != null)
		{
			player.sendPacket(new ExFestivalBmTopItemInfo(_exchangeEnabled ? this.getNextEndTime() : this.getNextStartTime(), _exchangeEnabled, _activeRewards));
		}
	}

	private void broadcastInfo()
	{
		if (_isEventPeriod)
		{
			Broadcast.toAllOnlinePlayers(new ExFestivalBmTopItemInfo(_exchangeEnabled ? this.getNextEndTime() : this.getNextStartTime(), _exchangeEnabled, _activeRewards));
		}
	}

	public List<SecretShopEventManager.SecretShopRewardHolder> getSortedRewards()
	{
		List<SecretShopEventManager.SecretShopRewardHolder> showRewards = new ArrayList<>(_activeRewards);
		showRewards.sort(Comparator.comparing(SecretShopEventManager.SecretShopRewardHolder::getId));
		return showRewards;
	}

	public long getNextStartTime()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(11, _startHour);
		calendar.set(12, _startMinute);
		calendar.set(13, 0);
		calendar.set(14, 0);
		if (System.currentTimeMillis() > calendar.getTimeInMillis())
		{
			calendar.add(5, 1);
		}

		return Math.max(0L, calendar.getTimeInMillis() - System.currentTimeMillis());
	}

	public long getNextEndTime()
	{
		if (!_exchangeEnabled)
		{
			return 0L;
		}
		Calendar endTime = Calendar.getInstance();
		endTime.set(11, _endHour);
		endTime.set(12, _endMinute);
		endTime.set(13, 0);
		endTime.set(14, 0);
		return Math.max(0L, endTime.getTimeInMillis() - System.currentTimeMillis());
	}

	public boolean isExchangePeriod()
	{
		if (!_isEventPeriod)
		{
			return false;
		}
		Calendar calendar = Calendar.getInstance();
		int currentHour = calendar.get(11);
		int currentMinute = calendar.get(12);
		return (currentHour > _startHour || currentHour == _startHour && currentMinute >= _startMinute) && (currentHour < _endHour || currentHour == _endHour && currentMinute < _endMinute);
	}

	public boolean hasAvailableRewards()
	{
		return _activeRewards.stream().anyMatch(SecretShopEventManager.SecretShopRewardHolder::hasAvailableReward);
	}

	public boolean noTopGradeRewardsRemaining()
	{
		for (SecretShopEventManager.SecretShopRewardHolder reward : _activeRewards)
		{
			if (reward.isTopGrade() && reward._currentAmount > 0L)
			{
				return false;
			}
		}

		return true;
	}

	public boolean isEventPeriod()
	{
		return _isEventPeriod;
	}

	public void addTickets(Player player)
	{
		PLAYER_REWARD_QUEUES.computeIfAbsent(player, _ -> new SecretShopEventManager.PlayerTicketData());
		SecretShopEventManager.PlayerTicketData playerData = PLAYER_REWARD_QUEUES.get(player);
		Queue<SecretShopEventManager.TicketTask> queue = playerData.getQueue();
		queue.add(new SecretShopEventManager.TicketTask(player));
		if (playerData.getProcessingFlag().compareAndSet(false, true))
		{
			this.processPlayerQueue(playerData);
		}
	}

	private void processPlayerQueue(SecretShopEventManager.PlayerTicketData playerData)
	{
		ThreadPool.execute(() -> {
			Queue<SecretShopEventManager.TicketTask> queue = playerData.getQueue();

			while (!queue.isEmpty())
			{
				SecretShopEventManager.TicketTask task = queue.poll();
				if (task != null)
				{
					if (!task.getPlayer().isInventoryUnder80(false))
					{
						task.getPlayer().sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
						continue;
					}

					this.exchange(task.getPlayer());
				}

				try
				{
					Thread.sleep(100L);
				}
				catch (InterruptedException var5)
				{
					Thread.currentThread().interrupt();
					break;
				}
			}

			playerData.getProcessingFlag().set(false);
		});
	}

	public static SecretShopEventManager getInstance()
	{
		return SecretShopEventManager.SingletonHolder.INSTANCE;
	}

	private static class PlayerTicketData
	{
		private final ConcurrentLinkedQueue<SecretShopEventManager.TicketTask> _queue = new ConcurrentLinkedQueue<>();
		private final AtomicBoolean _processingFlag = new AtomicBoolean();

		public Queue<SecretShopEventManager.TicketTask> getQueue()
		{
			return this._queue;
		}

		public AtomicBoolean getProcessingFlag()
		{
			return this._processingFlag;
		}
	}

	public static class SecretShopRewardHolder
	{
		public static final int TOP_GRADE = 1;
		public static final int MIDDLE_GRADE = 2;
		public static final int LOW_GRADE = 3;
		private final int _id;
		private final long _count;
		private final int _grade;
		private final long _totalAmount;
		private long _currentAmount;
		private final double _chance;

		public SecretShopRewardHolder(int id, long count, int grade, long totalAmount, long currentAmount, double chance)
		{
			this._id = id;
			this._count = count;
			this._grade = grade;
			this._totalAmount = totalAmount;
			this._currentAmount = currentAmount;
			this._chance = chance;
		}

		public SecretShopRewardHolder(SecretShopEventManager.SecretShopRewardHolder reward)
		{
			this._id = reward._id;
			this._count = reward._count;
			this._grade = reward._grade;
			this._totalAmount = reward._totalAmount;
			this._currentAmount = reward._currentAmount;
			this._chance = reward._chance;
		}

		public int getId()
		{
			return this._id;
		}

		public long getCount()
		{
			return this._count;
		}

		public int getGrade()
		{
			return this._grade;
		}

		public long getTotalAmount()
		{
			return this._totalAmount;
		}

		public long getCurrentAmount()
		{
			return this._currentAmount;
		}

		public double getChance()
		{
			return this._chance;
		}

		public boolean hasAvailableReward()
		{
			return this._totalAmount > 0L;
		}

		public boolean isTopGrade()
		{
			return this._grade == 1;
		}

		public boolean isMiddleGrade()
		{
			return this._grade == 2;
		}

		public boolean isLowGrade()
		{
			return this._grade == 3;
		}
	}

	private static class SingletonHolder
	{
		protected static final SecretShopEventManager INSTANCE = new SecretShopEventManager();
	}

	private static class TicketTask
	{
		private final Player _player;

		public TicketTask(Player player)
		{
			this._player = player;
		}

		public Player getPlayer()
		{
			return this._player;
		}
	}
}
