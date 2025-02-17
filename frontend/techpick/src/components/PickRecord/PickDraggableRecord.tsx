'use client';

import useSearchElementId from '@/hooks/useSearchElementId';
import { useFetchPickListByFolderId } from '@/queries/useFetchPickListByFolderId';
import { usePickStore } from '@/stores/pickStore';
import { useUpdatePickStore } from '@/stores/updatePickStore';
import type { PickViewDraggableItemComponentProps } from '@/types/PickViewDraggableItemComponentProps';
import { getSelectedPickRange } from '@/utils/getSelectedPickRange';
import { isSelectionActive } from '@/utils/isSelectionActive';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import type { CSSProperties, MouseEvent } from 'react';
import { PickContextMenu } from './PickContextMenu';
import { PickRecord } from './PickRecord';
import {
  isActiveDraggingItemStyle,
  searchedItemStyle,
  selectedDragItemStyle,
} from './pickDraggableRecord.css';

// ref={index === pickList.length - 10 ? ref : null}

export function PickDraggableRecord({
  pickInfo,
}: PickViewDraggableItemComponentProps) {
  const { data } = useFetchPickListByFolderId(pickInfo.parentFolderId);
  const pickList = data?.pages.flatMap((page) => page.content) ?? [];
  const selectedPickIdList = usePickStore((state) => state.selectedPickIdList);
  const selectSinglePick = usePickStore((state) => state.selectSinglePick);
  const focusPickId = usePickStore((state) => state.focusPickId);
  const setSelectedPickIdList = usePickStore(
    (state) => state.setSelectedPickIdList,
  );
  const isDragging = usePickStore((state) => state.isDragging);
  const {
    setCurrentUpdateTitlePickIdToNull,
    currentUpdateTitlePickId,
    currentUpdateTagPickId,
  } = useUpdatePickStore();
  const { searchElementId, isOccurClickEvent } = useSearchElementId();
  const { id: pickId, parentFolderId } = pickInfo;
  const isSelected = selectedPickIdList.includes(pickId);
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging: isActiveDragging,
  } = useSortable({
    id: pickId,
    data: {
      id: pickId,
      type: 'pick',
      parentFolderId,
      pickInfo,
    },
    disabled:
      currentUpdateTitlePickId === pickInfo.id ||
      currentUpdateTagPickId === pickInfo.id,
  });
  const pickElementId = `pickId-${pickId}`;
  const isSearchedPickHighlight =
    searchElementId === pickElementId && !isOccurClickEvent;

  const style: CSSProperties = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: 1,
  };

  const handleShiftSelect = (pickId: number) => {
    if (!focusPickId) {
      return;
    }

    // 새로운 선택된 배열 만들기.
    const orderedPickIdList = pickList.map((pickInfo) => pickInfo.id);

    const newSelectedPickIdList = getSelectedPickRange({
      orderedPickIdList,
      startPickId: focusPickId,
      endPickId: pickId,
    });

    setSelectedPickIdList(newSelectedPickIdList);
  };

  const handleClick = (
    pickId: number,
    event: MouseEvent<HTMLDivElement, globalThis.MouseEvent>,
  ) => {
    if (event.shiftKey && isSelectionActive(selectedPickIdList.length)) {
      event.preventDefault();
      handleShiftSelect(pickId);
      return;
    }

    setCurrentUpdateTitlePickIdToNull();
    selectSinglePick(pickId);
  };

  const handleMouseDown = (event: MouseEvent<HTMLDivElement>) => {
    if (event.shiftKey) {
      // 텍스트 선택 방지
      event.preventDefault();
    }
  };

  /**
   * @description multi-select에 포함이 됐으나 dragging target이 아닐때.
   */
  if (isDragging && isSelected && !isActiveDragging) {
    return null;
  }

  return (
    <div
      className={`${isSearchedPickHighlight ? searchedItemStyle : ''}`}
      ref={setNodeRef}
      {...attributes}
      {...listeners}
      style={{ width: 'fit-content', ...style }}
      data-pick-draggable={true} // 해당 data는 focus를 바꾸는 동작과 연관이 있습니다.
      suppressHydrationWarning
    >
      {/* biome-ignore lint/a11y/useKeyWithClickEvents: <explanation> */}
      <div
        className={`${isSelected ? selectedDragItemStyle : ''} ${isActiveDragging ? isActiveDraggingItemStyle : ''}`}
        id={pickElementId}
        onClick={(event) => handleClick(pickId, event)}
        onMouseDown={handleMouseDown}
      >
        <PickContextMenu pickInfo={pickInfo}>
          <PickRecord
            pickInfo={pickInfo}
            key={`${pickInfo.id}-${pickInfo.title}`}
          />
        </PickContextMenu>
      </div>
    </div>
  );
}
