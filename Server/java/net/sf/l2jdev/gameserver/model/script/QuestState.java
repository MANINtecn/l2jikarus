package net.sf.l2jdev.gameserver.model.script;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.managers.ScriptManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.Containers;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerQuestAccept;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerQuestComplete;
import net.sf.l2jdev.gameserver.model.script.newquestdata.QuestCondType;
import net.sf.l2jdev.gameserver.network.serverpackets.quest.ExQuestAcceptableList;
import net.sf.l2jdev.gameserver.network.serverpackets.quest.ExQuestNotification;
import net.sf.l2jdev.gameserver.network.serverpackets.quest.ExQuestUI;

public class QuestState
{
	protected static final Logger LOGGER = Logger.getLogger(QuestState.class.getName());
	protected static final String COND_VAR = "cond";
	protected static final String COUNT_VAR = "count";
	protected static final String RESTART_VAR = "restartTime";
	protected static final String MEMO_VAR = "memoState";
	protected static final String MEMO_EX_VAR = "memoStateEx";
	protected static final int RESET_HOUR = 6;
	protected static final int RESET_MINUTES = 30;
	private final String _questName;
	private final Player _player;
	private byte _state;
	private int _cond = 0;
	private boolean _simulated = false;
	private Map<String, String> _vars;
	private boolean _isExitQuestOnCleanUp = false;

	public QuestState(Quest quest, Player player, byte state)
	{
		this._questName = quest.getName();
		this._player = player;
		this._state = state;
		player.setQuestState(this);
	}

	public String getQuestName()
	{
		return this._questName;
	}

