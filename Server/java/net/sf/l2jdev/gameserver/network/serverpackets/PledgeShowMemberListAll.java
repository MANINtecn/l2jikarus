package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.ServerConfig;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PledgeShowMemberListAll extends ServerPacket
{
	private final Clan _clan;
	private final Clan.SubPledge _pledge;
	private final String _name;
	private final String _leaderName;
	private final Collection<ClanMember> _members;
	private final int _pledgeId;
	private final boolean _isSubPledge;
	private String _allyMasterLeaderName = "";
	private String _allyMasterName = "";
	private String _allyClansNames = "";

	private PledgeShowMemberListAll(Clan clan, Clan.SubPledge pledge, boolean isSubPledge)
	{
		this._clan = clan;
		this._pledge = pledge;
		this._pledgeId = this._pledge == null ? 0 : this._pledge.getId();
		this._leaderName = pledge == null ? clan.getLeaderName() : CharInfoTable.getInstance().getNameById(pledge.getLeaderId());
		this._name = pledge == null ? clan.getName() : pledge.getName();
		this._members = this._clan.getMembers();
		this._isSubPledge = isSubPledge;
		if (clan.getAllyId() != 0)
		{
			Clan leaderAlyClan = ClanTable.getInstance().getClan(clan.getAllyId());
			if (leaderAlyClan.getLeader() != null)
			{
				this._allyMasterName = leaderAlyClan.getName();
				this._allyMasterLeaderName = leaderAlyClan.getLeaderName();
				List<Clan> allies = ClanTable.getInstance().getClanAllies(clan.getAllyId());
				if (allies != null && !allies.isEmpty())
				{
					this._allyClansNames = allies.stream().map(Clan::getName).filter(clanFilterName -> !clanFilterName.equalsIgnoreCase(this._allyMasterName)).collect(Collectors.joining(", "));
				}
			}
		}
	}

	public static void sendAllTo(Player player)
	{
		Clan clan = player.getClan();
		if (clan != null)
		{
			for (Clan.SubPledge subPledge : clan.getAllSubPledges())
			{
				player.sendPacket(new PledgeShowMemberListAll(clan, subPledge, false));
			}

			player.sendPacket(new PledgeShowMemberListAll(clan, null, true));
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PLEDGE_SHOW_MEMBER_LIST_ALL.writeId(this, buffer);
		buffer.writeInt(!this._isSubPledge);
		buffer.writeInt(this._clan.getId());
		buffer.writeInt(ServerConfig.SERVER_ID);
		buffer.writeInt(this._pledgeId);
		buffer.writeString(this._name);
		buffer.writeString(this._leaderName);
		buffer.writeInt(this._clan.getCrestId());
		buffer.writeInt(this._clan.getLevel());
		buffer.writeInt(this._clan.getCastleId());
		buffer.writeInt(0);
		buffer.writeInt(this._clan.getHideoutId());
		buffer.writeInt(this._clan.getFortId());
		buffer.writeInt(this._clan.getRank());
		buffer.writeInt(this._clan.getReputationScore());
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(this._clan.getAllyId());
		buffer.writeString(this._clan.getAllyName());
		buffer.writeInt(this._clan.getAllyCrestId());
		buffer.writeString(this._allyMasterLeaderName);
		buffer.writeString(this._allyMasterName);
		buffer.writeString(this._allyClansNames);
		buffer.writeInt(this._clan.isAtWar());
		buffer.writeInt(0);
		buffer.writeInt(this._clan.getSubPledgeMembersCount(this._pledgeId));

		for (ClanMember m : this._members)
		{
			if (m.getPledgeType() == this._pledgeId)
			{
				buffer.writeString(m.getName());
				buffer.writeInt(m.getLevel());
				buffer.writeInt(m.getClassId());
				Player player = m.getPlayer();
				if (player != null)
				{
					buffer.writeInt(player.getAppearance().isFemale());
					buffer.writeInt(player.getRace().ordinal());
				}
				else
				{
					buffer.writeInt(1);
					buffer.writeInt(1);
				}

				buffer.writeInt(m.isOnline() ? m.getObjectId() : 0);
				buffer.writeInt(m.getSponsor() != 0);
				buffer.writeByte(m.getOnlineStatus());
			}
		}
	}
}
