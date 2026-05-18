package net.sf.l2jdev.gameserver.model.actor.instance;

import net.sf.l2jdev.gameserver.data.xml.NpcData;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;

public class Chest extends Monster
{
	private volatile boolean _specialDrop;

	public Chest(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.Chest);
		this.setRandomWalking(false);
		this._specialDrop = false;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		this._specialDrop = false;
		this.setMustRewardExpSp(true);
	}

	public synchronized void setSpecialDrop()
	{
		this._specialDrop = true;
	}

	@Override
	public void doItemDrop(NpcTemplate npcTemplate, Creature lastAttacker)
	{
		int id = this.getTemplate().getId();
		if (!this._specialDrop)
		{
			if (id >= 18265 && id <= 18286)
			{
				id += 3536;
			}
			else if (id == 18287 || id == 18288)
			{
				id = 21671;
			}
			else if (id == 18289 || id == 18290)
			{
				id = 21694;
			}
			else if (id == 18291 || id == 18292)
			{
				id = 21717;
			}
			else if (id == 18293 || id == 18294)
			{
				id = 21740;
			}
			else if (id == 18295 || id == 18296)
			{
				id = 21763;
			}
			else if (id == 18297 || id == 18298)
			{
				id = 21786;
			}
		}

		super.doItemDrop(NpcData.getInstance().getTemplate(id), lastAttacker);
	}

	@Override
	public boolean isMovementDisabled()
	{
		return true;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
}
