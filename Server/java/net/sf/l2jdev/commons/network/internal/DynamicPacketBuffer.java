package net.sf.l2jdev.commons.network.internal;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.commons.network.ResourcePool;

public class DynamicPacketBuffer extends InternalWritableBuffer
{
	private static final Map<Class<?>, Integer> MAXIMUM_PACKET_SIZE = new ConcurrentHashMap<>();
	private final ResourcePool _resourcePool;
	private final Class<?> _packetClass;
	private final int _initialSize;
	private DynamicPacketBuffer.PacketNode[] _nodes = new DynamicPacketBuffer.PacketNode[8];
	private DynamicPacketBuffer.PacketNode _currentNode;
	private int _nodeCount;
	private int _bufferIndex;
	private int _limit;

	public DynamicPacketBuffer(ResourcePool resourcePool, Class<?> packetClass)
	{
		this._resourcePool = resourcePool;
		this._packetClass = packetClass;
		this._initialSize = MAXIMUM_PACKET_SIZE.getOrDefault(packetClass, resourcePool.getSegmentSize());
		this.newNode(resourcePool.getBuffer(this._initialSize), 0);
	}

	public DynamicPacketBuffer(ByteBuffer buffer, ResourcePool resourcePool, Class<?> packetClass)
	{
		this._resourcePool = resourcePool;
		this._packetClass = packetClass;
		this._initialSize = MAXIMUM_PACKET_SIZE.getOrDefault(packetClass, buffer.capacity());
		this.newNode(buffer, 0);
	}

	private void newNode(ByteBuffer buffer, int initialIndex)
	{
		if (this._nodes.length == this._nodeCount)
		{
			this._nodes = Arrays.copyOf(this._nodes, (int) ((this._nodes.length + 1) * 1.2));
		}

		DynamicPacketBuffer.PacketNode node = new DynamicPacketBuffer.PacketNode(buffer, initialIndex, this._nodeCount);
		this._nodes[this._nodeCount++] = node;
		this._limit = node.endIndex;
	}

	@Override
	public void writeByte(byte value)
	{
		this.ensureSize(this._bufferIndex + 1);
		this.setByte(this._bufferIndex++, value);
	}

	@Override
	public void writeByte(int index, byte value)
	{
		this.checkBounds(index, 1);
		this.setByte(index, value);
	}

	private void checkBounds(int index, int length)
	{
		if (index < 0 || index + length > this._limit)
		{
			throw new IndexOutOfBoundsException("Trying access index " + index + " until index " + (index + length) + " , max accessible index is " + this._limit);
		}
	}

	private void setByte(int index, byte value)
	{
		DynamicPacketBuffer.PacketNode node = this.indexToNode(index);
		node.buffer.put(node.idx(index), value);
	}

	@Override
	public void writeBytes(byte... bytes)
	{
		if (bytes != null && bytes.length != 0)
		{
			this.ensureSize(this._bufferIndex + bytes.length);
			this.setBytes(this._bufferIndex, bytes);
			this._bufferIndex += bytes.length;
		}
	}

	private void setBytes(int index, byte[] bytes)
	{
		DynamicPacketBuffer.PacketNode node = this.indexToNode(index);
		int length = bytes.length;
		int offset = 0;

		do
		{
			int available = Math.min(length, node.endIndex - index);
			node.buffer.position(node.idx(index));
			node.buffer.put(bytes, offset, available);
			node.buffer.position(0);
			length -= available;
			offset += available;
			index += available;
			node = this._nodes[Math.min(node.offset + 1, this._nodes.length - 1)];
		}
		while (length > 0);
	}

	@Override
	public void writeShort(short value)
	{
		this.ensureSize(this._bufferIndex + 2);
		this.setShort(this._bufferIndex, value);
		this._bufferIndex += 2;
	}

	@Override
	public void writeShort(int index, short value)
	{
		this.checkBounds(index, 2);
		this.setShort(index, value);
	}

