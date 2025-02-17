'use client';

import { updatePick } from '@/apis/pick/updatePick';
import { PICK_LIST_SIZE } from '@/constants/pickListSize';
import { microtaskUpdate } from '@/libs/@react-query/taskScheduler';
import type { FolderIdType } from '@/types/FolderIdType';
import type { GetPickListResponseType } from '@/types/GetPickListResponseType';
import type { UpdatePickRequestType } from '@/types/UpdatePickRequestType';
import { convertToInfiniteDataFromPickList } from '@/utils/convertToInfiniteDataFromPickList';
import {
  type InfiniteData,
  useMutation,
  useQueryClient,
} from '@tanstack/react-query';
import { pickKeys } from './pickKeys';

export function useUpdatePickInfo() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ updatePickInfo }: UseUpdatePickInfoMutationFnParam) =>
      updatePick(updatePickInfo),
    onMutate: async ({ pickParentFolderId, updatePickInfo }) => {
      await queryClient.cancelQueries({
        queryKey: pickKeys.folderInfinite(pickParentFolderId),
      });

      const prevInfiniteData = queryClient.getQueryData<
        InfiniteData<GetPickListResponseType>
      >(pickKeys.folderInfinite(pickParentFolderId));
      const prevPickList =
        prevInfiniteData?.pages.flatMap((page) => page.content) ?? [];

      const nextPickList = prevPickList.map((pickInfo) => {
        if (pickInfo.id === updatePickInfo.id) {
          return {
            ...pickInfo,
            ...updatePickInfo,
          };
        }

        return pickInfo;
      });
      const nextInfiniteData = convertToInfiniteDataFromPickList({
        pickList: nextPickList,
        contentSize: PICK_LIST_SIZE,
        oldData: prevInfiniteData,
      });

      microtaskUpdate(() => {
        queryClient.setQueryData<InfiniteData<GetPickListResponseType>>(
          pickKeys.folderInfinite(pickParentFolderId),
          (oldData) => {
            if (!oldData) {
              return oldData;
            }

            return nextInfiniteData;
          },
        );
      });

      return { prevInfiniteData };
    },
    onError(_error, { pickParentFolderId }, context) {
      const prevInfiniteData = context?.prevInfiniteData ?? [];

      queryClient.setQueryData(
        pickKeys.folderInfinite(pickParentFolderId),
        prevInfiniteData,
      );
    },
    onSettled: (_data, _error, { pickParentFolderId }) => {
      queryClient.invalidateQueries({
        queryKey: pickKeys.folderInfinite(pickParentFolderId),
      });
    },
  });
}

interface UseUpdatePickInfoMutationFnParam {
  pickParentFolderId: FolderIdType;
  updatePickInfo: UpdatePickRequestType;
}
