package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.xml.PlayerTemplateData;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;
import net.sf.l2jdev.gameserver.network.serverpackets.NewCharacterSuccess;

public class NewCharacter extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		NewCharacterSuccess ct = new NewCharacterSuccess();
		ct.addChar(PlayerTemplateData.getInstance().getTemplate(PlayerClass.FIGHTER));
		ct.addChar(PlayerTemplateData.getInstance().getTemplate(PlayerClass.MAGE));
		ct.addChar(PlayerTemplateData.getInstance().getTemplate(PlayerClass.ELVEN_FIGHTER));
		ct.addChar(PlayerTemplateData.getInstance().getTemplate(PlayerClass.ELVEN_MAGE));
		ct.addChar(PlayerTemplateData.getInstance().getTemplate(PlayerClass.DARK_FIGHTER));
		ct.addChar(PlayerTemplateData.getInstance().getTemplate(PlayerClass.DARK_MAGE));
		ct.addChar(PlayerTemplateData.getInstance().getTemplate(PlayerClass.ORC_FIGHTER));
		ct.addChar(PlayerTemplateData.getInstance().getTemplate(PlayerClass.ORC_MAGE));
		ct.addChar(PlayerTemplateData.getInstance().getTemplate(PlayerClass.DWARVEN_FIGHTER));
		ct.addChar(PlayerTemplateData.getInstance().getTemplate(PlayerClass.KAMAEL_SOLDIER));
		this.getClient().sendPacket(ct);
	}
}
