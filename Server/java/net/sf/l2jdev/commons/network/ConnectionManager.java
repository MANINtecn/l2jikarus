package net.sf.l2jdev.commons.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.util.Objects;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import net.sf.l2jdev.commons.network.internal.MMOThreadFactory;

public class ConnectionManager<T extends Client<Connection<T>>>
{
	private final AsynchronousChannelGroup _group;
	private final AsynchronousServerSocketChannel _socketChannel;
	private final ConnectionConfig _config;
	private final WriteHandler<T> _writeHandler;
	private final ReadHandler<T> _readHandler;
	private final Function<Connection<T>, T> _clientFactory;

	@SuppressWarnings(
	{
		"unchecked",
		"rawtypes"
	})
	public ConnectionManager(InetSocketAddress address, Function<Connection<T>, T> clientFactory, PacketHandler<T> packetHandler) throws IOException
	{
		this._config = new ConnectionConfig(address);
		this._clientFactory = clientFactory;
		this._readHandler = new ReadHandler<>(packetHandler, new PacketExecutor<>(this._config));
		this._writeHandler = new WriteHandler<>();
		this._group = AsynchronousChannelGroup.withCachedThreadPool(new ThreadPoolExecutor(this._config.threadPoolSize, Integer.MAX_VALUE, 1L, TimeUnit.MINUTES, new SynchronousQueue<>(), new MMOThreadFactory("Server", this._config.threadPriority)), 0);
		this._socketChannel = this._group.provider().openAsynchronousServerSocketChannel(this._group);
		this._socketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		this._socketChannel.bind(this._config.address);
		this._socketChannel.accept(null, new ConnectionManager.AcceptConnectionHandler());
	}

	public void shutdown()
	{
		try
		{
			this._socketChannel.close();
			this._group.shutdown();
			this._group.awaitTermination(this._config.shutdownWaitTime, TimeUnit.MILLISECONDS);
			this._group.shutdownNow();
		}
		catch (InterruptedException var2)
		{
			Thread.currentThread().interrupt();
		}
		catch (Exception var3)
		{
		}
	}

	private class AcceptConnectionHandler implements CompletionHandler<AsynchronousSocketChannel, Void>
	{
		private AcceptConnectionHandler()
		{
			Objects.requireNonNull(ConnectionManager.this);
			super();
		}

		@Override
		public void completed(AsynchronousSocketChannel clientChannel, Void attachment)
		{
			if (ConnectionManager.this._socketChannel.isOpen())
			{
				ConnectionManager.this._socketChannel.accept(null, this);
			}

			this.processNewConnection(clientChannel);
		}

		@Override
		public void failed(Throwable t, Void attachment)
		{
			if (ConnectionManager.this._socketChannel.isOpen())
			{
				ConnectionManager.this._socketChannel.accept(null, this);
			}
		}

		private void processNewConnection(AsynchronousSocketChannel channel)
		{
			if (channel != null && channel.isOpen())
			{
				try
				{
					channel.setOption(StandardSocketOptions.TCP_NODELAY, !ConnectionManager.this._config.useNagle);
					Connection<T> connection = new Connection<>(channel, ConnectionManager.this._readHandler, ConnectionManager.this._writeHandler, ConnectionManager.this._config);
					T client = ConnectionManager.this._clientFactory.apply(connection);
					connection.setClient(client);
					client.onConnected();
					client.read();
				}
				catch (ClosedChannelException var5)
				{
				}
				catch (Exception var6)
				{
					try
					{
						channel.close();
					}
					catch (IOException var4)
					{
					}
				}
			}
		}
	}
}
