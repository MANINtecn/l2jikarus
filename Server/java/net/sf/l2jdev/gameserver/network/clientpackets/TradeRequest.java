package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.FakePlayerData;
import net.sf.l2jdev.gameserver.model.BlockList;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.SendTradeRequest;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class TradeRequest extends ClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
	}

	protected void scheduleDeny(Player player, String name)
	{
		if (player != null)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_DENIED_YOUR_REQUEST_TO_TRADE);
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
			if (!player.getAccessLevel().allowTransaction())
			{
				player.sendMessage("Transactions are disabled for your current Access Level.");
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				BuffInfo info = player.getEffectList().getFirstBuffInfoByAbnormalType(AbnormalType.BOT_PENALTY);
				if (info != null)
				{
					for (AbstractEffect effect : info.getEffects())
					{
						if (!effect.checkCondition(-2))
						{
							player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_ACTIONS_HAVE_BEEN_RESTRICTED);
							player.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
					}
				}

				WorldObject target = World.getInstance().findObject(this._objectId);
				if (target != null && player.isInSurroundingRegion(target) && target.getInstanceWorld() == player.getInstanceWorld())
				{
					if (target.getObjectId() == player.getObjectId())
					{
						player.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
					}
					else if (FakePlayerData.getInstance().isTalkable(target.getName()))
					{
						String name = FakePlayerData.getInstance().getProperName(target.getName());
						boolean npcInRange = false;

						for (Npc npc : World.getInstance().getVisibleObjectsInRange(player, Npc.class, 150))
						{
							if (npc.getName().equals(name))
							{
								npcInRange = true;
							}
						}

						if (!npcInRange)
						{
							player.sendPacket(SystemMessageId.YOUR_TARGET_IS_OUT_OF_RANGE);
						}
						else
						{
							if (!player.isProcessingRequest())
							{
								SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_REQUESTED_A_TRADE_WITH_C1);
								sm.addString(name);
								player.sendPacket(sm);
								ThreadPool.schedule(() -> this.scheduleDeny(player, name), 10000L);
								player.blockRequest();
							}
							else
							{
								player.sendPacket(SystemMessageId.YOU_ARE_ALREADY_TRADING_WITH_SOMEONE);
							}
						}
					}
					else if (!target.isPlayer())
					{
						player.sendPacket(SystemMessageId.INVALID_TARGET);
					}
					else
					{
						Player partner = target.asPlayer();
						if (!partner.isInOlympiadMode() && !player.isInOlympiadMode())
						{
							info = partner.getEffectList().getFirstBuffInfoByAbnormalType(AbnormalType.BOT_PENALTY);
							if (info != null)
							{
								for (AbstractEffect effectx : info.getEffects())
								{
									if (!effectx.checkCondition(-2))
									{
										SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_AND_IS_CURRENTLY_BEING_INVESTIGATED);
										sm.addString(partner.getName());
										player.sendPacket(sm);
										player.sendPacket(ActionFailed.STATIC_PACKET);
										return;
									}
								}
							}

							if (!PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_TRADE && player.getReputation() < 0)
							{
								player.sendMessage("You cannot trade while you are in a chaotic state.");
							}
							else if (!PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_TRADE && partner.getReputation() < 0)
							{
								player.sendMessage("You cannot request a trade while your target is in a chaotic state.");
							}
							else if (!GeneralConfig.JAIL_DISABLE_TRANSACTION || !player.isJailed() && !partner.isJailed())
							{
								if (player.isInStoreMode() || partner.isInStoreMode())
								{
									player.sendPacket(SystemMessageId.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
								}
								else if (player.isProcessingTransaction())
								{
									player.sendPacket(SystemMessageId.YOU_ARE_ALREADY_TRADING_WITH_SOMEONE);
								}
								else if (partner.isProcessingRequest() || partner.isProcessingTransaction())
								{
									SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_ALREADY_TRADING_WITH_ANOTHER_PERSON_PLEASE_TRY_AGAIN_LATER);
									sm.addString(partner.getName());
									player.sendPacket(sm);
								}
								else if (partner.getTradeRefusal())
								{
									player.sendMessage("That person is in trade refusal mode.");
								}
								else if (BlockList.isBlocked(partner, player))
								{
									SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_ADDED_YOU_TO_THEIR_IGNORE_LIST);
									sm.addString(partner.getName());
									player.sendPacket(sm);
								}
								else if (player.calculateDistance3D(partner) > 150.0)
								{
									player.sendPacket(SystemMessageId.YOUR_TARGET_IS_OUT_OF_RANGE);
								}
								else
								{
									player.onTransactionRequest(partner);
									partner.sendPacket(new SendTradeRequest(player.getObjectId()));
									SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_REQUESTED_A_TRADE_WITH_C1);
									sm.addString(partner.getName());
									player.sendPacket(sm);
								}
							}
							else
							{
								player.sendMessage("You cannot trade while you are in in Jail.");
							}
						}
						else
						{
							player.sendMessage("A user currently participating in the Olympiad cannot accept or request a trade.");
						}
					}
				}
			}
		}
	}
}
