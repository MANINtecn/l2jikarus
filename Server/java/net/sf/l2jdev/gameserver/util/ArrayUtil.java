package net.sf.l2jdev.gameserver.util;

public class ArrayUtil
{
	public static boolean contains(int[] array, int value)
	{
		for (int element : array)
		{
			if (element == value)
			{
				return true;
			}
		}

		return false;
	}

	public static boolean contains(Object[] array, Object value)
	{
		if (value == null)
		{
			for (Object element : array)
			{
				if (value == element)
				{
					return true;
				}
			}
		}
		else
		{
			for (Object element : array)
			{
				if (value.equals(element))
				{
					return true;
				}
			}
		}

		return false;
	}

	public static boolean contains(String[] array, String value, boolean ignoreCase)
	{
		if (ignoreCase)
		{
			for (String element : array)
			{
				if (value.equalsIgnoreCase(element))
				{
					return true;
				}
			}
		}
		else
		{
			for (String element : array)
			{
				if (value.equals(element))
				{
					return true;
				}
			}
		}

		return false;
	}
}
