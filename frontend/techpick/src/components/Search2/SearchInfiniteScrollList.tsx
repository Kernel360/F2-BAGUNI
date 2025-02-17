import { useFetchSearchPickList } from '@/queries/useFetchSearchPickList';
import { useSearchPickStore } from '@/stores/searchPickStore';
import AutoSizer from 'react-virtualized-auto-sizer';
import { FixedSizeList as List } from 'react-window';
import InfiniteLoader from 'react-window-infinite-loader';
import SearchItemRenderer from './SearchItemRenderer';
import * as styles from './searchInfiniteScrollList.css';

export function SearchInfiniteScrollList({
  onClose,
}: SearchInfiniteScrollListProps) {
  const { searchQuery, searchTag, searchFolder } = useSearchPickStore();

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage } =
    useFetchSearchPickList({
      searchTokenList: encodeURIComponent(searchQuery),
      folderIdList: searchFolder,
      tagIdList: searchTag,
    });

  const searchResultList = data?.pages.flatMap((page) => page.content) ?? [];

  const loadMoreItems = () => {
    if (!isFetchingNextPage) {
      fetchNextPage();
    }
  };

  const isItemLoaded = (index: number) => {
    return !hasNextPage || index < searchResultList.length;
  };

  const itemCount = hasNextPage
    ? searchResultList.length + 1
    : searchResultList.length;

  return (
    <div className={styles.searchListContainer}>
      <AutoSizer>
        {({ height, width }) => (
          <InfiniteLoader
            isItemLoaded={isItemLoaded}
            itemCount={itemCount}
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
                className={styles.searchResultListStyle}
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
