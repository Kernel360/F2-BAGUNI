import { API_URLS } from '@/constants/apiUrls';
import type { MoveTagRequestType } from '@/types/MoveTagRequestType';
import { returnErrorFromHTTPError } from '@/utils/returnErrorFromHTTPError';
import { HTTPError } from 'ky';
import { apiClient } from './apiClient';

export const moveTag = async (moveTagInfo: MoveTagRequestType) => {
  try {
    await apiClient.patch(API_URLS.getMoveTagsUrl(), {
      json: moveTagInfo,
    });
  } catch (httpError) {
    if (httpError instanceof HTTPError) {
      const error = await returnErrorFromHTTPError(httpError);
      throw error;
    }
    throw httpError;
  }
};
