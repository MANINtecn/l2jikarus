package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.pledgeV3.ExAllianceCreateResult;

public class AllyCreate extends ClientPacket
{
	private String _allianceName;

	@Override
	protected void readImpl()
	{
		this.readInt();
		this._allianceName = this.readSizedString();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (player.getClan() == null)
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER_2);
				player.sendPacket(new ExAllianceCreateResult(0));
			}
			else if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.ONLY_CLAN_LEADERS_MAY_CREATE_ALLIANCES);
				player.sendPacket(new ExAllianceCreateResult(0));
			}
			else
			{
				Clan clan = player.getClan();
				if (clan.getAllyId() != 0)
				{
					player.sendPacket(SystemMessageId.YOU_ALREADY_BELONG_TO_ANOTHER_ALLIANCE);
					player.sendPacket(new ExAllianceCreateResult(0));
				}
				else if (clan.getId() == clan.getAllyId())
				{
					player.sendPacket(SystemMessageId.ALLIANCE_LEADERS_CANNOT_WITHDRAW);
					player.sendPacket(new ExAllianceCreateResult(0));
				}
				else
				{
					player.getClan().createAlly(player, this._allianceName);
				}
			}
		}
	}
}
