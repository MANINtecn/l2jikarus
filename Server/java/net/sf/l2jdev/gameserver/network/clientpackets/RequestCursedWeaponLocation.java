package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.LinkedList;
import java.util.List;

import net.sf.l2jdev.gameserver.managers.CursedWeaponsManager;
import net.sf.l2jdev.gameserver.model.CursedWeapon;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExCursedWeaponLocation;

public class RequestCursedWeaponLocation extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			List<ExCursedWeaponLocation.CursedWeaponInfo> list = new LinkedList<>();

			for (CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons())
			{
				if (cw.isActive())
				{
					Location pos = cw.getWorldPosition();
					if (pos != null)
					{
						list.add(new ExCursedWeaponLocation.CursedWeaponInfo(pos, cw.getItemId(), cw.isActivated() ? 1 : 0));
					}
				}
			}

			if (!list.isEmpty())
			{
				player.sendPacket(new ExCursedWeaponLocation(list));
			}
		}
	}
}
