package net.sf.l2jdev.gameserver.model.petition;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.gameserver.managers.IdManager;
import net.sf.l2jdev.gameserver.managers.PetitionManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2jdev.gameserver.network.serverpackets.PetitionVotePacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class Petition
{
	private final long _submitTime = System.currentTimeMillis();
	private final int _id;
	private final PetitionType _type;
	private PetitionState _state = PetitionState.PENDING;
	private final String _content;
	private final Collection<CreatureSay> _messageLog = ConcurrentHashMap.newKeySet();
	private final Player _petitioner;
	private Player _responder;

	public Petition(Player petitioner, String petitionText, int petitionType)
	{
		this._id = IdManager.getInstance().getNextId();
		this._type = PetitionType.values()[petitionType - 1];
		this._content = petitionText;
		this._petitioner = petitioner;
	}

	public boolean addLogMessage(CreatureSay cs)
	{
		return this._messageLog.add(cs);
	}

	public Collection<CreatureSay> getLogMessages()
	{
		return this._messageLog;
	}

	public boolean endPetitionConsultation(PetitionState endState)
	{
		this.setState(endState);
		if (this._responder != null && this._responder.isOnline())
		{
			if (endState == PetitionState.RESPONDER_REJECT)
			{
				this._petitioner.sendMessage("Your petition was rejected. Please try again later.");
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.A_GLOBAL_SUPPORT_CONSULTATION_C1_HAS_BEEN_FINISHED);
				sm.addString(this._petitioner.getName());
				this._responder.sendPacket(sm);
				if (endState == PetitionState.PETITIONER_CANCEL)
				{
					sm = new SystemMessage(SystemMessageId.REQUEST_NO_S1_TO_THE_GLOBAL_SUPPORT_WAS_CANCELLED);
					sm.addInt(this._id);
					this._responder.sendPacket(sm);
				}
			}
		}

		if (this._petitioner != null && this._petitioner.isOnline())
		{
			this._petitioner.sendPacket(SystemMessageId.GLOBAL_SUPPORT_HAS_ALREADY_RESPONDED_TO_YOUR_REQUEST_PLEASE_GIVE_US_FEEDBACK_ON_THE_SERVICE_QUALITY);
			this._petitioner.sendPacket(PetitionVotePacket.STATIC_PACKET);
		}

		PetitionManager.getInstance().getCompletedPetitions().put(this.getId(), this);
		return PetitionManager.getInstance().getPendingPetitions().remove(this.getId()) != null;
	}

	public String getContent()
	{
		return this._content;
	}

	public int getId()
	{
		return this._id;
	}

	public Player getPetitioner()
	{
		return this._petitioner;
	}

	public Player getResponder()
	{
		return this._responder;
	}

	public long getSubmitTime()
	{
		return this._submitTime;
	}

	public PetitionState getState()
	{
		return this._state;
	}

	public String getTypeAsString()
	{
		return this._type.toString().replace("_", " ");
	}

	public void sendPetitionerPacket(ServerPacket responsePacket)
	{
		if (this._petitioner != null && this._petitioner.isOnline())
		{
			this._petitioner.sendPacket(responsePacket);
		}
	}

	public void sendResponderPacket(ServerPacket responsePacket)
	{
		if (this._responder != null && this._responder.isOnline())
		{
			this._responder.sendPacket(responsePacket);
		}
		else
		{
			this.endPetitionConsultation(PetitionState.RESPONDER_MISSING);
		}
	}

	public void setState(PetitionState state)
	{
		this._state = state;
	}

	public void setResponder(Player respondingAdmin)
	{
		if (this._responder == null)
		{
			this._responder = respondingAdmin;
		}
	}
}
