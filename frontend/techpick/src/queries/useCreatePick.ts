'use client';

import { createPick } from '@/apis/pick/createPick';
import { PICK_LIST_SIZE } from '@/constants/pickListSize';
import type { GetPickListResponseType } from '@/types/GetPickListResponseType';
import { convertToInfiniteDataFromPickList } from '@/utils/convertToInfiniteDataFromPickList';
import {
  type InfiniteData,
  useMutation,
  useQueryClient,
} from '@tanstack/react-query';
import { pickKeys } from './pickKeys';

export function useCreatePick() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createPick,
    onSuccess(data) {
      const prevInfiniteData = queryClient.getQueryData<
        InfiniteData<GetPickListResponseType>
      >(pickKeys.folderInfinite(data.parentFolderId));
      const prevPickList =
        prevInfiniteData?.pages.flatMap((page) => page.content) ?? [];

      const nextPickList = [data, ...prevPickList];
      const nextInfiniteData = convertToInfiniteDataFromPickList({
        pickList: nextPickList,
        contentSize: PICK_LIST_SIZE,
        oldData: prevInfiniteData,
      });

      queryClient.setQueryData<InfiniteData<GetPickListResponseType>>(
        pickKeys.folderInfinite(data.parentFolderId),
        (oldData) => {
          if (!oldData) {
            return oldData;
          }

          return nextInfiniteData;
        },
      );
    },
    onSettled(_data, _error, variables) {
      queryClient.invalidateQueries({
        queryKey: pickKeys.folderInfinite(variables.parentFolderId),
        refetchType: 'all',
      });
    },
  });
}
