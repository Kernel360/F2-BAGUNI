import { HTTPError } from 'ky';
import { apiClient } from '../apiClient';
import { API_URLS } from '../apiConstants';
import { returnErrorFromHTTPError } from '../error';

export const deleteAllPicksFromRecycleBin = async () => {
  try {
    await apiClient.delete(API_URLS.DELETE_ALL_PICKS_FROM_RECYCLE_BIN);
  } catch (httpError) {
    if (httpError instanceof HTTPError) {
      const error = await returnErrorFromHTTPError(httpError);
      throw error;
    }
    throw httpError;
  }
};
