package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.managers.BoatManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Boat;
import net.sf.l2jdev.gameserver.model.item.type.WeaponType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.MoveToLocationInVehicle;
import net.sf.l2jdev.gameserver.network.serverpackets.StopMoveInVehicle;

public class RequestMoveToLocationInVehicle extends ClientPacket
{
	private int _boatId;
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	private int _originX;
	private int _originY;
	private int _originZ;

	@Override
	protected void readImpl()
	{
		this._boatId = this.readInt();
		this._targetX = this.readInt();
		this._targetY = this.readInt();
		this._targetZ = this.readInt();
		this._originX = this.readInt();
		this._originY = this.readInt();
		this._originZ = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (PlayerConfig.PLAYER_MOVEMENT_BLOCK_TIME > 0 && !player.isGM() && player.getNotMoveUntil() > System.currentTimeMillis())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_MOVE_WHILE_SPEAKING_TO_AN_NPC_ONE_MOMENT_PLEASE);
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (this._targetX == this._originX && this._targetY == this._originY && this._targetZ == this._originZ)
			{
				player.sendPacket(new StopMoveInVehicle(player, this._boatId));
			}
			else if (player.isAttackingNow() && player.getActiveWeaponItem() != null && player.getActiveWeaponItem().getItemType() == WeaponType.BOW)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (player.isSitting() || player.isMovementDisabled())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (player.hasSummon())
			{
				player.sendPacket(SystemMessageId.YOU_SHOULD_RELEASE_YOUR_SERVITOR_SO_THAT_IT_DOES_NOT_FALL_OFF_OF_THE_BOAT_AND_DROWN);
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (player.isTransformed())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_POLYMORPH_WHILE_RIDING_A_BOAT);
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				if (player.isInBoat())
				{
					Boat boat = player.getBoat();
					if (boat.getObjectId() != this._boatId)
					{
						boat = BoatManager.getInstance().getBoat(this._boatId);
						player.setVehicle(boat);
					}
				}
				else
				{
					Boat boat = BoatManager.getInstance().getBoat(this._boatId);
					player.setVehicle(boat);
				}

				Location pos = new Location(this._targetX, this._targetY, this._targetZ);
				Location originPos = new Location(this._originX, this._originY, this._originZ);
				player.setInVehiclePosition(pos);
				player.broadcastPacket(new MoveToLocationInVehicle(player, pos, originPos));
			}
		}
	}
}
