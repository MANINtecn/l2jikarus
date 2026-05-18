package net.sf.l2jdev.commons.network;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

public class ReadHandler<T extends Client<Connection<T>>> implements CompletionHandler<Integer, T>
{
	private final PacketHandler<T> _packetHandler;
	private final PacketExecutor<T> _executor;

	public ReadHandler(PacketHandler<T> packetHandler, PacketExecutor<T> executor)
	{
		this._packetHandler = packetHandler;
		this._executor = executor;
	}

	@Override
	public void completed(Integer bytesRead, T client)
	{
		if (client.isConnected())
		{
			if (bytesRead < 0)
			{
				client.disconnect();
			}
			else if (bytesRead < client.getExpectedReadSize())
			{
				client.resumeRead(bytesRead);
			}
			else
			{
				if (client.isReadingPayload())
				{
					this.handlePayload(client);
				}
				else
				{
					this.handleHeader(client);
				}
			}
		}
	}

	private void handleHeader(T client)
	{
		ByteBuffer buffer = client.getConnection().getReadingBuffer();
		if (buffer == null)
		{
			client.disconnect();
		}
		else
		{
			buffer.flip();
			int dataSize = Short.toUnsignedInt(buffer.getShort()) - 2;
			if (dataSize > 0)
			{
				client.readPayload(dataSize);
			}
			else
			{
				client.read();
			}
		}
	}

	private void handlePayload(T client)
	{
		ByteBuffer buffer = client.getConnection().getReadingBuffer();
		if (buffer == null)
		{
			client.disconnect();
		}
		else
		{
			buffer.flip();
			this.parseAndExecutePacket(client, buffer);
			client.read();
		}
	}

	private void parseAndExecutePacket(T client, ByteBuffer incomingBuffer)
	{
		try
		{
			ReadableBuffer buffer = ReadableBuffer.of(incomingBuffer);
			if (client.decrypt(buffer, 0, buffer.remaining()))
			{
				ReadablePacket<T> packet = this._packetHandler.handlePacket(buffer, client);
				if (packet != null)
				{
					packet.init(client, buffer);
					this.execute(packet);
				}
			}
		}
		catch (Exception var5)
		{
			this.failed(var5, client);
		}
	}

	private void execute(ReadablePacket<T> packet)
	{
		if (packet.read())
		{
			this._executor.execute(packet);
		}
	}

	@Override
	public void failed(Throwable e, T client)
	{
		client.disconnect();
	}
}
