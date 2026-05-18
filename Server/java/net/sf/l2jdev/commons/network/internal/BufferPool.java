package net.sf.l2jdev.commons.network.internal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class BufferPool
{
	private final Queue<ByteBuffer> _buffers = new ConcurrentLinkedQueue<>();
	private final AtomicInteger _maxSize = new AtomicInteger();
	private final AtomicInteger _estimateSize = new AtomicInteger();
	private final int _bufferSize;

	public BufferPool(int maxSize, int bufferSize)
	{
		this._maxSize.set(maxSize);
		this._bufferSize = bufferSize;
	}

	public void initialize(float factor)
	{
		int maxSize = this._maxSize.get();
		int amount = (int) Math.min(maxSize, maxSize * factor);

		for (int i = 0; i < amount; i++)
		{
			this._buffers.offer(ByteBuffer.allocateDirect(this._bufferSize).order(ByteOrder.LITTLE_ENDIAN));
		}

		this._estimateSize.set(amount);
	}

	public ByteBuffer get()
	{
		ByteBuffer buffer = this._buffers.poll();
		if (buffer != null)
		{
			this._estimateSize.decrementAndGet();
		}

		return buffer;
	}

	public boolean recycle(ByteBuffer buffer)
	{
		boolean recycle = this._estimateSize.get() < this._maxSize.get();
		if (recycle)
		{
			this._buffers.offer(buffer.clear());
			this._estimateSize.incrementAndGet();
		}

		return recycle;
	}

	public synchronized void expandCapacity(float factor, int limit)
	{
		int maxSize = this._maxSize.get();
		if (maxSize <= limit)
		{
			if (factor > 0.0F)
			{
				int amount = (int) (maxSize * factor);

				for (int i = 0; i < amount; i++)
				{
					this._buffers.offer(ByteBuffer.allocateDirect(this._bufferSize).order(ByteOrder.LITTLE_ENDIAN));
				}

				this._maxSize.set(maxSize + amount);
				this._estimateSize.addAndGet(amount);
			}
			else
			{
				this._maxSize.set(maxSize * 2);
			}
		}
	}

	public synchronized int getMaxSize()
	{
		return this._maxSize.get();
	}

	public boolean isFull()
	{
		return this._buffers.size() >= this._maxSize.get();
	}

	public boolean isEmpty()
	{
		return this._buffers.isEmpty();
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Pool {maxSize=");
		sb.append(this._maxSize.get());
		sb.append(", bufferSize=");
		sb.append(this._bufferSize);
		sb.append(", estimateUse=");
		sb.append(this._estimateSize.get());
		sb.append('}');
		return sb.toString();
	}
}
