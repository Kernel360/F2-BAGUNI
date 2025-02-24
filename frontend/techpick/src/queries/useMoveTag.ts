'use client';

import { moveTag } from '@/apis/tag/moveTag';
import { syncUpdate } from '@/libs/@react-query/taskScheduler';
import type { GetTagListResponseType } from '@/types/GetTagListResponseType';
import { hasIndex } from '@/utils/hasIndex';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { produce } from 'immer';
import { tagKeys } from './tagKeys';

export function useMoveTag() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: moveTag,
    onMutate({ id, orderIdx }) {
      syncUpdate(() => {
        const prevTagList =
          queryClient.getQueryData<GetTagListResponseType>(tagKeys.all) ?? [];

        const curIndex = prevTagList.findIndex((item) => item.id === id);
        if (!hasIndex(curIndex)) {
          return;
        }

        const curTag = prevTagList[curIndex];

        if (!curTag) {
          return;
        }

        const targetIndex = orderIdx;

        const nextIndex =
          curIndex < targetIndex
            ? Math.min(targetIndex + 1, prevTagList.length)
            : targetIndex;

        if (curIndex === nextIndex) {
          return;
        }

        if (!hasIndex(nextIndex)) {
          return;
        }

        const nextTagList = produce(prevTagList, (prevTagList) => {
          const beforeNextIndexList = prevTagList
            .slice(0, nextIndex)
            .filter((tag) => tag.id !== id);

          const afterNextIndexList = prevTagList
            .slice(nextIndex)
            .filter((tag) => tag.id !== id);

          return [...beforeNextIndexList, curTag, ...afterNextIndexList];
        });

        queryClient.setQueryData<GetTagListResponseType>(
          tagKeys.all,
          nextTagList,
        );
      });
    },
    onSettled() {
      queryClient.invalidateQueries({ queryKey: tagKeys.all });
    },
  });
}
