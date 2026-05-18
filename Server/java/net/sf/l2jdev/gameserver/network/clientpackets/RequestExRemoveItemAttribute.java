package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExBaseAttributeCancelResult;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestExRemoveItemAttribute extends ClientPacket
{
	private int _objectId;
	private long _price;
	private byte _element;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
		this._element = (byte) this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Item targetItem = player.getInventory().getItemByObjectId(this._objectId);
			if (targetItem != null)
			{
				AttributeType type = AttributeType.findByClientId(this._element);
				if (type != null)
				{
					if (targetItem.getAttributes() != null && targetItem.getAttribute(type) != null)
					{
						if (player.reduceAdena(ItemProcessType.FEE, this.getPrice(targetItem), player, true))
						{
							targetItem.clearAttribute(type);
							player.updateUserInfo();
							InventoryUpdate iu = new InventoryUpdate();
							iu.addModifiedItem(targetItem);
							player.sendInventoryUpdate(iu);
							AttributeType realElement = targetItem.isArmor() ? type.getOpposite() : type;
							SystemMessage sm;
							if (targetItem.getEnchantLevel() > 0)
							{
								if (targetItem.isArmor())
								{
									sm = new SystemMessage(SystemMessageId.S3_POWER_HAS_BEEN_REMOVED_FROM_S1_S2_S4_RESISTANCE_IS_DECREASED);
								}
								else
								{
									sm = new SystemMessage(SystemMessageId.S1_S2_S_S3_ATTRIBUTE_HAS_BEEN_REMOVED);
								}

								sm.addInt(targetItem.getEnchantLevel());
								sm.addItemName(targetItem);
								sm.addAttribute(realElement.getClientId());
								if (targetItem.isArmor())
								{
									sm.addAttribute(realElement.getClientId());
								}
							}
							else
							{
								if (targetItem.isArmor())
								{
									sm = new SystemMessage(SystemMessageId.S2_POWER_HAS_BEEN_REMOVED_FROM_S1_S3_RESISTANCE_IS_DECREASED);
								}
								else
								{
									sm = new SystemMessage(SystemMessageId.S1_S_S2_ATTRIBUTE_HAS_BEEN_REMOVED);
								}

								sm.addItemName(targetItem);
								if (targetItem.isArmor())
								{
									sm.addAttribute(realElement.getClientId());
									sm.addAttribute(realElement.getOpposite().getClientId());
								}
							}

							player.sendPacket(sm);
							player.sendPacket(new ExBaseAttributeCancelResult(targetItem.getObjectId(), this._element));
						}
						else
						{
							player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_FUNDS_TO_CANCEL_THIS_ATTRIBUTE);
						}
					}
				}
			}
		}
	}

	private long getPrice(Item item)
	{
		switch (item.getTemplate().getCrystalType())
		{
			case S:
				if (item.getTemplate() instanceof Weapon)
				{
					this._price = 50000L;
				}
				else
				{
					this._price = 40000L;
				}
				break;
			case S80:
				if (item.getTemplate() instanceof Weapon)
				{
					this._price = 100000L;
				}
				else
				{
					this._price = 80000L;
				}
				break;
			case S84:
				if (item.getTemplate() instanceof Weapon)
				{
					this._price = 200000L;
				}
				else
				{
					this._price = 160000L;
				}
				break;
			case R:
				if (item.getTemplate() instanceof Weapon)
				{
					this._price = 400000L;
				}
				else
				{
					this._price = 320000L;
				}
				break;
			case R95:
				if (item.getTemplate() instanceof Weapon)
				{
					this._price = 800000L;
				}
				else
				{
					this._price = 640000L;
				}
				break;
			case R99:
				if (item.getTemplate() instanceof Weapon)
				{
					this._price = 3200000L;
				}
				else
				{
					this._price = 2560000L;
				}
		}

		return this._price;
	}
}
