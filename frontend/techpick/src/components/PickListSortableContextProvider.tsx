'use client';

import { useFetchPickListByFolderId } from '@/queries/useFetchPickListByFolderId';
import { usePickStore } from '@/stores/pickStore';
import type { PickViewDraggableItemListLayoutComponentProps } from '@/types/PickViewDraggableItemListLayoutComponentProps';
import {
  SortableContext,
  rectSortingStrategy,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';

export function PickListSortableContextProvider({
  folderId,
  children,
  viewType,
}: PickViewDraggableItemListLayoutComponentProps) {
  const selectedPickIdList = usePickStore((state) => state.selectedPickIdList);
  const isDragging = usePickStore((state) => state.isDragging);
  const focusPickId = usePickStore((state) => state.focusPickId);
  const { data } = useFetchPickListByFolderId(folderId);
  const pickList = data?.pages.flatMap((page) => page.content) ?? [];
  const pickListWithoutSelectedIdList = isDragging
    ? pickList.filter(
        (pickInfo) =>
          !selectedPickIdList.includes(pickInfo.id) ||
          pickInfo.id === focusPickId,
      )
    : pickList;

  /**
   * @description card일때와 vertical일 때(listItem, record) 렌더링이 다릅니다.
   */
  const strategy =
    viewType === 'record' ? verticalListSortingStrategy : rectSortingStrategy;

  return (
    <SortableContext
      id={`${folderId}`}
      items={pickListWithoutSelectedIdList.map((pickInfo) => pickInfo.id)}
      strategy={strategy}
    >
      {children}
    </SortableContext>
  );
}
