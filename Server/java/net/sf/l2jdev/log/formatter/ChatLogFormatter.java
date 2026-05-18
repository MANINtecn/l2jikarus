package net.sf.l2jdev.log.formatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import net.sf.l2jdev.commons.util.StringUtil;

public class ChatLogFormatter extends Formatter
{
	private final SimpleDateFormat _dateFormat = new SimpleDateFormat("dd MMM H:mm:ss");

	@Override
	public String format(LogRecord record)
	{
		Object[] params = record.getParameters();
		StringBuilder output = new StringBuilder(30 + record.getMessage().length() + (params != null ? 10 * params.length : 0));
		StringUtil.append(output, "[", this._dateFormat.format(new Date(record.getMillis())), "] ");
		if (params != null)
		{
			for (Object p : params)
			{
				StringUtil.append(output, p, " ");
			}
		}

		StringUtil.append(output, record.getMessage(), System.lineSeparator());
		return output.toString();
	}
}
