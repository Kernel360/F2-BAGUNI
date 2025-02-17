'use client';

import type { PickViewDraggableItemListLayoutComponentProps } from '@/types/PickViewDraggableItemListLayoutComponentProps';
import dynamic from 'next/dynamic';
import { pickDraggableListLayoutStyle } from './pickDraggableListLayout.css';
const PickListSortableContextProvider = dynamic(
  () =>
    import('./PickListSortableContextProvider').then(
      (mod) => mod.PickListSortableContextProvider,
    ),
  { ssr: true },
);

export function PickDraggableListLayout({
  viewType = 'record',
  folderId,
  children,
}: PickViewDraggableItemListLayoutComponentProps) {
  return (
    <PickListSortableContextProvider folderId={folderId} viewType={viewType}>
      <div className={pickDraggableListLayoutStyle}>{children}</div>
    </PickListSortableContextProvider>
  );
}
