package net.sf.l2jdev.gameserver.network.clientpackets.adenlab;

import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.config.AdenLaboratoryConfig;
import net.sf.l2jdev.gameserver.managers.AdenLaboratoryManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.AdenLabRequest;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;

public class RequestAdenLabTranscendentEnchant extends ClientPacket
{
	private static final Logger LOGGER = Logger.getLogger(RequestAdenLabTranscendentEnchant.class.getName());
	private int _bossId;
	private int _feeIndex;

	@Override
	protected void readImpl()
	{
		this._bossId = this.readInt();
		this._feeIndex = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (AdenLaboratoryConfig.ADENLAB_ENABLED && !player.hasRequest(AdenLabRequest.class))
			{
				AdenLaboratoryManager.processRequestAdenLabTranscendentEnchant(player, (byte) this._bossId, this._feeIndex);
			}
			else
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				player.sendPacket(SystemMessageId.NOT_WORKING_PLEASE_TRY_AGAIN_LATER);
				LOGGER.warning("Player " + player.getName() + " [" + player.getObjectId() + "] tried to access the Aden Lab while it is disabled or has another active request.");
			}
		}
	}
}
