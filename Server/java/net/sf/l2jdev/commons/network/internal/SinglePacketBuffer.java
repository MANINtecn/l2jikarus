package net.sf.l2jdev.commons.network.internal;

import java.nio.ByteBuffer;

import net.sf.l2jdev.commons.network.ReadableBuffer;

public class SinglePacketBuffer implements ReadableBuffer
{
	private final ByteBuffer _buffer;

	public SinglePacketBuffer(ByteBuffer buffer)
	{
		this._buffer = buffer;
	}

	@Override
	public char readChar()
	{
		return this._buffer.getChar();
	}

	@Override
	public byte readByte()
	{
		return this._buffer.get();
	}

	@Override
	public byte readByte(int index)
	{
		return this._buffer.get(index);
	}

	@Override
	public byte[] readBytes(int length)
	{
		byte[] result = new byte[length];
		this._buffer.get(result);
		return result;
	}

	@Override
	public void readBytes(byte[] dst)
	{
		this._buffer.get(dst);
	}

	@Override
	public void readBytes(byte[] dst, int offset, int length)
	{
		this._buffer.get(dst, offset, length);
	}

	@Override
	public short readShort()
	{
		return this._buffer.getShort();
	}

	@Override
	public short readShort(int index)
	{
		return this._buffer.getShort(index);
	}

	@Override
	public int readInt()
	{
		return this._buffer.getInt();
	}

	@Override
	public int readInt(int index)
	{
		return this._buffer.getInt(index);
	}

	@Override
	public float readFloat()
	{
		return this._buffer.getFloat();
	}

	@Override
	public long readLong()
	{
		return this._buffer.getLong();
	}

	@Override
	public double readDouble()
	{
		return this._buffer.getDouble();
	}

	@Override
	public void writeByte(int index, byte value)
	{
		this._buffer.put(index, value);
	}

	@Override
	public void writeShort(int index, short value)
	{
		this._buffer.putShort(index, value);
	}

	@Override
	public void writeInt(int index, int value)
	{
		this._buffer.putInt(index, value);
	}

	@Override
	public int limit()
	{
		return this._buffer.limit();
	}

	@Override
	public void limit(int newLimit)
	{
		this._buffer.limit(newLimit);
	}

	@Override
	public int remaining()
	{
		return this._buffer.remaining();
	}
}
