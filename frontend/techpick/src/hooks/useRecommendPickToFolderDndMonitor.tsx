'use client';

import { createPick } from '@/apis/pick/createPick';
import { useTreeStore } from '@/stores/dndTreeStore/dndTreeStore';
import { useDraggingRecommendPickStore } from '@/stores/draggingRecommendPickStore';
import { usePickStore } from '@/stores/pickStore/pickStore';
import { isPickToFolderDroppableObject } from '@/utils/isPickToFolderDroppableObject';
import { isRecommendPickDraggableObject } from '@/utils/isRecommendPickDraggableObject';
import { notifySuccess } from '@/utils/toast';
import { useDndMonitor } from '@dnd-kit/core';
import type {
  DragEndEvent,
  DragOverEvent,
  DragStartEvent,
} from '@dnd-kit/core';
import { useEventLogger } from './useEventLogger';

/**
 * @description 추천 목록에서 folder로 dnd를 할 때의 이벤트를 감지하고 동작하는 hook입니다.
 */
export function useRecommendPickToFolderDndMonitor() {
  const { setHoverFolderId } = useTreeStore();
  const { insertPickInfo } = usePickStore();
  const { setIsDragging, setDraggingPickInfo } =
    useDraggingRecommendPickStore();
  const { trackEvent: trackRecommendBookmarkSave } = useEventLogger({
    eventName: 'recommend_page_bookmark_save',
  });

  const onDragStart = (event: DragStartEvent) => {
    const { active } = event;
    const activeObject = active.data.current;

    if (!isRecommendPickDraggableObject(activeObject)) {
      return;
    }

    setIsDragging(true);
    setDraggingPickInfo(activeObject);
  };

  const onDragOver = (event: DragOverEvent) => {
    const { active, over } = event;
    if (!over) return; // 드래그 중 놓은 위치가 없을 때 종료

    const activeObject = active.data.current;
    const overObject = over.data.current;

    if (
      !isRecommendPickDraggableObject(activeObject) ||
      !isPickToFolderDroppableObject(overObject)
    )
      return;

    const currentHoverFolderId = Number(overObject.id);
    setHoverFolderId(currentHoverFolderId);
  };

  const onDragEnd = async (event: DragEndEvent) => {
    setHoverFolderId(null);

    const { active, over } = event;
    if (!over) return; // 드래그 중 놓은 위치가 없을 때 종료

    const activeObject = active.data.current;
    const overObject = over.data.current;

    if (
      !isRecommendPickDraggableObject(activeObject) ||
      !isPickToFolderDroppableObject(overObject)
    )
      return;

    const { url, title, imageUrl, description } = activeObject;

    try {
      const createdPickInfo = await createPick({
        title,
        parentFolderId: overObject.id,
        tagIdOrderedList: [],
        linkInfo: { url, description, imageUrl, title },
      });
      insertPickInfo(createdPickInfo, overObject.id);
      notifySuccess('성공적으로 북마크가 추가되었습니다!');
      trackRecommendBookmarkSave({ title: title });
    } catch {
      /** empty */
    }
  };

  useDndMonitor({
    onDragStart: onDragStart,
    onDragOver: onDragOver,
    onDragEnd: onDragEnd,
  });
}
