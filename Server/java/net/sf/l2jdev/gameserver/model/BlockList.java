package net.sf.l2jdev.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.BlockListPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class BlockList
{
	private static final Logger LOGGER = Logger.getLogger(BlockList.class.getName());
	private static final Map<Integer, Set<Integer>> OFFLINE_LIST = new ConcurrentHashMap<>();
	private final Player _owner;
	private Set<Integer> _blockList;

	public BlockList(Player owner)
	{
		this._owner = owner;
		this._blockList = OFFLINE_LIST.get(owner.getObjectId());
		if (this._blockList == null)
		{
			this._blockList = loadList(this._owner.getObjectId());
		}
	}

	private void addToBlockList(int target)
	{
		this._blockList.add(target);
		this.updateInDB(target, true);
	}

	private void removeFromBlockList(int target)
	{
		this._blockList.remove(target);
		this.updateInDB(target, false);
	}

	public void playerLogout()
	{
		OFFLINE_LIST.put(this._owner.getObjectId(), this._blockList);
	}

	private static Set<Integer> loadList(int objId)
	{
		Set<Integer> list = new HashSet<>();

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT friendId FROM character_friends WHERE charId=? AND relation=1");)
		{
			statement.setInt(1, objId);

			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int friendId = rset.getInt("friendId");
					if (friendId != objId)
					{
						list.add(friendId);
					}
				}
			}
		}
		catch (Exception var13)
		{
			LOGGER.log(Level.WARNING, "Error found in " + objId + " FriendList while loading BlockList: " + var13.getMessage(), var13);
		}

		return list;
	}

	private void updateInDB(int targetId, boolean state)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			if (state)
			{
				try (PreparedStatement statement = con.prepareStatement("INSERT INTO character_friends (charId, friendId, relation) VALUES (?, ?, 1)"))
				{
					statement.setInt(1, this._owner.getObjectId());
					statement.setInt(2, targetId);
					statement.execute();
				}
			}
			else
			{
				try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_friends WHERE charId=? AND friendId=? AND relation=1"))
				{
					statement.setInt(1, this._owner.getObjectId());
					statement.setInt(2, targetId);
					statement.execute();
				}
			}
		}
		catch (Exception var13)
		{
			LOGGER.log(Level.WARNING, "Could not add block player: " + var13.getMessage(), var13);
		}
	}

	public boolean isInBlockList(Player target)
	{
		return this._blockList.contains(target.getObjectId());
	}

	public boolean isInBlockList(int targetId)
	{
		return this._blockList.contains(targetId);
	}

	public boolean isBlockAll()
	{
		return this._owner.getMessageRefusal();
	}

	public static boolean isBlocked(Player listOwner, Player target)
	{
		BlockList blockList = listOwner.getBlockList();
		return blockList.isBlockAll() || blockList.isInBlockList(target);
	}

	public static boolean isBlocked(Player listOwner, int targetId)
	{
		BlockList blockList = listOwner.getBlockList();
		return blockList.isBlockAll() || blockList.isInBlockList(targetId);
	}

	private void setBlockAll(boolean value)
	{
		this._owner.setMessageRefusal(value);
	}

	private Set<Integer> getBlockList()
	{
		return this._blockList;
	}

	public static void addToBlockList(Player listOwner, int targetId)
	{
		if (listOwner != null)
		{
			String charName = CharInfoTable.getInstance().getNameById(targetId);
			if (listOwner.getFriendList().contains(targetId))
			{
				listOwner.sendPacket(SystemMessageId.THIS_PLAYER_IS_ALREADY_REGISTERED_ON_YOUR_FRIENDS_LIST);
			}
			else if (listOwner.getBlockList().getBlockList().contains(targetId))
			{
				listOwner.sendMessage("Already in ignore list.");
			}
			else
			{
				listOwner.getBlockList().addToBlockList(targetId);
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_ADDED_TO_YOUR_IGNORE_LIST);
				sm.addString(charName);
				listOwner.sendPacket(sm);
				Player player = World.getInstance().getPlayer(targetId);
				if (player != null)
				{
					sm = new SystemMessage(SystemMessageId.C1_HAS_ADDED_YOU_TO_THEIR_IGNORE_LIST);
					sm.addString(listOwner.getName());
					player.sendPacket(sm);
				}
			}
		}
	}

	public static void removeFromBlockList(Player listOwner, int targetId)
	{
		if (listOwner != null)
		{
			String charName = CharInfoTable.getInstance().getNameById(targetId);
			if (!listOwner.getBlockList().getBlockList().contains(targetId))
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
				listOwner.sendPacket(sm);
			}
			else
			{
				listOwner.getBlockList().removeFromBlockList(targetId);
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_REMOVED_FROM_YOUR_IGNORE_LIST);
				sm.addString(charName);
				listOwner.sendPacket(sm);
			}
		}
	}

	public static boolean isInBlockList(Player listOwner, Player target)
	{
		return listOwner.getBlockList().isInBlockList(target);
	}

	public boolean isBlockAll(Player listOwner)
	{
		return listOwner.getBlockList().isBlockAll();
	}

	public static void setBlockAll(Player listOwner, boolean newValue)
	{
		listOwner.getBlockList().setBlockAll(newValue);
	}

	public static void sendListToOwner(Player listOwner)
	{
		listOwner.sendPacket(new BlockListPacket(listOwner.getBlockList().getBlockList()));
	}

	public static boolean isInBlockList(int ownerId, int targetId)
	{
		Player player = World.getInstance().getPlayer(ownerId);
		if (player != null)
		{
			return isBlocked(player, targetId);
		}
		if (!OFFLINE_LIST.containsKey(ownerId))
		{
			OFFLINE_LIST.put(ownerId, loadList(ownerId));
		}

		return OFFLINE_LIST.get(ownerId).contains(targetId);
	}
}
