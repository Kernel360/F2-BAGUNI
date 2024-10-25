import { create } from 'zustand';
import { immer } from 'zustand/middleware/immer';
import type { Active, Over } from '@dnd-kit/core';
import type { FolderType } from '@/types';

type SelectedFolderList = number[];

type MoveFolderPayload = {
  from: Active;
  to: Over;
  selectedFolderList: SelectedFolderList;
};

type TreeState = {
  treeDataList: FolderType[];
  selectedItems: SelectedFolderList;
};

type TreeAction = {
  createFolder: () => void;
  readFolder: () => void;
  updateFolder: () => void;
  deleteFolder: () => void;
  moveFolder: ({ from, to, selectedFolderList }: MoveFolderPayload) => void;
  focusFolder: () => void;
  movePick: () => void;
  replaceTreeData: (newTreeDate: FolderType[]) => void;
};

const initialState: TreeState = {
  treeDataList: [],
  selectedItems: [],
};

export const useTreeStore = create<TreeState & TreeAction>()(
  immer((set) => ({
    ...initialState,
    createFolder: () => {},
    readFolder: () => {},
    updateFolder: () => {},
    deleteFolder: () => {},
    moveFolder: ({ from, to, selectedFolderList }) => {
      set((state) => {
        const curIndex = state.treeDataList.findIndex(
          (item) => item.id === from.id
        );
        const nextIndex = state.treeDataList.findIndex(
          (item) => item.id === to.id
        );

        if (curIndex === -1 || nextIndex === -1) return;

        // 이동할 폴더 리스트를 생성합니다.
        const foldersToMove = state.treeDataList.filter((treeData) => {
          return selectedFolderList.includes(treeData.id);
        });

        // 기존 리스트에서 폴더를 제거합니다.
        const updatedTreeDataList = state.treeDataList.filter(
          (item) => !selectedFolderList.includes(item.id)
        );

        // 이동할 폴더들을 다음 위치에 삽입합니다.
        const newIndex = nextIndex > curIndex ? nextIndex + 1 : nextIndex; // 삽입 위치 조정

        // 새 리스트를 만들어 상태를 업데이트합니다.
        state.treeDataList = [
          ...updatedTreeDataList.slice(0, newIndex),
          ...foldersToMove,
          ...updatedTreeDataList.slice(newIndex),
        ];
      });
    },
    focusFolder: () => {},
    movePick: () => {},
    replaceTreeData: (newTreeDate) => {
      set((state) => {
        state.treeDataList = newTreeDate;
      });
    },
  }))
);
