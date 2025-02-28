export const CREATABLE_TAG_KEYWORD = '생성';

export const filterCommandItems = (
  value: string,
  search: string,
  keywords?: string[] | undefined,
): number => {
  const keywordString = keywords?.join(' ') ?? '';

  if (keywordString?.includes(CREATABLE_TAG_KEYWORD)) {
    return 0.0001; // 낮은 우선순위
  }

  const extendValue = `${value.trim()} ${keywordString}`;

  return extendValue.includes(search.trim()) ? 1 : 0;
};

export const MAXIMUM_INT_RANGE = 512;

export const getRandomInt = () => {
  return Math.floor(Math.random() * MAXIMUM_INT_RANGE);
};
