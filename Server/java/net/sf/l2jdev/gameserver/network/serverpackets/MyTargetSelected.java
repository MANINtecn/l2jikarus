package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.ControllableAirShip;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class MyTargetSelected extends ServerPacket
{
	private final int _objectId;
	private final int _color;
	private final int _isDead;

	public MyTargetSelected(Player player, Creature target)
	{
		this._objectId = target instanceof ControllableAirShip ? ((ControllableAirShip) target).getHelmObjectId() : target.getObjectId();
		this._color = target.isAutoAttackable(player) ? player.getLevel() - target.getLevel() : 0;
		this._isDead = target.isDead() ? 1 : 0;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.MY_TARGET_SELECTED.writeId(this, buffer);
		buffer.writeInt(1);
		buffer.writeInt(this._objectId);
		buffer.writeShort(this._color);
		buffer.writeInt(0);
		buffer.writeByte(this._isDead);
	}
}
