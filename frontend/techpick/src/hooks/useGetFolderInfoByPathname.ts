'use client';

import { ROUTES } from '@/constants/route';
import { useFetchBasicFolders } from '@/queries/useFetchBasicFolders';
import { useFetchFolders } from '@/queries/useFetchFolders';
import { getFolderInfoByFolderId } from '@/utils/getFolderInfoByFolderId';

export function useGetFolderInfoByPathname(pathname: string) {
  const { data: basicFolderRecord } = useFetchBasicFolders();
  const { data: folderRecord } = useFetchFolders();

  if (!basicFolderRecord || !folderRecord) {
    return null;
  }

  switch (pathname) {
    case ROUTES.RECOMMEND:
      return basicFolderRecord.ROOT;
    default: {
      // ' /folders/folderId'
      //  => 'unclassified' | 'recycle-bin' |folderId
      const path = pathname.split('/').slice(2).join('');
      return getFolderInfoByFolderId({ folderId: Number(path), folderRecord });
    }
  }
}
