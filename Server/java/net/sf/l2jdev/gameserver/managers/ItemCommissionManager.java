package net.sf.l2jdev.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.model.Message;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.CommissionManager;
import net.sf.l2jdev.gameserver.model.commission.CommissionItem;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.Mail;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.MailType;
import net.sf.l2jdev.gameserver.network.serverpackets.commission.ExResponseCommissionBuyItem;
import net.sf.l2jdev.gameserver.network.serverpackets.commission.ExResponseCommissionDelete;
import net.sf.l2jdev.gameserver.network.serverpackets.commission.ExResponseCommissionInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.commission.ExResponseCommissionList;
import net.sf.l2jdev.gameserver.network.serverpackets.commission.ExResponseCommissionRegister;

public class ItemCommissionManager
{
	private static final Logger LOGGER = Logger.getLogger(ItemCommissionManager.class.getName());
	public static final int INTERACTION_DISTANCE = 250;
	public static final int ITEMS_LIMIT_PER_REQUEST = 999;
	public static final int MAX_ITEMS_REGISTRED_PER_PLAYER = 10;
	public static final long MIN_REGISTRATION_AND_SALE_FEE = 1000L;
	public static final double REGISTRATION_FEE_PER_DAY = 1.0E-4;
	public static final double SALE_FEE_PER_DAY = 0.005;
	private static final int[] DURATION = new int[]
	{
		1,
		3,
		5,
		7,
		15,
		30
	};
	public static final String SELECT_ALL_ITEMS = "SELECT * FROM `items` WHERE `loc` = ?";
	public static final String SELECT_ALL_COMMISSION_ITEMS = "SELECT * FROM `commission_items`";
	public static final String INSERT_COMMISSION_ITEM = "INSERT INTO `commission_items`(`item_object_id`, `price_per_unit`, `start_time`, `duration_in_days`, `discount_in_percentage`) VALUES (?, ?, ?, ?, ?)";
	public static final String DELETE_COMMISSION_ITEM = "DELETE FROM `commission_items` WHERE `commission_id` = ?";
	private final Map<Long, CommissionItem> _commissionItems = new ConcurrentSkipListMap<>();

