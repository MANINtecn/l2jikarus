package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.handler.IUserCommandHandler;
import net.sf.l2jdev.gameserver.handler.UserCommandHandler;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class BypassUserCmd extends ClientPacket
{
	private int _command;

	@Override
	protected void readImpl()
	{
		this._command = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			IUserCommandHandler handler = UserCommandHandler.getInstance().getHandler(this._command);
			if (handler == null)
			{
				if (player.isGM())
				{
					player.sendMessage("User commandID " + this._command + " not implemented yet.");
				}
			}
			else
			{
				handler.onCommand(this._command, player);
			}
		}
	}
}
