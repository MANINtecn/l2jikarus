package net.sf.l2jdev.gameserver.network.clientpackets.newhenna;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.data.xml.HennaCombinationData;
import net.sf.l2jdev.gameserver.data.xml.HennaData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.combination.CombinationItemType;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.henna.CombinationHenna;
import net.sf.l2jdev.gameserver.model.item.henna.CombinationHennaReward;
import net.sf.l2jdev.gameserver.model.item.henna.Henna;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.newhenna.NewHennaPotenCompose;

public class RequestNewHennaCompose extends ClientPacket
{
	private int _slotOneIndex;
	private int _slotOneItemId;
	private int _slotTwoItemId;

	@Override
	protected void readImpl()
	{
		this._slotOneIndex = this.readInt();
		this._slotOneItemId = this.readInt();
		this._slotTwoItemId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Inventory inventory = player.getInventory();
			if (player.getHenna(this._slotOneIndex) != null && (this._slotOneItemId == -1 || inventory.getItemByObjectId(this._slotOneItemId) != null) && (this._slotTwoItemId == -1 || inventory.getItemByObjectId(this._slotTwoItemId) != null))
			{
				Henna henna = player.getHenna(this._slotOneIndex);
				CombinationHenna combinationHennas = HennaCombinationData.getInstance().getByHenna(henna.getDyeId());
				if (combinationHennas == null)
				{
					player.sendPacket(new NewHennaPotenCompose(henna.getDyeId(), -1, false));
				}
				else
				{
					if (this._slotOneItemId != -1 && combinationHennas.getItemOne() != inventory.getItemByObjectId(this._slotOneItemId).getId() || this._slotTwoItemId != -1 && combinationHennas.getItemTwo() != inventory.getItemByObjectId(this._slotTwoItemId).getId())
					{
						PacketLogger.info(this.getClass().getSimpleName() + ": " + player + " has modified client or combination data is outdated!" + System.lineSeparator() + "Henna DyeId: " + henna.getDyeId() + " ItemOne: " + combinationHennas.getItemOne() + " ItemTwo: " + combinationHennas.getItemTwo());
					}

					long commission = combinationHennas.getCommission();
					if (commission <= player.getAdena())
					{
						ItemHolder one = new ItemHolder(combinationHennas.getItemOne(), combinationHennas.getCountOne());
						ItemHolder two = new ItemHolder(combinationHennas.getItemTwo(), combinationHennas.getCountTwo());
						if ((this._slotOneItemId == -1 || inventory.getItemByItemId(one.getId()) != null && inventory.getItemByItemId(one.getId()).getCount() >= one.getCount()) && (this._slotTwoItemId == -1 || inventory.getItemByItemId(two.getId()) != null && inventory.getItemByItemId(two.getId()).getCount() >= two.getCount()))
						{
							InventoryUpdate iu = new InventoryUpdate();
							if (this._slotOneItemId != -1)
							{
								iu.addModifiedItem(inventory.getItemByItemId(one.getId()));
							}

							if (this._slotTwoItemId != -1)
							{
								iu.addModifiedItem(inventory.getItemByItemId(two.getId()));
							}

							iu.addModifiedItem(inventory.getItemByItemId(57));
							if ((this._slotOneItemId == -1 || inventory.destroyItemByItemId(ItemProcessType.FEE, one.getId(), one.getCount(), player, null) != null) && (this._slotTwoItemId == -1 || inventory.destroyItemByItemId(ItemProcessType.FEE, two.getId(), two.getCount(), player, null) != null) && inventory.destroyItemByItemId(ItemProcessType.FEE, 57, commission, player, null) != null)
							{
								if (Rnd.get(0, 100) <= combinationHennas.getChance())
								{
									CombinationHennaReward reward = combinationHennas.getReward(CombinationItemType.ON_SUCCESS);
									player.removeHenna(this._slotOneIndex, false);
									player.addHenna(this._slotOneIndex, HennaData.getInstance().getHenna(reward.getHennaId()));
									player.addItem(ItemProcessType.REWARD, reward.getId(), reward.getCount(), null, false);
									player.sendPacket(new NewHennaPotenCompose(reward.getHennaId(), reward.getId() == 0 ? -1 : reward.getId(), true));
								}
								else
								{
									CombinationHennaReward reward = combinationHennas.getReward(CombinationItemType.ON_FAILURE);
									if (henna.getDyeId() != reward.getHennaId())
									{
										player.removeHenna(this._slotOneIndex, false);
										player.addHenna(this._slotOneIndex, HennaData.getInstance().getHenna(reward.getHennaId()));
									}

									player.addItem(ItemProcessType.REWARD, reward.getId(), reward.getCount(), null, false);
									player.sendPacket(new NewHennaPotenCompose(reward.getHennaId(), reward.getId() == 0 ? -1 : reward.getId(), false));
								}

								player.sendPacket(iu);
							}
							else
							{
								player.sendPacket(new NewHennaPotenCompose(henna.getDyeId(), -1, false));
							}
						}
						else
						{
							player.sendPacket(new NewHennaPotenCompose(henna.getDyeId(), -1, false));
						}
					}
				}
			}
		}
	}
}
