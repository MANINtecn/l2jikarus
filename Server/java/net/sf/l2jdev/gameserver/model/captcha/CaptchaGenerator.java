package net.sf.l2jdev.gameserver.model.captcha;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.util.DXT1ImageCompressor;

public class CaptchaGenerator
{
	private static final Map<Integer, Captcha> CAPTCHAS = new ConcurrentHashMap<>();
	private static final DXT1ImageCompressor COMPRESSOR = new DXT1ImageCompressor();

	private CaptchaGenerator()
	{
	}

	public Captcha next()
	{
		int id = Rnd.get(CAPTCHAS.size() + 5);
		return CAPTCHAS.computeIfAbsent(id, this::generateCaptcha);
	}

	public Captcha next(int previousId)
	{
		int id = Rnd.get(CAPTCHAS.size() + 5);
		if (id == previousId)
		{
			id++;
		}

		return CAPTCHAS.computeIfAbsent(id, this::generateCaptcha);
	}

	public int generateCaptchaCode()
	{
		return Rnd.get(111111, 999999);
	}

	private Captcha generateCaptcha(int id)
	{
		BufferedImage image = new BufferedImage(128, 32, 7);
		Graphics2D graphics = this.createGraphics(32, 128, image);
		graphics.setFont(new Font("SansSerif", 1, 22));
		int code = this.generateCaptchaCode();
		this.writeCode(code, graphics);
		this.addNoise(graphics);
		graphics.dispose();
		return new Captcha(id, code, COMPRESSOR.compress(image));
	}

	public Graphics2D createGraphics(int height, int width, BufferedImage image)
	{
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 0, width, height);
		return graphics;
	}

	private void writeCode(int code, Graphics2D graphics)
	{
		String text = String.valueOf(code);
		FontMetrics metrics = graphics.getFontMetrics();
		for (int i = 0; i < text.length(); i++)
		{
			char character = text.charAt(i);
			int charWidth = metrics.charWidth(character) + 5;
			graphics.setColor(this.getColor());
			graphics.drawString(character + "", 10 + i * charWidth, Rnd.get(24, 32));
		}
	}

	public void addNoise(Graphics2D graphics)
	{
		graphics.setColor(Color.WHITE);

		for (int i = 0; i < 20; i++)
		{
			graphics.fillOval(Rnd.get(10, 122), Rnd.get(6, 20), 4, 4);
		}

		for (int i = 0; i < 6; i++)
		{
			graphics.drawLine(Rnd.get(30, 90), Rnd.get(6, 28), Rnd.get(80, 120), Rnd.get(10, 26));
		}
	}

	public Color getColor()
	{
		switch (Rnd.get(5))
		{
			case 1:
				return Color.WHITE;
			case 2:
				return Color.RED;
			case 3:
				return Color.YELLOW;
			case 4:
				return Color.CYAN;
			default:
				return Color.GREEN;
		}
	}

	public static CaptchaGenerator getInstance()
	{
		return CaptchaGenerator.Singleton.INSTANCE;
	}

	private static class Singleton
	{
		private static final CaptchaGenerator INSTANCE = new CaptchaGenerator();
	}
}