	protected ItemCommissionManager()
	{
		Map<Integer, Item> itemInstances = new HashMap<>();

		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `items` WHERE `loc` = ?"))
			{
				ps.setString(1, ItemLocation.COMMISSION.name());

				try (ResultSet rs = ps.executeQuery())
				{
					while (rs.next())
					{
						Item itemInstance = new Item(rs);
						itemInstances.put(itemInstance.getObjectId(), itemInstance);
					}
				}
			}

			try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM `commission_items`");)
			{
				while (rs.next())
				{
					long commissionId = rs.getLong("commission_id");
					Item itemInstance = itemInstances.get(rs.getInt("item_object_id"));
					if (itemInstance == null)
					{
						LOGGER.warning(this.getClass().getSimpleName() + ": Failed loading commission item with commission id " + commissionId + " because item instance does not exist or failed to load.");
					}
					else
					{
						CommissionItem commissionItem = new CommissionItem(commissionId, itemInstance, rs.getLong("price_per_unit"), rs.getTimestamp("start_time").toInstant(), rs.getByte("duration_in_days"), rs.getByte("discount_in_percentage"));
						this._commissionItems.put(commissionItem.getCommissionId(), commissionItem);
						if (commissionItem.getEndTime().isBefore(Instant.now()))
						{
							this.expireSale(commissionItem);
						}
						else
						{
							commissionItem.setSaleEndTask(ThreadPool.schedule(() -> this.expireSale(commissionItem), Duration.between(Instant.now(), commissionItem.getEndTime()).toMillis()));
						}
					}
				}
			}
		}
		catch (SQLException var19)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Failed loading commission items.", var19);
		}
	}

	public void showAuctions(Player player, Predicate<ItemTemplate> filter)
	{
		List<CommissionItem> commissionItems = new LinkedList<>();

		for (CommissionItem item : this._commissionItems.values())
		{
			if (filter.test(item.getItemInfo().getItem()))
			{
				commissionItems.add(item);
				if (commissionItems.size() >= 999)
				{
					break;
				}
			}
		}

		if (commissionItems.isEmpty())
		{
			player.sendPacket(new ExResponseCommissionList(ExResponseCommissionList.CommissionListReplyType.ITEM_DOES_NOT_EXIST));
		}
		else
		{
			int chunks = commissionItems.size() / 120;
			if (commissionItems.size() > chunks * 120)
			{
				chunks++;
			}

			for (int i = chunks - 1; i >= 0; i--)
			{
				player.sendPacket(new ExResponseCommissionList(ExResponseCommissionList.CommissionListReplyType.AUCTIONS, commissionItems, i, i * 120));
			}
		}
	}

	public void showPlayerAuctions(Player player)
	{
		List<CommissionItem> commissionItems = new LinkedList<>();

		for (CommissionItem c : this._commissionItems.values())
		{
			if (c.getItemInstance().getOwnerId() == player.getObjectId())
			{
				commissionItems.add(c);
				if (commissionItems.size() == 10)
				{
					break;
				}
			}
		}

		if (!commissionItems.isEmpty())
		{
			player.sendPacket(new ExResponseCommissionList(ExResponseCommissionList.CommissionListReplyType.PLAYER_AUCTIONS, commissionItems));
		}
		else
		{
			player.sendPacket(new ExResponseCommissionList(ExResponseCommissionList.CommissionListReplyType.PLAYER_AUCTIONS_EMPTY));
		}
	}

	public void registerItem(Player player, int itemObjectId, long itemCount, long pricePerUnit, int durationType, byte discountInPercentage)
	{
		if (itemCount < 1L)
		{
			player.sendPacket(SystemMessageId.THE_ITEM_HAS_FAILED_TO_BE_REGISTERED);
			player.sendPacket(ExResponseCommissionRegister.FAILED);
		}
		else
		{
			long totalPrice = itemCount * pricePerUnit;
			if (totalPrice <= 1000L)
			{
				player.sendPacket(SystemMessageId.THE_ITEM_CANNOT_BE_REGISTERED_BECAUSE_REQUIREMENTS_ARE_NOT_MET);
				player.sendPacket(ExResponseCommissionRegister.FAILED);
			}
			else
			{
				Item itemInstance = player.getInventory().getItemByObjectId(itemObjectId);
				if (itemInstance != null && itemInstance.isAvailable(player, false, false) && itemInstance.getCount() >= itemCount)
				{
					byte durationInDays = (byte) DURATION[durationType];
					synchronized (this)
					{
						long playerRegisteredItems = 0L;

						for (CommissionItem item : this._commissionItems.values())
						{
							if (item.getItemInstance().getOwnerId() == player.getObjectId())
							{
								playerRegisteredItems++;
							}
						}

						if (playerRegisteredItems >= 10L)
						{
							player.sendPacket(SystemMessageId.THE_MAXIMUM_NUMBER_OF_AUCTION_HOUSE_ITEMS_FOR_REGISTRATION_IS_10);
							player.sendPacket(ExResponseCommissionRegister.FAILED);
						}
						else
						{
							long registrationFee = (long) Math.max(1000.0, totalPrice * 1.0E-4 * Math.min(durationInDays, 7));
							if (!player.getInventory().reduceAdena(ItemProcessType.FEE, registrationFee, player, null))
							{
								player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA_TO_REGISTER_THE_ITEM);
								player.sendPacket(ExResponseCommissionRegister.FAILED);
							}
							else
							{
								itemInstance = player.getInventory().detachItem(ItemProcessType.TRANSFER, itemInstance, itemCount, ItemLocation.COMMISSION, player, null);
								if (itemInstance == null)
								{
									player.getInventory().addAdena(ItemProcessType.REFUND, registrationFee, player, null);
									player.sendPacket(SystemMessageId.THE_ITEM_HAS_FAILED_TO_BE_REGISTERED);
									player.sendPacket(ExResponseCommissionRegister.FAILED);
								}
								else
								{
									switch (Math.max(durationType, discountInPercentage))
									{
										case 4:
											player.destroyItemByItemId(null, 22353, 1L, player, true);
											break;
										case 5:
											player.destroyItemByItemId(null, 22354, 1L, player, true);
											break;
										case 30:
											player.destroyItemByItemId(null, 22351, 1L, player, true);
											break;
										case 100:
											player.destroyItemByItemId(null, 22352, 1L, player, true);
									}

									try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO `commission_items`(`item_object_id`, `price_per_unit`, `start_time`, `duration_in_days`, `discount_in_percentage`) VALUES (?, ?, ?, ?, ?)", 1);)
									{
										Instant startTime = Instant.now();
										ps.setInt(1, itemInstance.getObjectId());
										ps.setLong(2, pricePerUnit);
										ps.setTimestamp(3, Timestamp.from(startTime));
										ps.setByte(4, durationInDays);
										ps.setByte(5, discountInPercentage);
										ps.executeUpdate();

										try (ResultSet rs = ps.getGeneratedKeys())
										{
											if (rs.next())
											{
												CommissionItem commissionItem = new CommissionItem(rs.getLong(1), itemInstance, pricePerUnit, startTime, durationInDays, discountInPercentage);
												ScheduledFuture<?> saleEndTask = ThreadPool.schedule(() -> this.expireSale(commissionItem), Duration.between(Instant.now(), commissionItem.getEndTime()).toMillis());
												commissionItem.setSaleEndTask(saleEndTask);
												this._commissionItems.put(commissionItem.getCommissionId(), commissionItem);
												player.getLastCommissionInfos().put(itemInstance.getId(), new ExResponseCommissionInfo(itemInstance.getId(), pricePerUnit, itemCount, (byte) ((durationInDays - 1) / 2)));
												player.sendPacket(SystemMessageId.THE_ITEM_HAS_BEEN_REGISTERED);
												player.sendPacket(ExResponseCommissionRegister.SUCCEED);
											}
										}
									}
									catch (SQLException var31)
									{
										LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Failed inserting commission item. ItemInstance: " + itemInstance, var31);
										player.sendPacket(SystemMessageId.THE_ITEM_HAS_FAILED_TO_BE_REGISTERED);
										player.sendPacket(ExResponseCommissionRegister.FAILED);
									}
								}
							}
						}
					}
				}
				else
				{
					player.sendPacket(SystemMessageId.THE_ITEM_HAS_FAILED_TO_BE_REGISTERED);
					player.sendPacket(ExResponseCommissionRegister.FAILED);
				}
			}
		}
	}

	public void deleteItem(Player player, long commissionId)
	{
		CommissionItem commissionItem = this.getCommissionItem(commissionId);
		if (commissionItem == null)
		{
			player.sendPacket(SystemMessageId.FAILED_TO_CANCEL_THE_SALE);
			player.sendPacket(ExResponseCommissionDelete.FAILED);
		}
		else if (commissionItem.getItemInstance().getOwnerId() != player.getObjectId())
		{
			player.sendPacket(ExResponseCommissionDelete.FAILED);
		}
		else if (!player.isInventoryUnder80(false) || player.getWeightPenalty() >= 3)
		{
			player.sendPacket(SystemMessageId.TO_BUY_CANCEL_YOU_NEED_TO_FREE_20_OF_WEIGHT_AND_10_OF_SLOTS_IN_YOUR_INVENTORY);
			player.sendPacket(SystemMessageId.FAILED_TO_CANCEL_THE_SALE);
			player.sendPacket(ExResponseCommissionDelete.FAILED);
		}
		else if (this._commissionItems.remove(commissionId) != null && commissionItem.getSaleEndTask().cancel(false))
		{
			if (this.deleteItemFromDB(commissionId))
			{
				player.getInventory().addItem(ItemProcessType.TRANSFER, commissionItem.getItemInstance(), player, null);
				player.sendPacket(SystemMessageId.THE_SALE_IS_CANCELLED);
				player.sendPacket(ExResponseCommissionDelete.SUCCEED);
			}
			else
			{
				player.sendPacket(SystemMessageId.FAILED_TO_CANCEL_THE_SALE);
				player.sendPacket(ExResponseCommissionDelete.FAILED);
			}
		}
		else
		{
			player.sendPacket(SystemMessageId.FAILED_TO_CANCEL_THE_SALE);
			player.sendPacket(ExResponseCommissionDelete.FAILED);
		}
	}

	public void buyItem(Player player, long commissionId)
	{
		CommissionItem commissionItem = this.getCommissionItem(commissionId);
		if (commissionItem == null)
		{
			player.sendPacket(SystemMessageId.ITEM_PURCHASE_HAS_FAILED);
			player.sendPacket(ExResponseCommissionBuyItem.FAILED);
		}
		else
		{
			Item itemInstance = commissionItem.getItemInstance();
			if (itemInstance.getOwnerId() == player.getObjectId())
			{
				player.sendPacket(SystemMessageId.ITEM_PURCHASE_HAS_FAILED);
				player.sendPacket(ExResponseCommissionBuyItem.FAILED);
			}
			else if (player.isInventoryUnder80(false) && player.getWeightPenalty() < 3)
			{
				long totalPrice = itemInstance.getCount() * commissionItem.getPricePerUnit();
				if (!player.getInventory().reduceAdena(ItemProcessType.FEE, totalPrice, player, null))
				{
					player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
					player.sendPacket(ExResponseCommissionBuyItem.FAILED);
				}
				else if (this._commissionItems.remove(commissionId) != null && commissionItem.getSaleEndTask().cancel(false))
				{
					if (this.deleteItemFromDB(commissionId))
					{
						float discountFee = commissionItem.getDiscountInPercentage() / 100.0F;
						long saleFee = (long) Math.max(1000.0, totalPrice * 0.005 * Math.min(commissionItem.getDurationInDays(), 7));
						long addDiscount = (long) (saleFee * discountFee);
						Message mail = new Message(itemInstance.getOwnerId(), itemInstance, MailType.COMMISSION_ITEM_SOLD);
						Mail attachement = mail.createAttachments();
						attachement.addItem(ItemProcessType.SELL, 57, totalPrice - saleFee + addDiscount, player, null);
						MailManager.getInstance().sendMessage(mail);
						player.sendPacket(new ExResponseCommissionBuyItem(commissionItem));
						player.getInventory().addItem(ItemProcessType.BUY, commissionItem.getItemInstance(), player, null);
					}
					else
					{
						player.getInventory().addAdena(ItemProcessType.REFUND, totalPrice, player, null);
						player.sendPacket(ExResponseCommissionBuyItem.FAILED);
					}
				}
				else
				{
					player.getInventory().addAdena(ItemProcessType.REFUND, totalPrice, player, null);
					player.sendPacket(SystemMessageId.ITEM_PURCHASE_HAS_FAILED);
					player.sendPacket(ExResponseCommissionBuyItem.FAILED);
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.TO_BUY_CANCEL_YOU_NEED_TO_FREE_20_OF_WEIGHT_AND_10_OF_SLOTS_IN_YOUR_INVENTORY);
				player.sendPacket(ExResponseCommissionBuyItem.FAILED);
			}
		}
	}

	private boolean deleteItemFromDB(long commissionId)
	{
		try
		{
			boolean var5;
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM `commission_items` WHERE `commission_id` = ?");)
			{
				ps.setLong(1, commissionId);
				if (ps.executeUpdate() <= 0)
				{
					return false;
				}

				var5 = true;
			}

			return var5;
		}
		catch (SQLException var11)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Failed deleting commission item. Commission ID: " + commissionId, var11);
			return false;
		}
	}

	private void expireSale(CommissionItem commissionItem)
	{
		if (this._commissionItems.remove(commissionItem.getCommissionId()) != null && this.deleteItemFromDB(commissionItem.getCommissionId()))
		{
			Message mail = new Message(commissionItem.getItemInstance().getOwnerId(), commissionItem.getItemInstance(), MailType.COMMISSION_ITEM_RETURNED);
			MailManager.getInstance().sendMessage(mail);
		}
	}

	public CommissionItem getCommissionItem(long commissionId)
	{
		return this._commissionItems.get(commissionId);
	}

	public boolean hasCommissionItems(int objectId)
	{
		for (CommissionItem item : this._commissionItems.values())
		{
			if (item.getItemInstance().getObjectId() == objectId)
			{
				return true;
			}
		}

		return false;
	}

	public boolean hasCommissionedItemId(Player player, int itemId)
	{
		for (CommissionItem item : this._commissionItems.values())
		{
			if (item.getItemInstance().getOwnerId() == player.getObjectId() && item.getItemInstance().getTemplate().getId() == itemId)
			{
				return true;
			}
		}

		return false;
	}

	public static boolean isPlayerAllowedToInteract(Player player)
	{
		Npc npc = player.getLastFolkNPC();
		return npc instanceof CommissionManager ? npc.calculateDistance3D(player) <= 250.0 : false;
	}

	public static ItemCommissionManager getInstance()
	{
		return ItemCommissionManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ItemCommissionManager INSTANCE = new ItemCommissionManager();
	}
}
