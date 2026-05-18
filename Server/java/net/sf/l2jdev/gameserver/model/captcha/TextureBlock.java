package net.sf.l2jdev.gameserver.model.captcha;

public class TextureBlock
{
	private final TextureBlock.ARGB[] _colors = new TextureBlock.ARGB[16];
	private final TextureBlock.ARGB[] _palette = new TextureBlock.ARGB[4];
	private int _minColorIndex;
	private int _maxColorIndex;
	private short _minColor;
	private short _maxColor;

	public TextureBlock()
	{
		for (int i = 0; i < 16; i++)
		{
			this._colors[i] = new TextureBlock.ARGB();
		}

		for (int i = 0; i < 4; i++)
		{
			this._palette[i] = new TextureBlock.ARGB();
		}
	}

	public void of(int[] buffer)
	{
		int maxDistance = -1;

		for (int i = 0; i < 16; i++)
		{
			this._colors[i].a = 0xFF & buffer[i] >> 24;
			this._colors[i].r = 0xFF & buffer[i] >> 16;
			this._colors[i].g = 0xFF & buffer[i] >> 8;
			this._colors[i].b = 0xFF & buffer[i];

			for (int j = i - 1; j >= 0; j--)
			{
				int distance = this.euclidianDistance(this._colors[i], this._colors[j]);
				if (distance > maxDistance)
				{
					maxDistance = distance;
					this._minColorIndex = j;
					this._maxColorIndex = i;
				}
			}
		}

		this.computMinMaxColor();
		this.computePalette();
	}

	private void computePalette()
	{
		this._palette[0] = this.colorAt(this._maxColorIndex);
		this._palette[1] = this.colorAt(this._minColorIndex);
		this._palette[2].a = 255;
		this._palette[2].r = (2 * this._palette[0].r + this._palette[1].r) / 3;
		this._palette[2].g = (2 * this._palette[0].g + this._palette[1].g) / 3;
		this._palette[2].b = (2 * this._palette[0].b + this._palette[1].b) / 3;
		this._palette[3].a = 255;
		this._palette[3].r = (2 * this._palette[1].r + this._palette[0].r) / 3;
		this._palette[3].g = (2 * this._palette[1].g + this._palette[0].g) / 3;
		this._palette[3].b = (2 * this._palette[1].b + this._palette[0].b) / 3;
	}

	private void computMinMaxColor()
	{
		this._maxColor = this._colors[this._maxColorIndex].toShortRGB565();
		this._minColor = this._colors[this._minColorIndex].toShortRGB565();
		if (this._maxColor < this._minColor)
		{
			short tmp = this._maxColor;
			this._maxColor = this._minColor;
			this._minColor = tmp;
			int tmp2 = this._maxColorIndex;
			this._maxColorIndex = this._minColorIndex;
			this._minColorIndex = tmp2;
		}
	}

	public int euclidianDistance(TextureBlock.ARGB c1, TextureBlock.ARGB c2)
	{
		return (c1.r - c2.r) * (c1.r - c2.r) + (c1.g - c2.g) * (c1.g - c2.g) + (c1.b - c2.b) * (c1.b - c2.b);
	}

	public short getMaxColor()
	{
		return this._maxColor;
	}

	public short getMinColor()
	{
		return this._minColor;
	}

	public TextureBlock.ARGB colorAt(int index)
	{
		return this._colors[index];
	}

	public TextureBlock.ARGB[] getPalette()
	{
		return this._palette;
	}

	public static class ARGB
	{
		public int a;
		public int r;
		public int g;
		public int b;

		public short toShortRGB565()
		{
			return (short) ((248 & this.r) << 8 | (252 & this.g) << 3 | this.b >> 3);
		}
	}
}
