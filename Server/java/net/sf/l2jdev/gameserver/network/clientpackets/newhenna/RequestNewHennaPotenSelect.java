package net.sf.l2jdev.gameserver.network.clientpackets.newhenna;

import net.sf.l2jdev.gameserver.data.xml.HennaPatternPotentialData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.henna.DyePotential;
import net.sf.l2jdev.gameserver.model.item.henna.HennaPoten;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.newhenna.NewHennaPotenSelect;

public class RequestNewHennaPotenSelect extends ClientPacket
{
	private int _slotId;
	private int _potenId;

	@Override
	protected void readImpl()
	{
		this._slotId = this.readByte();
		this._potenId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._slotId >= 1 && this._slotId <= player.getHennaPotenList().length)
			{
				DyePotential potential = HennaPatternPotentialData.getInstance().getPotential(this._potenId);
				HennaPoten hennaPoten = player.getHennaPoten(this._slotId);
				if (potential != null && potential.getSlotId() == this._slotId)
				{
					hennaPoten.setPotenId(this._potenId);
					player.sendPacket(new NewHennaPotenSelect(this._slotId, this._potenId, hennaPoten.getActiveStep(), true));
					player.applyDyePotenSkills();
				}
				else
				{
					player.sendPacket(new NewHennaPotenSelect(this._slotId, this._potenId, hennaPoten.getActiveStep(), false));
				}
			}
		}
	}
}
