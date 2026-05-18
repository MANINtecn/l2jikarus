package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.MacroType;
import net.sf.l2jdev.gameserver.model.actor.holders.player.Macro;
import net.sf.l2jdev.gameserver.model.actor.holders.player.MacroCmd;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class RequestMakeMacro extends ClientPacket
{
	private Macro _macro;
	private int _commandsLength = 0;
 

	@Override
	protected void readImpl()
	{
		int id = this.readInt();
		String name = this.readString();
		String desc = this.readString();
		String acronym = this.readString();
		int icon = this.readInt();
		int count = this.readByte();
		if (count > 20)
		{
			count = 20;
		}

		List<MacroCmd> commands = new ArrayList<>(count);

		for (int i = 0; i < count; i++)
		{
			int entry = this.readByte();
			int type = this.readByte();
			int d1 = this.readInt();
			int d2 = this.readByte();
			String command = this.readString();
			this._commandsLength = this._commandsLength + command.length();
			commands.add(new MacroCmd(entry, MacroType.values()[type >= 1 && type <= 6 ? type : 0], d1, d2, command));
		}

		this._macro = new Macro(id, icon, name, desc, acronym, commands);
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._macro.getName().isEmpty())
			{
				player.sendPacket(SystemMessageId.ENTER_THE_NAME_OF_THE_MACRO);
			}
			else if (this._commandsLength > 255)
			{
				player.sendPacket(SystemMessageId.INVALID_MACRO_REFER_TO_THE_HELP_FILE_FOR_INSTRUCTIONS);
			}
			else
			{
				Collection<Macro> macros = player.getMacros().getAllMacroses().values();
				if (macros.size() > 48)
				{
					player.sendPacket(SystemMessageId.YOU_MAY_CREATE_UP_TO_48_MACROS);
				}
				else if (macros.stream().anyMatch(m -> m.getName().equalsIgnoreCase(this._macro.getName()) && m.getId() != this._macro.getId()))
				{
					player.sendPacket(SystemMessageId.ENTER_THE_NAME_OF_THE_MACRO);
				}
				else if (this._macro.getDescr().length() > 32)
				{
					player.sendPacket(SystemMessageId.MACRO_DESCRIPTIONS_MAY_CONTAIN_UP_TO_32_CHARACTERS);
				}
				else
				{
					player.registerMacro(this._macro);
				}
			}
		}
	}
}
