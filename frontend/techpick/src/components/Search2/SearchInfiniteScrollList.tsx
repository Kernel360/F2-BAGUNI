import { useSearchPickStore } from '@/stores/searchPickStore';
import { useEffect, useCallback } from 'react';
import AutoSizer from 'react-virtualized-auto-sizer';
import { FixedSizeList as List } from 'react-window';
import InfiniteLoader from 'react-window-infinite-loader';
import SearchItemRenderer from './SearchItemRenderer';
import * as styles from './searchInfiniteScrollList.css';

export function SearchInfiniteScrollList({
  onClose,
}: SearchInfiniteScrollListProps) {
  const {
    searchResultList,
    hasNext,
    isLoading,
    searchPicksByQueryParam,
    loadMoreSearchPicks,
    lastCursor,
    searchQuery,
    searchTag,
    searchFolder,
  } = useSearchPickStore();

  // biome-ignore lint/correctness/useExhaustiveDependencies: <explanation>
  useEffect(() => {
    if (!searchResultList.length) searchPicksByQueryParam();
  }, [searchQuery, searchTag, searchFolder, searchResultList]);

  // biome-ignore lint/correctness/useExhaustiveDependencies: <explanation>
  const loadMoreItems = useCallback(async () => {
    await loadMoreSearchPicks();
  }, [hasNext, isLoading, lastCursor]);

  const isItemLoaded = (index: number) => {
    return !hasNext || index < searchResultList.length;
  };

  return (
    <div className={styles.searchListContainer}>
      <AutoSizer>
        {({ height, width }) => (
          <InfiniteLoader
            isItemLoaded={isItemLoaded}
            itemCount={
              hasNext ? searchResultList.length + 1 : searchResultList.length
            }
            loadMoreItems={loadMoreItems}
            threshold={5}
          >
            {({ onItemsRendered, ref }) => (
              <List
                height={height}
                width={width}
                itemCount={searchResultList.length}
                itemSize={60}
                onItemsRendered={onItemsRendered}
                ref={ref}
                itemData={searchResultList}
              >
                {({ index, style }) => (
                  <SearchItemRenderer
                    index={index}
                    item={searchResultList[index]}
                    style={style}
                    onClose={onClose}
                  />
                )}
              </List>
            )}
          </InfiniteLoader>
        )}
      </AutoSizer>
    </div>
  );
}

interface SearchInfiniteScrollListProps {
  onClose: () => void;
}
