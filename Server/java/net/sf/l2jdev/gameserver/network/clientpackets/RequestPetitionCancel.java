package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.AdminData;
import net.sf.l2jdev.gameserver.managers.PetitionManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.ChatType;
import net.sf.l2jdev.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestPetitionCancel extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (PetitionManager.getInstance().isPlayerInConsultation(player))
			{
				if (player.isGM())
				{
					PetitionManager.getInstance().endActivePetition(player);
				}
				else
				{
					player.sendPacket(SystemMessageId.YOUR_GLOBAL_SUPPORT_REQUEST_IS_BEING_PROCESSED);
				}
			}
			else if (PetitionManager.getInstance().isPlayerPetitionPending(player))
			{
				if (PetitionManager.getInstance().cancelActivePetition(player))
				{
					int numRemaining = PlayerConfig.MAX_PETITIONS_PER_PLAYER - PetitionManager.getInstance().getPlayerTotalPetitionCount(player);
					SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_GLOBAL_SUPPORT_REQUEST_HAS_BEEN_REVOKED_NUMBER_OR_REQUESTS_YOU_CAN_SEND_S1);
					sm.addString(String.valueOf(numRemaining));
					player.sendPacket(sm);
					String msgContent = player.getName() + " has canceled a pending petition.";
					AdminData.getInstance().broadcastToGMs(new CreatureSay(player, ChatType.HERO_VOICE, "Petition System", msgContent));
				}
				else
				{
					player.sendPacket(SystemMessageId.FAILED_TO_CANCEL_YOUR_GLOBAL_SUPPORT_REQUEST_PLEASE_TRY_AGAIN_LATER);
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.GLOBAL_SUPPORT_DOES_NOT_ACCEPT_REQUESTS_AT_THE_MOMENT);
			}
		}
	}
}
