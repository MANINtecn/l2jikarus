package net.sf.l2jdev.gameserver.network.clientpackets.captcha;

import net.sf.l2jdev.gameserver.data.BotReportTable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.CaptchaRequest;
import net.sf.l2jdev.gameserver.model.captcha.Captcha;
import net.sf.l2jdev.gameserver.model.captcha.CaptchaGenerator;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.captcha.ReceiveBotCaptchaImage;
import net.sf.l2jdev.gameserver.network.serverpackets.captcha.ReceiveBotCaptchaResult;

public class RequestCaptchaAnswer extends ClientPacket
{
	private int _answer;

	@Override
	protected void readImpl()
	{
		this.readLong();
		this._answer = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			CaptchaRequest request = player.getRequest(CaptchaRequest.class);
			if (request != null)
			{
				if (this._answer == request.getCaptcha().getCode())
				{
					player.sendPacket(ReceiveBotCaptchaResult.SUCCESS);
					player.sendPacket(SystemMessageId.IDENTIFICATION_COMPLETED_HAVE_A_GOOD_TIME_WITH_LINEAGE_II_THANK_YOU);
					request.cancelTimeout();
					player.removeRequest(CaptchaRequest.class);
				}
				else
				{
					this.onWrongCode(player, request);
				}
			}
			else
			{
				Captcha captcha = CaptchaGenerator.getInstance().next();
				request = new CaptchaRequest(player, captcha);
				player.addRequest(request);
				player.sendPacket(new ReceiveBotCaptchaImage(captcha, request.getRemainingTime()));
			}
		}
	}

	protected void onWrongCode(Player player, CaptchaRequest request)
	{
		if (request.isLimitReached())
		{
			request.cancelTimeout();
			BotReportTable.getInstance().punishBotDueUnsolvedCaptcha(player);
		}
		else
		{
			Captcha captcha = CaptchaGenerator.getInstance().next();
			request.newRequest(captcha);
			player.sendPacket(new ReceiveBotCaptchaImage(captcha, request.getRemainingTime()));
			SystemMessage msg = new SystemMessage(SystemMessageId.WRONG_AUTHENTICATION_CODE_IF_YOU_ENTER_THE_WRONG_CODE_S1_TIME_S_THE_SYSTEM_WILL_QUALIFY_YOU_AS_A_PROHIBITED_SOFTWARE_USER_AND_CHARGE_A_PENALTY_ATTEMPTS_LEFT_S2);
			msg.addInt(request.maxAttemps());
			msg.addInt(request.remainingAttemps());
			player.sendPacket(msg);
		}
	}
}
