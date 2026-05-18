package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillFinishType;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.network.serverpackets.mablegame.ExMableGameMinigame;
import net.sf.l2jdev.gameserver.network.serverpackets.mablegame.ExMableGameMove;
import net.sf.l2jdev.gameserver.network.serverpackets.mablegame.ExMableGamePrison;
import net.sf.l2jdev.gameserver.network.serverpackets.mablegame.ExMableGameRewardItem;
import net.sf.l2jdev.gameserver.network.serverpackets.mablegame.ExMableGameSkillInfo;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class MableGameData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(MableGameData.class.getName());
	public static final int COMMON_DICE_ITEM_ID = 93885;
	public static final int ENHANCED_DICE_ITEM_ID = 93886;
	public static final int MIN_PRISON_DICE = 5;
	public static final int MAX_PRISON_DICE = 6;
	public static final int BUFF_ID = 40205;
	private static final SkillHolder[] BUFF_CACHE = new SkillHolder[]
	{
		new SkillHolder(40205, 1),
		new SkillHolder(40205, 2),
		new SkillHolder(40205, 3),
		new SkillHolder(40205, 4),
		new SkillHolder(40205, 5),
		new SkillHolder(40205, 6),
		new SkillHolder(40205, 7),
		new SkillHolder(40205, 8)
	};
	private final List<ItemHolder> _resetItems = new ArrayList<>();
	private final Map<Integer, MableGameData.MableGameCell> _cells = new HashMap<>();
	private final Map<String, MableGameData.MableGamePlayerState> _playerStates = new HashMap<>();
	private boolean _isEnabled;
	private int _dailyAvailableRounds;
	private int _commonDiceLimit;
	private ItemHolder _roundReward;

	public MableGameData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._roundReward = null;
		this._resetItems.clear();
		this._cells.clear();
		this.parseDatapackFile("data/MableGameData.xml");
		if (this._isEnabled)
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded (" + this._cells.size() + " cells)");
			if (this._playerStates.isEmpty())
			{
				try (Connection conn = DatabaseFactory.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM mable_game"); ResultSet rs = ps.executeQuery();)
				{
					while (rs.next())
					{
						String accountName = rs.getString("account_name");
						int round = rs.getInt("round");
						int currentCellId = rs.getInt("current_cell_id");
						int remainCommonDice = rs.getInt("remain_common_dice");
						int remainPrisonRolls = rs.getInt("remain_prison_rolls");
						this._playerStates.put(accountName, new MableGameData.MableGamePlayerState(round, currentCellId, remainCommonDice, remainPrisonRolls));
					}
				}
				catch (Exception var15)
				{
					LOGGER.warning(this.getClass().getSimpleName() + ": Failed loading player states. " + var15.getMessage());
				}
			}
		}
		else
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Disabled");
		}
	}

	public void save()
	{
		if (this._isEnabled)
		{
			try (Connection conn = DatabaseFactory.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE mable_game SET round=?, current_cell_id=?, remain_common_dice=?, remain_prison_rolls=? WHERE account_name=?");)
			{
				for (Entry<String, MableGameData.MableGamePlayerState> entry : this._playerStates.entrySet())
				{
					MableGameData.MableGamePlayerState state = entry.getValue();
					ps.setInt(1, state.getRound());
					ps.setInt(2, state.getCurrentCellId());
					ps.setInt(3, state.getRemainCommonDice());
					ps.setInt(4, state.getRemainingPrisonRolls());
					ps.setString(5, entry.getKey());
					ps.addBatch();
				}

				ps.executeBatch();
				LOGGER.warning(this.getClass().getSimpleName() + ": Saved player states.");
			}
			catch (Exception var10)
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": Failed saving player states. " + var10.getMessage());
			}
		}
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> {
			this._isEnabled = true;
			NamedNodeMap at = listNode.getAttributes();
			Node attribute = at.getNamedItem("enabled");
			if (attribute != null && Boolean.parseBoolean(attribute.getNodeValue()))
			{
				this.forEach(listNode, "dailyAvailableRounds", dailyAvailableRounds -> this._dailyAvailableRounds = Integer.parseInt(dailyAvailableRounds.getTextContent()));
				this.forEach(listNode, "commonDiceLimit", commonDiceLimit -> this._commonDiceLimit = Integer.parseInt(commonDiceLimit.getTextContent()));
				this.forEach(listNode, "roundReward", roundReward -> {
					NamedNodeMap attrs = roundReward.getAttributes();
					int itemId = this.parseInteger(attrs, "id");
					int itemCount = this.parseInteger(attrs, "count");
					this._roundReward = new ItemHolder(itemId, itemCount);
				});
				this.forEach(listNode, "resetItems", resetItems -> this.forEach(resetItems, "item", item -> {
					NamedNodeMap attrs = item.getAttributes();
					int itemId = this.parseInteger(attrs, "id");
					int itemCount = this.parseInteger(attrs, "count");
					this._resetItems.add(new ItemHolder(itemId, itemCount));
				}));
				this.forEach(listNode, "cells", cells -> this.forEach(cells, "cell", cell -> {
					NamedNodeMap attrs = cell.getAttributes();
					int id = this.parseInteger(attrs, "id");
					int color = this.parseInteger(attrs, "color");
					String name = this.parseString(attrs, "name");
					String[] paramsStr = this.parseString(attrs, "params", "").split(";");
					int[] params = new int[paramsStr.length];
					if (paramsStr.length > 0 && !paramsStr[0].isBlank())
					{
						for (int i = 0; i < paramsStr.length; i++)
						{
							params[i] = Integer.parseInt(paramsStr[i]);
						}
					}

					List<ItemHolder> rewards = new ArrayList<>();
					this.forEach(cell, "rewards", rewardsNode -> this.forEach(rewardsNode, "item", item -> {
						NamedNodeMap itemAttrs = item.getAttributes();
						int itemId = this.parseInteger(itemAttrs, "id");
						int itemCount = this.parseInteger(itemAttrs, "count");
						rewards.add(new ItemHolder(itemId, itemCount));
					}));
					MableGameData.MableGameCellColor cellColor = MableGameData.MableGameCellColor.getByClientId(color);
					if (cellColor == null)
					{
						LOGGER.warning(this.getClass().getSimpleName() + ": Missing color: " + color + " for cell id: " + id);
					}
					else
					{
						this._cells.put(id, new MableGameData.MableGameCell(id, cellColor, name, params, rewards));
					}
				}));
			}
			else
			{
				this._isEnabled = false;
				this._dailyAvailableRounds = 0;
				this._commonDiceLimit = 0;
			}
		});
	}

	public boolean isEnabled()
	{
		return this._isEnabled;
	}

	public int getDailyAvailableRounds()
	{
		return this._dailyAvailableRounds;
	}

	public int getCommonDiceLimit()
	{
		return this._commonDiceLimit;
	}

	public int getHighestCellId()
	{
		return this._cells.size();
	}

	public ItemHolder getRoundReward()
	{
		return this._roundReward;
	}

	public List<ItemHolder> getResetItems()
	{
		return this._resetItems;
	}

	public MableGameData.MableGameCell getCellById(int cellId)
	{
		return this._cells.get(cellId);
	}

	public MableGameData.MableGamePlayerState getPlayerState(String accountName)
	{
		MableGameData.MableGamePlayerState state = this._playerStates.get(accountName);
		if (state == null)
		{
			state = new MableGameData.MableGamePlayerState(1, 1, this._commonDiceLimit, 0);
			this._playerStates.put(accountName, state);

			try (Connection conn = DatabaseFactory.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO mable_game VALUES (?,?,?,?,?)");)
			{
				ps.setString(1, accountName);
				ps.setInt(2, state.getRound());
				ps.setInt(3, state.getCurrentCellId());
				ps.setInt(4, state.getRemainCommonDice());
				ps.setInt(5, state.getRemainingPrisonRolls());
				ps.execute();
			}
			catch (Exception var11)
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": Failed inserting player state for account: " + accountName + ". " + var11.getMessage());
			}
		}

		return state;
	}

	public static MableGameData getInstance()
	{
		return MableGameData.SingletonHolder.INSTANCE;
	}

	public static class MableGameCell
	{
		private final int _id;
		private final MableGameData.MableGameCellColor _color;
		private final String _name;
		private final int[] _params;
		private final List<ItemHolder> _rewards;

		protected MableGameCell(int id, MableGameData.MableGameCellColor color, String name, int[] params, List<ItemHolder> rewards)
		{
			this._id = id;
			this._color = color;
			this._name = name;
			this._params = params;
			this._rewards = rewards;
		}

		public int getId()
		{
			return this._id;
		}

		public MableGameData.MableGameCellColor getColor()
		{
			return this._color;
		}

		public String getName()
		{
			return this._name;
		}

		public int[] getParams()
		{
			return this._params;
		}

		public List<ItemHolder> getRewards()
		{
			return this._rewards;
		}
	}

	public static enum MableGameCellColor
	{
		LIGHT_BLUE(1),
		YELLOW(2),
		PURPLE(3),
		RED(4),
		DARK_RED(5),
		GREEN(7),
		BURNING_RED(8),
		DARK_PURPLE(9);

		private final int _clientId;

		private MableGameCellColor(int clientId)
		{
			this._clientId = clientId;
		}

		public static MableGameData.MableGameCellColor getByClientId(int id)
		{
			for (MableGameData.MableGameCellColor color : values())
			{
				if (color.getClientId() == id)
				{
					return color;
				}
			}

			return null;
		}

		public int getClientId()
		{
			return this._clientId;
		}
	}

	public static class MableGamePlayerState
	{
		private int _round;
		private int _currentCellId;
		private int _remainCommonDice;
		private int _remainingPrisonRolls;
		private int _pendingCellIdPopup = -1;
		private ItemHolder _pendingReward = null;
		private boolean _isMoved = false;

		private MableGamePlayerState(int round, int currentCellId, int remainCommonDice, int remainingPrisonRolls)
		{
			this._round = round;
			this._currentCellId = currentCellId;
			this._remainCommonDice = remainCommonDice;
			this._remainingPrisonRolls = remainingPrisonRolls;
		}

		public int getRound()
		{
			return this._round;
		}

		public void setRound(int round)
		{
			this._round = round;
		}

		public int getCurrentCellId()
		{
			return this._currentCellId;
		}

		public void setCurrentCellId(int currentCellId)
		{
			this._currentCellId = currentCellId;
		}

		public int getRemainCommonDice()
		{
			return this._remainCommonDice;
		}

		public void setRemainCommonDice(int remainCommonDice)
		{
			this._remainCommonDice = remainCommonDice;
		}

		public int getRemainingPrisonRolls()
		{
			return this._remainingPrisonRolls;
		}

		public void setRemainingPrisonRolls(int count)
		{
			this._remainingPrisonRolls = count;
		}

		public int getPendingCellIdPopup()
		{
			return this._pendingCellIdPopup;
		}

		public void setPendingCellIdPopup(int cellId)
		{
			this._pendingCellIdPopup = cellId;
		}

		public ItemHolder getPendingReward()
		{
			return this._pendingReward;
		}

		public void setPendingReward(ItemHolder reward)
		{
			this._pendingReward = reward;
		}

		public boolean isMoved()
		{
			return this._isMoved;
		}

		public void setMoved(boolean val)
		{
			this._isMoved = val;
		}

		public void handleCell(Player player, MableGameData.MableGameCell cell)
		{
			switch (cell.getColor())
			{
				case LIGHT_BLUE:
					if (cell.getId() == MableGameData.getInstance().getHighestCellId())
					{
						ItemHolder roundReward = MableGameData.getInstance().getRoundReward();
						if (roundReward != null)
						{
							this.setPendingCellIdPopup(cell.getId());
							this.setPendingReward(roundReward);
							player.sendPacket(new ExMableGameRewardItem(roundReward.getId(), roundReward.getCount()));
						}
					}
					break;
				case YELLOW:
					int newCellId = cell.getId();
					if (Rnd.nextBoolean())
					{
						newCellId -= cell.getParams()[Rnd.get(cell.getParams().length)];
						newCellId = Math.max(newCellId, 1);
					}
					else
					{
						newCellId += cell.getParams()[Rnd.get(cell.getParams().length)];
						newCellId = Math.min(newCellId, MableGameData.getInstance().getHighestCellId());
					}

					this.setPendingCellIdPopup(newCellId);
					this.setCurrentCellId(newCellId);
					this.setMoved(true);
					MableGameData.MableGameCell newCell = MableGameData.getInstance().getCellById(newCellId);
					player.sendPacket(new ExMableGameMove(newCellId - cell.getId(), newCellId, newCell.getColor().getClientId()));
					break;
				case PURPLE:
					int currentLevel = player.getAffectedSkillLevel(40205);
					int newLevel = Math.min(MableGameData.BUFF_CACHE.length, currentLevel + 1);
					Skill skill = MableGameData.BUFF_CACHE[newLevel - 1].getSkill();
					skill.applyEffects(player, player);
					player.sendPacket(new ExMableGameSkillInfo(40205, newLevel));
					break;
				case RED:
				case BURNING_RED:
					int luckyNumber = Rnd.get(1, 6);
					int dice = Rnd.get(1, 6);
					int bossDice = Rnd.get(1, 6);
					int result = dice < bossDice ? 0 : (dice == bossDice ? 2 : 1);
					boolean isLuckyNumber = luckyNumber == dice;
					int rewardIndex = dice < bossDice ? 0 : (dice == bossDice ? 1 : 2);
					List<ItemHolder> rewards = cell.getRewards();
					ItemHolder reward = rewards.size() - 1 < rewardIndex ? new ItemHolder(57, 1L) : rewards.get(rewardIndex);
					int itemId = reward.getId();
					long itemCount = reward.getCount() * (isLuckyNumber && itemId != 57 ? 2 : 1);
					this.setPendingCellIdPopup(cell.getId());
					this.setPendingReward(new ItemHolder(itemId, itemCount));
					player.sendPacket(new ExMableGameMinigame(cell.getParams()[0], luckyNumber, dice, bossDice, result, isLuckyNumber, itemId, itemCount));
					break;
				case DARK_RED:
					player.stopSkillEffects(SkillFinishType.REMOVED, 40205);
					this.setRemainingPrisonRolls(3);
					player.sendPacket(new ExMableGamePrison(5, 6, this.getRemainingPrisonRolls()));
					break;
				case GREEN:
					if (cell.getRewards() != null)
					{
						Iterator<ItemHolder> rewardIter = cell.getRewards().iterator();
						if (rewardIter.hasNext())
						{
							ItemHolder greenReward = rewardIter.next();
							this.setPendingCellIdPopup(cell.getId());
							this.setPendingReward(greenReward);
							player.sendPacket(new ExMableGameRewardItem(greenReward.getId(), greenReward.getCount()));
						}
					}
					break;
				case DARK_PURPLE:
					player.stopSkillEffects(SkillFinishType.REMOVED, 40205);
					break;
				default:
					MableGameData.LOGGER.warning(this.getClass().getSimpleName() + ": Unhandled Cell Id:" + cell.getId() + " Color:" + cell.getColor());
			}
		}
	}

	private static class SingletonHolder
	{
		protected static final MableGameData INSTANCE = new MableGameData();
	}
}
