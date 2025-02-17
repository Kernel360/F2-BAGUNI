'use client';

import { movePicks } from '@/apis/pick/movePicks';
import { PICK_LIST_SIZE } from '@/constants/pickListSize';
import type { GetPickListResponseType } from '@/types/GetPickListResponseType';
import type { MutateOptionType } from '@/types/MutateOptionType';
import type { UseMovePicksMutationFnParamType } from '@/types/UseMovePicksMutationFnParamType';
import { convertToInfiniteDataFromPickList } from '@/utils/convertToInfiniteDataFromPickList';
import { getMovedToEqualFolderPickList } from '@/utils/getMovedToEqualFolderPickList';
import { type InfiniteData, useQueryClient } from '@tanstack/react-query';
import { pickKeys } from './pickKeys';

export function useMovePicksToEqualFolder() {
  const queryClient = useQueryClient();

  const mutate = async (
    movePicksParam: UseMovePicksMutationFnParamType,
    { onSuccess = () => {}, onError = () => {} }: MutateOptionType,
  ) => {
    const { toPickId, sourceFolderId, fromPickId, movePicksInfo } =
      movePicksParam;

    const prevInfiniteData = queryClient.getQueryData<
      InfiniteData<GetPickListResponseType>
    >(pickKeys.folderInfinite(sourceFolderId));
    const prevPickList =
      prevInfiniteData?.pages.flatMap((page) => page.content) ?? [];

    const nextPickList = getMovedToEqualFolderPickList({
      prevPickList,
      fromPickId,
      toPickId,
      movePicksInfo,
    });

    const nextInfiniteData = convertToInfiniteDataFromPickList({
      pickList: nextPickList,
      contentSize: PICK_LIST_SIZE,
      oldData: prevInfiniteData,
    });

    queryClient.setQueryData<InfiniteData<GetPickListResponseType>>(
      pickKeys.folderInfinite(sourceFolderId),
      (oldData) => {
        if (!oldData) {
          return oldData;
        }

        return nextInfiniteData;
      },
    );

    try {
      await movePicks(movePicksInfo);
      onSuccess();
    } catch {
      queryClient.setQueryData(
        pickKeys.folderInfinite(sourceFolderId),
        prevInfiniteData,
      );
      onError();
    }

    queryClient.invalidateQueries({
      queryKey: pickKeys.folderInfinite(sourceFolderId),
    });
  };

  return { mutate };
}
