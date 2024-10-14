import { HTTPError } from 'ky';
import { ApiErrorBody } from '../type';

export const handleHTTPError = async (error: HTTPError): Promise<never> => {
  const errorData = await error.response.json<ApiErrorBody>();

  if (errorData) {
    throw new Error(`${errorData.message}`);
  }

  throw new Error(`알 수 없는 에러: ${error.response.statusText}`);
};
