package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanAccess;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;
import net.sf.l2jdev.gameserver.model.clan.ClanWar;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanWarState;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.UserInfoType;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeReceiveWarList;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestStartPledgeWar extends ClientPacket
{
	private String _pledgeName;

	@Override
	protected void readImpl()
	{
		this._pledgeName = this.readString();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Clan clanDeclaringWar = player.getClan();
			if (clanDeclaringWar != null)
			{
				if (clanDeclaringWar.getLevel() < 3 || clanDeclaringWar.getMembersCount() < PlayerConfig.ALT_CLAN_MEMBERS_FOR_WAR)
				{
					player.sendPacket(SystemMessageId.A_CLAN_WAR_CAN_ONLY_BE_DECLARED_IF_THE_CLAN_IS_LV_3_OR_HIGHER_AND_THE_NUMBER_OF_CLAN_MEMBERS_IS_15_OR_GREATER);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else if (!player.hasAccess(ClanAccess.WAR_DECLARATION))
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else if (clanDeclaringWar.getWarCount() >= 30)
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_DECLARE_WAR_ON_MORE_THAN_30_CLANS_AT_A_TIME);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					Clan clanDeclaredWar = ClanTable.getInstance().getClanByName(this._pledgeName);
					if (clanDeclaredWar == null)
					{
						player.sendPacket(SystemMessageId.A_CLAN_WAR_CANNOT_BE_DECLARED_AGAINST_A_CLAN_THAT_DOES_NOT_EXIST);
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else if (clanDeclaredWar == clanDeclaringWar)
					{
						player.sendPacket(SystemMessageId.FOOL_YOU_CANNOT_DECLARE_WAR_AGAINST_YOUR_OWN_CLAN);
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else if (clanDeclaringWar.getAllyId() == clanDeclaredWar.getAllyId() && clanDeclaringWar.getAllyId() != 0)
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_DECLARE_WAR_ON_AN_ALLIED_CLAN_2);
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else if (clanDeclaredWar.getLevel() >= 3 && clanDeclaredWar.getMembersCount() >= PlayerConfig.ALT_CLAN_MEMBERS_FOR_WAR)
					{
						if (clanDeclaredWar.getDissolvingExpiryTime() > System.currentTimeMillis())
						{
							player.sendPacket(SystemMessageId.A_CLAN_WAR_CAN_NOT_BE_DECLARED_AGAINST_A_CLAN_THAT_IS_BEING_DISSOLVED);
							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
						else
						{
							ClanWar clanWar = clanDeclaringWar.getWarWith(clanDeclaredWar.getId());
							if (clanWar != null)
							{
								if (clanWar.getClanWarState(clanDeclaringWar) == ClanWarState.WIN)
								{
									SystemMessage sm = new SystemMessage(SystemMessageId.YOU_CANNOT_DECLARE_WAR_AS_THE_21_DAY_PERIOD_HAS_NOT_PASSED_SINCE_THE_DEFEAT_FROM_THE_S1_CLAN);
									sm.addString(clanDeclaredWar.getName());
									player.sendPacket(sm);
									player.sendPacket(ActionFailed.STATIC_PACKET);
									return;
								}

								if (clanWar.getState() == ClanWarState.MUTUAL)
								{
									player.sendMessage("You have already been at war with " + clanDeclaredWar.getName() + ".");
									player.sendPacket(ActionFailed.STATIC_PACKET);
									return;
								}

								if (clanWar.getState() == ClanWarState.BLOOD_DECLARATION)
								{
									clanWar.mutualClanWarAccepted(clanDeclaredWar, clanDeclaringWar);
									ClanTable.getInstance().storeClanWars(clanWar);

									for (ClanMember member : clanDeclaringWar.getMembers())
									{
										if (member != null && member.isOnline())
										{
											member.getPlayer().broadcastUserInfo(UserInfoType.CLAN);
										}
									}

									for (ClanMember memberx : clanDeclaredWar.getMembers())
									{
										if (memberx != null && memberx.isOnline())
										{
											memberx.getPlayer().broadcastUserInfo(UserInfoType.CLAN);
										}
									}

									player.sendPacket(new PledgeReceiveWarList(player.getClan(), 0));
									return;
								}
							}

							ClanWar newClanWar = new ClanWar(clanDeclaringWar, clanDeclaredWar);
							ClanTable.getInstance().storeClanWars(newClanWar);

							for (ClanMember memberxx : clanDeclaringWar.getMembers())
							{
								if (memberxx != null && memberxx.isOnline())
								{
									memberxx.getPlayer().broadcastUserInfo(UserInfoType.CLAN);
								}
							}

							for (ClanMember memberxxx : clanDeclaredWar.getMembers())
							{
								if (memberxxx != null && memberxxx.isOnline())
								{
									memberxxx.getPlayer().broadcastUserInfo(UserInfoType.CLAN);
								}
							}

							player.sendPacket(new PledgeReceiveWarList(player.getClan(), 0));
						}
					}
					else
					{
						player.sendPacket(SystemMessageId.A_CLAN_WAR_CAN_ONLY_BE_DECLARED_IF_THE_CLAN_IS_LV_3_OR_HIGHER_AND_THE_NUMBER_OF_CLAN_MEMBERS_IS_15_OR_GREATER);
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
				}
			}
		}
	}
}
