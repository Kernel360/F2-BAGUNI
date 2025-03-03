'use client';
import { useEventLogger } from '@/libs/@eventlog/useEventLogger';
import { useCreatePick } from '@/queries/useCreatePick';
import { useDraggingRecommendPickStore } from '@/stores/draggingRecommendPickStore';
import { isPickToFolderDroppableObject } from '@/utils/isPickToFolderDroppableObject';
import { isRecommendPickDraggableObject } from '@/utils/isRecommendPickDraggableObject';
import { notifySuccess } from '@/utils/toast';
import { useDndMonitor } from '@dnd-kit/core';
import type {
  DragEndEvent,
  DragOverEvent,
  DragStartEvent,
} from '@dnd-kit/core';

/**
 * @description 추천 목록에서 folder로 dnd를 할 때의 이벤트를 감지하고 동작하는 hook입니다.
 */
export function useRecommendPickToFolderDndMonitor() {
  const { mutate: createPick } = useCreatePick();
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
  };

  const onDragEnd = async (event: DragEndEvent) => {
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

    createPick(
      {
        title,
        parentFolderId: overObject.id,
        tagIdOrderedList: [],
        linkInfo: { url, description, imageUrl, title },
      },
      {
        onSuccess: () => {
          notifySuccess('성공적으로 북마크가 추가되었습니다!');
          trackRecommendBookmarkSave({ title: title });
        },
      },
    );
  };

  useDndMonitor({
    onDragStart: onDragStart,
    onDragOver: onDragOver,
    onDragEnd: onDragEnd,
  });
}
