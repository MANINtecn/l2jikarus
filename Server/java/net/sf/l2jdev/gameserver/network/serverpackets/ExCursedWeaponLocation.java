package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExCursedWeaponLocation extends ServerPacket
{
	private final List<ExCursedWeaponLocation.CursedWeaponInfo> _cursedWeaponInfo;

	public ExCursedWeaponLocation(List<ExCursedWeaponLocation.CursedWeaponInfo> cursedWeaponInfo)
	{
		this._cursedWeaponInfo = cursedWeaponInfo;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_EXISTING_CURSED_WEAPON_LOCATION.writeId(this, buffer);
		if (!this._cursedWeaponInfo.isEmpty())
		{
			buffer.writeInt(this._cursedWeaponInfo.size());

			for (ExCursedWeaponLocation.CursedWeaponInfo w : this._cursedWeaponInfo)
			{
				buffer.writeInt(w.id);
				buffer.writeInt(w.activated);
				buffer.writeInt(w.pos.getX());
				buffer.writeInt(w.pos.getY());
				buffer.writeInt(w.pos.getZ());
			}
		}
		else
		{
			buffer.writeInt(0);
		}
	}

	public static class CursedWeaponInfo
	{
		public Location pos;
		public int id;
		public int activated;

		public CursedWeaponInfo(Location p, int cwId, int status)
		{
			this.pos = p;
			this.id = cwId;
			this.activated = status;
		}
	}
}
