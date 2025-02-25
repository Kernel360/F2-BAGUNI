import type { TagSortableObjectType } from '@/types/TagSortableObjectType';

export const isTagSortableObject = (
  data: unknown,
): data is TagSortableObjectType => {
  if (
    !data ||
    typeof data !== 'object' ||
    !('id' in data) ||
    !('type' in data && data.type === 'tag') ||
    !('sortable' in data) ||
    !('tagOrder' in data && typeof data.tagOrder === 'number')
  ) {
    return false;
  }

  const { sortable } = data;

  if (
    !sortable ||
    typeof sortable !== 'object' ||
    !('containerId' in sortable) ||
    !('items' in sortable && Array.isArray(sortable.items)) ||
    !('index' in sortable)
  ) {
    return false;
  }

  return true;
};
