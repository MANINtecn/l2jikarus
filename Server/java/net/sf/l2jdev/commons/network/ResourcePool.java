package net.sf.l2jdev.commons.network;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import net.sf.l2jdev.commons.network.internal.BufferPool;

public class ResourcePool
{
	private final NavigableMap<Integer, BufferPool> _bufferPools = new TreeMap<>();
	private boolean _autoExpandCapacity = true;
	private boolean _initBufferPools = false;
	private float _initBufferPoolFactor = 0.0F;
	private int _bufferSegmentSize = 64;

	public ByteBuffer getHeaderBuffer()
	{
		return this.getSizedBuffer(2);
	}

	public ByteBuffer getBuffer(int size)
	{
		return this.getSizedBuffer(this.determineBufferSize(size));
	}

	public ByteBuffer recycleAndGetNew(ByteBuffer buffer, int newSize)
	{
		int bufferSize = this.determineBufferSize(newSize);
		if (buffer != null)
		{
			if (buffer.clear().limit() == bufferSize)
			{
				return buffer.limit(newSize);
			}

			this.recycleBuffer(buffer);
		}

		return this.getSizedBuffer(bufferSize).limit(newSize);
	}

	private ByteBuffer getSizedBuffer(int size)
	{
		ByteBuffer buffer = null;
		BufferPool pool = null;
		Entry<Integer, BufferPool> entry = this._bufferPools.ceilingEntry(size);
		if (entry != null)
		{
			pool = entry.getValue();
			if (pool != null)
			{
				if (this._autoExpandCapacity)
				{
					if (this._initBufferPools)
					{
						if (pool.isEmpty())
						{
							pool.expandCapacity(this._initBufferPoolFactor, pool.getMaxSize());
						}
					}
					else if (pool.isFull())
					{
						pool.expandCapacity(this._initBufferPoolFactor, pool.getMaxSize());
					}
				}

				buffer = pool.get();
			}
		}

		if (buffer == null)
		{
			if (pool == null)
			{
				pool = new BufferPool(10, size);
				if (this._initBufferPools)
				{
					pool.initialize(this._initBufferPoolFactor);
				}

				this._bufferPools.put(size, pool);
			}

			buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.LITTLE_ENDIAN);
		}

		return buffer;
	}

	private int determineBufferSize(int size)
	{
		Entry<Integer, BufferPool> entry = this._bufferPools.ceilingEntry(size);
		if (entry != null)
		{
			return entry.getKey();
		}
		BufferPool pool = new BufferPool(10, size);
		if (this._initBufferPools)
		{
			pool.initialize(this._initBufferPoolFactor);
		}

		this._bufferPools.put(size, pool);
		return size;
	}

	public void recycleBuffer(ByteBuffer buffer)
	{
		if (buffer != null)
		{
			BufferPool pool = this._bufferPools.get(buffer.capacity());
			if (pool != null && !pool.recycle(buffer))
			{
			}
		}
	}

	public int getSegmentSize()
	{
		return this._bufferSegmentSize;
	}

	public void addBufferPool(int bufferSize, BufferPool bufferPool)
	{
		this._bufferPools.putIfAbsent(bufferSize, bufferPool);
	}

	public int bufferPoolSize()
	{
		return this._bufferPools.size();
	}

	public void initializeBuffers(boolean autoExpandCapacity, float initBufferPoolFactor)
	{
		this._autoExpandCapacity = autoExpandCapacity;
		this._initBufferPoolFactor = initBufferPoolFactor;
		this._initBufferPools = initBufferPoolFactor > 0.0F;
		if (this._initBufferPools)
		{
			this._bufferPools.values().forEach(pool -> pool.initialize(initBufferPoolFactor));
		}
	}

	public void setBufferSegmentSize(int size)
	{
		this._bufferSegmentSize = size;
	}

	public String stats()
	{
		StringBuilder sb = new StringBuilder();

		for (BufferPool pool : this._bufferPools.values())
		{
			sb.append(pool.toString());
			sb.append(System.lineSeparator());
		}

		return sb.toString();
	}
}
