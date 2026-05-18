package net.sf.l2jdev.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.gameserver.config.custom.CustomMailManagerConfig;
import net.sf.l2jdev.gameserver.model.Message;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemEnchantHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.Mail;
import net.sf.l2jdev.gameserver.network.enums.MailType;

public class CustomMailManager
{
	private static final Logger LOGGER = Logger.getLogger(CustomMailManager.class.getName());
	public static final String READ_SQL = "SELECT * FROM custom_mail";
	public static final String DELETE_SQL = "DELETE FROM custom_mail WHERE date=? AND receiver=?";

	protected CustomMailManager()
	{
		ThreadPool.scheduleAtFixedRate(() -> {
			try (Connection con = DatabaseFactory.getConnection(); Statement ps = con.createStatement(); ResultSet rs = ps.executeQuery("SELECT * FROM custom_mail");)
			{
				while (rs.next())
				{
					int playerId = rs.getInt("receiver");
					Player player = World.getInstance().getPlayer(playerId);
					if (player != null && player.isOnline())
					{
						String items = rs.getString("items");
						Message msg = new Message(playerId, rs.getString("subject"), rs.getString("message"), items.length() > 0 ? MailType.PRIME_SHOP_GIFT : MailType.REGULAR);
						List<ItemEnchantHolder> itemHolders = new ArrayList<>();

						for (String str : items.split(";"))
						{
							if (str.contains(" "))
							{
								String[] split = str.split(" ");
								String itemId = split[0];
								String itemCount = split[1];
								String enchant = split.length > 2 ? split[2] : "0";
								if (StringUtil.isNumeric(itemId) && StringUtil.isNumeric(itemCount))
								{
									itemHolders.add(new ItemEnchantHolder(Integer.parseInt(itemId), Long.parseLong(itemCount), Integer.parseInt(enchant)));
								}
							}
							else if (StringUtil.isNumeric(str))
							{
								itemHolders.add(new ItemEnchantHolder(Integer.parseInt(str), 1L));
							}
						}

						if (!itemHolders.isEmpty())
						{
							Mail attachments = msg.createAttachments();

							for (ItemEnchantHolder itemHolder : itemHolders)
							{
								Item item = attachments.addItem(ItemProcessType.TRANSFER, itemHolder.getId(), itemHolder.getCount(), null, null);
								if (itemHolder.getEnchantLevel() > 0)
								{
									item.setEnchantLevel(itemHolder.getEnchantLevel());
								}
							}
						}

						try (PreparedStatement stmt = con.prepareStatement("DELETE FROM custom_mail WHERE date=? AND receiver=?"))
						{
							stmt.setString(1, rs.getString("date"));
							stmt.setInt(2, playerId);
							stmt.execute();
						}
						catch (SQLException var22)
						{
							LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Error deleting entry from database: ", var22);
						}

						MailManager.getInstance().sendMessage(msg);
						LOGGER.info(this.getClass().getSimpleName() + ": Message sent to " + player.getName() + ".");
					}
				}
			}
			catch (SQLException var26)
			{
				LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Error reading from database: ", var26);
			}
		}, CustomMailManagerConfig.CUSTOM_MAIL_MANAGER_DELAY, CustomMailManagerConfig.CUSTOM_MAIL_MANAGER_DELAY);
		LOGGER.info(this.getClass().getSimpleName() + ": Enabled.");
	}

	public static CustomMailManager getInstance()
	{
		return CustomMailManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final CustomMailManager INSTANCE = new CustomMailManager();
	}
}
