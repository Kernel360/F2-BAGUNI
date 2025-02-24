'use client';

import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';

import type {
  CSSProperties,
  ComponentPropsWithoutRef,
  PropsWithChildren,
} from 'react';
import { getTagSortableContextId } from './getTagSortableContextId';

export function TagSortableDraggable({
  tagId,
  tagOrder,
  children,
}: PropsWithChildren<TagSortableDraggableProps>) {
  const { attributes, listeners, setNodeRef, transform, transition, active } =
    useSortable({
      id: getTagSortableContextId(tagId),
      data: {
        id: tagId,
        type: 'tag',
        tagOrder,
      },
    });

  const style: CSSProperties = {
    transform: CSS.Transform.toString(transform),
    transition,
    pointerEvents: active ? 'none' : 'auto',
  };

  return (
    <div ref={setNodeRef} style={style} {...attributes} {...listeners}>
      {children}
    </div>
  );
}

interface TagSortableDraggableProps extends ComponentPropsWithoutRef<'div'> {
  tagId: number;
  tagOrder: number;
}
