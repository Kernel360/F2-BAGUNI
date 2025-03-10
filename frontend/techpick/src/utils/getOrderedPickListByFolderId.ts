import type { PickInfoType } from '@/types/PickInfoType';
import type { PickRecordValueType } from '@/types/PickRecordValueType';

export const getOrderedPickListByFolderId = (
  pickRecordValue: PickRecordValueType | null | undefined,
) => {
  if (!pickRecordValue) {
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
};
