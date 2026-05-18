package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.events.Containers;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerDelete;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.enums.CharacterDeleteFailType;
import net.sf.l2jdev.gameserver.network.holders.CharacterInfoHolder;
import net.sf.l2jdev.gameserver.network.serverpackets.CharDeleteFail;
import net.sf.l2jdev.gameserver.network.serverpackets.CharDeleteSuccess;
import net.sf.l2jdev.gameserver.network.serverpackets.CharSelectionInfo;

public class CharacterDelete extends ClientPacket
{
	private int _charSlot;

	@Override
	protected void readImpl()
	{
		this._charSlot = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		GameClient client = this.getClient();

		try
		{
			CharacterDeleteFailType failType = client.markToDeleteChar(this._charSlot);
			switch (failType)
			{
				case NONE:
					client.sendPacket(new CharDeleteSuccess());
					CharacterInfoHolder charInfo = client.getCharSelection(this._charSlot);
					if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_DELETE, Containers.Players()))
					{
						EventDispatcher.getInstance().notifyEvent(new OnPlayerDelete(charInfo.getObjectId(), charInfo.getName(), client), Containers.Players());
					}
					break;
				default:
					client.sendPacket(new CharDeleteFail(failType));
			}
		}
		catch (Exception var4)
		{
			PacketLogger.warning(this.getClass().getSimpleName() + ": " + var4.getMessage());
		}

		CharSelectionInfo cl = new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1, 0);
		client.sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
}
