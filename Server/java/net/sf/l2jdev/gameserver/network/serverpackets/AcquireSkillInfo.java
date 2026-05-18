package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.SkillLearn;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.skill.CommonSkill;
import net.sf.l2jdev.gameserver.model.skill.enums.AcquireSkillType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class AcquireSkillInfo extends ServerPacket
{
	private final Player _player;
	private final AcquireSkillType _type;
	private final int _id;
	private final int _level;
	private final long _spCost;
	private final List<AcquireSkillInfo.Req> _reqs;

	public AcquireSkillInfo(Player player, AcquireSkillType skillType, SkillLearn skillLearn)
	{
		this._player = player;
		this._id = skillLearn.getSkillId();
		this._level = skillLearn.getSkillLevel();
		this._spCost = skillLearn.getLevelUpSp();
		this._type = skillType;
		this._reqs = new ArrayList<>();
		if (skillType != AcquireSkillType.PLEDGE || PlayerConfig.LIFE_CRYSTAL_NEEDED)
		{
			for (List<ItemHolder> item : skillLearn.getRequiredItems())
			{
				if (PlayerConfig.DIVINE_SP_BOOK_NEEDED || this._id != CommonSkill.DIVINE_INSPIRATION.getId())
				{
					this._reqs.add(new AcquireSkillInfo.Req(99, item.get(0).getId(), item.get(0).getCount(), 50));
				}
			}
		}
	}

	public AcquireSkillInfo(Player player, AcquireSkillType skillType, SkillLearn skillLearn, int sp)
	{
		this._player = player;
		this._id = skillLearn.getSkillId();
		this._level = skillLearn.getSkillLevel();
		this._spCost = sp;
		this._type = skillType;
		this._reqs = new ArrayList<>();

		for (List<ItemHolder> item : skillLearn.getRequiredItems())
		{
			this._reqs.add(new AcquireSkillInfo.Req(99, item.get(0).getId(), item.get(0).getCount(), 50));
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.ACQUIRE_SKILL_INFO.writeId(this, buffer);
		buffer.writeInt(this._player.getReplacementSkill(this._id));
		buffer.writeInt(this._level);
		buffer.writeLong(this._spCost);
		buffer.writeInt(this._type.getId());
		buffer.writeInt(this._reqs.size());

		for (AcquireSkillInfo.Req temp : this._reqs)
		{
			buffer.writeInt(temp.type);
			buffer.writeInt(temp.itemId);
			buffer.writeLong(temp.count);
			buffer.writeInt(temp.unk);
		}
	}

	private static class Req
	{
		public int itemId;
		public long count;
		public int type;
		public int unk;

		public Req(int pType, int pItemId, long itemCount, int pUnk)
		{
			this.itemId = pItemId;
			this.type = pType;
			this.count = itemCount;
			this.unk = pUnk;
		}
	}
}
