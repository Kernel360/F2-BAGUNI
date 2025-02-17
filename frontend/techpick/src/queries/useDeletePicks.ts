'use client';

import { deletePicks } from '@/apis/pick/deletePicks';
import { PICK_LIST_SIZE } from '@/constants/pickListSize';
import type { FolderIdType } from '@/types/FolderIdType';
import type { GetPickListResponseType } from '@/types/GetPickListResponseType';
import { convertToInfiniteDataFromPickList } from '@/utils/convertToInfiniteDataFromPickList';
import {
  type InfiniteData,
  useMutation,
  useQueryClient,
} from '@tanstack/react-query';
import { pickKeys } from './pickKeys';

export function useDeletePicks() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ deletePickIdList }: UseDeletePickMutationFnParam) =>
      deletePicks({ idList: deletePickIdList }),
    onMutate: async ({ recycleBinFolderId, deletePickIdList }) => {
      await queryClient.cancelQueries({
        queryKey: pickKeys.folderInfinite(recycleBinFolderId),
      });

      const prevInfiniteData = queryClient.getQueryData<
        InfiniteData<GetPickListResponseType>
      >(pickKeys.folderInfinite(recycleBinFolderId));
      const prevPickList =
        prevInfiniteData?.pages.flatMap((page) => page.content) ?? [];

      const updatedPickList = prevPickList.filter(
        (pick) => !deletePickIdList.includes(pick.id),
      );
      const nextInfiniteData = convertToInfiniteDataFromPickList({
        pickList: updatedPickList,
        contentSize: PICK_LIST_SIZE,
        oldData: prevInfiniteData,
      });

      queryClient.setQueryData<InfiniteData<GetPickListResponseType>>(
        pickKeys.folderInfinite(recycleBinFolderId),
        (oldData) => {
          if (!oldData) {
            return oldData;
          }

          return nextInfiniteData;
        },
      );

      return { prevInfiniteData };
    },
    onError: (_error, { recycleBinFolderId }, context) => {
      const prevInfiniteData = context?.prevInfiniteData ?? [];

      queryClient.setQueryData(
        pickKeys.folderInfinite(recycleBinFolderId),
        prevInfiniteData,
      );
    },
    onSettled: (_data, _error, { recycleBinFolderId }) => {
      queryClient.invalidateQueries({
        queryKey: pickKeys.folderInfinite(recycleBinFolderId),
      });
    },
  });
}

interface UseDeletePickMutationFnParam {
  recycleBinFolderId: FolderIdType;
  deletePickIdList: FolderIdType[];
}
