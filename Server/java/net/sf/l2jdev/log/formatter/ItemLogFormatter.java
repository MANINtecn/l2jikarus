package net.sf.l2jdev.log.formatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class ItemLogFormatter extends Formatter
{
	private final SimpleDateFormat _dateFormat = new SimpleDateFormat("dd MMM H:mm:ss");

	@Override
	public String format(LogRecord record)
	{
		Object[] params = record.getParameters();
		StringBuilder output = new StringBuilder(30 + record.getMessage().length() + (params != null ? params.length * 50 : 0));
		StringUtil.append(output, "[", this._dateFormat.format(new Date(record.getMillis())), "] ", record.getMessage());
		if (params != null)
		{
			for (Object p : params)
			{
				if (p != null)
				{
					output.append(", ");
					if (p instanceof Item item)
					{
						StringUtil.append(output, "item ", String.valueOf(item.getObjectId()), ":");
						if (item.getEnchantLevel() > 0)
						{
							StringUtil.append(output, "+", String.valueOf(item.getEnchantLevel()), " ");
						}

						StringUtil.append(output, item.getTemplate().getName(), "(", String.valueOf(item.getCount()), ")");
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
