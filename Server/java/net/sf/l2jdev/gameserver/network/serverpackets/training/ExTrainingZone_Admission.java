package net.sf.l2jdev.gameserver.network.serverpackets.training;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.TrainingCampConfig;
import net.sf.l2jdev.gameserver.data.xml.ExperienceData;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExTrainingZone_Admission extends ServerPacket
{
	private final long _timeElapsed;
	private final long _timeRemaining;
	private final double _maxExp;
	private final double _maxSp;

	public ExTrainingZone_Admission(int level, long timeElapsed, long timeRemaing)
	{
		this._timeElapsed = timeElapsed;
		this._timeRemaining = timeRemaing;
		this._maxExp = TrainingCampConfig.TRAINING_CAMP_EXP_MULTIPLIER * (ExperienceData.getInstance().getExpForLevel(level) * ExperienceData.getInstance().getTrainingRate(level) / TrainingCampConfig.TRAINING_CAMP_MAX_DURATION);
		this._maxSp = TrainingCampConfig.TRAINING_CAMP_SP_MULTIPLIER * (this._maxExp / 250.0);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_TRAININGZONE_ADMISSION.writeId(this, buffer);
		buffer.writeInt((int) this._timeElapsed);
		buffer.writeInt((int) this._timeRemaining);
		buffer.writeDouble(this._maxExp);
		buffer.writeDouble(this._maxSp);
	}
}
