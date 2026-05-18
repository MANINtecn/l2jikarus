package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class Ex2ndPasswordVerify extends ServerPacket
{
	public static final int PASSWORD_OK = 0;
	public static final int PASSWORD_WRONG = 1;
	public static final int PASSWORD_BAN = 2;
	private final int _wrongTentatives;
	private final int _mode;

	public Ex2ndPasswordVerify(int mode, int wrongTentatives)
	{
		this._mode = mode;
		this._wrongTentatives = wrongTentatives;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_2ND_PASSWORD_VERIFY.writeId(this, buffer);
		buffer.writeInt(this._mode);
		buffer.writeInt(this._wrongTentatives);
	}
}
