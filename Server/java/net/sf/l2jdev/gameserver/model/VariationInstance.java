package net.sf.l2jdev.gameserver.model;

import net.sf.l2jdev.gameserver.data.xml.OptionData;
import net.sf.l2jdev.gameserver.model.actor.Playable;
import net.sf.l2jdev.gameserver.model.options.Options;

public class VariationInstance
{
	private final int _mineralId;
	private final Options _option1;
	private final Options _option2;
	private final Options _option3;

	public VariationInstance(int mineralId, int option1Id, int option2Id, int option3Id)
	{
		this._mineralId = mineralId;
		this._option1 = OptionData.getInstance().getOptions(option1Id);
		this._option2 = OptionData.getInstance().getOptions(option2Id);
		this._option3 = OptionData.getInstance().getOptions(option3Id);
	}

	public VariationInstance(int mineralId, Options op1, Options op2, Options op3)
	{
		this._mineralId = mineralId;
		this._option1 = op1;
		this._option2 = op2;
		this._option3 = op3;
	}

	public int getMineralId()
	{
		return this._mineralId;
	}

	public int getOption1Id()
	{
		return this._option1 == null ? 0 : this._option1.getId();
	}

	public int getOption2Id()
	{
		return this._option2 == null ? 0 : this._option2.getId();
	}

	public int getOption3Id()
	{
		return this._option3 == null ? 0 : this._option3.getId();
	}

	public void applyBonus(Playable playable)
	{
		if (this._option1 != null)
		{
			this._option1.apply(playable);
		}

		if (this._option2 != null)
		{
			this._option2.apply(playable);
		}

		if (this._option3 != null)
		{
			this._option3.apply(playable);
		}
	}

	public void removeBonus(Playable playable)
	{
		if (this._option1 != null)
		{
			this._option1.remove(playable);
		}

		if (this._option2 != null)
		{
			this._option2.remove(playable);
		}

		if (this._option3 != null)
		{
			this._option3.remove(playable);
		}
	}
}
