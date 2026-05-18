package net.sf.l2jdev.gameserver.network.clientpackets.elementalspirits;

import net.sf.l2jdev.gameserver.model.ElementalSpirit;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ElementalSpiritType;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits.ElementalSpiritSetTalent;

public class ExElementalInitTalent extends ClientPacket
{
	private byte _type;

	@Override
	protected void readImpl()
	{
		this._type = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			ElementalSpirit spirit = player.getElementalSpirit(ElementalSpiritType.of(this._type));
			if (spirit == null)
			{
				player.sendPacket(SystemMessageId.NO_SPIRITS_ARE_AVAILABLE);
			}
			else if (player.isInBattle())
			{
				player.sendPacket(new SystemMessage(SystemMessageId.UNABLE_TO_RESET_THE_SPIRIT_ATTRIBUTES_WHILE_IN_BATTLE));
				player.sendPacket(new ElementalSpiritSetTalent(player, this._type, false));
			}
			else
			{
				if (player.reduceAdena(ItemProcessType.FEE, 50000L, player, true))
				{
					spirit.resetCharacteristics();
					player.sendPacket(new SystemMessage(SystemMessageId.RESET_THE_SELECTED_SPIRIT_S_CHARACTERISTICS_SUCCESSFULLY));
					player.sendPacket(new ElementalSpiritSetTalent(player, this._type, true));
				}
				else
				{
					player.sendPacket(new ElementalSpiritSetTalent(player, this._type, false));
				}
			}
		}
	}
}
