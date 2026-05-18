package net.sf.l2jdev.gameserver.network.clientpackets.surveillance;

import java.sql.Connection;
import java.sql.PreparedStatement;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.RelationChanged;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.surveillance.ExUserWatcherTargetList;

public class ExRequestUserWatcherDelete extends ClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		this._name = this.readSizedString();
		this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			int targetId = CharInfoTable.getInstance().getIdByName(this._name);
			if (targetId == -1)
			{
				player.sendPacket(SystemMessageId.THAT_CHARACTER_DOES_NOT_EXIST);
			}
			else if (!player.getSurveillanceList().contains(targetId))
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_NOT_ON_YOUR_FRIEND_LIST);
				sm.addString(this._name);
				player.sendPacket(sm);
			}
			else
			{
				try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM character_surveillances WHERE (charId=? AND targetId=?)");)
				{
					statement.setInt(1, player.getObjectId());
					statement.setInt(2, targetId);
					statement.execute();
				}
				catch (Exception var11)
				{
					PacketLogger.warning("ExRequestUserWatcherDelete: Could not add surveillance objectid: " + var11.getMessage());
				}

				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_VE_REMOVED_C1_FROM_YOUR_SURVEILLANCE_LIST);
				sm.addString(this._name);
				player.sendPacket(sm);
				player.getSurveillanceList().remove(targetId);
				player.sendPacket(new ExUserWatcherTargetList(player));
				Player target = World.getInstance().getPlayer(targetId);
				if (target != null && target.isVisibleFor(player))
				{
					player.sendPacket(new RelationChanged());
				}
			}
		}
	}
}
