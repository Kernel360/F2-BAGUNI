import type { Concrete } from './util.type';
import type { components } from '@/schema';

export type PickInfoType = Concrete<
  components['schemas']['techpick.api.domain.pick.dto.PickResult$Pick']
>;

export type PickInfoRecordType = {
  [pickId: string]: PickInfoType | undefined;
};

export type PickIdOrderedListType = number[];

export type PickRecordValueType = {
  pickIdOrderedList: PickIdOrderedListType;
  pickInfoRecord: PickInfoRecordType;
};

export type PickRecordType = {
  [folderId: string]: PickRecordValueType | undefined;
};

export type PickListType = Concrete<
  components['schemas']['techpick.api.domain.pick.dto.PickResult$Pick']
>[];

export type GetPicksByFolderIdResponseType = {
  folderId: number;
  pickList: PickListType;
}[];

export type OrderedPickIdListType = number[];
export type SelectedPickIdListType = number[];
