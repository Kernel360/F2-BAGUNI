import type { CreatePickRequestType } from '@/types/CreatePickRequestType';
import type { PickDraggableObjectType } from '@/types/PickDraggableObjectType';
import type { PickInfoType } from '@/types/PickInfoType';
import type { PickRecordType } from '@/types/PickRecordType';
import type { PickRecordValueType } from '@/types/PickRecordValueType';
import type { PickToFolderDroppableObjectType } from '@/types/PickToFolderDroppableObjectType';
import type { SearchPicksResponseType } from '@/types/SearchPicksResponseType';
import type { SelectedPickIdListType } from '@/types/SelectedPickIdListType';
import type { UpdatePickRequestType } from '@/types/UpdatePickRequestType';
import type { Active, Over } from '@dnd-kit/core';

export type PickState = {
  searchResult: SearchPicksResponseType;
  pickRecord: PickRecordType;
  focusPickId: number | null;
  selectedPickIdList: SelectedPickIdListType;
  isDragging: boolean;
  draggingPickInfo: PickInfoType | null | undefined;
  isMovingDestinationFolderId: number | null | undefined;
};

export type PickAction = {
  fetchPickDataByFolderId: (folderId: number) => Promise<void>;
  getOrderedPickIdListByFolderId: (folderId: number) => number[];
  getPickInfoByFolderIdAndPickId: (
    folderId: number,
    pickId: number,
  ) => PickInfoType | null | undefined;
  hasPickRecordValue: (
    pickRecordValue: PickRecordValueType | undefined | null,
  ) => pickRecordValue is PickRecordValueType;
  movePicksToEqualFolder: (movePickPayload: MovePickPayload) => Promise<void>;
  movePicksToDifferentFolder: (
    movePickPayload: movePicksToDifferentFolder,
  ) => Promise<void>;
  moveSelectedPicksToRecycleBinFolder: (
    payload: MoveSelectedPicksToRecycleBinFolderPayload,
  ) => Promise<void>;
  deleteSelectedPicks: (
    deleteSelectedPicksPayload: DeleteSelectedPicksPayload,
  ) => Promise<void>;
  setSelectedPickIdList: (
    newSelectedPickIdList: SelectedPickIdListType,
  ) => void;
  selectSinglePick: (pickId: number) => void;
  setIsDragging: (isDragging: boolean) => void;
  setFocusedPickId: (focusedPickId: number) => void;
  setDraggingPickInfo: (
    draggingPickInfo: PickInfoType | null | undefined,
  ) => void;

  /**
   * queryParam을 통으로 검색에 사용합니다. (search 패널)
   */
  searchPicksByQueryParam: (
    param: string,
    cursor?: number | string,
    size?: number,
  ) => Promise<void>;
  getSearchResult: () => SearchPicksResponseType;
  updatePickInfo: (
    pickParentFolderId: number,
    pickInfo: UpdatePickRequestType,
  ) => Promise<void>;
  insertPickInfo: (pickInfo: PickInfoType, pickParentFolderId: number) => void;
  createInitialRecordValue: (folderId: number) => void;
  createPick: (pickInfo: CreatePickRequestType) => Promise<void>;
};

export type MovePickPayload = {
  folderId: number;
  from: Active;
  to: Over;
};

export type movePicksToDifferentFolder = {
  from: PickDraggableObjectType;
  to: PickToFolderDroppableObjectType;
};

export type MoveSelectedPicksToRecycleBinFolderPayload = {
  picksParentFolderId: number;
  recycleBinFolderId: number;
};

export type DeleteSelectedPicksPayload = {
  recycleBinFolderId: number;
};
