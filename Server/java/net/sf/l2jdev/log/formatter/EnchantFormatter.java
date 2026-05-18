package net.sf.l2jdev.log.formatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class EnchantFormatter extends Formatter
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
					if (p instanceof Player player)
					{
						StringUtil.append(output, "Character:", player.getName(), " [", String.valueOf(player.getObjectId()), "] Account:", player.getAccountName());
						if (player.getClient() != null && !player.getClient().isDetached())
						{
							StringUtil.append(output, " IP:", player.getClient().getIp());
						}
					}
					else if (p instanceof Item item)
					{
						if (item.getEnchantLevel() > 0)
						{
							StringUtil.append(output, "+", String.valueOf(item.getEnchantLevel()), " ");
						}

						StringUtil.append(output, item.getTemplate().getName(), "(", String.valueOf(item.getCount()), ")", " [", String.valueOf(item.getObjectId()), "]");
					}
					else if (p instanceof Skill skill)
					{
						if (skill.getLevel() > 100)
						{
							StringUtil.append(output, "+", String.valueOf(skill.getLevel() % 100), " ");
						}

						StringUtil.append(output, skill.getName(), "(", String.valueOf(skill.getId()), " ", String.valueOf(skill.getLevel()), ")");
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
