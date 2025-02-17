import { useOpenUrlInNewTab } from '@/hooks/useOpenUrlInNewTab';
import { useFetchBasicFolders } from '@/queries/useFetchBasicFolders';
import { useFetchFolders } from '@/queries/useFetchFolders';
import { useSearchPickStore } from '@/stores/searchPickStore';
import type { PickInfoType } from '@/types/PickInfoType';
import { formatDateString } from '@/utils/formatDateString';
import { getBasicFolderInfoByFolderId } from '@/utils/getBasicFolderInfoByFolderId';
import { getFolderInfoByFolderId } from '@/utils/getFolderInfoByFolderId';
import type { CSSProperties } from 'react';
import { CurrentPathIndicator } from './CurrentPathIndicator';
import * as styles from './searchItemRenderer.css';

export default function SearchItemRenderer({
  item,
  style,
  onClose,
}: ItemRendererProps) {
  const { setHoverPickInfo } = useSearchPickStore();
  const { data: folderRecord } = useFetchFolders();
  const { data: basicFolderRecord } = useFetchBasicFolders();
  let folderInfo = getFolderInfoByFolderId({
    folderId: item.parentFolderId,
    folderRecord,
  });
  const { openUrlInNewTab } = useOpenUrlInNewTab(item.linkInfo.url);

  if (!folderInfo) {
    folderInfo = getBasicFolderInfoByFolderId({
      folderId: item.parentFolderId,
      basicFolderRecord,
    });
  }

  const handleMouseEnter = () => {
    setHoverPickInfo(item);
  };

  if (!item) {
    return <div style={style}>Loading...</div>;
  }

  return (
    // biome-ignore lint/a11y/useKeyWithClickEvents: <explanation>
    <div
      style={{
        ...style,
      }}
      className={styles.searchListItemContainer}
      onClick={openUrlInNewTab}
    >
      <div
        className={styles.searchListItemTextContainer}
        onMouseEnter={handleMouseEnter}
      >
        <h3 className={styles.searchListItemTitle}>{item.title}</h3>
        <span className={styles.searchListItemDate}>
          {formatDateString(item.createdAt)}
        </span>
      </div>
      <CurrentPathIndicator folderInfo={folderInfo} onClose={onClose} />
    </div>
  );
}

export interface ItemRendererProps {
  item: PickInfoType;
  index: number;
  style: CSSProperties;
  onClose: () => void;
}