	public Quest getQuest()
	{
		return ScriptManager.getInstance().getScript(this._questName);
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public byte getState()
	{
		return this._state;
	}

	public boolean isCreated()
	{
		return this._state == 0;
	}

	public boolean isStarted()
	{
		return this._state == 1;
	}

	public boolean isCompleted()
	{
		return this._state == 2;
	}

	public void setState(byte state)
	{
		this.setState(state, true);
	}

	public void setState(byte state, boolean saveInDb)
	{
		if (!this._simulated)
		{
			if (this._state != state)
			{
				boolean newQuest = this.isCreated();
				this._state = state;
				if (saveInDb)
				{
					if (newQuest)
					{
						Quest.createQuestInDb(this);
					}
					else
					{
						Quest.updateQuestInDb(this);
					}
				}
			}
		}
	}

	public void setInternal(String variable, String value)
	{
		if (!this._simulated)
		{
			if (this._vars == null)
			{
				this._vars = new HashMap<>();
			}

			if (value == null)
			{
				this._vars.put(variable, "");
			}
			else
			{
				if ("cond".equals(variable))
				{
					try
					{
						this._cond = Integer.parseInt(value);
					}
					catch (Exception var4)
					{
					}
				}

				this._vars.put(variable, value);
			}
		}
	}

	public void set(String variable, int value)
	{
		if (!this._simulated)
		{
			this.set(variable, Integer.toString(value));
		}
	}

	public void set(String variable, String value)
	{
		if (!this._simulated)
		{
			if (this._vars == null)
			{
				this._vars = new HashMap<>();
			}

			String newValue = value;
			if (value == null)
			{
				newValue = "";
			}

			String old = this._vars.put(variable, newValue);
			if (old != null)
			{
				Quest.updateQuestVarInDb(this, variable, newValue);
			}
			else
			{
				Quest.createQuestVarInDb(this, variable, newValue);
			}

			if ("cond".equals(variable))
			{
				try
				{
					int previousVal = 0;

					try
					{
						previousVal = Integer.parseInt(old);
					}
					catch (Exception var9)
					{
					}

					int newCond = 0;

					try
					{
						newCond = Integer.parseInt(newValue);
					}
					catch (Exception var8)
					{
					}

					this._cond = newCond;
					this.setCond(newCond, previousVal);
					this.getQuest().sendNpcLogList(this.getPlayer());
				}
				catch (Exception var10)
				{
					LOGGER.log(Level.WARNING, this._player.getName() + ", " + this._questName + " cond [" + newValue + "] is not an integer.  Value stored, but no packet was sent: " + var10.getMessage(), var10);
				}
			}
		}
	}

	private void setCond(int cond, int old)
	{
		if (!this._simulated)
		{
			if (cond != old)
			{
				int completedStateFlags = 0;
				if (cond >= 3 && cond <= 31)
				{
					completedStateFlags = this.getInt("__compltdStateFlags");
				}
				else
				{
					this.unset("__compltdStateFlags");
				}

				if (completedStateFlags == 0)
				{
					if (cond > old + 1)
					{
						completedStateFlags = -2147483647;
						completedStateFlags |= (1 << old) - 1;
						completedStateFlags |= 1 << cond - 1;
						this.set("__compltdStateFlags", String.valueOf(completedStateFlags));
					}
				}
				else if (cond < old)
				{
					completedStateFlags &= (1 << cond) - 1;
					if (completedStateFlags == (1 << cond) - 1)
					{
						this.unset("__compltdStateFlags");
					}
					else
					{
						completedStateFlags |= -2147483647;
						this.set("__compltdStateFlags", String.valueOf(completedStateFlags));
					}
				}
				else
				{
					completedStateFlags |= 1 << cond - 1;
					this.set("__compltdStateFlags", String.valueOf(completedStateFlags));
				}

				this._player.sendPacket(new ExQuestUI(this._player));
				this._player.sendPacket(new ExQuestNotification(this));
			}
		}
	}

	public void unset(String variable)
	{
		if (!this._simulated)
		{
			if (this._vars != null)
			{
				String old = this._vars.remove(variable);
				if (old != null)
				{
					if ("cond".equals(variable))
					{
						this._cond = 0;
					}

					Quest.deleteQuestVarInDb(this, variable);
				}
			}
		}
	}

	public String get(String variable)
	{
		return this._vars == null ? null : this._vars.get(variable);
	}

	public int getInt(String variable)
	{
		if (this._vars == null)
		{
			return 0;
		}
		String varStr = this._vars.get(variable);
		if (varStr != null && !varStr.isEmpty())
		{
			int varInt = 0;

			try
			{
				varInt = Integer.parseInt(varStr);
			}
			catch (NumberFormatException var5)
			{
				LOGGER.log(Level.INFO, "Quest " + this._questName + ", method getInt(" + variable + "), tried to parse a non-integer value (" + varStr + "). Char Id: " + this._player.getObjectId(), var5);
			}

			return varInt;
		}
		return 0;
	}

	public boolean isCond(int condition)
	{
		return this._cond == condition;
	}

	public boolean isCond(QuestCondType condition)
	{
		return this._cond == condition.getId();
	}

	public void setCond(int condition)
	{
		if (!this._simulated)
		{
			if (this.isStarted())
			{
				this.set("cond", Integer.toString(condition));
				if (condition == QuestCondType.DONE.getId())
				{
					this._player.sendPacket(QuestSound.ITEMSOUND_QUEST_MIDDLE.getPacket());
				}
			}
		}
	}

	public void setCond(QuestCondType condition)
	{
		this.setCond(condition.getId());
	}

	public int getCond()
	{
		return this.isStarted() ? this._cond : 0;
	}

	public int getCondBitSet()
	{
		if (!this.isStarted())
		{
			return 0;
		}
		int val = this.getInt("cond");
		if ((val & -2147483648) != 0)
		{
			val &= Integer.MAX_VALUE;

			for (int i = 1; i < 32; i++)
			{
				val >>= 1;
				if (val == 0)
				{
					val = i;
					break;
				}
			}
		}

		return val;
	}

	public boolean isSet(String variable)
	{
		return this.get(variable) != null;
	}

	public void setCond(int value, boolean playQuestMiddle)
	{
		if (!this._simulated)
		{
			if (this.isStarted())
			{
				this.set("cond", String.valueOf(value));
				if (playQuestMiddle)
				{
					this._player.sendPacket(QuestSound.ITEMSOUND_QUEST_MIDDLE.getPacket());
				}
			}
		}
	}

	public void setMemoState(int value)
	{
		if (!this._simulated)
		{
			this.set("memoState", String.valueOf(value));
		}
	}

	public int getMemoState()
	{
		return this.isStarted() ? this.getInt("memoState") : 0;
	}

	public void setCount(int value)
	{
		if (!this._simulated)
		{
			this.set("count", String.valueOf(value));
			this._player.sendPacket(QuestSound.ITEMSOUND_QUEST_ITEMGET.getPacket());
			this._player.sendPacket(new ExQuestUI(this._player));
			this._player.sendPacket(new ExQuestNotification(this));
		}
	}

	public int getCount()
	{
		return this.isStarted() ? this.getInt("count") : 0;
	}

	public boolean isMemoState(int memoState)
	{
		return this.getInt("memoState") == memoState;
	}

	public int getMemoStateEx(int slot)
	{
		return this.isStarted() ? this.getInt("memoStateEx" + slot) : 0;
	}

	public void setMemoStateEx(int slot, int value)
	{
		if (!this._simulated)
		{
			this.set("memoStateEx" + slot, String.valueOf(value));
		}
	}

	public boolean isMemoStateEx(int slot, int memoStateEx)
	{
		return this.getMemoStateEx(slot) == memoStateEx;
	}

	public boolean isExitQuestOnCleanUp()
	{
		return this._isExitQuestOnCleanUp;
	}

	public void setIsExitQuestOnCleanUp(boolean isExitQuestOnCleanUp)
	{
		if (!this._simulated)
		{
			this._isExitQuestOnCleanUp = isExitQuestOnCleanUp;
		}
	}

	public void startQuest()
	{
		if (!this._simulated)
		{
			if (this.isCreated() && !this.getQuest().isCustomQuest())
			{
				this.set("cond", "1");
				this.set("count", "0");
				this.setState((byte) 1);
				this._player.sendPacket(QuestSound.ITEMSOUND_QUEST_ACCEPT.getPacket());
				this._player.sendPacket(new ExQuestAcceptableList(this._player));
				if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_QUEST_ACCEPT, this._player, Containers.Players()))
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnPlayerQuestAccept(this._player, this.getQuest().getId()), this._player, Containers.Players());
				}
			}
		}
	}

	public void exitQuest(QuestType type)
	{
		if (!this._simulated)
		{
			switch (type)
			{
				case DAILY:
					this.exitQuest(false);
					this.setRestartTime();
					break;
				default:
					this.exitQuest(type == QuestType.REPEATABLE);
			}

			if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_QUEST_COMPLETE, this._player))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerQuestComplete(this._player, this.getQuest().getId(), type), this._player);
			}
		}
	}

	public void exitQuest(QuestType type, boolean playExitQuest)
	{
		if (!this._simulated)
		{
			this.exitQuest(type);
			if (playExitQuest)
			{
				this._player.sendPacket(QuestSound.ITEMSOUND_QUEST_FINISH.getPacket());
			}
		}
	}

	private void exitQuest(boolean repeatable)
	{
		if (!this._simulated)
		{
			this._player.removeNotifyQuestOfDeath(this);
			if (this.isStarted())
			{
				this.getQuest().removeRegisteredQuestItems(this._player);
				Quest.deleteQuestInDb(this, repeatable);
				if (repeatable)
				{
					this._player.delQuestState(this._questName);
				}
				else
				{
					this.setState((byte) 2);
				}

				this._player.sendPacket(new ExQuestNotification(this));
				this._player.sendPacket(new ExQuestUI(this._player));
				this._vars = null;
			}
		}
	}

	public void exitQuest(boolean repeatable, boolean playExitQuest)
	{
		if (!this._simulated)
		{
			this.exitQuest(repeatable);
			if (playExitQuest)
			{
				this._player.sendPacket(QuestSound.ITEMSOUND_QUEST_FINISH.getPacket());
			}

			if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_QUEST_COMPLETE, this._player))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerQuestComplete(this._player, this.getQuest().getId(), repeatable ? QuestType.REPEATABLE : QuestType.ONE_TIME), this._player);
			}
		}
	}

	public void setRestartTime()
	{
		if (!this._simulated)
		{
			Calendar reDo = Calendar.getInstance();
			if (reDo.get(11) >= 6)
			{
				reDo.add(5, 1);
			}

			reDo.set(11, 6);
			reDo.set(12, 30);
			this.set("restartTime", String.valueOf(reDo.getTimeInMillis()));
		}
	}

	public boolean isNowAvailable()
	{
		String val = this.get("restartTime");
		return val != null && Long.parseLong(val) <= System.currentTimeMillis();
	}

	public void setSimulated(boolean simulated)
	{
		this._simulated = simulated;
	}
}