	private void setShort(int index, short value)
	{
		DynamicPacketBuffer.PacketNode node = this.indexToNode(index);
		if (index + 2 <= node.endIndex)
		{
			node.buffer.putShort(node.idx(index), value);
		}
		else
		{
			this.setByte(index, (byte) value);
			this.setByte(index + 1, (byte) (value >>> 8));
		}
	}

	@Override
	public void writeChar(char value)
	{
		this.writeShort((short) value);
	}

	@Override
	public void writeInt(int value)
	{
		this.ensureSize(this._bufferIndex + 4);
		this.setInt(this._bufferIndex, value);
		this._bufferIndex += 4;
	}

	@Override
	public void writeInt(int index, int value)
	{
		this.checkBounds(index, 4);
		this.setInt(index, value);
	}

	private void setInt(int index, int value)
	{
		DynamicPacketBuffer.PacketNode node = this.indexToNode(index);
		if (index + 4 <= node.endIndex)
		{
			node.buffer.putInt(node.idx(index), value);
		}
		else
		{
			this.setShort(index, (short) value);
			this.setShort(index + 2, (short) (value >>> 16));
		}
	}

	@Override
	public void writeFloat(float value)
	{
		this.writeInt(Float.floatToRawIntBits(value));
	}

	@Override
	public void writeLong(long value)
	{
		this.ensureSize(this._bufferIndex + 8);
		this.setLong(this._bufferIndex, value);
		this._bufferIndex += 8;
	}

	private void setLong(int index, long value)
	{
		DynamicPacketBuffer.PacketNode node = this.indexToNode(index);
		if (index + 8 <= node.endIndex)
		{
			node.buffer.putLong(node.idx(index), value);
		}
		else
		{
			this.setInt(index, (int) value);
			this.setInt(index + 4, (int) (value >>> 32));
		}
	}

	@Override
	public void writeDouble(double value)
	{
		this.writeLong(Double.doubleToRawLongBits(value));
	}

	@Override
	public int position()
	{
		return this._bufferIndex;
	}

	@Override
	public void position(int pos)
	{
		this._bufferIndex = pos;
	}

	@Override
	public byte readByte(int index)
	{
		this.checkSize(index + 1);
		return this.getByte(index);
	}

	private byte getByte(int index)
	{
		DynamicPacketBuffer.PacketNode node = this.indexToNode(index);
		return node.buffer.get(node.idx(index));
	}

	public void readBytes(int index, byte[] data)
	{
		this.checkSize(index + data.length);
		DynamicPacketBuffer.PacketNode node = this.indexToNode(index);
		int length = data.length;
		int offset = 0;

		do
		{
			int available = Math.min(length, node.endIndex - index);
			node.buffer.position(node.idx(index));
			node.buffer.get(data, offset, available);
			length -= available;
			offset += available;
			index += available;
			node = this._nodes[Math.min(node.offset + 1, this._nodes.length - 1)];
		}
		while (length > 0);
	}

	@Override
	public short readShort(int index)
	{
		this.checkSize(index + 2);
		return this.getShort(index);
	}

	private short getShort(int index)
	{
		DynamicPacketBuffer.PacketNode node = this.indexToNode(index);
		return index + 2 <= node.endIndex ? node.buffer.getShort(node.idx(index)) : (short) (this.getByte(index) & 255 | (this.getByte(index + 1) & 255) << 8);
	}

	@Override
	public int readInt(int index)
	{
		this.checkSize(index + 4);
		return this.getInt(index);
	}

	private int getInt(int index)
	{
		DynamicPacketBuffer.PacketNode node = this.indexToNode(index);
		return index + 4 <= node.endIndex ? node.buffer.getInt(node.idx(index)) : this.getShort(index) & 65535 | (this.getShort(index + 2) & 65535) << 16;
	}

	public float readFloat(int index)
	{
		return Float.intBitsToFloat(this.readInt(index));
	}

