package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.managers.CastleManorManager;
import net.sf.l2jdev.gameserver.model.CropProcure;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Merchant;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.UniqueItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestProcureCropList extends ClientPacket
{
 
	private List<RequestProcureCropList.CropHolder> _items = null;

	@Override
	protected void readImpl()
	{
		int count = this.readInt();
		if (count > 0 && count <= PlayerConfig.MAX_ITEM_IN_PACKET && count * 20 == this.remaining())
		{
			this._items = new ArrayList<>(count);

			for (int i = 0; i < count; i++)
			{
				int objId = this.readInt();
				int itemId = this.readInt();
				int manorId = this.readInt();
				long cnt = this.readLong();
				if (objId < 1 || itemId < 1 || manorId < 0 || cnt < 0L)
				{
					this._items = null;
					return;
				}

				this._items.add(new RequestProcureCropList.CropHolder(objId, itemId, cnt, manorId));
			}
		}
	}

	@Override
	protected void runImpl()
	{
		if (this._items != null)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				CastleManorManager manor = CastleManorManager.getInstance();
				if (manor.isUnderMaintenance())
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					Npc manager = player.getLastFolkNPC();
					if (manager instanceof Merchant && manager.canInteract(player))
					{
						int castleId = manager.getCastle().getResidenceId();
						if (manager.getParameters().getInt("manor_id", -1) != castleId)
						{
							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
						else
						{
							int slots = 0;
							int weight = 0;

							for (RequestProcureCropList.CropHolder i : this._items)
							{
								Item item = player.getInventory().getItemByObjectId(i.getObjectId());
								if (item != null && item.getCount() >= i.getCount() && item.getId() == i.getId())
								{
									CropProcure cp = i.getCropProcure();
									if (cp != null && cp.getAmount() >= i.getCount())
									{
										ItemTemplate template = ItemData.getInstance().getTemplate(i.getRewardId());
										weight = (int) (weight + i.getCount() * template.getWeight());
										if (!template.isStackable())
										{
											slots = (int) (slots + i.getCount());
										}
										else if (player.getInventory().getItemByItemId(i.getRewardId()) == null)
										{
											slots++;
										}
										continue;
									}

									player.sendPacket(ActionFailed.STATIC_PACKET);
									return;
								}

								player.sendPacket(ActionFailed.STATIC_PACKET);
								return;
							}

							if (!player.getInventory().validateWeight(weight))
							{
								player.sendPacket(SystemMessageId.WEIGHT_LIMIT_IS_EXCEEDED);
							}
							else if (!player.getInventory().validateCapacity(slots))
							{
								player.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
							}
							else
							{
								int updateListSize = GeneralConfig.ALT_MANOR_SAVE_ALL_ACTIONS ? this._items.size() : 0;
								List<CropProcure> updateList = new ArrayList<>(updateListSize);

								for (RequestProcureCropList.CropHolder i : this._items)
								{
									long rewardPrice = ItemData.getInstance().getTemplate(i.getRewardId()).getReferencePrice();
									if (rewardPrice != 0L)
									{
										long rewardItemCount = i.getPrice() / rewardPrice;
										if (rewardItemCount < 1L)
										{
											SystemMessage sm = new SystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_S1_CROPS);
											sm.addItemName(i.getId());
											sm.addLong(i.getCount());
											player.sendPacket(sm);
										}
										else
										{
											long fee = castleId == i.getManorId() ? 0L : (long) (i.getPrice() * 0.05);
											if (fee != 0L && player.getAdena() < fee)
											{
												SystemMessage sm = new SystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_S1_CROPS);
												sm.addItemName(i.getId());
												sm.addLong(i.getCount());
												player.sendPacket(sm);
												sm = new SystemMessage(SystemMessageId.NOT_ENOUGH_ADENA);
												player.sendPacket(sm);
											}
											else
											{
												CropProcure cp = i.getCropProcure();
												if (cp.decreaseAmount(i.getCount()) && (fee <= 0L || player.reduceAdena(ItemProcessType.FEE, fee, manager, true)) && player.destroyItem(ItemProcessType.FEE, i.getObjectId(), i.getCount(), manager, true))
												{
													player.addItem(ItemProcessType.REWARD, i.getRewardId(), rewardItemCount, manager, true);
													if (GeneralConfig.ALT_MANOR_SAVE_ALL_ACTIONS)
													{
														updateList.add(cp);
													}
												}
											}
										}
									}
								}

								if (GeneralConfig.ALT_MANOR_SAVE_ALL_ACTIONS)
								{
									manor.updateCurrentProcure(castleId, updateList);
								}
							}
						}
					}
					else
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
				}
			}
		}
	}

	private class CropHolder extends UniqueItemHolder
	{
		private final int _manorId;
		private CropProcure _cp;
		private int _rewardId;

		public CropHolder(int objectId, int id, long count, int manorId)
		{
			Objects.requireNonNull(RequestProcureCropList.this);
			super(id, objectId, count);
			this._rewardId = 0;
			this._manorId = manorId;
		}

		public int getManorId()
		{
			return this._manorId;
		}

		public long getPrice()
		{
			return this.getCount() * this._cp.getPrice();
		}

		public CropProcure getCropProcure()
		{
			if (this._cp == null)
			{
				this._cp = CastleManorManager.getInstance().getCropProcure(this._manorId, this.getId(), false);
			}

			return this._cp;
		}

		public int getRewardId()
		{
			if (this._rewardId == 0)
			{
				this._rewardId = CastleManorManager.getInstance().getSeedByCrop(this._cp.getId()).getReward(this._cp.getReward());
			}

			return this._rewardId;
		}
	}
}
