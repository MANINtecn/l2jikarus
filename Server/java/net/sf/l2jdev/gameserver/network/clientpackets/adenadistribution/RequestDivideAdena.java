package net.sf.l2jdev.gameserver.network.clientpackets.adenadistribution;

import java.util.List;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.AdenaDistributionRequest;
import net.sf.l2jdev.gameserver.model.groups.CommandChannel;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.adenadistribution.ExDivideAdenaCancel;
import net.sf.l2jdev.gameserver.network.serverpackets.adenadistribution.ExDivideAdenaDone;

public class RequestDivideAdena extends ClientPacket
{
	private int _adenaObjId;
	private long _adenaCount;

	@Override
	protected void readImpl()
	{
		this._adenaObjId = this.readInt();
		this._adenaCount = this.readLong();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			AdenaDistributionRequest request = player.getRequest(AdenaDistributionRequest.class);
			if (request != null)
			{
				if (request.getDistributor() != player)
				{
					this.cancelDistribution(request);
				}
				else if (request.getAdenaObjectId() != this._adenaObjId)
				{
					this.cancelDistribution(request);
				}
				else
				{
					Party party = player.getParty();
					if (party == null)
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_DISTRIBUTE_ADENA_IF_YOU_ARE_NOT_A_MEMBER_OF_AN_ALLIANCE_OR_A_COMMAND_CHANNEL);
						this.cancelDistribution(request);
					}
					else
					{
						CommandChannel commandChannel = party.getCommandChannel();
						if (commandChannel != null && !commandChannel.isLeader(player))
						{
							player.sendPacket(SystemMessageId.YOU_CANNOT_PROCEED_AS_YOU_ARE_NOT_AN_ALLIANCE_LEADER_OR_PARTY_LEADER);
							this.cancelDistribution(request);
						}
						else if (!party.isLeader(player))
						{
							player.sendPacket(SystemMessageId.YOU_CANNOT_PROCEED_AS_YOU_ARE_NOT_A_PARTY_LEADER);
							this.cancelDistribution(request);
						}
						else
						{
							List<Player> targets = commandChannel != null ? commandChannel.getMembers() : party.getMembers();
							if (player.getAdena() < targets.size())
							{
								player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_2);
								this.cancelDistribution(request);
							}
							else if (player.getAdena() < request.getAdenaCount())
							{
								player.sendPacket(SystemMessageId.THE_ADENA_IN_POSSESSION_HAS_BEEN_DECREASED_ADENA_DISTRIBUTION_HAS_BEEN_CANCELLED);
								this.cancelDistribution(request);
							}
							else if (targets.size() < request.getPlayers().size())
							{
								player.sendPacket(SystemMessageId.THE_DISTRIBUTION_PARTICIPANTS_HAVE_CHANGED_ADENA_DISTRIBUTION_HAS_BEEN_CANCELLED);
								this.cancelDistribution(request);
							}
							else if (player.getAdena() < this._adenaCount)
							{
								player.sendPacket(SystemMessageId.DISTRIBUTION_CANNOT_PROCEED_AS_THERE_IS_INSUFFICIENT_ADENA_FOR_DISTRIBUTION);
								this.cancelDistribution(request);
							}
							else
							{
								long memberAdenaGet = (long) Math.floor(this._adenaCount / targets.size());
								if (player.reduceAdena(ItemProcessType.TRANSFER, memberAdenaGet * targets.size(), player, false))
								{
									for (Player target : targets)
									{
										if (target != null)
										{
											target.addAdena(ItemProcessType.TRANSFER, memberAdenaGet, player, false);
											target.sendPacket(new ExDivideAdenaDone(party.isLeader(target), commandChannel != null && commandChannel.isLeader(target), this._adenaCount, memberAdenaGet, targets.size(), player.getName()));
											target.removeRequest(AdenaDistributionRequest.class);
										}
									}
								}
								else
								{
									this.cancelDistribution(request);
								}
							}
						}
					}
				}
			}
		}
	}

	protected void cancelDistribution(AdenaDistributionRequest request)
	{
		for (Player player : request.getPlayers())
		{
			if (player != null)
			{
				player.sendPacket(SystemMessageId.ADENA_DISTRIBUTION_HAS_BEEN_CANCELLED);
				player.sendPacket(ExDivideAdenaCancel.STATIC_PACKET);
				player.removeRequest(AdenaDistributionRequest.class);
			}
		}
	}
}
