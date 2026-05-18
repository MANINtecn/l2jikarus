package net.sf.l2jdev.gameserver.network.serverpackets.relics;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.holders.RelicCouponHolder;
import net.sf.l2jdev.gameserver.data.holders.RelicSummonCategoryHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExRelicsSummonResult extends ServerPacket
{
	public static final byte RESULT_ERROR = 0;
	public static final byte RESULT_SUCCESS = 1;
	public static final byte RESULT_OVER_100_COUPON_ERROR = 2;
	private final List<Integer> _relics = new ArrayList<>();
	private final byte _result = 1;
	private final RelicCouponHolder _relicCoupon;
	private final RelicSummonCategoryHolder _summonCategoryHolder;

	public ExRelicsSummonResult(RelicCouponHolder relicCoupon, List<Integer> relics)
	{
		this._relicCoupon = relicCoupon;
		this._summonCategoryHolder = null;
		this._relics.addAll(relics);
	}

	public ExRelicsSummonResult(RelicSummonCategoryHolder holder, List<Integer> relics)
	{
		this._relicCoupon = null;
		this._summonCategoryHolder = holder;
		this._relics.addAll(relics);
	}

	public ExRelicsSummonResult(byte result)
	{
		this._relicCoupon = null;
		this._summonCategoryHolder = null;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if (!this._relics.isEmpty())
		{
			ServerPackets.EX_RELICS_SUMMON_RESULT.writeId(this, buffer);
			buffer.writeByte(this._result);
			buffer.writeInt(this._relicCoupon == null ? this._summonCategoryHolder.getCategoryId() : this._relicCoupon.getItemId());
			buffer.writeInt(this._relics.size());

			for (int obtainedRelicId : this._relics)
			{
				buffer.writeInt(obtainedRelicId);
			}
		}
	}
}
