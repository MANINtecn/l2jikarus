package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.Hit;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.BroochJewel;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class Attack extends ServerPacket
{
	private final int _attackerObjId;
	private final Location _attackerLoc;
	private final Location _targetLoc;
	private final List<Hit> _hits = new ArrayList<>();
	private final int _soulshotVisualSubstitute;

	public Attack(Creature attacker, Creature target)
	{
		this._attackerObjId = attacker.getObjectId();
		this._attackerLoc = new Location(attacker);
		this._targetLoc = new Location(target);
		Player player = attacker.asPlayer();
		if (player == null)
		{
			this._soulshotVisualSubstitute = 0;
		}
		else
		{
			BroochJewel activeRuby = player.getActiveRubyJewel();
			BroochJewel activeShappire = player.getActiveShappireJewel();
			if (activeRuby != null)
			{
				this._soulshotVisualSubstitute = activeRuby.getItemId();
			}
			else if (activeShappire != null)
			{
				this._soulshotVisualSubstitute = activeShappire.getItemId();
			}
			else
			{
				this._soulshotVisualSubstitute = 0;
			}
		}
	}

	public void addHit(Hit hit)
	{
		this._hits.add(hit);
	}

	public List<Hit> getHits()
	{
		return this._hits;
	}

	public boolean hasHits()
	{
		return !this._hits.isEmpty();
	}

	protected void writeHit(Hit hit, WritableBuffer buffer)
	{
		buffer.writeInt(hit.getTargetId());
		buffer.writeInt(hit.getDamage());
		buffer.writeInt(hit.getFlags());
		buffer.writeInt(hit.getGrade());
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		Iterator<Hit> it = this._hits.iterator();
		Hit firstHit = it.next();
		ServerPackets.ATTACK.writeId(this, buffer);
		buffer.writeInt(this._attackerObjId);
		buffer.writeInt(firstHit.getTargetId());
		buffer.writeInt(this._soulshotVisualSubstitute);
		buffer.writeInt(firstHit.getDamage());
		buffer.writeInt(firstHit.getFlags());
		buffer.writeInt(firstHit.getGrade());
		buffer.writeInt(this._attackerLoc.getX());
		buffer.writeInt(this._attackerLoc.getY());
		buffer.writeInt(this._attackerLoc.getZ());
		buffer.writeShort(this._hits.size() - 1);

		while (it.hasNext())
		{
			this.writeHit(it.next(), buffer);
		}

		buffer.writeInt(this._targetLoc.getX());
		buffer.writeInt(this._targetLoc.getY());
		buffer.writeInt(this._targetLoc.getZ());
	}
}
