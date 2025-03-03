'use client';

import { ROUTES } from '@/constants/route';
import { useFetchFolders } from '@/queries/useFetchFolders';
import type { FolderIdType } from '@/types/FolderIdType';
import { getFolderInfoByFolderId } from '@/utils/getFolderInfoByFolderId';
import { FolderClosedIcon, FolderOpenIcon, FolderUpIcon } from 'lucide-react';
import { useContext } from 'react';
import { ActiveNavigationItemIdContext } from './ActiveNavigationItemIdProvider';
import { NavigationItem } from './NavigationItem';

export function FolderNavigationItem({ folderId }: FolderNavigationItemProps) {
  const { data: folderRecord } = useFetchFolders();
  const folderInfo = getFolderInfoByFolderId({ folderId, folderRecord });
  const href = folderInfo ? ROUTES.FOLDER_DETAIL(folderInfo.id) : '';
  const activeNavigationItemId = useContext(ActiveNavigationItemIdContext);
  const isActive = folderId === activeNavigationItemId;
  const isShared = !!folderInfo?.folderAccessToken;
  let folderIcon = FolderClosedIcon;

  if (isShared) {
    folderIcon = FolderUpIcon;
  } else if (isActive) {
    folderIcon = FolderOpenIcon;
  }

  return (
    <NavigationItem
      href={href}
      isActive={isActive}
      icon={folderIcon}
      text={folderInfo?.name}
    />
  );
}

interface FolderNavigationItemProps {
  folderId: FolderIdType;
}
