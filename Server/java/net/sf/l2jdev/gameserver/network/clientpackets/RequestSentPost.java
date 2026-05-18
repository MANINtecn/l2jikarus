package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.managers.MailManager;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.Message;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExReplySentPost;

public class RequestSentPost extends ClientPacket
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
				if (msg.getSenderId() != player.getObjectId())
				{
					PunishmentManager.handleIllegalPlayerAction(player, player + " tried to read not own post!", GeneralConfig.DEFAULT_PUNISH);
				}
				else if (!msg.isDeletedBySender())
				{
					player.sendPacket(new ExReplySentPost(msg));
				}
			}
		}
	}
}
