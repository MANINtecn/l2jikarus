package net.sf.l2jdev.gameserver.network.clientpackets.adenlab;

import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.config.AdenLaboratoryConfig;
import net.sf.l2jdev.gameserver.managers.AdenLaboratoryManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.AdenLabRequest;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;

public class RequestAdenLabBossInfo extends ClientPacket
{
	private static final Logger LOGGER = Logger.getLogger(RequestAdenLabBossInfo.class.getName());
	private int _bossId;

	@Override
	protected void readImpl()
	{
		this._bossId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = this.getPlayer();
		if (activeChar != null)
		{
			if (AdenLaboratoryConfig.ADENLAB_ENABLED && !activeChar.hasRequest(AdenLabRequest.class))
			{
				AdenLaboratoryManager.processRequestAdenLabBossInfo(activeChar, (byte) this._bossId);
			}
			else
			{
				LOGGER.warning("Player " + activeChar.getName() + " [" + activeChar.getObjectId() + "] tried to access the Aden Lab while it is disabled or has another active request.");
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				activeChar.sendPacket(SystemMessageId.THE_FUNCTION_IS_UNAVAILABLE);
			}
		}
	}
}
