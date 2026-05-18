package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2jdev.gameserver.util.Broadcast;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public class RequestExMagicSkillUseGround extends ClientPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _skillId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	@Override
	protected void readImpl()
	{
		this._x = this.readInt();
		this._y = this.readInt();
		this._z = this.readInt();
		this._skillId = this.readInt();
		this._ctrlPressed = this.readInt() != 0;
		this._shiftPressed = this.readByte() != 0;
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			int level = player.getSkillLevel(this._skillId);
			if (level <= 0)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				Skill skill = SkillData.getInstance().getSkill(this._skillId, level);
				if (skill != null)
				{
					player.setCurrentSkillWorldPosition(new Location(this._x, this._y, this._z));
					player.setHeading(LocationUtil.calculateHeadingFrom(player.getX(), player.getY(), this._x, this._y));
					Broadcast.toKnownPlayers(player, new ValidateLocation(player));
					player.useMagic(skill, null, this._ctrlPressed, this._shiftPressed);
				}
				else
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					PacketLogger.warning("No skill found with id " + this._skillId + " and level " + level + " !!");
				}
			}
		}
	}
}
