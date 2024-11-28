'use client';

import { useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { PickRecordHeader } from '@/components';
import { EmptyPickRecordImage } from '@/components/EmptyPickRecordImage';
import { FolderContentHeader } from '@/components/FolderContentHeader/FolderContentHeader';
import { FolderContentLayout } from '@/components/FolderContentLayout';
import { PickContentLayout } from '@/components/PickContentLayout';
import { PickDraggableListLayout } from '@/components/PickDraggableListLayout';
import { PickDraggableRecord } from '@/components/PickRecord/PickDraggableRecord';
import { ROUTES } from '@/constants';
import {
  useResetPickFocusOnOutsideClick,
  useClearSelectedPickIdsOnMount,
  useFetchTagList,
  useFetchPickRecordByFolderId,
} from '@/hooks';
import { useTreeStore } from '@/stores';
import { getOrderedPickListByFolderId } from '@/utils';

export default function FolderDetailPage() {
  const router = useRouter();
  const { folderId: stringFolderId } = useParams<{ folderId: string }>();
  const selectSingleFolder = useTreeStore((state) => state.selectSingleFolder);
  const folderId = Number(stringFolderId);
  const basicFolderMap = useTreeStore((state) => state.basicFolderMap);
  const { isLoading, data } = useFetchPickRecordByFolderId({
    folderId: folderId,
  });
  useResetPickFocusOnOutsideClick();
  useClearSelectedPickIdsOnMount();
  useFetchTagList();

  useEffect(
    function selectFolderId() {
      if (!isFolderIdValid(folderId)) {
        router.replace(ROUTES.UNCLASSIFIED_FOLDER);
        return;
      }

      selectSingleFolder(Number(folderId));
    },
    [folderId, router, selectSingleFolder]
  );

  const isFolderIdValid = (folderId: number) => {
    if (Number.isNaN(folderId)) {
      return false;
    }

    return true;
  };

  if (!basicFolderMap || isLoading) {
    return <div>loading...</div>;
  }

  const pickList = getOrderedPickListByFolderId(data);

  return (
    <FolderContentLayout>
      <FolderContentHeader />
      <PickContentLayout>
        <PickRecordHeader />
        {pickList.length === 0 ? (
          <EmptyPickRecordImage />
        ) : (
          <>
            <PickDraggableListLayout folderId={folderId} viewType="record">
              {pickList.map((pickInfo) => {
                return (
                  <PickDraggableRecord key={pickInfo.id} pickInfo={pickInfo} />
                );
              })}
            </PickDraggableListLayout>
          </>
        )}
      </PickContentLayout>
    </FolderContentLayout>
  );
}
