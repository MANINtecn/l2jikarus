package net.sf.l2jdev.commons.network;

import java.nio.channels.CompletionHandler;

public class WriteHandler<T extends Client<Connection<T>>> implements CompletionHandler<Long, T>
{
	@Override
	public void completed(Long result, T client)
	{
		if (client != null)
		{
			int bytesWritten = result.intValue();
			if (bytesWritten < 0)
			{
				if (client.isConnected())
				{
					client.disconnect();
				}
			}
			else
			{
				if (bytesWritten > 0 && bytesWritten < client.getDataSentSize())
				{
					client.resumeSend(bytesWritten);
				}
				else
				{
					client.finishWriting();
				}
			}
		}
	}

	@Override
	public void failed(Throwable e, T client)
	{
		client.disconnect();
	}
}
