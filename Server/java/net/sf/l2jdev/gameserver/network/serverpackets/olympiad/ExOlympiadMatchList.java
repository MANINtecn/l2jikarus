package net.sf.l2jdev.gameserver.network.serverpackets.olympiad;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.olympiad.AbstractOlympiadGame;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadGameClassed;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadGameManager;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadGameNonClassed;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadGameTask;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExOlympiadMatchList extends ServerPacket
{
	private final List<OlympiadGameTask> _tasks = new ArrayList<>();

	public ExOlympiadMatchList()
	{
		for (int i = 0; i < OlympiadGameManager.getInstance().getNumberOfStadiums(); i++)
		{
			OlympiadGameTask task = OlympiadGameManager.getInstance().getOlympiadTask(i);
			if (task != null && task.isGameStarted() && !task.isBattleFinished())
			{
				this._tasks.add(task);
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_GFX_OLYMPIAD.writeId(this, buffer);
		buffer.writeInt(0);
		buffer.writeInt(this._tasks.size());
		buffer.writeInt(0);

		for (OlympiadGameTask task : this._tasks)
		{
			AbstractOlympiadGame game = task.getGame();
			if (game != null)
			{
				buffer.writeInt(game.getStadiumId());
				if (game instanceof OlympiadGameNonClassed)
				{
					buffer.writeInt(1);
				}
				else if (game instanceof OlympiadGameClassed)
				{
					buffer.writeInt(2);
				}
				else
				{
					buffer.writeInt(0);
				}

				buffer.writeInt(task.isRunning() ? 2 : 1);
				String[] names = game.getPlayerNames();
				buffer.writeString(names[0]);
				buffer.writeString(names[1]);
			}
		}
	}
}
