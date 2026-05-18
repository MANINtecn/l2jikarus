package net.sf.l2jdev.gameserver.network.serverpackets.appearance;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCuriousHouseMemberUpdate extends ServerPacket
{
	public int _objId;
	public int _maxHp;
	public int _maxCp;
	public int _currentHp;
	public int _currentCp;

	public ExCuriousHouseMemberUpdate(Player player)
	{
		this._objId = player.getObjectId();
		this._maxHp = (int) player.getMaxHp();
		this._maxCp = player.getMaxCp();
		this._currentHp = (int) player.getCurrentHp();
		this._currentCp = (int) player.getCurrentCp();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CURIOUS_HOUSE_MEMBER_UPDATE.writeId(this, buffer);
		buffer.writeInt(this._objId);
		buffer.writeInt(this._maxHp);
		buffer.writeInt(this._maxCp);
		buffer.writeInt(this._currentHp);
		buffer.writeInt(this._currentCp);
	}
}
