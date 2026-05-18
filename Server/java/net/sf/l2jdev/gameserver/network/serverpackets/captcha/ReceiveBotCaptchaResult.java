package net.sf.l2jdev.gameserver.network.serverpackets.captcha;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ReceiveBotCaptchaResult extends ServerPacket
{
	public static final ReceiveBotCaptchaResult SUCCESS = new ReceiveBotCaptchaResult(1);
	public static final ReceiveBotCaptchaResult FAILED = new ReceiveBotCaptchaResult(0);
	private final int _answer;

	private ReceiveBotCaptchaResult(int answer)
	{
		this._answer = answer;
	}

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CAPTCHA_ANSWER_RESULT.writeId(this, buffer);
		buffer.writeInt(this._answer);
	}
}
