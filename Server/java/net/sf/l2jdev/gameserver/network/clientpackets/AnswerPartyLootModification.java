package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.Party;

public class AnswerPartyLootModification extends ClientPacket
{
	public int _answer;

	@Override
	protected void readImpl()
	{
		this._answer = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Party party = player.getParty();
			if (party != null)
			{
				party.answerLootChangeRequest(player, this._answer == 1);
			}
		}
	}
}
