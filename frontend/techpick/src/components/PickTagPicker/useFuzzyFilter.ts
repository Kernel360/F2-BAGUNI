import { useMemo } from 'react';
import { commandScore } from './useFuzzyFilter.core';

interface FuzzyItem<T> {
  id: number | string;
  value: string;
  keywords: string[];
  item: T;
}

type CustomFilterFn = (
  value: string,
  search: string,
  keywords?: string[],
) => number;

interface ConvertToFuzzyItemsParam<T> {
  items: T[];
  getValue: (item: T) => string;
  getKeywords: (item: T) => string[];
  getId: (item: T) => number | string;
}

export const convertToFuzzyItems = <T>({
  items,
  getId,
  getKeywords,
  getValue,
}: ConvertToFuzzyItemsParam<T>): FuzzyItem<T>[] => {
  return items.map((item) => ({
    id: getId(item),
    value: getValue(item),
    keywords: getKeywords(item),
    item,
  }));
};

export function useFuzzyFilter(customFilter?: CustomFilterFn) {
  return useMemo(
    () =>
      <T>(list: FuzzyItem<T>[], search: string): T[] => {
        if (!search.trim()) {
          list.map((item) => item.item);
        }

        return list
          .map((item) => {
            let score = 0;
            if (customFilter) {
              score = customFilter(item.value, search, item.keywords);
              if (score === 0) {
                score = commandScore(item.value, search, item.keywords);
              }
            } else {
              score = commandScore(item.value, search, item.keywords);
            }
            return { ...item, score };
          })
          .filter(({ score }) => score > 0)
          .sort((a, b) => b.score - a.score)
          .map(({ item }) => ({ ...item }));
      },
    [customFilter],
  );
}
