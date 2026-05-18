package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanAccess;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;

public class RequestPledgeReorganizeMember extends ClientPacket
{
	private int _isMemberSelected;
	private String _memberName;
	private int _newPledgeType;
	private String _selectedMember;

	@Override
	protected void readImpl()
	{
		this._isMemberSelected = this.readInt();
		this._memberName = this.readString();
		this._newPledgeType = this.readInt();
		this._selectedMember = this.readString();
	}

	@Override
	protected void runImpl()
	{
		if (this._isMemberSelected != 0)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				Clan clan = player.getClan();
				if (clan != null)
				{
					if (player.hasAccess(ClanAccess.MODIFY_RANKS))
					{
						ClanMember member1 = clan.getClanMember(this._memberName);
						if (member1 != null && member1.getObjectId() != clan.getLeaderId())
						{
							ClanMember member2 = clan.getClanMember(this._selectedMember);
							if (member2 != null && member2.getObjectId() != clan.getLeaderId())
							{
								int oldPledgeType = member1.getPledgeType();
								if (oldPledgeType != this._newPledgeType)
								{
									member1.setPledgeType(this._newPledgeType);
									member2.setPledgeType(oldPledgeType);
									clan.broadcastClanStatus();
								}
							}
						}
					}
				}
			}
		}
	}
}
