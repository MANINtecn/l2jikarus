package net.sf.l2jdev.gameserver.network.clientpackets.newhenna;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.henna.HennaPoten;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.newhenna.NewHennaPotenOpenslot;

public class RequestNewHennaPotenOpenslot extends ClientPacket
{
	private int _slotId;
	private int _reqOpenSlotStep;

	@Override
	protected void readImpl()
	{
		this._slotId = this.readInt();
		this._reqOpenSlotStep = this.readInt();
		this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			HennaPoten henna = player.getHennaPotenList()[this._slotId - 1];
			if (this._reqOpenSlotStep > 0 && this._reqOpenSlotStep < 31)
			{
				if (henna.getUnlockSlot() + 1 == this._reqOpenSlotStep)
				{
					henna.setUnlockSlot(this._reqOpenSlotStep);
					player.sendPacket(new NewHennaPotenOpenslot(true, this._slotId, this._reqOpenSlotStep, henna.getActiveStep()));
					player.applyDyePotenSkills();
				}
				else
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					player.sendPacket(new NewHennaPotenOpenslot(false, this._slotId, this._reqOpenSlotStep, henna.getActiveStep()));
				}
			}
		}
	}
}
