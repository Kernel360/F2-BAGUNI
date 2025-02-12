import type { GetFolderListResponseType } from '@/types/GetFolderListResponseType';
import { HTTPError } from 'ky';
import { apiClient } from '../apiClient';
import { API_URLS } from '../apiConstants';
import { returnErrorFromHTTPError } from '../error';

export const getRootFolderList = async () => {
  try {
    const response = await apiClient.get<GetFolderListResponseType>(
      API_URLS.GET_FOLDERS,
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
