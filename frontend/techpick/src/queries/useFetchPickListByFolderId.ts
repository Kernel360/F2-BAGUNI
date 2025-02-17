'use client';
import { getPickListByFolderId } from '@/apis/pick/getPickListByFolderId';
import { PICK_LIST_SIZE } from '@/constants/pickListSize';
import type { FolderIdType } from '@/types/FolderIdType';
import { useInfiniteQuery } from '@tanstack/react-query';
import { pickKeys } from './pickKeys';

export function useFetchPickListByFolderId(
  folderId: FolderIdType,
  size = PICK_LIST_SIZE,
) {
  return useInfiniteQuery({
    queryKey: pickKeys.folderInfinite(folderId),
    queryFn: ({ pageParam = 0 }) => {
      return getPickListByFolderId(folderId, pageParam, size);
    },
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      return lastPage.hasNext ? lastPage.lastCursor : undefined;
    },
  });
}
