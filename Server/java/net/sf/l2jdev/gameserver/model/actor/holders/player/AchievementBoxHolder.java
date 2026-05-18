package net.sf.l2jdev.gameserver.model.actor.holders.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.commons.util.TraceUtil;
import net.sf.l2jdev.gameserver.config.AchievementBoxConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.AchievementBoxStateType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.AchievementBoxType;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.serverpackets.achievementbox.ExSteadyAllBoxUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.achievementbox.ExSteadyBoxReward;

public class AchievementBoxHolder
{
	private static final Logger LOGGER = Logger.getLogger(AchievementBoxHolder.class.getName());
	public static final int ACHIEVEMENT_BOX_2H = 7200000;
	public static final int ACHIEVEMENT_BOX_6H = 21600000;
	public static final int ACHIEVEMENT_BOX_12H = 43200000;
	private final Player _owner;
	private int _boxOwned = 1;
	private int _monsterPoints = 0;
	private int _pvpPoints = 0;
	private int _pendingBoxSlotId = 0;
	private int _pvpEndDate;
	private long _boxTimeForOpen;
	private final List<AchievementBoxInfoHolder> _achievementBox = new ArrayList<>();
	private ScheduledFuture<?> _boxOpenTask;

	public AchievementBoxHolder(Player owner)
	{
		this._owner = owner;
	}

	public int pvpEndDate()
	{
		return this._pvpEndDate;
	}

	public void addPoints(int value)
	{
		int newPoints = Math.min(AchievementBoxConfig.ACHIEVEMENT_BOX_POINTS_FOR_REWARD, this._monsterPoints + value);
		if (newPoints >= AchievementBoxConfig.ACHIEVEMENT_BOX_POINTS_FOR_REWARD)
		{
			if (this.addNewBox())
			{
				this._monsterPoints = 0;
			}
			else
			{
				this._monsterPoints = AchievementBoxConfig.ACHIEVEMENT_BOX_POINTS_FOR_REWARD;
			}
		}
		else
		{
			this._monsterPoints += value;
		}
	}

	public void addPvpPoints(int value)
	{
		int newPoints = Math.min(AchievementBoxConfig.ACHIEVEMENT_BOX_PVP_POINTS_FOR_REWARD, this._pvpPoints);
		if (newPoints >= AchievementBoxConfig.ACHIEVEMENT_BOX_PVP_POINTS_FOR_REWARD)
		{
			if (this.addNewBox())
			{
				this._pvpPoints = 0;
			}
			else
			{
				this._pvpPoints = AchievementBoxConfig.ACHIEVEMENT_BOX_PVP_POINTS_FOR_REWARD;
			}
		}
		else
		{
			this._pvpPoints += value;
		}
	}

