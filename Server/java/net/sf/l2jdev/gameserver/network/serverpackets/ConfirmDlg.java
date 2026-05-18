package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class ConfirmDlg extends ServerPacket
{
	private int _time;
	private int _requesterId;
	private final SystemMessage _systemMessage;

	public ConfirmDlg(SystemMessageId smId)
	{
		this._systemMessage = new SystemMessage(smId);
	}

	public ConfirmDlg(int id)
	{
		this._systemMessage = new SystemMessage(id);
	}

	public ConfirmDlg(String text)
	{
		this._systemMessage = new SystemMessage(SystemMessageId.S1_3);
		this._systemMessage.addString(text);
	}

	public ConfirmDlg addTime(int time)
	{
		this._time = time;
		return this;
	}

	public ConfirmDlg addRequesterId(int id)
	{
		this._requesterId = id;
		return this;
	}

	public SystemMessage getSystemMessage()
	{
		return this._systemMessage;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.CONFIRM_DLG.writeId(this, buffer);
		SystemMessage.SMParam[] params = this._systemMessage.getParams();
		buffer.writeInt(this._systemMessage.getId());
		buffer.writeInt(params.length);

		for (SystemMessage.SMParam param : params)
		{
			buffer.writeInt(param.getType());
			switch (param.getType())
			{
				case 0:
				case 12:
					buffer.writeString(param.getStringValue());
					break;
				case 1:
				case 2:
				case 3:
				case 11:
					buffer.writeInt(param.getIntValue());
					break;
				case 4:
				{
					int[] array = param.getIntArrayValue();
					buffer.writeInt(array[0]);
					buffer.writeShort(array[1]);
					buffer.writeShort(array[2]);
					break;
				}
				case 5:
				case 10:
				case 13:
				case 15:
					buffer.writeShort(param.getIntValue());
					break;
				case 6:
					buffer.writeLong(param.getLongValue());
					break;
				case 7:
				case 16:
				{
					int[] array = param.getIntArrayValue();
					buffer.writeInt(array[0]);
					buffer.writeInt(array[1]);
					buffer.writeInt(array[2]);
				}
				case 8:
				case 14:
				case 17:
				case 18:
				case 19:
				case 21:
				case 22:
				case 23:
				default:
					break;
				case 9:
				case 20:
				case 24:
					buffer.writeByte(param.getIntValue());
			}
		}

		buffer.writeInt(this._time);
		buffer.writeInt(this._requesterId);
	}
}
