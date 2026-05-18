package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.data.xml.AdminData;
import net.sf.l2jdev.gameserver.data.xml.FakePlayerData;
import net.sf.l2jdev.gameserver.managers.MailManager;
import net.sf.l2jdev.gameserver.model.AccessLevel;
import net.sf.l2jdev.gameserver.model.BlockList;
import net.sf.l2jdev.gameserver.model.Message;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.itemcontainer.Mail;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExNoticePostSent;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestSendPost extends ClientPacket
{
 
	private String _receiver;
	private boolean _isCod;
	private String _subject;
	private String _text;
	private RequestSendPost.AttachmentItem[] _items = null;
	private long _reqAdena;

	@Override
	protected void readImpl()
	{
		this._receiver = this.readString();
		this._isCod = this.readInt() != 0;
		this._subject = this.readString();
		this._text = this.readString();
		int attachCount = this.readInt();
		if (attachCount >= 0 && attachCount <= PlayerConfig.MAX_ITEM_IN_PACKET && attachCount * 12 + 8 == this.remaining())
		{
			if (attachCount > 0)
			{
				this._items = new RequestSendPost.AttachmentItem[attachCount];

				for (int i = 0; i < attachCount; i++)
				{
					int objectId = this.readInt();
					long count = this.readLong();
					if (objectId < 1 || count < 1L)
					{
						this._items = null;
						return;
					}

					this._items[i] = new RequestSendPost.AttachmentItem(objectId, count);
				}
			}

			this._reqAdena = this.readLong();
			if (this._reqAdena < 0L)
			{
				this._items = null;
			}
		}
	}

	@Override
	protected void runImpl()
	{
		if (GeneralConfig.ALLOW_MAIL)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				if (!GeneralConfig.ALLOW_ATTACHMENTS)
				{
					this._items = null;
					this._isCod = false;
					this._reqAdena = 0L;
				}

				if (!player.getAccessLevel().allowTransaction())
				{
					player.sendMessage("Transactions are disabled for your Access Level.");
				}
				else if (player.isInCombat() && this._items != null)
				{
					player.sendPacket(SystemMessageId.NOT_AVAILABLE_IN_COMBAT);
				}
				else if (player.isDead() && this._items != null)
				{
					player.sendPacket(SystemMessageId.YOU_ARE_DEAD_AND_CANNOT_PERFORM_THIS_ACTION);
				}
				else if (player.getActiveTradeList() != null)
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_SEND_ANYTHING_WHILE_TRADING);
				}
				else if (player.isInventoryDisabled())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_SEND_ANYTHING_WHILE_TRADING);
				}
				else if (player.hasItemRequest())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_SEND_ANYTHING_WHILE_ENCHANTING_IMBUING_WITH_ATTRIBUTES_OR_COMPOUNDING_WITH_JEWELS);
				}
				else if (player.isInStoreMode())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_SEND_ANYTHING_WITH_THE_PRIVATE_STORE_OR_WORKSHOP_OPEN);
				}
				else if (this._receiver.length() > 16)
				{
					player.sendPacket(SystemMessageId.THE_ALLOWED_LENGTH_FOR_RECIPIENT_EXCEEDED);
				}
				else if (this._subject.length() > 128)
				{
					player.sendPacket(SystemMessageId.THE_ALLOWED_LENGTH_FOR_A_TITLE_EXCEEDED);
				}
				else if (this._text.length() > 512)
				{
					player.sendPacket(SystemMessageId.THE_ALLOWED_LENGTH_FOR_A_TITLE_EXCEEDED);
				}
				else if (this._items != null && this._items.length > 8)
				{
					player.sendPacket(SystemMessageId.ITEM_SELECTION_IS_POSSIBLE_UP_TO_8);
				}
				else if (this._reqAdena >= 0L && this._reqAdena <= Inventory.MAX_ADENA)
				{
					if (this._isCod)
					{
						if (this._reqAdena == 0L)
						{
							player.sendPacket(SystemMessageId.MIN_PAYABLE_AMOUNT_S1_ADENA);
							return;
						}

						if (this._items == null || this._items.length == 0)
						{
							player.sendPacket(SystemMessageId.ATTACH_AN_ITEM_TO_SEND_IT_BY_PAID_MAIL);
							return;
						}
					}

					if (FakePlayerData.getInstance().isTalkable(this._receiver))
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_BLOCKED_YOU_YOU_CANNOT_SEND_MAIL_TO_THIS_CHARACTER);
						sm.addString(FakePlayerData.getInstance().getProperName(this._receiver));
						player.sendPacket(sm);
					}
					else
					{
						int receiverId = CharInfoTable.getInstance().getIdByName(this._receiver);
						if (receiverId <= 0)
						{
							player.sendPacket(SystemMessageId.A_LETTER_WILL_NOT_BE_DELIVERED_IF_THE_RECIPIENT_S_NAME_IS_NOT_FOUND_OR_THE_CHARACTER_HAS_BEEN_DELETED);
						}
						else if (receiverId == player.getObjectId())
						{
							player.sendPacket(SystemMessageId.YOU_CANNOT_SEND_A_MAIL_TO_YOURSELF);
						}
						else
						{
							int level = CharInfoTable.getInstance().getAccessLevelById(receiverId);
							AccessLevel accessLevel = AdminData.getInstance().getAccessLevel(level);
							if (accessLevel != null && accessLevel.isGm() && !player.getAccessLevel().isGm())
							{
								SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_MESSAGE_TO_C1_DID_NOT_REACH_ITS_RECIPIENT_YOU_CANNOT_SEND_MAIL_TO_THE_GM_STAFF);
								sm.addString(this._receiver);
								player.sendPacket(sm);
							}
							else if (player.isJailed() && (GeneralConfig.JAIL_DISABLE_TRANSACTION && this._items != null || GeneralConfig.JAIL_DISABLE_CHAT))
							{
								player.sendPacket(SystemMessageId.YOU_CAN_ONLY_SEND_MAIL_WHEN_IN_A_PEACEFUL_ZONE);
							}
							else if (BlockList.isInBlockList(receiverId, player.getObjectId()))
							{
								SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_BLOCKED_YOU_YOU_CANNOT_SEND_MAIL_TO_THIS_CHARACTER);
								sm.addString(this._receiver);
								player.sendPacket(sm);
							}
							else if (MailManager.getInstance().getOutboxSize(player.getObjectId()) >= 240)
							{
								player.sendPacket(SystemMessageId.THE_MAIL_LIMIT_240_HAS_BEEN_EXCEEDED_AND_THIS_CANNOT_BE_FORWARDED);
							}
							else if (MailManager.getInstance().getInboxSize(receiverId) >= 240)
							{
								player.sendPacket(SystemMessageId.THE_MAIL_LIMIT_240_HAS_BEEN_EXCEEDED_AND_THIS_CANNOT_BE_FORWARDED);
							}
							else if (!this.getClient().getFloodProtectors().canSendMail())
							{
								player.sendPacket(SystemMessageId.THE_PREVIOUS_MAIL_WAS_SENT_LESS_THAN_10_SEC_AGO_WAIT_A_BIT_AND_TRY_AGAIN);
							}
							else
							{
								Message msg = new Message(player.getObjectId(), receiverId, this._isCod, this._subject, this._text, this._reqAdena);
								if (this.removeItems(player, msg))
								{
									player.setMultiSell(null);
									MailManager.getInstance().sendMessage(msg);
									player.sendPacket(ExNoticePostSent.valueOf(true));
									player.sendPacket(SystemMessageId.THE_MAIL_IS_SENT_2);
								}
							}
						}
					}
				}
			}
		}
	}

	private boolean removeItems(Player player, Message msg)
	{
		long currentAdena = player.getAdena();
		long fee = 100L;
		if (this._items != null)
		{
			for (RequestSendPost.AttachmentItem i : this._items)
			{
				Item item = player.checkItemManipulation(i.getObjectId(), i.getCount(), "attach");
				if (item == null || !item.isTradeable() || item.isEquipped())
				{
					player.sendPacket(SystemMessageId.THE_ITEM_THAT_YOU_ARE_TRYING_TO_SEND_DOES_NOT_MEET_THE_REQUIREMENTS);
					return false;
				}

				fee += 1000L;
				if (item.getId() == 57)
				{
					currentAdena -= i.getCount();
				}
			}
		}

		if (currentAdena < fee || !player.reduceAdena(ItemProcessType.FEE, fee, null, false))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_SEND_ANYTHING_AS_YOU_DO_NOT_HAVE_ENOUGH_MONEY);
			return false;
		}
		else if (this._items == null)
		{
			return true;
		}
		else
		{
			Mail attachments = msg.createAttachments();
			if (attachments == null)
			{
				return false;
			}
			InventoryUpdate playerIU = new InventoryUpdate();

			for (RequestSendPost.AttachmentItem i : this._items)
			{
				Item oldItem = player.checkItemManipulation(i.getObjectId(), i.getCount(), "attach");
				if (oldItem == null || !oldItem.isTradeable() || oldItem.isEquipped())
				{
					PacketLogger.warning("Error adding attachment for char " + player.getName() + " (olditem == null)");
					return false;
				}

				Item newItem = player.getInventory().transferItem(ItemProcessType.TRANSFER, i.getObjectId(), i.getCount(), attachments, player, msg.getReceiverName() + "[" + msg.getReceiverId() + "]");
				if (newItem == null)
				{
					PacketLogger.warning("Error adding attachment for char " + player.getName() + " (newitem == null)");
				}
				else
				{
					newItem.setItemLocation(newItem.getItemLocation(), msg.getId());
					if (oldItem.getCount() > 0L && oldItem != newItem)
					{
						playerIU.addModifiedItem(oldItem);
					}
					else
					{
						playerIU.addRemovedItem(oldItem);
					}
				}
			}

			player.sendInventoryUpdate(playerIU);
			return true;
		}
	}

	private static class AttachmentItem
	{
		private final int _objectId;
		private final long _count;

		public AttachmentItem(int id, long num)
		{
			this._objectId = id;
			this._count = num;
		}

		public int getObjectId()
		{
			return this._objectId;
		}

		public long getCount()
		{
			return this._count;
		}
	}
}
