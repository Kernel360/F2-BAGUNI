import type { MoveTagRequestType } from '@/types/MoveTagRequestType';
import { HTTPError } from 'ky';
import { apiClient } from '../apiClient';
import { API_URLS } from '../apiConstants';
import { returnErrorFromHTTPError } from '../error';

export const moveTag = async (moveTagInfo: MoveTagRequestType) => {
  try {
    await apiClient.patch(API_URLS.MOVE_TAGS, {
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
