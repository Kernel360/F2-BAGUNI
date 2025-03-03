'use client';
import { useFetchBasicFolders } from '@/queries/useFetchBasicFolders';
import { useFetchFolders } from '@/queries/useFetchFolders';
import type { FolderIdType } from '@/types/FolderIdType';
import { getBasicFolderInfoByFolderId } from '@/utils/getBasicFolderInfoByFolderId';
import { getFolderInfoByFolderId } from '@/utils/getFolderInfoByFolderId';
import { ChromeExtensionLinkButton } from './ChromeExtensionLinkButton';
import { CurrentFolderNameSection } from './CurrentFolderNameSection';
import { CurrentPathIndicator } from './CurrentPathIndicator';
import { DeleteAllPicksFromRecycleBin } from './DeleteAllPicksFromRecycleBin';
import {
  createPickPopoverButtonLayoutStyle,
  currentPathIndicatorLayoutStyle,
  folderContentHeaderLayoutStyle,
  folderContentHeaderStyle,
  folderDescriptionStyle,
} from './folderContentHeader.css';

export function FolderContentHeader({ folderId }: FolderContentHeaderProps) {
  const { data: basicFolderRecord } = useFetchBasicFolders();
  const { data: folderRecord } = useFetchFolders();
  const folderInfo =
    getFolderInfoByFolderId({
      folderId,
      folderRecord,
    }) ?? getBasicFolderInfoByFolderId({ folderId, basicFolderRecord });

  return (
    <div className={folderContentHeaderLayoutStyle}>
      <div className={folderContentHeaderStyle}>
        <div className={folderDescriptionStyle}>
          <CurrentFolderNameSection folderInfo={folderInfo} />
          <div className={currentPathIndicatorLayoutStyle}>
            <CurrentPathIndicator folderInfo={folderInfo} />
          </div>
        </div>
        <div className={createPickPopoverButtonLayoutStyle}>
          {folderInfo?.folderType !== 'RECYCLE_BIN' ? (
            <ChromeExtensionLinkButton />
          ) : (
            <DeleteAllPicksFromRecycleBin />
          )}
        </div>
      </div>
    </div>
  );
}

interface FolderContentHeaderProps {
  folderId: FolderIdType;
}
