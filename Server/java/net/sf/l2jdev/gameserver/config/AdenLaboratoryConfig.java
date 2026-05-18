package net.sf.l2jdev.gameserver.config;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.ConfigReader;

public class AdenLaboratoryConfig
{
	private static final Logger LOGGER = Logger.getLogger(AdenLaboratoryConfig.class.getName());
	public static final String ADENLAB_CONFIG_FILE = "./config/AdenLaboratory.ini";
	public static boolean ADENLAB_ENABLED;
	public static Map<Integer, Object[]> ADENLAB_NORMAL_ROLL_FEE_TYPE_CACHE;
	public static long ADENLAB_NORMAL_ADENA_FEE_AMOUNT;
	public static long ADENLAB_SPECIAL_RESEARCH_FEE;
	public static long ADENLAB_SPECIAL_CONFIRM_FEE;
	public static Map<Integer, Object[]> ADENLAB_INCREDIBLE_ROLL_FEE_TYPE_CACHE;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/AdenLaboratory.ini");
		ADENLAB_ENABLED = config.getBoolean("AdenLabEnabled", true);
		String[] normalFeeType = config.getString("NormalItemFeeList", "101588,1;101567,1;101572,1").split(";");
		ADENLAB_NORMAL_ROLL_FEE_TYPE_CACHE = new HashMap<>(normalFeeType.length);
		int adenLabCounter = 0;

		for (String feeType : normalFeeType)
		{
			if (!feeType.isEmpty())
			{
				try
				{
					String[] tempString = feeType.split(",", 2);
					if (tempString.length < 2)
					{
						LOGGER.warning("AdenLab: Incorrect fee type format `" + feeType + "`. Skipping.");
					}
					else
					{
						int itemId = Integer.parseInt(tempString[0].trim().replace("_", ""));
						long amount = Long.parseLong(tempString[1].trim().replace("_", ""));
						ADENLAB_NORMAL_ROLL_FEE_TYPE_CACHE.put(adenLabCounter++, new Object[]
						{
							itemId,
							amount
						});
					}
				}
				catch (NumberFormatException var14)
				{
					LOGGER.warning("AdenLab: Invalid fee type structure `" + feeType + "`. Skipping.");
				}
			}
		}

		ADENLAB_NORMAL_ADENA_FEE_AMOUNT = config.getLong("NormalAdenaFeeAmount", 10000000L);
		ADENLAB_SPECIAL_RESEARCH_FEE = config.getLong("SpecialResearchAdenaFeeAmount", 10000000L);
		ADENLAB_SPECIAL_CONFIRM_FEE = config.getLong("SpecialConfirmAdenaFeeAmount", 200000000L);
		String[] incredibleFeeType = config.getString("IncredibleItemFeeList", "98039,1;98040,1;100602,1").split(";");
		ADENLAB_INCREDIBLE_ROLL_FEE_TYPE_CACHE = new HashMap<>(incredibleFeeType.length);
		adenLabCounter = 0;

		for (String feeTypex : incredibleFeeType)
		{
			if (!feeTypex.isEmpty())
			{
				try
				{
					String[] tempString = feeTypex.split(",", 2);
					if (tempString.length == 2)
					{
						int itemId = Integer.parseInt(tempString[0].trim().replace("_", ""));
						long amount = Long.parseLong(tempString[1].trim().replace("_", ""));
						Object[] tempFee = new Object[]
						{
							itemId,
							amount
						};
						ADENLAB_INCREDIBLE_ROLL_FEE_TYPE_CACHE.put(adenLabCounter++, tempFee);
					}
					else
					{
						LOGGER.warning("AdenLab: Invalid fee structure for entry `" + feeTypex + "`. Skipping.");
					}
				}
				catch (NumberFormatException var13)
				{
					LOGGER.warning("AdenLab: Invalid fee type structure for entry `" + feeTypex + "`. Skipping.");
				}
			}
		}
	}
}
