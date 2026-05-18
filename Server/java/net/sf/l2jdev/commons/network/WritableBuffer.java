package net.sf.l2jdev.commons.network;

import java.nio.charset.StandardCharsets;

public abstract class WritableBuffer implements Buffer
{
	public abstract void writeChar(char var1);

	public abstract void writeByte(byte var1);

	public void writeByte(int value)
	{
		this.writeByte((byte) value);
	}

	public void writeByte(boolean value)
	{
		this.writeByte((byte) (value ? 1 : 0));
	}

	public abstract void writeBytes(byte... var1);

	public abstract void writeShort(short var1);

	public void writeShort(int value)
	{
		this.writeShort((short) value);
	}

	public void writeShort(boolean value)
	{
		this.writeShort((short) (value ? 1 : 0));
	}

	public abstract void writeInt(int var1);

	public void writeInt(boolean value)
	{
		this.writeInt(value ? 1 : 0);
	}

	public abstract void writeFloat(float var1);

	public abstract void writeLong(long var1);

	public abstract void writeDouble(double var1);

	public void writeString(CharSequence text)
	{
		if (text == null)
		{
			this.writeChar('\u0000');
		}
		else
		{
			this.writeStringWithCharset(text);
			this.writeChar('\u0000');
		}
	}

	private void writeStringWithCharset(CharSequence text)
	{
		this.writeBytes(text.toString().getBytes(StandardCharsets.UTF_16LE));
	}

	public void writeSizedString(CharSequence text)
	{
		if (text != null && text.length() > 0)
		{
			this.writeShort(text.length());
			this.writeStringWithCharset(text);
		}
		else
		{
			this.writeShort(0);
		}
	}
}
