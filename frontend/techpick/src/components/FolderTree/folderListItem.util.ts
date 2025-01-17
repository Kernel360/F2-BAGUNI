import type { FolderMapType } from '@/types/FolderMapType';
import { hasIndex } from '@/utils/hasIndex';

export const isSameParentFolder = (
  id: number,
  selectedId: number,
  treeDataMap: FolderMapType,
) => {
  return (
    treeDataMap[id].parentFolderId === treeDataMap[selectedId].parentFolderId
  );
};

// 여기서 selectedFolderId
export const getSelectedFolderRange = ({
  startFolderId,
  endFolderId,
  treeDataMap,
}: GetSelectedFolderRangePayload) => {
  const parentFolderInfo = treeDataMap[treeDataMap[endFolderId].parentFolderId];
  const firstSelectedIndex =
    parentFolderInfo.childFolderIdOrderedList.findIndex(
      (childFolderId) => childFolderId === startFolderId,
    );
  const endSelectedIndex = parentFolderInfo.childFolderIdOrderedList.findIndex(
    (childFolderId) => childFolderId === endFolderId,
  );

  if (!hasIndex(firstSelectedIndex) || !hasIndex(endSelectedIndex)) return [];

  const start = Math.min(firstSelectedIndex, endSelectedIndex);
  const end = Math.max(firstSelectedIndex, endSelectedIndex);

  const newSelectedFolderList = parentFolderInfo.childFolderIdOrderedList.slice(
    start,
    end + 1,
  );

  return newSelectedFolderList;
};

interface GetSelectedFolderRangePayload {
  startFolderId: number;
  endFolderId: number;
  treeDataMap: FolderMapType;
}
