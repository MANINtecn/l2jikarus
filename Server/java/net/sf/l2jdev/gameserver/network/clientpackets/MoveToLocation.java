package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.Arrays;

import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.DoorData;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.managers.ZoneBuildManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.SayuneEntry;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.AdminTeleportType;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerMoveRequest;
import net.sf.l2jdev.gameserver.model.events.returns.TerminateReturn;
import net.sf.l2jdev.gameserver.model.skill.enums.FlyType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.SayuneType;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.FlyToLocation;
import net.sf.l2jdev.gameserver.network.serverpackets.MagicSkillLaunched;
import net.sf.l2jdev.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2jdev.gameserver.network.serverpackets.sayune.ExFlyMove;
import net.sf.l2jdev.gameserver.network.serverpackets.sayune.ExFlyMoveBroadcast;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class MoveToLocation extends ClientPacket
{
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	private int _originX;
	private int _originY;
	private int _originZ;
	private int _movementMode;

	@Override
	protected void readImpl()
	{
		this._targetX = this.readInt();
		this._targetY = this.readInt();
		this._targetZ = this.readInt();
		this._originX = this.readInt();
		this._originY = this.readInt();
		this._originZ = this.readInt();
		this._movementMode = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (player.isOverloaded())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_MOVE_DUE_TO_THE_WEIGHT_OF_YOUR_INVENTORY);
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (PlayerConfig.PLAYER_MOVEMENT_BLOCK_TIME > 0 && !player.isGM() && player.getNotMoveUntil() > System.currentTimeMillis())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_MOVE_WHILE_SPEAKING_TO_AN_NPC_ONE_MOMENT_PLEASE);
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (this._targetX == this._originX && this._targetY == this._originY && this._targetZ == this._originZ)
			{
				player.stopMove(player.getLocation());
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (GeoEngine.getInstance().isCompletelyBlocked(GeoEngine.getGeoX(this._targetX), GeoEngine.getGeoY(this._targetY), this._targetZ))
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (DoorData.getInstance().checkIfDoorsBetween(player.getLastServerPosition(), player.getLocation(), player.getInstanceWorld()))
			{
				player.stopMove(player.getLastServerPosition());
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				if (this._movementMode == 1)
				{
					player.setCursorKeyMovement(false);
					if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_MOVE_REQUEST, player))
					{
						TerminateReturn terminate = EventDispatcher.getInstance().notifyEvent(new OnPlayerMoveRequest(player, new Location(this._targetX, this._targetY, this._targetZ)), player, TerminateReturn.class);
						if (terminate != null && terminate.terminate())
						{
							player.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
					}
				}
				else
				{
					if (!PlayerConfig.ENABLE_KEYBOARD_MOVEMENT)
					{
						return;
					}

					player.setCursorKeyMovement(true);
					player.setLastServerPosition(player.getX(), player.getY(), player.getZ());
				}

				AdminTeleportType teleMode = player.getTeleMode();
				switch (teleMode)
				{
					case DEMONIC:
						player.sendPacket(ActionFailed.STATIC_PACKET);
						player.teleToLocation(new Location(this._targetX, this._targetY, this._targetZ));
						player.setTeleMode(AdminTeleportType.NORMAL);
						break;
					case SAYUNE:
						player.sendPacket(new ExFlyMove(player, SayuneType.ONE_WAY_LOC, -1, Arrays.asList(new SayuneEntry(false, -1, this._targetX, this._targetY, this._targetZ))));
						player.setXYZ(this._targetX, this._targetY, this._targetZ);
						Broadcast.toKnownPlayers(player, new ExFlyMoveBroadcast(player, SayuneType.ONE_WAY_LOC, -1, new Location(this._targetX, this._targetY, this._targetZ)));
						player.setTeleMode(AdminTeleportType.NORMAL);
						break;
					case CHARGE:
						player.setXYZ(this._targetX, this._targetY, this._targetZ);
						Broadcast.toSelfAndKnownPlayers(player, new MagicSkillUse(player, 30012, 10, 500, 0));
						Broadcast.toSelfAndKnownPlayers(player, new FlyToLocation(player, this._targetX, this._targetY, this._targetZ, FlyType.CHARGE));
						Broadcast.toSelfAndKnownPlayers(player, new MagicSkillLaunched(player, 30012, 10));
						player.sendPacket(ActionFailed.STATIC_PACKET);
						break;
					case ZONE_POINT:
						ZoneBuildManager.getInstance().addPoint(player, new Location(this._targetX, this._targetY, this._targetZ));
						player.sendPacket(ActionFailed.STATIC_PACKET);
						return;
					default:
						if (player.isControlBlocked())
						{
							player.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}

						double dx = this._targetX - player.getX();
						double dy = this._targetY - player.getY();
						if (dx * dx + dy * dy > 9.801E7)
						{
							player.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}

						player.getAI().setIntention(Intention.MOVE_TO, new Location(this._targetX, this._targetY, this._targetZ));
				}

				if (player.getQueuedSkill() != null)
				{
					player.setQueuedSkill(null, null, false, false);
				}

				player.onActionRequest();
			}
		}
	}
}
