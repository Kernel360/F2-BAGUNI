import { HTTPError } from 'ky';
import { returnErrorFromHTTPError, apiClient } from '@/apis';
import { API_URLS } from '../apiConstants';
import { DeleteFolderRequestType } from '@/types';

export const deleteFolder = async (
  deleteFolderList: DeleteFolderRequestType['idList']
) => {
  try {
    await apiClient.delete<DeleteFolderRequestType>(API_URLS.DELETE_FOLDER, {
      json: deleteFolderList,
    });

    return;
  } catch (httpError) {
    if (httpError instanceof HTTPError) {
      const error = returnErrorFromHTTPError(httpError);
      throw error;
    }
    throw httpError;
  }
};
