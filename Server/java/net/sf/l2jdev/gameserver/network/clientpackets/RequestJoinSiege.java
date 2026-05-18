package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanAccess;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.castlewar.MercenaryCastleWarCastleSiegeAttackerList;
import net.sf.l2jdev.gameserver.network.serverpackets.castlewar.MercenaryCastleWarCastleSiegeDefenderList;

public class RequestJoinSiege extends ClientPacket
{
	private int _castleId;
	private int _isAttacker;
	private int _isJoining;

	@Override
	protected void readImpl()
	{
		this._castleId = this.readInt();
		this._isAttacker = this.readInt();
		this._isJoining = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (!player.hasAccess(ClanAccess.CASTLE_SIEGE))
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			}
			else
			{
				Clan clan = player.getClan();
				if (clan != null)
				{
					Castle castle = CastleManager.getInstance().getCastleById(this._castleId);
					if (castle != null)
					{
						if (this._isJoining == 1)
						{
							if (System.currentTimeMillis() < clan.getDissolvingExpiryTime())
							{
								player.sendPacket(SystemMessageId.YOUR_CLAN_MAY_NOT_REGISTER_TO_PARTICIPATE_IN_A_SIEGE_WHILE_UNDER_A_GRACE_PERIOD_OF_THE_CLAN_S_DISSOLUTION);
								return;
							}

							if (this._isAttacker == 1)
							{
								castle.getSiege().registerAttacker(player);
								player.sendPacket(new MercenaryCastleWarCastleSiegeAttackerList(castle.getResidenceId()));
							}
							else
							{
								castle.getSiege().registerDefender(player);
								player.sendPacket(new MercenaryCastleWarCastleSiegeDefenderList(castle.getResidenceId()));
							}
						}
						else
						{
							if (clan.isRecruitMercenary() && clan.getMapMercenary().size() > 0)
							{
								return;
							}

							castle.getSiege().removeSiegeClan(player);
							if (this._isAttacker == 1)
							{
								player.sendPacket(new MercenaryCastleWarCastleSiegeAttackerList(castle.getResidenceId()));
							}
							else
							{
								player.sendPacket(new MercenaryCastleWarCastleSiegeDefenderList(castle.getResidenceId()));
							}
						}
					}
				}
			}
		}
	}
}
