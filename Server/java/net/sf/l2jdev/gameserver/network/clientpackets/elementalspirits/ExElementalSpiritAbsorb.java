package net.sf.l2jdev.gameserver.network.clientpackets.elementalspirits;

import net.sf.l2jdev.gameserver.model.ElementalSpirit;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ElementalSpiritType;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ElementalSpiritAbsorbItemHolder;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.enums.UserInfoType;
import net.sf.l2jdev.gameserver.network.serverpackets.UserInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits.ElementalSpiritAbsorb;

public class ExElementalSpiritAbsorb extends ClientPacket
{
	private byte _type;
	private int _itemId;
	private int _amount;

	@Override
	protected void readImpl()
	{
		this._type = this.readByte();
		this.readInt();
		this._itemId = this.readInt();
		this._amount = this.readInt();
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
			else
			{
				ElementalSpiritAbsorbItemHolder absorbItem = spirit.getAbsorbItem(this._itemId);
				if (absorbItem == null)
				{
					player.sendPacket(new ElementalSpiritAbsorb(player, this._type, false));
				}
				else
				{
					boolean canAbsorb = this.checkConditions(player, spirit);
					if (canAbsorb)
					{
						player.sendPacket(SystemMessageId.SUCCESSFUL_ABSORPTION);
						spirit.addExperience(absorbItem.getExperience() * this._amount);
						UserInfo userInfo = new UserInfo(player);
						userInfo.addComponentType(UserInfoType.ATT_SPIRITS);
						player.sendPacket(userInfo);
					}

					player.sendPacket(new ElementalSpiritAbsorb(player, this._type, canAbsorb));
				}
			}
		}
	}

	private boolean checkConditions(Player player, ElementalSpirit spirit)
	{
		if (player.isInStoreMode())
		{
			player.sendPacket(SystemMessageId.CANNOT_EVOLVE_ABSORB_EXTRACT_WHILE_USING_THE_PRIVATE_STORE_WORKSHOP);
			return false;
		}
		else if (player.isInBattle())
		{
			player.sendPacket(SystemMessageId.UNABLE_TO_ABSORB_DURING_BATTLE);
			return false;
		}
		else if (spirit.getLevel() == spirit.getMaxLevel() && spirit.getExperience() == spirit.getExperienceToNextLevel())
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_REACHED_THE_MAXIMUM_LEVEL_AND_CANNOT_ABSORB_ANY_FURTHER);
			return false;
		}
		else if (this._amount >= 1 && player.destroyItemByItemId(ItemProcessType.FEE, this._itemId, this._amount, player, true))
		{
			return true;
		}
		else
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_INGREDIENTS_TO_ABSORB);
			return false;
		}
	}
}
