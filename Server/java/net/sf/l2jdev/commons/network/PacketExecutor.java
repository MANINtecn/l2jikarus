package net.sf.l2jdev.commons.network;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.network.internal.MMOThreadFactory;

public class PacketExecutor<T extends Client<Connection<T>>>
{
	private static final Logger LOGGER = Logger.getLogger(PacketExecutor.class.getName());
	private final ThreadPoolExecutor _executor;

	public PacketExecutor(ConnectionConfig config)
	{
		this._executor = new ThreadPoolExecutor(config.threadPoolSize, Integer.MAX_VALUE, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new MMOThreadFactory("PacketExecutor", config.threadPriority));
	}

	public void execute(ReadablePacket<T> packet)
	{
		try
		{
			this._executor.execute(new PacketExecutor.PacketRunnable<>(packet));
		}
		catch (Exception var3)
		{
			LOGGER.warning(packet.getClass().getSimpleName() + System.lineSeparator() + var3.getMessage() + System.lineSeparator() + var3.getStackTrace());
		}
	}

	private static class PacketRunnable<T extends Client<Connection<T>>> implements Runnable
	{
		private final ReadablePacket<T> _packet;

		public PacketRunnable(ReadablePacket<T> packet)
		{
			this._packet = packet;
		}

		@Override
		public void run()
		{
			try
			{
				this._packet.run();
			}
			catch (Throwable var4)
			{
				Thread currentThread = Thread.currentThread();
				UncaughtExceptionHandler exceptionHandler = currentThread.getUncaughtExceptionHandler();
				if (exceptionHandler != null)
				{
					exceptionHandler.uncaughtException(currentThread, var4);
				}
			}
		}
	}
}
