package net.sf.l2jdev.gameserver.util;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.sf.l2jdev.gameserver.model.captcha.TextureBlock;

public class DXT1ImageCompressor
{
 

	public byte[] compress(BufferedImage image)
	{
		int height = image.getHeight();
		int width = image.getWidth();
		int compressedSize = Math.max(width, 4) * Math.max(height, 4) / 2;
		ByteBuffer buffer = ByteBuffer.allocate(128 + compressedSize).order(ByteOrder.LITTLE_ENDIAN);
		this.writeHeader(image, buffer);
		int[] texelBuffer = new int[16];
		TextureBlock block = new TextureBlock();

		for (int i = 0; i < height; i += 4)
		{
			for (int j = 0; j < width; j += 4)
			{
				this.extractBlock(image, j, i, texelBuffer, block);
				buffer.putShort(block.getMaxColor());
				buffer.putShort(block.getMinColor());
				buffer.putInt(this.computColorIndexes(block));
			}
		}

		return buffer.array();
	}

	private int computColorIndexes(TextureBlock block)
	{
		TextureBlock.ARGB[] palette = block.getPalette();
		long encodedColors = 0L;

		for (int i = 15; i >= 0; i--)
		{
			TextureBlock.ARGB color = block.colorAt(i);
			int d0 = Math.abs(palette[0].r - color.r) + Math.abs(palette[0].g - color.g) + Math.abs(palette[0].b - color.b);
			int d1 = Math.abs(palette[1].r - color.r) + Math.abs(palette[1].g - color.g) + Math.abs(palette[1].b - color.b);
			int d2 = Math.abs(palette[2].r - color.r) + Math.abs(palette[2].g - color.g) + Math.abs(palette[2].b - color.b);
			int d3 = Math.abs(palette[3].r - color.r) + Math.abs(palette[3].g - color.g) + Math.abs(palette[3].b - color.b);
			int b0 = this.compare(d0, d3);
			int b1 = this.compare(d1, d2);
			int b2 = this.compare(d0, d2);
			int b3 = this.compare(d1, d3);
			int b4 = this.compare(d2, d3);
			int x0 = b1 & b2;
			int x1 = b0 & b3;
			int x2 = b0 & b4;
			long index = x2 | (x0 | x1) << 1;
			encodedColors |= index << (i << 1);
		}

		return (int) encodedColors;
	}

	protected int compare(int a, int b)
	{
		return b - a >>> 31;
	}

	protected void writeHeader(BufferedImage image, ByteBuffer buffer)
	{
		buffer.putInt(542327876);
		buffer.putInt(124);
		buffer.putInt(4103);
		buffer.putInt(image.getHeight());
		buffer.putInt(image.getWidth());
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.put(new byte[44]);
		buffer.putInt(32);
		buffer.putInt(4);
		buffer.putInt(827611204);
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putInt(4096);
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putInt(0);
	}

	protected void extractBlock(BufferedImage image, int x, int y, int[] buffer, TextureBlock block)
	{
		int blockWidth = Math.min(image.getWidth() - x, 4);
		int blockHeight = Math.min(image.getHeight() - y, 4);
		image.getRGB(x, y, blockWidth, blockHeight, buffer, 0, 4);
		block.of(buffer);
	}
}
