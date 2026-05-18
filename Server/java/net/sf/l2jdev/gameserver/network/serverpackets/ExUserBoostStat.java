package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.BonusExpType;
import net.sf.l2jdev.gameserver.model.actor.stat.PlayerStat;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExUserBoostStat extends ServerPacket
{
	private final Player _player;
	private final BonusExpType _type;

	public ExUserBoostStat(Player player, BonusExpType type)
	{
		this._player = player;
		this._type = type;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_USER_BOOST_STAT.writeId(this, buffer);
		int count = 0;
		int bonus = 0;
		PlayerStat stat = this._player.getStat();
		switch (this._type)
		{
			case VITALITY:
				int vitalityBonus = (int) (stat.getVitalityExpBonus() * 100.0);
				if (vitalityBonus > 0)
				{
					count = 1;
					bonus = vitalityBonus;
				}
				break;
			case BUFFS:
				count = (int) stat.getValue(Stat.BONUS_EXP_BUFFS, 0.0);
				bonus = (int) (stat.getValue(Stat.ACTIVE_BONUS_EXP, 0.0) * 10.0);
				break;
			case PASSIVE:
				count = (int) stat.getValue(Stat.BONUS_EXP_PASSIVES, 0.0);
				bonus = (int) ((stat.getValue(Stat.BONUS_EXP, 0.0) - stat.getValue(Stat.ACTIVE_BONUS_EXP, 0.0)) * 10.0);
		}

		buffer.writeByte(this._type.getId());
		buffer.writeByte(count);
		buffer.writeShort(bonus);
	}
}
