package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.VariationData;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.options.OptionDataCategory;
import net.sf.l2jdev.gameserver.model.options.OptionDataGroup;
import net.sf.l2jdev.gameserver.model.options.Options;
import net.sf.l2jdev.gameserver.model.options.Variation;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class VariationProbList extends ServerPacket
{
	private final int _refineryId;
	private final Item _targetItem;
	private final Map<Options, Double> _options1 = new TreeMap<>(Comparator.comparingInt(Options::getId));
	private final Map<Options, Double> _options2 = new TreeMap<>(Comparator.comparingInt(Options::getId));
	private final Map<Options, Double> _options3 = new TreeMap<>(Comparator.comparingInt(Options::getId));

	public VariationProbList(int refineryId, Item targetItem)
	{
		this._refineryId = refineryId;
		this._targetItem = targetItem;
		Variation variation = VariationData.getInstance().getVariation(this._refineryId, targetItem);
		OptionDataGroup group1 = variation.getOptionDataGroup()[0];
		if (group1 != null)
		{
			for (OptionDataCategory category : group1.getCategories())
			{
				for (Entry<Options, Double> entry : category.getOptions().entrySet())
				{
					this._options1.put(entry.getKey(), entry.getValue());
				}
			}
		}

		OptionDataGroup group2 = variation.getOptionDataGroup()[1];
		if (group2 != null)
		{
			for (OptionDataCategory category : group2.getCategories())
			{
				for (Entry<Options, Double> entry : category.getOptions().entrySet())
				{
					this._options2.put(entry.getKey(), entry.getValue());
				}
			}
		}

		OptionDataGroup group3 = variation.getOptionDataGroup()[2];
		if (group3 != null)
		{
			for (OptionDataCategory category : group3.getCategories())
			{
				for (Entry<Options, Double> entry : category.getOptions().entrySet())
				{
					this._options3.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_VARIATION_PROB_LIST.writeId(this, buffer);
		buffer.writeInt(this._refineryId);
		buffer.writeInt(this._targetItem.getId());
		buffer.writeInt(1);
		buffer.writeInt(1);
		buffer.writeInt(this._options1.size() + this._options2.size() + this._options3.size());

		for (Entry<Options, Double> entry : this._options1.entrySet())
		{
			buffer.writeInt(1);
			buffer.writeInt(entry.getKey().getId());
			buffer.writeLong((long) (entry.getValue() * 1000000.0));
		}

		for (Entry<Options, Double> entry : this._options2.entrySet())
		{
			buffer.writeInt(2);
			buffer.writeInt(entry.getKey().getId());
			buffer.writeLong((long) (entry.getValue() * 1000000.0));
		}

		for (Entry<Options, Double> entry : this._options3.entrySet())
		{
			buffer.writeInt(3);
			buffer.writeInt(entry.getKey().getId());
			buffer.writeLong((long) (entry.getValue() * 1000000.0));
		}
	}
}
