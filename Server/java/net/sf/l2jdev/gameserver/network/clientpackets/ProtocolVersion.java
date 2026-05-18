package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.config.ServerConfig;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.serverpackets.KeyPacket;

public class ProtocolVersion extends ClientPacket
{
	private static final Logger LOGGER_ACCOUNTING = Logger.getLogger("accounting");
	private int _version;

	@Override
	protected void readImpl()
	{
		try
		{
			this._version = this.readInt();
		}
		catch (Exception var2)
		{
			this._version = 0;
		}
	}

	@Override
	protected void runImpl()
	{
		GameClient client = this.getClient();
		if (this._version == -2)
		{
			client.disconnect();
		}
		else if (!ServerConfig.PROTOCOL_LIST.contains(this._version))
		{
			LOGGER_ACCOUNTING.warning("Wrong protocol version " + this._version + ", " + client);
			client.setProtocolOk(false);
			client.close(new KeyPacket(client.enableCrypt(), 0));
		}
		else
		{
			client.setProtocolVersion(this._version);
			client.setProtocolOk(true);
			client.sendPacket(new KeyPacket(client.enableCrypt(), 1));
		}
	}
}
