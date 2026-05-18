package net.sf.l2jdev.gameserver.network.clientpackets.captcha;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.CaptchaRequest;
import net.sf.l2jdev.gameserver.model.captcha.Captcha;
import net.sf.l2jdev.gameserver.model.captcha.CaptchaGenerator;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.captcha.ReceiveBotCaptchaImage;

public class RequestRefreshCaptcha extends ClientPacket
{
	private long _captchaId;

	@Override
	protected void readImpl()
	{
		this._captchaId = this.readLong();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			CaptchaRequest request = player.getRequest(CaptchaRequest.class);
			Captcha captcha = CaptchaGenerator.getInstance().next((int) this._captchaId);
			if (request != null)
			{
				request.refresh(captcha);
			}
			else
			{
				request = new CaptchaRequest(player, captcha);
				player.addRequest(request);
			}

			player.sendPacket(new ReceiveBotCaptchaImage(captcha, request.getRemainingTime()));
		}
	}
}
