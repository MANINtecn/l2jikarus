package net.sf.l2jdev.gameserver.network.clientpackets.settings;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.ConnectionState;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class RequestSaveKeyMapping extends ClientPacket
{
	public static final String SPLIT_VAR = "\t";
	private byte[] _uiKeyMapping;

	@Override
	protected void readImpl()
	{
		int dataSize = this.readInt();
		if (dataSize > 0)
		{
			this._uiKeyMapping = this.readBytes(dataSize);
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (PlayerConfig.STORE_UI_SETTINGS && player != null && this._uiKeyMapping != null && this.getClient().getConnectionState() == ConnectionState.IN_GAME)
		{
			String uiKeyMapping = "";
			byte[] var3 = this._uiKeyMapping;
			int var4 = var3.length;

			for (int var5 = 0; var5 < var4; var5++)
			{
				Byte b = var3[var5];
				uiKeyMapping = uiKeyMapping + b + "\t";
			}

			player.getVariables().set("UI_KEY_MAPPING", uiKeyMapping);
		}
	}
}
