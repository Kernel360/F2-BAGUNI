'use client';

import { deleteAllPicksFromRecycleBin } from '@/apis/pick/deleteAllPicksFromRecycleBin';
import type { BasicFolderRecordType } from '@/types/BasicFolderRecordType';
import type { GetPickListResponseType } from '@/types/GetPickListResponseType';
import {
  type InfiniteData,
  useMutation,
  useQueryClient,
} from '@tanstack/react-query';
import { folderKeys } from './folderKeys';
import { pickKeys } from './pickKeys';

export function useDeleteAllPicksFromRecycleBin() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: deleteAllPicksFromRecycleBin,
    onMutate: async () => {
      const basicFolderRecord = queryClient.getQueryData<BasicFolderRecordType>(
        folderKeys.basic(),
      );

      if (!basicFolderRecord) {
        return;
      }

      const recycleBinId = basicFolderRecord.RECYCLE_BIN.id;

      await queryClient.cancelQueries({
        queryKey: pickKeys.folderInfinite(recycleBinId),
      });

      const prevInfiniteData = queryClient.getQueryData<
        InfiniteData<GetPickListResponseType>
      >(pickKeys.folderInfinite(recycleBinId));

      const nextInfiniteData: InfiniteData<GetPickListResponseType> = {
        pages: [{ content: [], hasNext: false, lastCursor: 0, size: 0 }],
        pageParams: [],
      };

      queryClient.setQueryData(
        pickKeys.folderInfinite(recycleBinId),
        nextInfiniteData,
      );

      return { prevInfiniteData, recycleBinId };
    },
    onError(_error, _variables, context) {
      const prevInfiniteData = context?.prevInfiniteData;
      const recycleBinId = context?.recycleBinId;

      if (prevInfiniteData && recycleBinId) {
        queryClient.setQueryData(
          pickKeys.folderInfinite(recycleBinId),
          prevInfiniteData,
        );
      }
    },
    onSettled(_data, _error, _variables, context) {
      const recycleBinId = context?.recycleBinId;

      if (recycleBinId) {
        queryClient.invalidateQueries({
          queryKey: pickKeys.folderInfinite(recycleBinId),
        });
      }
    },
  });
}
