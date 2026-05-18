package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.LinkedList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.model.TimeStamp;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class SkillCoolTime extends ServerPacket
{
	private final List<TimeStamp> _reuseTimestamps = new LinkedList<>();

	public SkillCoolTime(Player player)
	{
		for (TimeStamp ts : player.getSkillReuseTimeStamps().values())
		{
			if (ts.hasNotPassed() && !SkillData.getInstance().getSkill(ts.getSkillId(), ts.getSkillLevel(), ts.getSkillSubLevel()).isNotBroadcastable())
			{
				this._reuseTimestamps.add(ts);
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SKILL_COOL_TIME.writeId(this, buffer);
		buffer.writeInt(this._reuseTimestamps.size());

		for (TimeStamp ts : this._reuseTimestamps)
		{
			long reuse = ts.getReuse();
			long remaining = ts.getRemaining();
			int sharedReuseGroup = ts.getSharedReuseGroup();
			buffer.writeInt(sharedReuseGroup > 0 ? sharedReuseGroup : ts.getSkillId());
			buffer.writeInt(ts.getSkillLevel());
			buffer.writeInt((int) (reuse > 0L ? reuse : remaining) / 1000);
			buffer.writeInt(Math.max(1, (int) remaining / 1000));
		}
	}
}
