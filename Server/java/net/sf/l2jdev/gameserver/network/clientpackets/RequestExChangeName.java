package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.network.PacketLogger;

public class RequestExChangeName extends ClientPacket
{
	private String _newName;
	private int _type;
	private int _charSlot;

	@Override
	protected void readImpl()
	{
		this._type = this.readInt();
		this._newName = this.readString();
		this._charSlot = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		PacketLogger.info("Recieved " + this.getClass().getSimpleName() + " name: " + this._newName + " type: " + this._type + " CharSlot: " + this._charSlot);
	}
}
