package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Set;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.CursedWeaponsManager;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExCursedWeaponList extends ServerPacket
{
	private final Set<Integer> _ids = CursedWeaponsManager.getInstance().getCursedWeaponsIds();

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CURSED_WEAPON_LIST.writeId(this, buffer);
		buffer.writeInt(this._ids.size());
		this._ids.forEach(buffer::writeInt);
	}
}
