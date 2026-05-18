package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.AdminTeleportType;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public class MoveToward extends ClientPacket
{
	private int _heading;

	@Override
	protected void readImpl()
	{
		this._heading = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		if (PlayerConfig.ENABLE_KEYBOARD_MOVEMENT)
		{
			Player player = this.getPlayer();
			if (player != null && !player.isControlBlocked() && player.getTeleMode() == AdminTeleportType.NORMAL)
			{
				double angle = LocationUtil.convertHeadingToDegree(this._heading);
				double radian = Math.toRadians(angle);
				double course = Math.toRadians(180.0);
				double frontDistance = player.getMoveSpeed();
				int x1 = (int) (Math.cos(Math.PI + radian + course) * frontDistance);
				int y1 = (int) (Math.sin(Math.PI + radian + course) * frontDistance);
				int x = player.getX() + x1;
				int y = player.getY() + y1;
				Location destination = GeoEngine.getInstance().getValidLocation(player.getX(), player.getY(), player.getZ(), x, y, player.getZ(), player.getInstanceWorld());
				player.setCursorKeyMovement(true);
				player.setLastServerPosition(player.getX(), player.getY(), player.getZ());
				player.getAI().setIntention(Intention.MOVE_TO, destination);
				if (player.getQueuedSkill() != null)
				{
					player.setQueuedSkill(null, null, false, false);
				}

				player.onActionRequest();
			}
		}
	}
}
