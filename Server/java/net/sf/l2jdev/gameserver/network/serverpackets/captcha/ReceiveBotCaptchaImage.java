package net.sf.l2jdev.gameserver.network.serverpackets.captcha;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.captcha.Captcha;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ReceiveBotCaptchaImage extends ServerPacket
{
	private final Captcha _captcha;
	private final int _time;

	public ReceiveBotCaptchaImage(Captcha captcha, int time)
	{
		this._captcha = captcha;
		this._time = time;
	}

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CAPTCHA_IMAGE.writeId(this, buffer);
		buffer.writeLong(this._captcha.getId());
		buffer.writeByte(2);
		buffer.writeInt(this._time);
		buffer.writeBytes(this._captcha.getData());
	}
}
