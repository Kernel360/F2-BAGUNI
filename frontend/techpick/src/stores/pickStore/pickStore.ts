import { enableMapSet } from 'immer';
import { create } from 'zustand';
import { subscribeWithSelector } from 'zustand/middleware';
import { immer } from 'zustand/middleware/immer';
import {
  getPicksByFolderId,
  movePicks,
  updatePick,
  deletePicks,
} from '@/apis/pick';
import { getPickListByQueryParam } from '@/apis/pick/getPicks';
import { isPickDraggableObject, reorderSortableIdList } from '@/utils';
import type {
  DeleteSelectedPicksPayload,
  PickAction,
  PickState,
} from './pickStore.type';
import type {
  PickInfoType,
  PickRecordValueType,
  SearchPicksResponseType,
} from '@/types';

enableMapSet();

const initialState: PickState = {
  searchResult: { lastCursor: 0, hasNext: true } as SearchPicksResponseType,
  pickRecord: {},
  focusPickId: null,
  selectedPickIdList: [],
  isDragging: false,
  draggingPickInfo: null,
};

export const usePickStore = create<PickState & PickAction>()(
  subscribeWithSelector(
    immer((set, get) => ({
      ...initialState,
      fetchPickDataByFolderId: async (folderId) => {
        // pickRecord[folderId]

        // isLoading true

        // isError

        try {
          const { pickInfoRecord, pickIdOrderedList } =
            await getPicksByFolderId(folderId);

          set((state) => {
            state.pickRecord[folderId] = {
              pickIdOrderedList,
              pickInfoRecord,
            };
          });
        } catch {
          /* empty */
        }
      },
      getOrderedPickIdListByFolderId: (folderId) => {
        const pickRecordValue = get().pickRecord[`${folderId}`];

        if (!get().hasPickRecordValue(pickRecordValue)) {
          return [];
        }
        const { pickIdOrderedList } = pickRecordValue;

        return pickIdOrderedList;
      },
      getOrderedPickListByFolderId: (folderId: number) => {
        const pickRecordValue = get().pickRecord[`${folderId}`];

        if (!get().hasPickRecordValue(pickRecordValue)) {
          return [];
        }

        const { pickIdOrderedList, pickInfoRecord } = pickRecordValue;
        const pickOrderedList: PickInfoType[] = [];

        for (const pickId of pickIdOrderedList) {
          const pickInfo = pickInfoRecord[`${pickId}`];

          if (pickInfo) {
            pickOrderedList.push(pickInfo);
          }
        }

        return pickOrderedList;
      },
      getPickInfoByFolderIdAndPickId: (folderId, pickId) => {
        const pickRecordValue = get().pickRecord[`${folderId}`];

        if (!get().hasPickRecordValue(pickRecordValue)) {
          return null;
        }

        const { pickIdOrderedList, pickInfoRecord } = pickRecordValue;

        if (!pickIdOrderedList.includes(pickId)) {
          return null;
        }

        return pickInfoRecord[`${pickId}`];
      },
      hasPickRecordValue: (
        pickRecordValue
      ): pickRecordValue is PickRecordValueType => {
        if (!pickRecordValue) {
          return false;
        }

        return true;
      },
      movePicksToEqualFolder: async ({ from, to }) => {
        const fromData = from.data.current;
        const toData = to.data.current;

        if (!isPickDraggableObject(fromData) || !isPickDraggableObject(toData))
          return;
        // SortableContext에 id가 없으면 종료
        if (!fromData.sortable.containerId || !toData.sortable.containerId)
          return;

        const folderId = fromData.sortable.containerId;
        const pickRecordValue = get().pickRecord[folderId];

        if (!get().hasPickRecordValue(pickRecordValue)) {
          return;
        }

        const prevPickIdOrderedList = pickRecordValue.pickIdOrderedList;
        const fromId = from.id;
        const toId = to.id;

        set((state) => {
          if (!state.pickRecord[folderId]) {
            return;
          }

          state.pickRecord[folderId].pickIdOrderedList = reorderSortableIdList({
            sortableIdList: prevPickIdOrderedList,
            fromId,
            toId,
            selectedFolderList: state.selectedPickIdList,
          });
        });

        try {
          await movePicks({
            idList: get().selectedPickIdList,
            orderIdx: toData.sortable.index,
            destinationFolderId: Number(folderId),
          });
        } catch {
          set((state) => {
            const curPickRecordValue = state.pickRecord[`${folderId}`];

            if (!get().hasPickRecordValue(curPickRecordValue)) {
              return;
            }

            curPickRecordValue.pickIdOrderedList = prevPickIdOrderedList;
            state.pickRecord[`${folderId}`] = curPickRecordValue;
          });
        }
      },
      movePicksToDifferentFolder: async ({ from, to }) => {
        const currentFolderId = from.parentFolderId;
        const nextFolderId = to.id;

        const currentPickRecordValue = get().pickRecord[currentFolderId];
        let nextPickRecordValue = get().pickRecord[nextFolderId];

        if (!get().hasPickRecordValue(currentPickRecordValue)) {
          return;
        }

        /**
         * @description 다음 들어갈 곳에 값이 없으면 만들어줘야한다.
         */
        if (!get().hasPickRecordValue(nextPickRecordValue)) {
          set((state) => {
            state.pickRecord[nextFolderId] = {
              pickIdOrderedList: [],
              pickInfoRecord: {},
            };
          });
          nextPickRecordValue = get().pickRecord[currentFolderId];
        }

        if (!nextPickRecordValue) {
          return;
        }

        // a. 다른 폴더에서 추가(0번째 인덕스)
        // 어떤 정보를 가져와야한다.
        const selectedPickIdList = get().selectedPickIdList;
        const selectedPickInfoList: PickInfoType[] = [];
        const {
          pickInfoRecord: prevCurrentPickInfoRecord,
          pickIdOrderedList: prevCurrentPickIdOrderedList,
        } = currentPickRecordValue;

        // 선택된 정보 가져오기.
        for (const selectedPickId of selectedPickIdList) {
          const selectedPickInfo = get().getPickInfoByFolderIdAndPickId(
            currentFolderId,
            selectedPickId
          );

          if (selectedPickInfo) {
            selectedPickInfoList.push(selectedPickInfo);
          }
        }

        // 다른 폴더에 저장하기 전에 이전 상태를 저장해야한다.
        const {
          pickIdOrderedList: prevNextPickIdOrderedList,
          pickInfoRecord: prevNextPickInfoRecord,
        } = nextPickRecordValue;

        // 값 추가하기.
        set((state) => {
          if (!get().hasPickRecordValue(state.pickRecord[nextFolderId])) {
            return;
          }

          for (const selectedPickInfo of selectedPickInfoList) {
            state.pickRecord[nextFolderId].pickInfoRecord[
              `${selectedPickInfo.id}`
            ] = selectedPickInfo;
          }

          state.pickRecord[nextFolderId].pickIdOrderedList.splice(
            0,
            0,
            ...selectedPickIdList
          );
        });

        // 현재 폴더에서 삭제.
        set((state) => {
          if (!get().hasPickRecordValue(state.pickRecord[currentFolderId])) {
            return;
          }

          for (const selectedPickInfo of selectedPickInfoList) {
            state.pickRecord[currentFolderId].pickInfoRecord[
              `${selectedPickInfo.id}`
            ] = undefined;
          }

          state.pickRecord[currentFolderId].pickIdOrderedList =
            prevCurrentPickIdOrderedList.filter(
              (pickId) => !selectedPickIdList.includes(pickId)
            );
        });

        // api 요청
        try {
          await movePicks({
            idList: get().selectedPickIdList,
            destinationFolderId: Number(nextFolderId),
          });
        } catch {
          // 현재 폴더에서 이전 상태로 원복
          set((state) => {
            if (!state.pickRecord[currentFolderId]) {
              return;
            }
            state.pickRecord[currentFolderId].pickIdOrderedList =
              prevCurrentPickIdOrderedList;
            state.pickRecord[currentFolderId].pickInfoRecord =
              prevCurrentPickInfoRecord;
          });

          // 이동한 폴더를 이전 상태로 원복
          set((state) => {
            if (!state.pickRecord[nextFolderId]) {
              return;
            }

            state.pickRecord[nextFolderId].pickIdOrderedList =
              prevNextPickIdOrderedList;
            state.pickRecord[nextFolderId].pickInfoRecord =
              prevNextPickInfoRecord;
          });
        }
      },
      moveSelectedPicksToRecycleBinFolder: async ({
        picksParentFolderId: currentFolderId,
        recycleBinFolderId: nextFolderId,
      }) => {
        // selected Id list의 값을 휴지통에 추가하고, 현재 값에서 지운뒤,
        const currentPickRecordValue = get().pickRecord[currentFolderId];
        let nextPickRecordValue = get().pickRecord[nextFolderId];

        if (!get().hasPickRecordValue(currentPickRecordValue)) {
          return;
        }

        /**
         * @description 다음 들어갈 곳에 값이 없으면 만들어줘야한다.
         */
        if (!get().hasPickRecordValue(nextPickRecordValue)) {
          set((state) => {
            state.pickRecord[nextFolderId] = {
              pickIdOrderedList: [],
              pickInfoRecord: {},
            };
          });
          nextPickRecordValue = get().pickRecord[currentFolderId];
        }

        if (!nextPickRecordValue) {
          return;
        }

        // a. 다른 폴더에서 추가(0번째 인덕스)
        // 어떤 정보를 가져와야한다.
        const selectedPickIdList = get().selectedPickIdList;
        const selectedPickInfoList: PickInfoType[] = [];
        const {
          pickInfoRecord: prevCurrentPickInfoRecord,
          pickIdOrderedList: prevCurrentPickIdOrderedList,
        } = currentPickRecordValue;

        // 선택된 정보 가져오기.
        for (const selectedPickId of selectedPickIdList) {
          const selectedPickInfo = get().getPickInfoByFolderIdAndPickId(
            currentFolderId,
            selectedPickId
          );

          if (selectedPickInfo) {
            selectedPickInfoList.push(selectedPickInfo);
          }
        }

        // 다른 폴더에 저장하기 전에 이전 상태를 저장해야한다.
        const {
          pickIdOrderedList: prevNextPickIdOrderedList,
          pickInfoRecord: prevNextPickInfoRecord,
        } = nextPickRecordValue;

        // 값 추가하기.
        set((state) => {
          if (!get().hasPickRecordValue(state.pickRecord[nextFolderId])) {
            return;
          }

          for (const selectedPickInfo of selectedPickInfoList) {
            state.pickRecord[nextFolderId].pickInfoRecord[
              `${selectedPickInfo.id}`
            ] = selectedPickInfo;
          }

          state.pickRecord[nextFolderId].pickIdOrderedList.splice(
            0,
            0,
            ...selectedPickIdList
          );
        });

        // 현재 폴더에서 삭제.
        set((state) => {
          if (!get().hasPickRecordValue(state.pickRecord[currentFolderId])) {
            return;
          }

          for (const selectedPickInfo of selectedPickInfoList) {
            state.pickRecord[currentFolderId].pickInfoRecord[
              `${selectedPickInfo.id}`
            ] = undefined;
          }

          state.pickRecord[currentFolderId].pickIdOrderedList =
            prevCurrentPickIdOrderedList.filter(
              (pickId) => !selectedPickIdList.includes(pickId)
            );
        });
        // api 요청
        try {
          await movePicks({
            idList: get().selectedPickIdList,
            destinationFolderId: Number(nextFolderId),
          });
        } catch {
          // 현재 폴더에서 이전 상태로 원복
          set((state) => {
            if (!state.pickRecord[currentFolderId]) {
              return;
            }
            state.pickRecord[currentFolderId].pickIdOrderedList =
              prevCurrentPickIdOrderedList;
            state.pickRecord[currentFolderId].pickInfoRecord =
              prevCurrentPickInfoRecord;
          });

          // 이동한 폴더를 이전 상태로 원복
          set((state) => {
            if (!state.pickRecord[nextFolderId]) {
              return;
            }

            state.pickRecord[nextFolderId].pickIdOrderedList =
              prevNextPickIdOrderedList;
            state.pickRecord[nextFolderId].pickInfoRecord =
              prevNextPickInfoRecord;
          });
        }
      },
      deleteSelectedPicks: async ({
        recycleBinFolderId,
      }: DeleteSelectedPicksPayload) => {
        const recycleBinFolderPickRecord = get().pickRecord[recycleBinFolderId];
        const selectedPickIdList = get().selectedPickIdList;

        if (!get().hasPickRecordValue(recycleBinFolderPickRecord)) {
          return;
        }

        const prevRecycleBinFolderPickRecord = recycleBinFolderPickRecord;

        // 미리 삭제.
        set((state) => {
          if (!get().hasPickRecordValue(state.pickRecord[recycleBinFolderId])) {
            return;
          }

          for (const selectedId of selectedPickIdList) {
            state.pickRecord[recycleBinFolderId].pickInfoRecord[selectedId] =
              undefined;
          }

          state.pickRecord[recycleBinFolderId].pickIdOrderedList =
            state.pickRecord[recycleBinFolderId].pickIdOrderedList.filter(
              (pickId) => !selectedPickIdList.includes(pickId)
            );
        });

        try {
          await deletePicks({ idList: selectedPickIdList });
        } catch {
          set((state) => {
            state.pickRecord[recycleBinFolderId] =
              prevRecycleBinFolderPickRecord;
          });
        }
      },
      setSelectedPickIdList: (newSelectedPickIdList) => {
        set((state) => {
          state.selectedPickIdList = newSelectedPickIdList;
        });
      },
      selectSinglePick: (pickId) => {
        set((state) => {
          state.focusPickId = pickId;
          state.selectedPickIdList = [pickId];
        });
      },
      setIsDragging: (isDragging) => {
        set((state) => {
          state.isDragging = isDragging;
        });
      },
      setFocusedPickId: (focusedPickId) => {
        set((state) => {
          state.focusPickId = focusedPickId;
        });
      },

      setDraggingPickInfo: (draggingPickInfo) => {
        set((state) => {
          state.draggingPickInfo = draggingPickInfo;
        });
      },

      searchPicksByQueryParam: async (
        param: string,
        cursor?: number | string,
        size?: number
      ) => {
        try {
          const result = await getPickListByQueryParam(param, cursor, size);
          set((state) => {
            state.searchResult = result;
          });
        } catch (error) {
          console.log('fetchPickDataByFolderId error', error);
        }
      },
      getSearchResult: () => {
        return get().searchResult;
      },
      updatePickInfo: async (pickParentFolderId, pickInfo) => {
        const {
          id: pickId,
          tagIdOrderedList: newTagOrderedList,
          title: newTitle,
        } = pickInfo;

        const pickRecordValue = get().pickRecord[pickParentFolderId];

        if (!get().hasPickRecordValue(pickRecordValue)) {
          return;
        }

        const { pickInfoRecord } = pickRecordValue;

        if (!pickInfoRecord[pickId]) {
          return;
        }

        const prevPickInfo = pickInfoRecord[pickId];
        const newPickInfo: PickInfoType = {
          ...prevPickInfo,
          title: newTitle ?? prevPickInfo.title,
          tagIdOrderedList: newTagOrderedList ?? prevPickInfo.tagIdOrderedList,
        };

        // 미리 업데이트
        set((state) => {
          if (!state.pickRecord[pickParentFolderId]) {
            return;
          }

          const { pickInfoRecord } = state.pickRecord[pickParentFolderId];
          pickInfoRecord[pickId] = newPickInfo;
        });

        try {
          await updatePick(pickInfo);
        } catch {
          // 실패하면 원복하기.
          set((state) => {
            if (!state.pickRecord[pickParentFolderId]) {
              return;
            }

            const { pickInfoRecord } = state.pickRecord[pickParentFolderId];
            pickInfoRecord[pickId] = prevPickInfo;
          });
        }
      },
    }))
  )
);
