import type { GetPicksResponseType } from '@/types/GetPicksResponseType';
import type { PickIdOrderedListType } from '@/types/PickIdOrderedListType';
import type { PickInfoRecordType } from '@/types/PickInfoRecordType';
import type { PickListType } from '@/types/PickListType';
import type { SearchPicksResponseType } from '@/types/SearchPicksResponseType';
import type { SearchQueryParamType } from '@/types/SearchQueryParamType';
import { HTTPError } from 'ky';
import { apiClient } from '../apiClient';
import { API_URLS } from '../apiConstants';
import { returnErrorFromHTTPError } from '../error';

export const getPicksByFolderId = async (folderId: number) => {
  const data = await getPickListByFolderId(folderId);
  return generatePickRecordData(data[0].pickList);
};

export const getPickListByQueryParam = async (
  queryParam: SearchQueryParamType,
  cursor?: number | string,
  size?: number,
): Promise<SearchPicksResponseType> => {
  try {
    const URL = API_URLS.SEARCH_PICKS_BY_QUERY_PARAM(queryParam, cursor, size);
    const response = await apiClient.get<SearchPicksResponseType>(URL);
    const data = await response.json();

    return data as SearchPicksResponseType;
  } catch (httpError) {
    if (httpError instanceof HTTPError) {
      const error = await returnErrorFromHTTPError(httpError);
      throw error;
    }
    throw httpError;
  }
};

const getPickListByFolderId = async (folderId: number) => {
  try {
    const response = await apiClient.get<GetPicksResponseType>(
      API_URLS.GET_PICKS_BY_FOLDER_ID(folderId),
    );
    const data = await response.json();

    return data;
  } catch (httpError) {
    if (httpError instanceof HTTPError) {
      const error = await returnErrorFromHTTPError(httpError);
      throw error;
    }
    throw httpError;
  }
};

const generatePickRecordData = (pickList: PickListType) => {
  const pickIdOrderedList: PickIdOrderedListType = [];
  const pickInfoRecord = {} as PickInfoRecordType;

  for (const pickInfo of pickList) {
    pickIdOrderedList.push(pickInfo.id);
    pickInfoRecord[`${pickInfo.id}`] = pickInfo;
  }

  return { pickIdOrderedList, pickInfoRecord };
};
