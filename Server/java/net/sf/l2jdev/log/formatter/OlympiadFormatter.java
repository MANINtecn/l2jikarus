package net.sf.l2jdev.log.formatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import net.sf.l2jdev.commons.util.StringUtil;

public class OlympiadFormatter extends Formatter
{
	private final SimpleDateFormat _dateFormat = new SimpleDateFormat("dd/MM/yyyy H:mm:ss");

	@Override
	public String format(LogRecord record)
	{
		Object[] params = record.getParameters();
		StringBuilder output = new StringBuilder(30 + record.getMessage().length() + (params == null ? 0 : params.length * 10));
		StringUtil.append(output, this._dateFormat.format(new Date(record.getMillis())), ",", record.getMessage());
		if (params != null)
		{
			for (Object p : params)
			{
				if (p != null)
				{
					StringUtil.append(output, ",", p.toString());
				}
			}
		}

		output.append(System.lineSeparator());
		return output.toString();
	}
}
