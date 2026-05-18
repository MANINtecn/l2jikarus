package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.sql.CrestTable;
import net.sf.l2jdev.gameserver.model.Crest;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPledgeEmblem;

public class RequestExPledgeCrestLarge extends ClientPacket
{
	private int _crestId;
	private int _clanId;

	@Override
	protected void readImpl()
	{
		this._crestId = this.readInt();
		this._clanId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Crest crest = CrestTable.getInstance().getCrest(this._crestId);
			byte[] data = crest != null ? crest.getData() : null;
			if (data != null)
			{
				for (int i = 0; i <= 4; i++)
				{
					int size = Math.max(Math.min(14336, data.length - 14336 * i), 0);
					if (size != 0)
					{
						byte[] chunk = new byte[size];
						System.arraycopy(data, 14336 * i, chunk, 0, size);
						player.sendPacket(new ExPledgeEmblem(this._crestId, chunk, this._clanId, i));
					}
				}
			}
		}
	}
}
