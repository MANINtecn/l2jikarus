package net.sf.l2jdev.gameserver.network.serverpackets.enchant.multi;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.EnchantItemRequest;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExResultMultiEnchantItemList extends ServerPacket
{
	public static final int SUCCESS = 0;
	public static final int FAIL = 1;
	public static final int ERROR = 2;
	private final Player _player;
	private boolean _error;
	private boolean _isResult;
	private Map<Integer, int[]> _successEnchant = new HashMap<>();
	private Map<Integer, Integer> _failureEnchant = new HashMap<>();
	private Map<Integer, ItemHolder> _failureReward = new HashMap<>();
	private final Map<Integer, Integer> _failChallengePointInfoList;

	public ExResultMultiEnchantItemList(Player player, boolean error)
	{
		this._player = player;
		this._error = error;
		this._failChallengePointInfoList = new HashMap<>();
	}

	public ExResultMultiEnchantItemList(Player player, Map<Integer, ItemHolder> failureReward)
	{
		this._player = player;
		this._failureReward = failureReward;
		this._failChallengePointInfoList = new HashMap<>();
	}

	public ExResultMultiEnchantItemList(Player player, Map<Integer, int[]> successEnchant, Map<Integer, Integer> failureEnchant)
	{
		this._player = player;
		this._successEnchant = successEnchant;
		this._failureEnchant = failureEnchant;
		this._failChallengePointInfoList = new HashMap<>();
	}

	public ExResultMultiEnchantItemList(Player player, Map<Integer, int[]> successEnchant, Map<Integer, Integer> failureEnchant, boolean isResult)
	{
		this._player = player;
		this._successEnchant = successEnchant;
		this._failureEnchant = failureEnchant;
		this._isResult = isResult;
		this._failChallengePointInfoList = new HashMap<>();
	}

	public ExResultMultiEnchantItemList(Player player, Map<Integer, int[]> successEnchant, Map<Integer, Integer> failureEnchant, Map<Integer, Integer> failChallengePointInfoList, boolean isResult)
	{
		this._player = player;
		this._successEnchant = successEnchant;
		this._failureEnchant = failureEnchant;
		this._isResult = isResult;
		this._failChallengePointInfoList = failChallengePointInfoList;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		EnchantItemRequest request = this._player.getRequest(EnchantItemRequest.class);
		if (request != null)
		{
			ServerPackets.EX_RES_MULTI_ENCHANT_ITEM_LIST.writeId(this, buffer);
			if (this._error)
			{
				buffer.writeByte(0);
			}
			else
			{
				buffer.writeByte(1);
				if (this._failureReward.isEmpty())
				{
					buffer.writeInt(this._successEnchant.size());
					if (!this._successEnchant.isEmpty())
					{
						for (int[] success : this._successEnchant.values())
						{
							buffer.writeInt(success[0]);
							buffer.writeInt(success[1]);
						}
					}
				}
				else
				{
					buffer.writeInt(0);
				}

				buffer.writeInt(this._failureEnchant.size());
				if (!this._failureEnchant.isEmpty())
				{
					for (int failure : this._failureEnchant.values())
					{
						buffer.writeInt(failure);
						buffer.writeInt(0);
					}
				}
				else
				{
					buffer.writeInt(0);
				}

				if (this._successEnchant.isEmpty() && request.getMultiFailItemsCount() != 0 || this._isResult && request.getMultiFailItemsCount() != 0)
				{
					buffer.writeInt(request.getMultiFailItemsCount());
					this._failureReward = request.getMultiEnchantFailItems();

					for (ItemHolder failure : this._failureReward.values())
					{
						buffer.writeInt(failure.getId());
						buffer.writeInt((int) failure.getCount());
					}

					if (this._isResult)
					{
						request.clearMultiSuccessEnchantList();
						request.clearMultiFailureEnchantList();
					}

					request.clearMultiFailReward();
				}
				else
				{
					buffer.writeInt(0);
				}

				buffer.writeInt(this._failChallengePointInfoList.size());

				for (Entry<Integer, Integer> item : this._failChallengePointInfoList.entrySet())
				{
					buffer.writeInt(item.getKey());
					buffer.writeInt(item.getValue());
				}
			}
		}
	}
}
