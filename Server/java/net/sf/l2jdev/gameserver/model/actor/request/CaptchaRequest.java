package net.sf.l2jdev.gameserver.model.actor.request;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import net.sf.l2jdev.gameserver.config.custom.CaptchaConfig;
import net.sf.l2jdev.gameserver.data.BotReportTable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.captcha.Captcha;

public class CaptchaRequest extends AbstractRequest
{
	public static final byte MAX_ATTEMPTS = 3;
	private Captcha _captcha;
	private byte _count = 0;
	private final Instant _timeout;

	public CaptchaRequest(Player activeChar, Captcha captcha)
	{
		super(activeChar);
		this._captcha = captcha;
		long currentTime = System.currentTimeMillis();
		this.setTimestamp(currentTime);
		this.scheduleTimeout(Duration.ofMinutes(CaptchaConfig.VALIDATION_TIME).toMillis());
		this._timeout = Instant.ofEpochMilli(currentTime).plus(CaptchaConfig.VALIDATION_TIME, ChronoUnit.MINUTES);
	}

	@Override
	public boolean isUsing(int objectId)
	{
		return false;
	}

	public int getRemainingTime()
	{
		return (int) this._timeout.minusMillis(System.currentTimeMillis()).getEpochSecond();
	}

	public void refresh(Captcha captcha)
	{
		this._captcha = captcha;
	}

	public void newRequest(Captcha captcha)
	{
		this._count++;
		this._captcha = captcha;
	}

	public boolean isLimitReached()
	{
		return this._count >= 2;
	}

	public Captcha getCaptcha()
	{
		return this._captcha;
	}

	@Override
	public void onTimeout()
	{
		BotReportTable.getInstance().punishBotDueUnsolvedCaptcha(this.getPlayer());
	}

	public int maxAttemps()
	{
		return 3;
	}

	public int remainingAttemps()
	{
		return Math.max(3 - this._count, 0);
	}
}
