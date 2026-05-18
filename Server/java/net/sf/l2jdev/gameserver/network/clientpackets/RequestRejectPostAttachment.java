package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.managers.MailManager;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.Message;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.MailType;
import net.sf.l2jdev.gameserver.network.serverpackets.ExChangePostState;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestRejectPostAttachment extends ClientPacket
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
		if (GeneralConfig.ALLOW_MAIL && GeneralConfig.ALLOW_ATTACHMENTS)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				if (this.getClient().getFloodProtectors().canPerformTransaction())
				{
					Message msg = MailManager.getInstance().getMessage(this._msgId);
					if (msg != null)
					{
						if (msg.getReceiverId() != player.getObjectId())
						{
							PunishmentManager.handleIllegalPlayerAction(player, player + " tried to reject not own attachment!", GeneralConfig.DEFAULT_PUNISH);
						}
						else if (msg.hasAttachments() && msg.getMailType() == MailType.REGULAR)
						{
							MailManager.getInstance().sendMessage(new Message(msg));
							player.sendPacket(SystemMessageId.THE_MAIL_HAS_BEEN_RETURNED);
							player.sendPacket(new ExChangePostState(true, this._msgId, 2));
							Player sender = World.getInstance().getPlayer(msg.getSenderId());
							if (sender != null)
							{
								SystemMessage sm = new SystemMessage(SystemMessageId.S1_RETURNED_THE_MAIL);
								sm.addString(player.getName());
								sender.sendPacket(sm);
							}
						}
					}
				}
			}
		}
	}
}
