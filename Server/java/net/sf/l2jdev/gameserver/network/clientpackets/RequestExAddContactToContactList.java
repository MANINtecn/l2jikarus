package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExConfirmAddingContact;

public class RequestExAddContactToContactList extends ClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		this._name = this.readString();
	}

	@Override
	protected void runImpl()
	{
		if (GeneralConfig.ALLOW_MAIL)
		{
			if (this._name != null)
			{
				Player player = this.getPlayer();
				if (player != null)
				{
					boolean charAdded = player.getContactList().add(this._name);
					player.sendPacket(new ExConfirmAddingContact(this._name, charAdded));
				}
			}
		}
	}
}
