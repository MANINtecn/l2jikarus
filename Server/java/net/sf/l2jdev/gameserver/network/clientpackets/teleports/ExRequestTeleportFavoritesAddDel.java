package net.sf.l2jdev.gameserver.network.clientpackets.teleports;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.data.xml.TeleportListData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class ExRequestTeleportFavoritesAddDel extends ClientPacket
{
	private boolean _enable;
	private int _teleportId;

	@Override
	protected void readImpl()
	{
		this._enable = this.readByte() == 1;
		this._teleportId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (TeleportListData.getInstance().getTeleport(this._teleportId) == null)
			{
				PacketLogger.warning("No registered teleport location for id: " + this._teleportId);
			}
			else
			{
				List<Integer> favorites = new ArrayList<>();

				for (int id : player.getVariables().getIntegerList("FAVORITE_TELEPORTS"))
				{
					if (TeleportListData.getInstance().getTeleport(this._teleportId) == null)
					{
						PacketLogger.warning("No registered teleport location for id: " + this._teleportId);
					}
					else
					{
						favorites.add(id);
					}
				}

				if (this._enable)
				{
					if (!favorites.contains(this._teleportId))
					{
						favorites.add(this._teleportId);
					}
				}
				else
				{
					favorites.remove(Integer.valueOf(this._teleportId));
				}

				player.getVariables().setIntegerList("FAVORITE_TELEPORTS", favorites);
			}
		}
	}
}
