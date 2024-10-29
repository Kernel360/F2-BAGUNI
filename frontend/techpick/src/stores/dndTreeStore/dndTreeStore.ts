import { create } from 'zustand';
import { immer } from 'zustand/middleware/immer';
import { isDnDCurrentData } from './utils/isDnDCurrentData';
import { reorderFoldersInSameParent } from './utils/reorderFoldersInSameParent';
import type { Active, Over, UniqueIdentifier } from '@dnd-kit/core';
import type { FolderType, FolderMapType } from '@/types';

export type SelectedFolderListType = number[];

type MoveFolderPayload = {
  from: Active;
  to: Over;
  selectedFolderList: SelectedFolderListType;
};

type TreeState = {
  treeDataMap: FolderMapType;
  selectedFolderList: SelectedFolderListType;
  from: Active | null;
  to: Over | null;
  isDragging: boolean;
};

type TreeAction = {
  createFolder: () => void;
  readFolder: () => void;
  updateFolder: () => void;
  deleteFolder: () => void;
  moveFolder: ({ from, to, selectedFolderList }: MoveFolderPayload) => void;
  focusFolder: () => void;
  movePick: () => void;
  setTreeMap: (newTreeDate: FolderMapType) => void;
  setSelectedFolderList: (
    newSelectedFolderData: SelectedFolderListType
  ) => void;
  setFrom: (newFrom: Active) => void;
  setTo: (newTo: Over) => void;
  setIsDragging: (isDragging: boolean) => void;
  filterByParentId: (parentId: UniqueIdentifier) => FolderType[];
};

const initialState: TreeState = {
  treeDataMap: {},
  selectedFolderList: [],
  from: null,
  to: null,
  isDragging: false,
};

export const useTreeStore = create<TreeState & TreeAction>()(
  immer((set, get) => ({
    ...initialState,
    createFolder: () => {},
    readFolder: () => {},
    updateFolder: () => {},
    deleteFolder: () => {},
    moveFolder: ({ from, to, selectedFolderList }) => {
      const fromData = from.data.current;
      const toData = to.data.current;

      if (!isDnDCurrentData(fromData) || !isDnDCurrentData(toData)) return;
      // SortableContext에 id가 없으면 종료
      if (!fromData.sortable.containerId || !toData.sortable.containerId)
        return;

      // 부모 containerId가 같으면
      if (fromData.sortable.containerId === toData.sortable.containerId) {
        const parentId = fromData.sortable.containerId;
        const fromId = from.id;
        const toId = to.id;

        set((state) => {
          const childFolderList = state.treeDataMap[parentId].childFolderList;
          state.treeDataMap[parentId].childFolderList =
            reorderFoldersInSameParent({
              childFolderList,
              fromId,
              toId,
              selectedFolderList,
            });
        });
      }
    },

    focusFolder: () => {},
    movePick: () => {},
    setTreeMap: (newTreeDate) => {
      set((state) => {
        state.treeDataMap = newTreeDate;
      });
    },
    setSelectedFolderList: (newSelectedFolderData) => {
      set((state) => {
        state.selectedFolderList = newSelectedFolderData;
      });
    },
    setFrom: (newFrom) => {
      set((state) => {
        state.from = newFrom;
      });
    },
    setTo: (newTo) => {
      set((state) => {
        state.to = newTo;
      });
    },
    setIsDragging: (isDragging) => {
      set((state) => {
        state.isDragging = isDragging; // 드래그 상태 설정
      });
    },
    filterByParentId: (parentId) => {
      const parentFolder = get().treeDataMap[parentId.toString()];

      if (!parentFolder) {
        return [];
      }

      const childFolderIdList = parentFolder.childFolderList;
      const filteredFolderList = [];

      for (const childFolderId of childFolderIdList) {
        const curFolderInfo = get().treeDataMap[childFolderId];

        if (!curFolderInfo) {
          continue;
        }

        filteredFolderList.push(curFolderInfo);
      }

      return filteredFolderList;
    },
  }))
);
