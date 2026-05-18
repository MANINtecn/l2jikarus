package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Set;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Team;
import net.sf.l2jdev.gameserver.model.skill.AbnormalVisualEffect;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExUserInfoAbnormalVisualEffect extends ServerPacket
{
	private final Player _player;
	private final boolean _invisible;
	private final Set<AbnormalVisualEffect> _abnormalVisualEffects;
	private final Team _team;

	public ExUserInfoAbnormalVisualEffect(Player player)
	{
		this._player = player;
		this._invisible = player.isInvisible();
		this._abnormalVisualEffects = player.getEffectList().getCurrentAbnormalVisualEffects();
		this._team = GeneralConfig.BLUE_TEAM_ABNORMAL_EFFECT != null && GeneralConfig.RED_TEAM_ABNORMAL_EFFECT != null ? this._player.getTeam() : Team.NONE;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_USER_INFO_ABNORMALVISUALEFFECT.writeId(this, buffer);
		buffer.writeInt(this._player.getObjectId());
		buffer.writeInt(this._player.getTransformationId());
		buffer.writeInt(this._abnormalVisualEffects.size() + (this._invisible ? 1 : 0) + (this._team != Team.NONE ? 1 : 0));

		for (AbnormalVisualEffect abnormalVisualEffect : this._abnormalVisualEffects)
		{
			buffer.writeShort(abnormalVisualEffect.getClientId());
		}

		if (this._invisible)
		{
			buffer.writeShort(AbnormalVisualEffect.STEALTH.getClientId());
		}

		if (this._team == Team.BLUE)
		{
			if (GeneralConfig.BLUE_TEAM_ABNORMAL_EFFECT != null)
			{
				buffer.writeShort(GeneralConfig.BLUE_TEAM_ABNORMAL_EFFECT.getClientId());
			}
		}
		else if (this._team == Team.RED && GeneralConfig.RED_TEAM_ABNORMAL_EFFECT != null)
		{
			buffer.writeShort(GeneralConfig.RED_TEAM_ABNORMAL_EFFECT.getClientId());
		}
	}
}
