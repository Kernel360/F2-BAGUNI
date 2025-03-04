package baguni.common.lib.util;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

// MapUtil
public class MapUtil {

	public enum SortBy {
		ASCENDING, // 내림차순
		DESCENDING // 오름차순
	}

	/**
	 * Defaults to Ascending Order
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		return map.entrySet()
				  .stream()
				  .sorted(Map.Entry.<K, V>comparingByValue())
				  .collect(
					  Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new)
				  );
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, SortBy sortBy) {
		// assign comparator by SortBy type
		Comparator<Map.Entry<K, V>> comparator;
		if (sortBy == SortBy.ASCENDING) {
			comparator = Map.Entry.<K, V>comparingByValue();
		}
		comparator = Map.Entry.<K, V>comparingByValue().reversed();

		return map.entrySet()
				  .stream()
				  .sorted(comparator)
				  .collect(
					  Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new)
				  );
	}
}
