import { NON_EXISTENT_TAG_ID } from '@/constants/nonExistentTagId';
import { useTagStore } from '@/stores/tagStore';
import { isTagSortableObject } from '@/utils/isTagSortableObject';
import {
  DndContext,
  type DragEndEvent,
  MouseSensor,
  TouchSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import {
  restrictToParentElement,
  restrictToVerticalAxis,
} from '@dnd-kit/modifiers';
import type { PropsWithChildren } from 'react';

export function TagDndContext({ children }: PropsWithChildren) {
  const { moveTag } = useTagStore();

  const mouseSensor = useSensor(MouseSensor, {
    activationConstraint: {
      distance: 10,
    },
  });
  const touchSensor = useSensor(TouchSensor, {
    activationConstraint: {
      delay: 250,
      tolerance: 5,
    },
  });

  const sensors = useSensors(mouseSensor, touchSensor);

  const onDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event;

    if (!over) {
      return;
    }
    const activeData = active.data.current;
    const overData = over.data.current;

    if (!isTagSortableObject(activeData) || !isTagSortableObject(overData)) {
      return;
    }

    // 움직인 대상이 생성중인 태그라면 종료
    if (activeData.id === NON_EXISTENT_TAG_ID) {
      return;
    }

    moveTag({ id: Number(activeData.id), orderIdx: overData.tagOrder });
  };

  return (
    <DndContext
      modifiers={[restrictToVerticalAxis, restrictToParentElement]}
      sensors={sensors}
      onDragEnd={onDragEnd}
    >
      {children}
    </DndContext>
  );
}
