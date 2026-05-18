package net.sf.l2jdev.commons.network;

import java.nio.charset.StandardCharsets;

public abstract class ReadablePacket<T extends Client<Connection<T>>> implements Runnable
{
	private ReadableBuffer _buffer;
	private T _client;

	protected ReadablePacket()
	{
	}

	public void init(T client, ReadableBuffer buffer)
	{
		this._client = client;
		this._buffer = buffer;
	}

	protected char readChar()
	{
		return this._buffer.readChar();
	}

	protected byte readByte()
	{
		return this._buffer.readByte();
	}

	protected int readUnsignedByte()
	{
		return Byte.toUnsignedInt(this.readByte());
	}

	protected boolean readBoolean()
	{
		return this.readByte() != 0;
	}

	protected byte[] readBytes(int length)
	{
		byte[] result = new byte[length];
		this._buffer.readBytes(result, 0, length);
		return result;
	}

	protected void readBytes(byte[] dst)
	{
		this._buffer.readBytes(dst, 0, dst.length);
	}

	protected void readBytes(byte[] dst, int offset, int length)
	{
		this._buffer.readBytes(dst, offset, length);
	}

	protected short readShort()
	{
		return this._buffer.readShort();
	}

	protected int readInt()
	{
		return this._buffer.readInt();
	}

	protected long readLong()
	{
		return this._buffer.readLong();
	}

	protected float readFloat()
	{
		return this._buffer.readFloat();
	}

	protected double readDouble()
	{
		return this._buffer.readDouble();
	}

	protected String readString()
	{
		StringBuilder result = new StringBuilder();

		int charId;
		try
		{
			while ((charId = this.readShort()) != 0)
			{
				result.append((char) charId);
			}
		}
		catch (Exception var3)
		{
		}

		return result.toString();
	}

	protected String readSizedString()
	{
		String result = "";

		try
		{
			result = new String(this.readBytes(this.readShort() * 2), StandardCharsets.UTF_16LE);
		}
		catch (Exception var3)
		{
		}

		return result;
	}

	protected int remaining()
	{
		return this._buffer.remaining();
	}

	public T getClient()
	{
		return this._client;
	}

	protected abstract boolean read();
}
