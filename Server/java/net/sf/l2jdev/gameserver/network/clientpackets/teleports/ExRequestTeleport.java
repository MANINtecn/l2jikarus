package net.sf.l2jdev.gameserver.network.clientpackets.teleports;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.holders.TeleportListHolder;
import net.sf.l2jdev.gameserver.data.xml.TeleportListData;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class ExRequestTeleport extends ClientPacket
{
	private int _teleportId;

	@Override
	protected void readImpl()
	{
		this._teleportId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			TeleportListHolder teleport = TeleportListData.getInstance().getTeleport(this._teleportId);
			if (teleport == null)
			{
				PacketLogger.warning("No registered teleport location for id: " + this._teleportId);
			}
			else if (player.isDead())
			{
				player.sendPacket(SystemMessageId.DEAD_CHARACTERS_CANNOT_USE_TELEPORTATION);
			}
			else if (player.isPrisoner())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_FUNCTION_IN_THE_UNDERGROUND_LABYRINTH);
			}
			else if (player.getMovieHolder() == null && !player.isFishing() && !player.isInInstance() && !player.isOnEvent() && !player.isInOlympiadMode() && !player.inObserverMode() && !player.isInTraingCamp() && !player.isInTimedHuntingZone() && !player.isInStoreMode())
			{
				if (PlayerConfig.TELEPORT_WHILE_PLAYER_IN_COMBAT || !player.isInCombat() && !player.isCastingNow())
				{
					if ((!PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT || !PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_USE_GK) && player.getReputation() < 0)
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_RIGHT_NOW);
					}
					else if (player.cannotEscape())
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_RIGHT_NOW);
					}
					else
					{
						Location location = teleport.getLocation();
						if (!PlayerConfig.TELEPORT_WHILE_SIEGE_IN_PROGRESS)
						{
							Castle castle = CastleManager.getInstance().getCastle(location.getX(), location.getY(), location.getZ());
							if (castle != null && castle.getSiege().isInProgress())
							{
								player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
								return;
							}
						}

						if (player.getLevel() > PlayerConfig.MAX_FREE_TELEPORT_LEVEL)
						{
							int price = teleport.getPrice();
							if (price > 0)
							{
								if (teleport.isSpecial())
								{
									if (player.getInventory().getInventoryItemCount(91663, -1) < price)
									{
										player.sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_L_COINS);
										return;
									}
								}
								else
								{
									price = (int) (price * 0.99F);
									if (player.getAdena() < price)
									{
										player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
										return;
									}
								}

								if (teleport.isSpecial())
								{
									player.destroyItemByItemId(ItemProcessType.FEE, 91663, price, player, true);
								}
								else
								{
									player.reduceAdena(ItemProcessType.FEE, price, player, true);
								}
							}
						}

						player.abortCast();
						player.stopMove(null);
						player.setTeleportLocation(location);
						player.castTeleportSkill();
					}
				}
				else
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_WHILE_IN_COMBAT);
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_RIGHT_NOW);
			}
		}
	}
}
