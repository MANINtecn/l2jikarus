package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.managers.MailManager;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.Message;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExChangePostState;

public class RequestDeleteReceivedPost extends ClientPacket
{
 
	int[] _msgIds = null;

	@Override
	protected void readImpl()
	{
		int count = this.readInt();
		if (count > 0 && count <= PlayerConfig.MAX_ITEM_IN_PACKET && count * 4 == this.remaining())
		{
			this._msgIds = new int[count];

			for (int i = 0; i < count; i++)
			{
				this._msgIds[i] = this.readInt();
			}
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null && this._msgIds != null && GeneralConfig.ALLOW_MAIL)
		{
			for (int msgId : this._msgIds)
			{
				Message msg = MailManager.getInstance().getMessage(msgId);
				if (msg != null)
				{
					if (msg.getReceiverId() != player.getObjectId())
					{
						PunishmentManager.handleIllegalPlayerAction(player, player + " tried to delete not own post!", GeneralConfig.DEFAULT_PUNISH);
						return;
					}

					if (msg.hasAttachments() || msg.isDeletedByReceiver())
					{
						return;
					}

					msg.setDeletedByReceiver();
				}
			}

			player.sendPacket(new ExChangePostState(true, this._msgIds, 0));
		}
	}
}
