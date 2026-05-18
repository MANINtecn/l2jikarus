package net.sf.l2jdev.commons.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.TimeUnit;

public class Connection<T extends Client<Connection<T>>>
{
	private final AsynchronousSocketChannel _channel;
	private final ReadHandler<T> _readHandler;
	private final WriteHandler<T> _writeHandler;
	private final ConnectionConfig _config;
	private T _client;
	private ByteBuffer _readingBuffer;
	private ByteBuffer[] _writingBuffers;

	public Connection(AsynchronousSocketChannel channel, ReadHandler<T> readHandler, WriteHandler<T> writeHandler, ConnectionConfig config)
	{
		this._channel = channel;
		this._readHandler = readHandler;
		this._writeHandler = writeHandler;
		this._config = config;
	}

	public void setClient(T client)
	{
		this._client = client;
	}

	public void read()
	{
		if (this._channel.isOpen())
		{
			this._channel.read(this._readingBuffer, this._client, this._readHandler);
		}
	}

	public void readHeader()
	{
		if (this._channel.isOpen())
		{
			this.releaseReadingBuffer();
			this._readingBuffer = this._config.resourcePool.getHeaderBuffer();
			this.read();
		}
	}

	public void read(int size)
	{
		if (this._channel.isOpen())
		{
			this._readingBuffer = this._config.resourcePool.recycleAndGetNew(this._readingBuffer, size);
			this.read();
		}
	}

	public boolean write(ByteBuffer[] buffers)
	{
		if (!this._channel.isOpen())
		{
			return false;
		}
		this._writingBuffers = buffers;
		this.write();
		return true;
	}

	public void write()
	{
		if (this._channel.isOpen() && this._writingBuffers != null)
		{
			this._channel.write(this._writingBuffers, 0, this._writingBuffers.length, -1L, TimeUnit.MILLISECONDS, this._client, this._writeHandler);
		}
		else if (this._client != null)
		{
			this._client.finishWriting();
		}
	}

	public ByteBuffer getReadingBuffer()
	{
		return this._readingBuffer;
	}

	private void releaseReadingBuffer()
	{
		if (this._readingBuffer != null)
		{
			this._config.resourcePool.recycleBuffer(this._readingBuffer);
			this._readingBuffer = null;
		}
	}

	public boolean releaseWritingBuffer()
	{
		boolean released = false;
		if (this._writingBuffers != null)
		{
			for (ByteBuffer buffer : this._writingBuffers)
			{
				this._config.resourcePool.recycleBuffer(buffer);
				released = true;
			}

			this._writingBuffers = null;
		}

		return released;
	}

	public void close()
	{
		this.releaseReadingBuffer();
		this.releaseWritingBuffer();

		try
		{
			if (this._channel.isOpen())
			{
				this._channel.close();
			}
		}
		catch (IOException var5)
		{
		}
		finally
		{
			this._client = null;
		}
	}

	public String getRemoteAddress()
	{
		try
		{
			InetSocketAddress address = (InetSocketAddress) this._channel.getRemoteAddress();
			return address.getAddress().getHostAddress();
		}
		catch (IOException var2)
		{
			return "";
		}
	}

	public boolean isOpen()
	{
		return this._channel.isOpen();
	}

	public ResourcePool getResourcePool()
	{
		return this._config.resourcePool;
	}

	public boolean dropPackets()
	{
		return this._config.dropPackets;
	}

	public int dropPacketThreshold()
	{
		return this._config.dropPacketThreshold;
	}
}
