package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExMPCCShowPartyMemberInfo;

public class RequestExMPCCShowPartyMembersInfo extends ClientPacket
{
	private int _partyLeaderId;

	@Override
	protected void readImpl()
	{
		this._partyLeaderId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Player target = World.getInstance().getPlayer(this._partyLeaderId);
			if (target != null && target.getParty() != null)
			{
				player.sendPacket(new ExMPCCShowPartyMemberInfo(target.getParty()));
			}
		}
	}
}
