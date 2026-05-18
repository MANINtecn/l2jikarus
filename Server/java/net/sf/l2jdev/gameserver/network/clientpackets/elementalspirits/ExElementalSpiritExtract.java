package net.sf.l2jdev.gameserver.network.clientpackets.elementalspirits;

import net.sf.l2jdev.gameserver.data.xml.ElementalSpiritData;
import net.sf.l2jdev.gameserver.model.ElementalSpirit;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ElementalSpiritType;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.enums.UserInfoType;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.UserInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits.ElementalSpiritExtract;

public class ExElementalSpiritExtract extends ClientPacket
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
			else
			{
				boolean canExtract = this.checkConditions(player, spirit);
				if (canExtract)
				{
					int amount = spirit.getExtractAmount();
					player.sendPacket(new SystemMessage(SystemMessageId.EXTRACTED_S1_S2_SUCCESSFULLY).addItemName(spirit.getExtractItem()).addInt(amount));
					spirit.reduceLevel();
					player.addItem(ItemProcessType.REWARD, spirit.getExtractItem(), amount, player, true);
					UserInfo userInfo = new UserInfo(player);
					userInfo.addComponentType(UserInfoType.ATT_SPIRITS);
					player.sendPacket(userInfo);
				}

				player.sendPacket(new ElementalSpiritExtract(player, this._type, canExtract));
			}
		}
	}

	protected boolean checkConditions(Player player, ElementalSpirit spirit)
	{
		if (spirit.getLevel() < 2 || spirit.getExtractAmount() < 1)
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ATTRIBUTE_XP_FOR_EXTRACTION);
			return false;
		}
		else if (!player.getInventory().validateCapacity(1L))
		{
			player.sendPacket(SystemMessageId.UNABLE_TO_EXTRACT_BECAUSE_INVENTORY_IS_FULL);
			return false;
		}
		else if (player.isInStoreMode())
		{
			player.sendPacket(SystemMessageId.CANNOT_EVOLVE_ABSORB_EXTRACT_WHILE_USING_THE_PRIVATE_STORE_WORKSHOP);
			return false;
		}
		else if (player.isInBattle())
		{
			player.sendPacket(SystemMessageId.UNABLE_TO_EVOLVE_DURING_BATTLE);
			return false;
		}
		else if (!player.reduceAdena(ItemProcessType.FEE, ElementalSpiritData.EXTRACT_FEES[spirit.getStage() - 1], player, true))
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_MATERIALS_FOR_EXTRACTION);
			return false;
		}
		else
		{
			return true;
		}
	}
}
