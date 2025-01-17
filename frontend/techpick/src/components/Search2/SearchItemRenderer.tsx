import { useTreeStore } from '@/stores/dndTreeStore/dndTreeStore';
import { useSearchPickStore } from '@/stores/searchPickStore';
import type { PickInfoType } from '@/types/PickInfoType';
import { formatDateString } from '@/utils/formatDateString';
import { useRouter } from 'next/navigation';
import React, { type CSSProperties } from 'react';
import { CurrentPathIndicator } from '../FolderContentHeader/CurrentPathIndicator';
import * as styles from './searchItemRenderer.css';

export default function SearchItemRenderer({
  item,
  index,
  style,
  onClose,
}: ItemRendererProps) {
  const router = useRouter();
  const { getFolderInfoByFolderId } = useTreeStore();
  const { setHoverPickIndex } = useSearchPickStore();
  const folderInfo = getFolderInfoByFolderId(item.parentFolderId);

  const handleMouseEnter = () => {
    setHoverPickIndex(index);
  };

  const handleClick = () => {
    onClose();

    let targetLocation = '';
    switch (folderInfo?.folderType) {
      case 'RECYCLE_BIN':
        targetLocation = 'recycle-bin';
        break;
      case 'UNCLASSIFIED':
        targetLocation = 'unclassified';
        break;
      case 'GENERAL':
        targetLocation = folderInfo?.id.toString();
    }
    /**
     * @description
     */
    const date = new Date();
    router.push(
      `/folders/${targetLocation}?searchId=pickId-${item.id}&dateId=${date.getMilliseconds()}`,
    );
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
      onClick={handleClick}
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
      <CurrentPathIndicator folderInfo={folderInfo} />
    </div>
  );
}

interface ItemRendererProps {
  item: PickInfoType;
  index: number;
  style: CSSProperties;
  onClose: () => void;
}
