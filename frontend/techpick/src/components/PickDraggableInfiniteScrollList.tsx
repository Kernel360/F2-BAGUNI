'use client';

import { PICK_LIST_SIZE } from '@/constants/pickListSize';
import { usePickDndRenderTrigger } from '@/hooks/usePickDndRenderTrigger';
import { useFetchPickListByFolderId } from '@/queries/useFetchPickListByFolderId';
import type { FolderIdType } from '@/types/FolderIdType';
import dynamic from 'next/dynamic';
import { useEffect } from 'react';
import { useInView } from 'react-intersection-observer';
import { PickDraggableListLayout } from './PickDraggableListLayout';
import { PickDraggableRecord } from './PickRecord/PickDraggableRecord';
import { PickRecordHeader } from './PickRecord/PickRecordHeader';
const EmptyPickRecordImage = dynamic(() =>
  import('@/components/EmptyPickRecordImage').then(
    (mod) => mod.EmptyPickRecordImage,
  ),
);

export function PickDraggableInfiniteScrollList({
  folderId,
}: PickDraggableInfiniteScrollListProps) {
  const { data, fetchNextPage, isFetchingNextPage, hasNextPage, isLoading } =
    useFetchPickListByFolderId(folderId, PICK_LIST_SIZE);
  const pickList = data?.pages.flatMap((page) => page.content) ?? [];
  usePickDndRenderTrigger();

  const { ref, inView } = useInView({
    threshold: 0,
  });

  useEffect(
    function loadNextPage() {
      if (inView && hasNextPage && !isFetchingNextPage) {
        fetchNextPage();
      }
    },
    [inView, hasNextPage, isFetchingNextPage, fetchNextPage],
  );

  if (!isLoading && pickList.length === 0) {
    return (
      <div>
        <PickRecordHeader />
        <EmptyPickRecordImage />
      </div>
    );
  }

  return (
    <PickDraggableListLayout folderId={folderId} viewType="record">
      {pickList.map((pick, index) => {
        return (
          <div
            key={pick.id}
            ref={index === pickList.length - 10 ? ref : undefined}
          >
            <PickDraggableRecord pickInfo={pick} />
          </div>
        );
      })}
    </PickDraggableListLayout>
  );
}

interface PickDraggableInfiniteScrollListProps {
  folderId: FolderIdType;
}
