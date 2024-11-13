'use client';

import { FolderInputIcon, Trash2Icon } from 'lucide-react';
import { ROUTES } from '@/constants';
import { useTreeStore } from '@/stores/dndTreeStore/dndTreeStore';
import { FolderLinkItem } from './FolderLinkItem';
import { folderTreeHeaderLayout } from './folderTreeHeader.css';
import { PickToFolderDropZone } from './PickToFolderDropZone';

export function FolderTreeHeader() {
  const { basicFolderMap, focusFolderId, hoverFolderId } = useTreeStore();

  const isUnclassifiedSelected = !!(
    basicFolderMap && focusFolderId === basicFolderMap['UNCLASSIFIED'].id
  );

  const isRecycleBinSelected = !!(
    basicFolderMap && focusFolderId === basicFolderMap['RECYCLE_BIN'].id
  );

  const isUnclassifiedFolderHover =
    (basicFolderMap && basicFolderMap['UNCLASSIFIED'].id === hoverFolderId) ??
    false;

  const isRecycleBinFolderHover =
    (basicFolderMap && basicFolderMap['RECYCLE_BIN'].id === hoverFolderId) ??
    false;

  return (
    <div className={folderTreeHeaderLayout}>
      {basicFolderMap && (
        <>
          <PickToFolderDropZone folderId={basicFolderMap['UNCLASSIFIED'].id}>
            <FolderLinkItem
              href={ROUTES.FOLDERS_UNCLASSIFIED}
              name="미분류"
              icon={FolderInputIcon}
              isSelected={isUnclassifiedSelected}
              isHovered={isUnclassifiedFolderHover}
            />
          </PickToFolderDropZone>
          <PickToFolderDropZone folderId={basicFolderMap['RECYCLE_BIN'].id}>
            <FolderLinkItem
              href={ROUTES.FOLDERS_RECYCLE_BIN}
              name="휴지통"
              icon={Trash2Icon}
              isSelected={isRecycleBinSelected}
              isHovered={isRecycleBinFolderHover}
            />
          </PickToFolderDropZone>
        </>
      )}
      <hr />
    </div>
  );
}