	public void restore()
	{
		this.tryFinishBox();
		this.refreshPvpEndDate();

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM achievement_box WHERE charId=?");)
		{
			ps.setInt(1, this._owner.getObjectId());

			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					try
					{
						this._boxOwned = rs.getInt("box_owned");
						this._monsterPoints = rs.getInt("monster_point");
						this._pvpPoints = rs.getInt("pvp_point");
						this._pendingBoxSlotId = rs.getInt("pending_box");
						this._boxTimeForOpen = rs.getLong("open_time");

						for (int i = 1; i <= 4; i++)
						{
							int state = rs.getInt("box_state_slot_" + i);
							int type = rs.getInt("boxtype_slot_" + i);
							if (i == 1 && state == 0)
							{
								state = 1;
							}

							AchievementBoxInfoHolder holder = new AchievementBoxInfoHolder(i, state, type);
							this._achievementBox.add(i - 1, holder);
						}
					}
					catch (Exception var11)
					{
						LOGGER.warning("Could not restore Achievement box for " + this._owner);
					}
				}
				else
				{
					this.storeNew();
					this._achievementBox.add(0, new AchievementBoxInfoHolder(1, 1, 0));
				}
			}
		}
		catch (Exception var15)
		{
			LOGGER.log(Level.WARNING, "Could not restore achievement box for " + this._owner, var15);
		}
	}

	public void storeNew()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO achievement_box VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");)
		{
			ps.setInt(1, this._owner.getObjectId());
			ps.setInt(2, this._boxOwned);
			ps.setInt(3, this._monsterPoints);
			ps.setInt(4, this._pvpPoints);
			ps.setInt(5, this._pendingBoxSlotId);
			ps.setLong(6, this._boxTimeForOpen);

			for (int i = 0; i < 4; i++)
			{
				ps.setInt(7 + i * 2, 0);
				ps.setInt(8 + i * 2, 0);
			}

			ps.executeUpdate();
		}
		catch (SQLException var9)
		{
			LOGGER.warning("Could not store new Archivement Box for: " + this._owner);
			LOGGER.warning(TraceUtil.getStackTrace(var9));
		}
	}

	public void store()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE achievement_box SET box_owned=?,monster_point=?,pvp_point=?,pending_box=?,open_time=?,box_state_slot_1=?,boxtype_slot_1=?,box_state_slot_2=?,boxtype_slot_2=?,box_state_slot_3=?,boxtype_slot_3=?,box_state_slot_4=?,boxtype_slot_4=? WHERE charId=?");)
		{
			ps.setInt(1, this.getBoxOwned());
			ps.setInt(2, this.getMonsterPoints());
			ps.setInt(3, this.getPvpPoints());
			ps.setInt(4, this.getPendingBoxSlotId());
			ps.setLong(5, this.getBoxOpenTime());

			for (int i = 0; i < 4; i++)
			{
				if (this._achievementBox.size() >= i + 1)
				{
					AchievementBoxInfoHolder holder = this._achievementBox.get(i);
					ps.setInt(6 + i * 2, holder == null ? 0 : holder.getState().ordinal());
					ps.setInt(7 + i * 2, holder == null ? 0 : holder.getType().ordinal());
				}
				else
				{
					ps.setInt(6 + i * 2, 0);
					ps.setInt(7 + i * 2, 0);
				}
			}

			ps.setInt(14, this._owner.getObjectId());
			ps.execute();
		}
		catch (SQLException var9)
		{
			LOGGER.log(Level.SEVERE, "Could not store Achievement Box for: " + this._owner, var9);
		}
	}

	public List<AchievementBoxInfoHolder> getAchievementBox()
	{
		return this._achievementBox;
	}

	public boolean addNewBox()
	{
		AchievementBoxInfoHolder free = null;
		int id = -1;

		for (int i = 1; i <= this.getBoxOwned(); i++)
		{
			AchievementBoxInfoHolder holder = this.getAchievementBox().get(i - 1);
			if (holder.getState() == AchievementBoxStateType.AVAILABLE)
			{
				free = holder;
				id = i;
				break;
			}
		}

		if (free != null)
		{
			int rnd = Rnd.get(0, 100);
			free.setType(rnd < 12 ? AchievementBoxType.BOX_12H : (rnd < 40 ? AchievementBoxType.BOX_6H : AchievementBoxType.BOX_2H));
			switch (free.getType())
			{
				case BOX_2H:
				case BOX_6H:
				case BOX_12H:
					free.setState(AchievementBoxStateType.OPEN);
					this.getAchievementBox().remove(id - 1);
					this.getAchievementBox().add(id - 1, free);
					this.sendBoxUpdate();
				default:
					return true;
			}
		}
		return false;
	}

	public void openBox(int slotId)
	{
		if (slotId <= this.getBoxOwned())
		{
			AchievementBoxInfoHolder holder = this.getAchievementBox().get(slotId - 1);
			if (holder != null && this._boxTimeForOpen == 0L)
			{
				this._pendingBoxSlotId = slotId;
				switch (holder.getType())
				{
					case BOX_2H:
						this.setBoxTimeForOpen(7200000L);
						holder.setState(AchievementBoxStateType.UNLOCK_IN_PROGRESS);
						this.getAchievementBox().remove(slotId - 1);
						this.getAchievementBox().add(slotId - 1, holder);
						this.sendBoxUpdate();
						break;
					case BOX_6H:
						this.setBoxTimeForOpen(21600000L);
						holder.setState(AchievementBoxStateType.UNLOCK_IN_PROGRESS);
						this.getAchievementBox().remove(slotId - 1);
						this.getAchievementBox().add(slotId - 1, holder);
						this.sendBoxUpdate();
						break;
					case BOX_12H:
						this.setBoxTimeForOpen(43200000L);
						holder.setState(AchievementBoxStateType.UNLOCK_IN_PROGRESS);
						this.getAchievementBox().remove(slotId - 1);
						this.getAchievementBox().add(slotId - 1, holder);
						this.sendBoxUpdate();
				}
			}
		}
	}

	public void skipBoxOpenTime(int slotId, long fee)
	{
		if (slotId <= this.getBoxOwned())
		{
			AchievementBoxInfoHolder holder = this.getAchievementBox().get(slotId - 1);
			if (holder != null && this._owner.destroyItemByItemId(ItemProcessType.FEE, 91663, fee, this._owner, true))
			{
				if (this._pendingBoxSlotId == slotId)
				{
					this.cancelTask();
				}

				this.finishAndUnlockChest(slotId);
			}
		}
	}

	public boolean setBoxTimeForOpen(long time)
	{
		if (this._boxOpenTask != null && !this._boxOpenTask.isDone() && !this._boxOpenTask.isCancelled())
		{
			return false;
		}
		this._boxTimeForOpen = System.currentTimeMillis() + time;
		return true;
	}

	public void tryFinishBox()
	{
		if (this._boxTimeForOpen != 0L && this._boxTimeForOpen < System.currentTimeMillis())
		{
			if (this._owner != null && this._owner.isOnline())
			{
				AchievementBoxInfoHolder holder = this.getAchievementBox().get(this._pendingBoxSlotId - 1);
				if (holder != null)
				{
					this.finishAndUnlockChest(this._pendingBoxSlotId);
				}
			}
		}
	}

	public int getBoxOwned()
	{
		return this._boxOwned;
	}

	public int getMonsterPoints()
	{
		return this._monsterPoints;
	}

	public int getPvpPoints()
	{
		return this._pvpPoints;
	}

	public int getPendingBoxSlotId()
	{
		return this._pendingBoxSlotId;
	}

	public long getBoxOpenTime()
	{
		return this._boxTimeForOpen;
	}

	public void finishAndUnlockChest(int id)
	{
		if (id <= this.getBoxOwned())
		{
			if (this._pendingBoxSlotId == id)
			{
				this._boxTimeForOpen = 0L;
				this._pendingBoxSlotId = 0;
			}

			this.getAchievementBox().get(id - 1).setState(AchievementBoxStateType.RECEIVE_REWARD);
			this.sendBoxUpdate();
		}
	}

	public void sendBoxUpdate()
	{
		this._owner.sendPacket(new ExSteadyAllBoxUpdate(this._owner));
	}

	public void cancelTask()
	{
		if (this._boxOpenTask != null)
		{
			this._boxOpenTask.cancel(false);
			this._boxOpenTask = null;
		}
	}

	public void unlockSlot(int slotId)
	{
		if (slotId - 1 == this.getBoxOwned() && slotId <= 4)
		{
			boolean paidSlot = false;
			switch (slotId)
			{
				case 2:
					if (this._owner.reduceAdena(ItemProcessType.FEE, 100000000L, this._owner, true))
					{
						paidSlot = true;
					}
					break;
				case 3:
					if (this._owner.destroyItemByItemId(ItemProcessType.FEE, 91663, 2000L, this._owner, true))
					{
						paidSlot = true;
					}
					break;
				case 4:
					if (this._owner.destroyItemByItemId(ItemProcessType.FEE, 91663, 8000L, this._owner, true))
					{
						paidSlot = true;
					}
			}

			if (paidSlot)
			{
				this._boxOwned = slotId;
				AchievementBoxInfoHolder holder = new AchievementBoxInfoHolder(slotId, 1, 0);
				holder.setState(AchievementBoxStateType.AVAILABLE);
				holder.setType(AchievementBoxType.LOCKED);
				this.getAchievementBox().add(slotId - 1, holder);
				this.sendBoxUpdate();
			}
		}
	}

	public void getReward(int slotId)
	{
		AchievementBoxInfoHolder holder = this.getAchievementBox().get(slotId - 1);
		if (holder.getState() == AchievementBoxStateType.RECEIVE_REWARD)
		{
			int rnd = Rnd.get(100);
			ItemHolder reward = null;
			switch (holder.getType())
			{
				case BOX_2H:
					if (rnd < 3)
					{
						reward = new ItemHolder(Rnd.get(72084, 72102), 1L);
					}
					else if (rnd < 30)
					{
						reward = new ItemHolder(93274, 5L);
					}
					else if (rnd < 70)
					{
						reward = new ItemHolder(90907, 250L);
					}
					else
					{
						reward = new ItemHolder(3031, 50L);
					}
					break;
				case BOX_6H:
					if (rnd < 10)
					{
						reward = new ItemHolder(Rnd.get(72084, 72102), 1L);
					}
					else if (rnd < 30)
					{
						reward = new ItemHolder(93274, 10L);
					}
					else if (rnd < 70)
					{
						reward = new ItemHolder(90907, 500L);
					}
					else
					{
						reward = new ItemHolder(3031, 100L);
					}
					break;
				case BOX_12H:
					if (rnd < 20)
					{
						reward = new ItemHolder(Rnd.get(72084, 72102), 1L);
					}
					else if (rnd < 30)
					{
						reward = new ItemHolder(93274, 20L);
					}
					else if (rnd < 70)
					{
						reward = new ItemHolder(90907, 1000L);
					}
					else
					{
						reward = new ItemHolder(3031, 200L);
					}
			}

			holder.setState(AchievementBoxStateType.AVAILABLE);
			holder.setType(AchievementBoxType.LOCKED);
			this.sendBoxUpdate();
			if (reward != null)
			{
				this._owner.addItem(ItemProcessType.REWARD, reward, this._owner, true);
				this._owner.sendPacket(new ExSteadyBoxReward(slotId, reward.getId(), reward.getCount()));
			}
		}
	}

	public void refreshPvpEndDate()
	{
		long currentTime = System.currentTimeMillis();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(currentTime);
		calendar.set(5, 1);
		calendar.set(11, 6);
		if (calendar.getTimeInMillis() < currentTime)
		{
			calendar.add(2, 1);
		}

		this._pvpEndDate = (int) (calendar.getTimeInMillis() / 1000L);
	}
}
