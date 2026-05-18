package net.sf.l2jdev.gameserver.network.clientpackets.adenlab;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.config.AdenLaboratoryConfig;
import net.sf.l2jdev.gameserver.managers.AdenLaboratoryManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.AdenLabRequest;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;

public class RequestAdenLabBossList extends ClientPacket
{
	private static final Logger LOGGER = Logger.getLogger(RequestAdenLabBossList.class.getName());
	private final List<Integer> _bossList = new ArrayList<>();

	@Override
	protected void readImpl()
	{
		while (this.remaining() > 0)
		{
			this._bossList.add(this.readInt());
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (AdenLaboratoryConfig.ADENLAB_ENABLED && !player.hasRequest(AdenLabRequest.class))
			{
				AdenLaboratoryManager.processRequestAdenLabBossList(player, this._bossList);
			}
			else
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				player.sendPacket(SystemMessageId.THE_FUNCTION_IS_UNAVAILABLE);
				LOGGER.warning("Player " + player.getName() + " [" + player.getObjectId() + "] tried to access the Aden Lab while it is disabled or has another active request.");
			}
		}
	}
}
