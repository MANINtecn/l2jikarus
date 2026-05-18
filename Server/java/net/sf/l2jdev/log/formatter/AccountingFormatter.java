package net.sf.l2jdev.log.formatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;

public class AccountingFormatter extends Formatter
{
	private final SimpleDateFormat _dateFormat = new SimpleDateFormat("dd MMM H:mm:ss");

	@Override
	public String format(LogRecord record)
	{
		Object[] params = record.getParameters();
		StringBuilder output = new StringBuilder(30 + record.getMessage().length() + (params == null ? 0 : params.length * 10));
		StringUtil.append(output, "[", this._dateFormat.format(new Date(record.getMillis())), "] ", record.getMessage());
		if (params != null)
		{
			for (Object p : params)
			{
				if (p != null)
				{
					output.append(", ");
					if (p instanceof GameClient client)
					{
						String address = null;

						try
						{
							if (!client.isDetached())
							{
								address = client.getIp();
							}
						}
						catch (Exception var11)
						{
						}

						switch (client.getConnectionState())
						{
							case ENTERING:
							case IN_GAME:
								if (client.getPlayer() != null)
								{
									StringUtil.append(output, client.getPlayer().getName(), "(", String.valueOf(client.getPlayer().getObjectId()), ") ");
								}
								break;
							case AUTHENTICATED:
								if (client.getAccountName() != null)
								{
									StringUtil.append(output, client.getAccountName(), " ");
								}
								break;
							case CONNECTED:
								if (address != null)
								{
									output.append(address);
								}
								break;
							default:
								throw new IllegalStateException("Missing state on switch");
						}
					}
					else if (p instanceof Player player)
					{
						StringUtil.append(output, player.getName(), "(", String.valueOf(player.getObjectId()), ")");
					}
					else
					{
						output.append(p.toString());
					}
				}
			}
		}

		output.append(System.lineSeparator());
		return output.toString();
	}
}
