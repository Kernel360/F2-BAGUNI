import type { GetPickListResponseType } from '@/types/GetPickListResponseType';
import type { PickListType } from '@/types/PickListType';
import type { InfiniteData } from '@tanstack/react-query';

export const convertToInfiniteDataFromPickList = ({
  pickList,
  contentSize,
  oldData,
}: ConvertToInfiniteDataFromPickListParamType): InfiniteData<GetPickListResponseType> => {
  const pages: GetPickListResponseType[] = [];
  let currentIndex = 0;

  while (currentIndex < pickList.length) {
    const pageContent = pickList.slice(
      currentIndex,
      currentIndex + contentSize,
    );
    const lastCursor = pageContent[pageContent.length - 1]?.id || 0;

    const isLastPage = currentIndex + contentSize >= pickList.length;
    const hasNext = isLastPage
      ? (oldData?.pages[oldData.pages.length - 1]?.hasNext ?? false)
      : true;

    pages.push({
      content: pageContent,
      lastCursor,
      size: pageContent.length,
      hasNext,
    });

    currentIndex += contentSize;
  }

  return {
    pages,
    pageParams: pages.map((_, index) =>
      index === 0 ? 0 : pages[index - 1].lastCursor,
    ),
  };
};

interface ConvertToInfiniteDataFromPickListParamType {
  pickList: PickListType;
  contentSize: number;
  oldData?: InfiniteData<GetPickListResponseType>;
}
