package net.sf.l2jdev.commons.network;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.l2jdev.commons.network.internal.InternalWritableBuffer;

public abstract class Client<T extends Connection<?>>
{
	private static final ConcurrentLinkedQueue<Client<?>> PENDING_CLIENTS = new ConcurrentLinkedQueue<>();
	private final T _connection;
	private final Queue<WritablePacket<? extends Client<T>>> _packetsToWrite = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean _writing = new AtomicBoolean();
	private final AtomicBoolean _disconnecting = new AtomicBoolean();
	private final AtomicBoolean _closing = new AtomicBoolean();
	private final AtomicInteger _estimateQueueSize = new AtomicInteger();
	private final AtomicInteger _dataSentSize = new AtomicInteger();
	private boolean _readingPayload;
	private int _expectedReadSize;

	protected Client(T connection)
	{
		if (connection != null && connection.isOpen())
		{
			this._connection = connection;
		}
		else
		{
			throw new IllegalArgumentException("The connection is null or closed.");
		}
	}

	protected void writePacket(WritablePacket<? extends Client<T>> packet)
	{
		if (this.isConnected() && packet != null && !this.packetCanBeDropped(packet))
		{
			this._estimateQueueSize.incrementAndGet();
			this._packetsToWrite.add(packet);
			this.writeFairPacket();
		}
	}

	@SuppressWarnings(
	{
		"unchecked",
		"rawtypes"
	})
	private boolean packetCanBeDropped(WritablePacket<? extends Client<T>> packet)
	{
		return this._connection.dropPackets() && this._estimateQueueSize.get() > this._connection.dropPacketThreshold() && ((WritablePacket) packet).canBeDropped(this);
	}

	protected void writePackets(Collection<WritablePacket<? extends Client<T>>> packets)
	{
		if (this.isConnected() && packets != null && !packets.isEmpty())
		{
			this._estimateQueueSize.addAndGet(packets.size());
			this._packetsToWrite.addAll(packets);
			this.writeFairPacket();
		}
	}

	private void writeFairPacket()
	{
		if (this._writing.compareAndSet(false, true))
		{
			this.sendFairPacket();
		}
	}

	private void writeNextPacket()
	{
		WritablePacket<? extends Client<T>> packet = this._packetsToWrite.poll();
		if (packet == null)
		{
			this.releaseWritingResource();
			if (this._closing.get())
			{
				this.disconnect();
			}
		}
		else
		{
			this._estimateQueueSize.decrementAndGet();
			this.write(packet);
		}
	}

	private void sendFairPacket()
	{
		PENDING_CLIENTS.offer(this);
		Client<?> nextClient = PENDING_CLIENTS.poll();
		if (nextClient != null)
		{
			nextClient.writeNextPacket();
		}
	}

	@SuppressWarnings(
	{
		"unchecked",
		"rawtypes"
	})
	private void write(WritablePacket<? extends Client<T>> packet)
	{
		boolean written = false;
		InternalWritableBuffer buffer = null;

		try
		{
			buffer = ((WritablePacket) packet).writeData(this);
			int payloadSize = buffer.limit() - 2;
			if ((payloadSize <= 0) || !this.encrypt(buffer, 2, payloadSize))
			{
				return;
			}

			int bufferLimit = buffer.limit();
			this._dataSentSize.set(bufferLimit);
			if (bufferLimit > 2)
			{
				packet.writeHeader(buffer, bufferLimit);
				written = this._connection.write(buffer.toByteBuffers());
				return;
			}
		}
		catch (Exception var9)
		{
			return;
		}
		finally
		{
			if (!written)
			{
				this.handleNotWritten(buffer);
			}
		}
	}

	private void handleNotWritten(InternalWritableBuffer buffer)
	{
		if (!this.releaseWritingResource() && buffer != null)
		{
			buffer.releaseResources();
		}

		if (this.isConnected())
		{
			this.writeFairPacket();
		}
	}

	public void read()
	{
		this._expectedReadSize = 2;
		this._readingPayload = false;
		this._connection.readHeader();
	}

	public void readPayload(int dataSize)
	{
		this._expectedReadSize = dataSize;
		this._readingPayload = true;
		this._connection.read(dataSize);
	}

	public void close()
	{
		this.close(null);
	}

	public void close(WritablePacket<? extends Client<T>> packet)
	{
		if (this.isConnected())
		{
			this._packetsToWrite.clear();
			if (packet != null)
			{
				this._packetsToWrite.add(packet);
			}

			this._closing.set(true);
			this.writeFairPacket();
		}
	}

	public void resumeSend(int result)
	{
		this._dataSentSize.addAndGet(-result);
		this._connection.write();
	}

	public void finishWriting()
	{
		this._connection.releaseWritingBuffer();
		this.sendFairPacket();
	}

	private boolean releaseWritingResource()
	{
		boolean released = this._connection.releaseWritingBuffer();
		this._writing.set(false);
		return released;
	}

	public void disconnect()
	{
		if (this._disconnecting.compareAndSet(false, true))
		{
			try
			{
				this.onDisconnection();
			}
			finally
			{
				this._packetsToWrite.clear();
				this._connection.close();
			}
		}
	}

	public T getConnection()
	{
		return this._connection;
	}

	public int getDataSentSize()
	{
		return this._dataSentSize.get();
	}

	public String getHostAddress()
	{
		return this._connection == null ? "" : this._connection.getRemoteAddress();
	}

	public boolean isConnected()
	{
		return this._connection.isOpen() && !this._closing.get();
	}

	public int getEstimateQueueSize()
	{
		return this._estimateQueueSize.get();
	}

	public ResourcePool getResourcePool()
	{
		return this._connection.getResourcePool();
	}

	public boolean isReadingPayload()
	{
		return this._readingPayload;
	}

	public void resumeRead(int bytesRead)
	{
		this._expectedReadSize -= bytesRead;
		this._connection.read();
	}

	public int getExpectedReadSize()
	{
		return this._expectedReadSize;
	}

	public abstract boolean encrypt(Buffer var1, int var2, int var3);

	public abstract boolean decrypt(Buffer var1, int var2, int var3);

	protected abstract void onDisconnection();

	public abstract void onConnected();
}
