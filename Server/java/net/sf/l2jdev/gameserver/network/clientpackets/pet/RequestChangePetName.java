package net.sf.l2jdev.gameserver.network.clientpackets.pet;

import net.sf.l2jdev.gameserver.data.sql.PetNameTable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class RequestChangePetName extends ClientPacket
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
		Player player = this.getPlayer();
		if (player != null)
		{
			Summon pet = player.getPet();
			if (pet != null)
			{
				if (!pet.isPet())
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_A_GUARDIAN);
				}
				else if (pet.getName() != null && !pet.getName().equals(""))
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_SET_A_NAME_FOR_THE_GUARDIAN);
				}
				else if (PetNameTable.getInstance().doesPetNameExist(this._name, pet.getTemplate().getId()))
				{
					player.sendPacket(SystemMessageId.THE_NAME_IS_ALREADY_IN_USE_BY_ANOTHER_GUARDIAN);
				}
				else if (this._name.length() < 3 || this._name.length() > 16)
				{
					player.sendMessage("Your pet's name can be up to 16 characters in length.");
				}
				else if (!PetNameTable.getInstance().isValidPetName(this._name))
				{
					player.sendPacket(SystemMessageId.THE_GUARDIAN_S_NAME_INCLUDES_PROHIBITED_CHARACTERS);
				}
				else
				{
					pet.setName(this._name);
					pet.updateAndBroadcastStatus(1);
				}
			}
		}
	}
}
