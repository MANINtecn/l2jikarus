package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.managers.MailManager;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.Message;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExChangePostState;
import net.sf.l2jdev.gameserver.network.serverpackets.ExReplyReceivedPost;

public class RequestReceivedPost extends ClientPacket
{
	private int _msgId;

	@Override
	protected void readImpl()
	{
		this._msgId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null && GeneralConfig.ALLOW_MAIL)
		{
			Message msg = MailManager.getInstance().getMessage(this._msgId);
			if (msg != null)
			{
				if (msg.getReceiverId() != player.getObjectId())
				{
					PunishmentManager.handleIllegalPlayerAction(player, player + " tried to receive not own post!", GeneralConfig.DEFAULT_PUNISH);
				}
				else if (!msg.isDeletedByReceiver())
				{
					player.sendPacket(new ExReplyReceivedPost(msg));
					player.sendPacket(new ExChangePostState(true, this._msgId, 1));
					msg.markAsRead();
				}
			}
		}
	}
}
