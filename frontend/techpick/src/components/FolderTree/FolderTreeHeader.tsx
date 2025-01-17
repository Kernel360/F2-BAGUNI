'use client';

import { NON_EXIST_FOLDER_ID } from '@/constants/nonExistFolderId';
import { ROUTES } from '@/constants/route';
import { useTreeStore } from '@/stores/dndTreeStore/dndTreeStore';
import { ArchiveIcon, Trash2Icon } from 'lucide-react';
import { FolderLinkItem } from './FolderLinkItem';
import { PickToFolderDropZone } from './PickToFolderDropZone';
import {
  folderTreeHeaderLayout,
  folderTreeHeaderTitleLayout,
} from './folderTreeHeader.css';

export function FolderTreeHeader() {
  const { basicFolderMap, focusFolderId, hoverFolderId } = useTreeStore();

  const isUnclassifiedSelected = !!(
    basicFolderMap && focusFolderId === basicFolderMap.UNCLASSIFIED.id
  );

  const isRecycleBinSelected = !!(
    basicFolderMap && focusFolderId === basicFolderMap.RECYCLE_BIN.id
  );

  const isRootSelected = !!(
    basicFolderMap && focusFolderId === basicFolderMap.ROOT.id
  );

  const isUnclassifiedFolderHover =
    (basicFolderMap && basicFolderMap.UNCLASSIFIED.id === hoverFolderId) ??
    false;

  const isRecycleBinFolderHover =
    (basicFolderMap && basicFolderMap.RECYCLE_BIN.id === hoverFolderId) ??
    false;

  return (
    <div className={folderTreeHeaderLayout}>
      {basicFolderMap && (
        <div>
          <div className={folderTreeHeaderTitleLayout}>
            <h1>정리함</h1>
          </div>

          <FolderLinkItem
            href={ROUTES.RECOMMEND}
            name="추천"
            icon={ArchiveIcon}
            isSelected={isRootSelected}
            folderId={NON_EXIST_FOLDER_ID}
          />

          <PickToFolderDropZone folderId={basicFolderMap.UNCLASSIFIED.id}>
            <FolderLinkItem
              href={ROUTES.FOLDERS_UNCLASSIFIED}
              name="미분류"
              icon={ArchiveIcon}
              isSelected={isUnclassifiedSelected}
              isHovered={isUnclassifiedFolderHover}
              folderId={basicFolderMap.UNCLASSIFIED.id}
            />
          </PickToFolderDropZone>
          <PickToFolderDropZone folderId={basicFolderMap.RECYCLE_BIN.id}>
            <FolderLinkItem
              href={ROUTES.FOLDERS_RECYCLE_BIN}
              name="휴지통"
              icon={Trash2Icon}
              isSelected={isRecycleBinSelected}
              isHovered={isRecycleBinFolderHover}
              folderId={basicFolderMap.RECYCLE_BIN.id}
            />
          </PickToFolderDropZone>
        </div>
      )}
    </div>
  );
}
