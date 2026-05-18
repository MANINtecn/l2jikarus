package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.NpcConfig;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;

public class Action extends ClientPacket
{
	private int _objectId;
	protected int _originX;
	protected int _originY;
	protected int _originZ;
	private int _actionId;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
		this._originX = this.readInt();
		this._originY = this.readInt();
		this._originZ = this.readInt();
		this._actionId = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		if (this.getClient().getFloodProtectors().canPerformPlayerAction())
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				if (player.inObserverMode())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_FUNCTION_IN_THE_SPECTATOR_MODE);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					BuffInfo info = player.getEffectList().getFirstBuffInfoByAbnormalType(AbnormalType.BOT_PENALTY);
					if (info != null)
					{
						for (AbstractEffect effect : info.getEffects())
						{
							if (!effect.checkCondition(-4))
							{
								player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_ACTIONS_HAVE_BEEN_RESTRICTED);
								player.sendPacket(ActionFailed.STATIC_PACKET);
								return;
							}
						}
					}

					WorldObject obj;
					if (player.getTargetId() == this._objectId)
					{
						obj = player.getTarget();
					}
					else if (player.isInAirShip() && player.getAirShip().getHelmObjectId() == this._objectId)
					{
						obj = player.getAirShip();
					}
					else
					{
						obj = World.getInstance().findObject(this._objectId);
					}

					if (obj == null)
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else if ((!obj.isTargetable() || player.isTargetingDisabled()) && !player.isGM())
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else if (obj.getInstanceWorld() != player.getInstanceWorld())
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else if (!obj.isVisibleFor(player))
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else if (player.getActiveRequester() != null)
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else
					{
						player.onActionRequest();
						switch (this._actionId)
						{
							case 0:
								obj.onAction(player);
								break;
							case 1:
								if (player.isGM() || obj.isNpc() && NpcConfig.ALT_GAME_VIEWNPC && !obj.isFakePlayer())
								{
									obj.onActionShift(player);
								}
								else
								{
									obj.onAction(player, false);
								}
								break;
							default:
								PacketLogger.warning(this.getClass().getSimpleName() + ": Character: " + player.getName() + " requested invalid action: " + this._actionId);
								player.sendPacket(ActionFailed.STATIC_PACKET);
						}
					}
				}
			}
		}
	}
}
