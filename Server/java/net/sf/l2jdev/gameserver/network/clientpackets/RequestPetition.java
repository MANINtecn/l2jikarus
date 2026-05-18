package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.AdminData;
import net.sf.l2jdev.gameserver.managers.PetitionManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestPetition extends ClientPacket
{
	private String _content;
	private int _type;

	@Override
	protected void readImpl()
	{
		this._content = this.readString();
		this._type = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._type > 0 && this._type < 10)
			{
				if (!AdminData.getInstance().isGmOnline(false))
				{
					player.sendPacket(SystemMessageId.THERE_ARE_NO_GMS_CURRENTLY_VISIBLE_IN_THE_PUBLIC_LIST_AS_THEY_MAY_BE_PERFORMING_OTHER_FUNCTIONS_AT_THE_MOMENT);
				}
				else if (!PetitionManager.getInstance().isPetitioningAllowed())
				{
					player.sendPacket(SystemMessageId.UNABLE_TO_CONNECT_TO_THE_GLOBAL_SUPPORT_SERVER);
				}
				else if (PetitionManager.getInstance().isPlayerPetitionPending(player))
				{
					player.sendPacket(SystemMessageId.YOUR_GLOBAL_SUPPORT_REQUEST_WAS_RECEIVED);
				}
				else if (PetitionManager.getInstance().getPendingPetitionCount() == PlayerConfig.MAX_PETITIONS_PENDING)
				{
					player.sendPacket(SystemMessageId.UNABLE_TO_SEND_YOUR_REQUEST_TO_THE_GLOBAL_SUPPORT_PLEASE_TRY_AGAIN_LATER);
				}
				else
				{
					int totalPetitions = PetitionManager.getInstance().getPlayerTotalPetitionCount(player) + 1;
					if (totalPetitions > PlayerConfig.MAX_PETITIONS_PER_PLAYER)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_SUBMITTED_MAXIMUM_NUMBER_OF_S1_GLOBAL_SUPPORT_REQUESTS_TODAY_YOU_CANNOT_SUBMIT_MORE_REQUESTS);
						sm.addInt(totalPetitions);
						player.sendPacket(sm);
					}
					else if (this._content.length() > 255)
					{
						player.sendPacket(SystemMessageId.YOUR_GLOBAL_SUPPORT_REQUEST_CAN_CONTAIN_UP_TO_800_CHARACTERS);
					}
					else
					{
						int petitionId = PetitionManager.getInstance().submitPetition(player, this._content, this._type);
						SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_GLOBAL_SUPPORT_REQUEST_WAS_RECEIVED_REQUEST_NO_S1);
						sm.addInt(petitionId);
						player.sendPacket(sm);
						sm = new SystemMessage(SystemMessageId.SUPPORT_RECEIVED_S1_TIME_S_GLOBAL_SUPPORT_REQUESTS_LEFT_FOR_TODAY_S2);
						sm.addInt(totalPetitions);
						sm.addInt(PlayerConfig.MAX_PETITIONS_PER_PLAYER - totalPetitions);
						player.sendPacket(sm);
						sm = new SystemMessage(SystemMessageId.S1_USERS_ARE_IN_LINE_TO_GET_THE_GLOBAL_SUPPORT);
						sm.addInt(PetitionManager.getInstance().getPendingPetitionCount());
						player.sendPacket(sm);
					}
				}
			}
		}
	}
}
