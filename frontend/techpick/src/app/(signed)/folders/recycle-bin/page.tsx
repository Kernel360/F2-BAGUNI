'use client';

import { useEffect } from 'react';
import { PickContextMenu } from '@/components/PickContextMenu';
import { PickDraggableListLayout } from '@/components/PickDraggableListLayout';
import { PickDraggableRecord } from '@/components/PickRecord/PickDraggableRecord';
import { usePickStore, useTreeStore } from '@/stores';

export default function RecycleBinFolderPage() {
  const { fetchPickDataByFolderId, getOrderedPickListByFolderId } =
    usePickStore();
  const selectSingleFolder = useTreeStore((state) => state.selectSingleFolder);
  const basicFolderMap = useTreeStore((state) => state.basicFolderMap);

  useEffect(
    function selectRecycleBinFolderId() {
      if (!basicFolderMap) {
        return;
      }

      selectSingleFolder(basicFolderMap['RECYCLE_BIN'].id);
    },
    [basicFolderMap, selectSingleFolder]
  );

  useEffect(
    function loadPickDataFromRemote() {
      if (!basicFolderMap) {
        return;
      }

      fetchPickDataByFolderId(basicFolderMap['RECYCLE_BIN'].id);
    },
    [basicFolderMap, fetchPickDataByFolderId]
  );

  if (!basicFolderMap) {
    return <div>loading...</div>;
  }

  const pickList = getOrderedPickListByFolderId(
    basicFolderMap['RECYCLE_BIN'].id
  );

  return (
    <PickDraggableListLayout
      folderId={basicFolderMap['RECYCLE_BIN'].id}
      viewType="record"
    >
      {pickList.map((pickInfo) => {
        return (
          <PickContextMenu
            basicFolderMap={basicFolderMap}
            pickInfo={pickInfo}
            key={pickInfo.id}
          >
            <PickDraggableRecord pickInfo={pickInfo} />
          </PickContextMenu>
        );
      })}
    </PickDraggableListLayout>
  );
}
