package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.holders.player.CombatPowerHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExItemScore extends ServerPacket
{
	private final int _total;
	private final int _equipedItem;
	private final int _relics;
	private final int _relicsCollection;
	private final int _adenLab;
	private final int _ensoul;
	private final int _bless;

	public ExItemScore(CombatPowerHolder combatPowerHolder)
	{
		this._total = combatPowerHolder.getTotalCombatPower();
		this._equipedItem = combatPowerHolder.getItemCombatPower();
		this._relics = combatPowerHolder.getRelicEffectCombatPower();
		this._relicsCollection = combatPowerHolder.getRelicCollectionCombatPower();
		this._adenLab = combatPowerHolder.getAdenLabCollectionCP();
		this._ensoul = combatPowerHolder.getEnsoulCP();
		this._bless = combatPowerHolder.getBlessCP();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ITEM_SCORE.writeId(this, buffer);
		buffer.writeInt(this._total);
		buffer.writeInt(this._equipedItem);
		buffer.writeInt(this._relics);
		buffer.writeInt(this._relicsCollection);
		buffer.writeInt(this._adenLab);
		buffer.writeInt(this._ensoul);
		buffer.writeInt(this._bless);
	}
}