	public long readLong(int index)
	{
		this.checkSize(index + 8);
		DynamicPacketBuffer.PacketNode node = this.indexToNode(index);
		return index + 8 <= node.endIndex ? node.buffer.getLong(node.idx(index)) : this.getInt(index) & 4294967295L | (this.getInt(index + 4) & 4294967295L) << 32;
	}

	public double readDouble(int index)
	{
		return Double.longBitsToDouble(this.readLong(index));
	}

	@Override
	public int limit()
	{
		return this._limit;
	}

	@Override
	public void limit(int newLimit)
	{
		if (this._limit != this.capacity())
		{
			DynamicPacketBuffer.PacketNode node = this.indexToNode(this._limit);
			node.buffer.clear();
		}

		this.ensureSize(newLimit + 1);
		this._limit = newLimit;
		this.limitBuffer();
	}

	public int capacity()
	{
		return this._nodes[this._nodeCount - 1].endIndex;
	}

	@Override
	public void mark()
	{
		this._limit = this._bufferIndex;
		this.limitBuffer();
	}

	private void limitBuffer()
	{
		DynamicPacketBuffer.PacketNode node = this.indexToNode(this._limit - 1);
		node.buffer.limit(node.idx(this._limit));
	}

	private void ensureSize(int sizeRequired)
	{
		if (this.capacity() < sizeRequired)
		{
			int newSize = 64;

			while (newSize < sizeRequired)
			{
				newSize <<= 1;
			}

			this.increaseBuffers(newSize);
		}
	}

	private void increaseBuffers(int size)
	{
		int diffSize = size - this.capacity();
		ByteBuffer buffer = this._resourcePool.getBuffer(diffSize);
		DynamicPacketBuffer.PacketNode lastNode = this._nodes[this._nodeCount - 1];
		this.newNode(buffer, lastNode.endIndex);
	}

	private void checkSize(int size)
	{
		if (this._limit < size || size < 0)
		{
			throw new IndexOutOfBoundsException("Trying access index " + size + ", max size is " + this._limit);
		}
	}

	private DynamicPacketBuffer.PacketNode indexToNode(int index)
	{
		if (this._currentNode != null && this._currentNode.initialIndex <= index && this._currentNode.endIndex > index)
		{
			return this._currentNode;
		}
		int min = 0;
		int max = this._nodeCount - 1;

		while (min <= max)
		{
			int mid = min + max >>> 1;
			DynamicPacketBuffer.PacketNode node = this._nodes[mid];
			if (index >= node.endIndex)
			{
				min = mid + 1;
			}
			else
			{
				if (index >= node.initialIndex)
				{
					this._currentNode = node;
					return node;
				}

				max = mid - 1;
			}
		}

		throw new IndexOutOfBoundsException("Could not map the index to a node: " + index);
	}

	@Override
	public ByteBuffer[] toByteBuffers()
	{
		if (this._limit > this._initialSize)
		{
			MAXIMUM_PACKET_SIZE.put(this._packetClass, Math.min(this._limit, 65535));
		}

		int maxNode = this.indexToNode(this._limit - 1).offset;
		ByteBuffer[] buffers = new ByteBuffer[maxNode + 1];

		for (int i = 0; i <= maxNode; i++)
		{
			buffers[i] = this._nodes[i].buffer;
		}

		return buffers;
	}

	@Override
	public void releaseResources()
	{
		for (int i = 0; i < this._nodeCount; i++)
		{
			this._resourcePool.recycleBuffer(this._nodes[i].buffer);
			this._nodes[i] = null;
		}

		this._nodeCount = 0;
		this._bufferIndex = 0;
	}

	private static class PacketNode
	{
		public final ByteBuffer buffer;
		public final int initialIndex;
		public final int endIndex;
		public final int offset;

		public PacketNode(ByteBuffer buffer, int initialIndex, int offset)
		{
			this.buffer = buffer;
			this.initialIndex = initialIndex;
			this.endIndex = initialIndex + buffer.capacity();
			this.offset = offset;
		}

		public int idx(int index)
		{
			return index - this.initialIndex;
		}
	}
}
