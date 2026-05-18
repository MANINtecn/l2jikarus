package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.data.xml.FakePlayerData;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExDuelAskStart;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestDuelStart extends ClientPacket
{
	private String _player;
	private int _partyDuel;

	@Override
	protected void readImpl()
	{
		this._player = this.readString();
		this._partyDuel = this.readInt();
	}

	protected void scheduleDeny(Player player, String name)
	{
		if (player != null)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL);
			sm.addString(name);
			player.sendPacket(sm);
			player.onTransactionResponse();
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (FakePlayerData.getInstance().isTalkable(this._player))
			{
				String name = FakePlayerData.getInstance().getProperName(this._player);
				if (!player.isInsideZone(ZoneId.PVP) && !player.isInsideZone(ZoneId.PEACE) && !player.isInsideZone(ZoneId.SIEGE))
				{
					boolean npcInRange = false;

					for (Npc npc : World.getInstance().getVisibleObjectsInRange(player, Npc.class, 250))
					{
						if (npc.getName().equals(name))
						{
							npcInRange = true;
						}
					}

					if (!npcInRange)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_TOO_FAR_AWAY_TO_RECEIVE_A_DUEL_CHALLENGE);
						sm.addString(name);
						player.sendPacket(sm);
					}
					else if (player.isProcessingRequest())
					{
						SystemMessage msg = new SystemMessage(SystemMessageId.C1_IS_ON_ANOTHER_TASK_PLEASE_TRY_AGAIN_LATER);
						msg.addString(name);
						player.sendPacket(msg);
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_BEEN_CHALLENGED_TO_A_DUEL);
						sm.addString(name);
						player.sendPacket(sm);
						ThreadPool.schedule(() -> this.scheduleDeny(player, name), 10000L);
						player.blockRequest();
					}
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_IN_AN_AREA_WHERE_DUEL_IS_NOT_ALLOWED_AND_YOU_CANNOT_APPLY_FOR_A_DUEL);
					sm.addString(name);
					player.sendPacket(sm);
				}
			}
			else
			{
				Player targetChar = World.getInstance().getPlayer(this._player);
				if (targetChar == null)
				{
					player.sendPacket(SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL);
				}
				else if (player == targetChar)
				{
					player.sendPacket(SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL);
				}
				else if (!player.canDuel())
				{
					player.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
				}
				else if (!targetChar.canDuel())
				{
					player.sendPacket(targetChar.getNoDuelReason());
				}
				else if (!player.isInsideRadius2D(targetChar, 250))
				{
					SystemMessage msg = new SystemMessage(SystemMessageId.C1_IS_TOO_FAR_AWAY_TO_RECEIVE_A_DUEL_CHALLENGE);
					msg.addString(targetChar.getName());
					player.sendPacket(msg);
				}
				else
				{
					if (this._partyDuel == 1)
					{
						Party party = player.getParty();
						if (party == null || !party.isLeader(player))
						{
							player.sendMessage("You have to be the leader of a party in order to request a party duel.");
							return;
						}

						if (!targetChar.isInParty())
						{
							player.sendPacket(SystemMessageId.SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY);
							return;
						}

						if (player.getParty().containsPlayer(targetChar))
						{
							player.sendMessage("This player is a member of your own party.");
							return;
						}

						for (Player temp : player.getParty().getMembers())
						{
							if (!temp.canDuel())
							{
								player.sendMessage("Not all the members of your party are ready for a duel.");
								return;
							}
						}

						Player partyLeader = null;

						for (Player tempx : targetChar.getParty().getMembers())
						{
							if (partyLeader == null)
							{
								partyLeader = tempx;
							}

							if (!tempx.canDuel())
							{
								player.sendPacket(SystemMessageId.THE_OPPOSING_PARTY_IS_CURRENTLY_UNABLE_TO_ACCEPT_A_CHALLENGE_TO_A_DUEL);
								return;
							}
						}

						if (partyLeader != null)
						{
							if (!partyLeader.isProcessingRequest())
							{
								player.onTransactionRequest(partyLeader);
								partyLeader.sendPacket(new ExDuelAskStart(player.getName(), this._partyDuel));
								SystemMessage msg = new SystemMessage(SystemMessageId.C1_S_PARTY_HAS_BEEN_CHALLENGED_TO_A_DUEL);
								msg.addString(partyLeader.getName());
								player.sendPacket(msg);
								msg = new SystemMessage(SystemMessageId.C1_S_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL);
								msg.addString(player.getName());
								targetChar.sendPacket(msg);
							}
							else
							{
								SystemMessage msg = new SystemMessage(SystemMessageId.C1_IS_ON_ANOTHER_TASK_PLEASE_TRY_AGAIN_LATER);
								msg.addString(partyLeader.getName());
								player.sendPacket(msg);
							}
						}
					}
					else if (!targetChar.isProcessingRequest())
					{
						player.onTransactionRequest(targetChar);
						targetChar.sendPacket(new ExDuelAskStart(player.getName(), this._partyDuel));
						SystemMessage msg = new SystemMessage(SystemMessageId.C1_HAS_BEEN_CHALLENGED_TO_A_DUEL);
						msg.addString(targetChar.getName());
						player.sendPacket(msg);
						msg = new SystemMessage(SystemMessageId.C1_HAS_CHALLENGED_YOU_TO_A_DUEL);
						msg.addString(player.getName());
						targetChar.sendPacket(msg);
					}
					else
					{
						SystemMessage msg = new SystemMessage(SystemMessageId.C1_IS_ON_ANOTHER_TASK_PLEASE_TRY_AGAIN_LATER);
						msg.addString(targetChar.getName());
						player.sendPacket(msg);
					}
				}
			}
		}
	}
}
