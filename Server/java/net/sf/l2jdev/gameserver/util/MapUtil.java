package net.sf.l2jdev.gameserver.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MapUtil
{
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean descending)
	{
		return descending ? map.entrySet().stream().sorted(Entry.comparingByValue(Collections.reverseOrder())).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, _) -> e1, LinkedHashMap::new)) : map.entrySet().stream().sorted(Entry.comparingByValue()).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, _) -> e1, LinkedHashMap::new));
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map)
	{
		return map.entrySet().stream().sorted(Entry.comparingByValue()).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, _) -> e1, LinkedHashMap::new));
	}
}
