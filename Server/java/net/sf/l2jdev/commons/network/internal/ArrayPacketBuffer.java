package net.sf.l2jdev.commons.network.internal;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.commons.network.ReadableBuffer;
import net.sf.l2jdev.commons.network.ResourcePool;

public class ArrayPacketBuffer extends InternalWritableBuffer implements ReadableBuffer
{
	private static final Map<Class<?>, Integer> MAXIMUM_PACKET_SIZE = new ConcurrentHashMap<>();
	private final ResourcePool _resourcePool;
	private final Class<?> _packetClass;
	private final int _initialSize;
	private byte[] _data;
	private int _index;
	private int _limit;

	public ArrayPacketBuffer(ResourcePool resourcePool, Class<?> packetClass)
	{
		this._resourcePool = resourcePool;
		this._packetClass = packetClass;
		this._initialSize = MAXIMUM_PACKET_SIZE.getOrDefault(packetClass, resourcePool.getSegmentSize());
		this._data = new byte[this._initialSize];
	}

	private void ensureSize(int size)
	{
		if (this._data.length < size)
		{
			this._data = Arrays.copyOf(this._data, (int) ((this._data.length + size) * 1.2));
			this._limit = this._data.length;
		}
	}

	@Override
	public void writeChar(char value)
	{
		this.writeShort((short) value);
	}

	@Override
	public void writeByte(byte value)
	{
		this.writeByte(this._index++, value);
	}

	@Override
	public void writeByte(int index, byte value)
	{
		this.ensureSize(index + 1);
		this._data[index] = value;
	}

	@Override
	public void writeBytes(byte... bytes)
	{
		if (bytes != null)
		{
			this.ensureSize(this._index + bytes.length);
			System.arraycopy(bytes, 0, this._data, this._index, bytes.length);
			this._index += bytes.length;
		}
	}

	@Override
	public void writeShort(short value)
	{
		this.writeShort(this._index, value);
		this._index += 2;
	}

	@Override
	public void writeShort(int index, short value)
	{
		this.ensureSize(index + 2);
		this._data[index++] = (byte) value;
		this._data[index] = (byte) (value >>> 8);
	}

	@Override
	public void writeInt(int value)
	{
		this.writeInt(this._index, value);
		this._index += 4;
	}

	@Override
	public void writeInt(int index, int value)
	{
		this.ensureSize(index + 4);
		this._data[index++] = (byte) value;
		this._data[index++] = (byte) (value >>> 8);
		this._data[index++] = (byte) (value >>> 16);
		this._data[index] = (byte) (value >>> 24);
	}

	@Override
	public void writeFloat(float value)
	{
		this.writeInt(Float.floatToRawIntBits(value));
	}

	@Override
	public void writeLong(long value)
	{
		this.ensureSize(this._index + 8);
		this._data[this._index++] = (byte) value;
		this._data[this._index++] = (byte) (value >>> 8);
		this._data[this._index++] = (byte) (value >>> 16);
		this._data[this._index++] = (byte) (value >>> 24);
		this._data[this._index++] = (byte) (value >>> 32);
		this._data[this._index++] = (byte) (value >>> 40);
		this._data[this._index++] = (byte) (value >>> 48);
		this._data[this._index++] = (byte) (value >>> 56);
	}

	@Override
	public void writeDouble(double value)
	{
		this.writeLong(Double.doubleToRawLongBits(value));
	}

	@Override
	public int position()
	{
		return this._index;
	}

	@Override
	public void position(int pos)
	{
		this._index = pos;
	}

	@Override
	public char readChar()
	{
		return (char) this.readShort();
	}

	@Override
	public byte readByte()
	{
		return this._data[this._index++];
	}

	@Override
	public byte readByte(int index)
	{
		return this._data[index];
	}

	private int readUnsigned(int index)
	{
		return Byte.toUnsignedInt(this._data[index]);
	}

	@Override
	public short readShort()
	{
		return (short) (this.readUnsigned(this._index++) | this.readUnsigned(this._index++) << 8);
	}

	@Override
	public short readShort(int index)
	{
		return (short) (this.readUnsigned(index++) | this.readUnsigned(index) << 8);
	}

	@Override
	public int readInt()
	{
		return this.readUnsigned(this._index++) | this.readUnsigned(this._index++) << 8 | this.readUnsigned(this._index++) << 16 | this.readUnsigned(this._index++) << 24;
	}

	@Override
	public float readFloat()
	{
		return Float.intBitsToFloat(this.readInt());
	}

	@Override
	public long readLong()
	{
		return Byte.toUnsignedLong(this._data[this._index++]) | Byte.toUnsignedLong(this._data[this._index++]) << 8 | Byte.toUnsignedLong(this._data[this._index++]) << 16 | Byte.toUnsignedLong(this._data[this._index++]) << 24 | Byte.toUnsignedLong(this._data[this._index++]) << 32 | Byte.toUnsignedLong(this._data[this._index++]) << 40 | Byte.toUnsignedLong(this._data[this._index++]) << 48 | Byte.toUnsignedLong(this._data[this._index++]) << 56;
	}

	@Override
	public double readDouble()
	{
		return Double.longBitsToDouble(this.readLong());
	}

	@Override
	public byte[] readBytes(int length)
	{
		byte[] result = new byte[length];
		this.readBytes(result, 0, length);
		return result;
	}

	@Override
	public void readBytes(byte[] dst)
	{
		this.readBytes(dst, 0, dst.length);
	}

	@Override
	public void readBytes(byte[] dst, int offset, int length)
	{
		System.arraycopy(this._data, this._index, dst, offset, length);
		this._index += length;
	}

	@Override
	public int readInt(int index)
	{
		return this.readUnsigned(index++) | this.readUnsigned(index++) << 8 | this.readUnsigned(index++) << 16 | this.readUnsigned(index) << 24;
	}

	@Override
	public int limit()
	{
		return this._limit;
	}

	@Override
	public void limit(int newLimit)
	{
		this.ensureSize(newLimit);
		this._limit = newLimit;
	}

	@Override
	public void mark()
	{
		this._limit = this._index;
	}

	@Override
	public ByteBuffer[] toByteBuffers()
	{
		return new ByteBuffer[]
		{
			this.toByteBuffer()
		};
	}

	public ByteBuffer toByteBuffer()
	{
		if (this._limit > this._initialSize)
		{
			MAXIMUM_PACKET_SIZE.put(this._packetClass, Math.min(this._limit, 65535));
		}

		ByteBuffer buffer = this._resourcePool.getBuffer(this._limit);
		buffer.put(this._data, 0, this._limit);
		return buffer.flip();
	}

	@Override
	public void releaseResources()
	{
		this._index = 0;
		this._limit = this._data.length;
	}

	@Override
	public int remaining()
	{
		return this._limit - this._index;
	}
}
