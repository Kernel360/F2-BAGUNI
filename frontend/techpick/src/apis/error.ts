import { HTTPError } from 'ky';
import { components } from '@/schema';

export const handleHTTPError = async (error: HTTPError): Promise<never> => {
  const errorData = await error.response.json<ApiErrorBody>();

  if (errorData) {
    throw new Error(`${errorData.message}`);
  }

  throw new Error(`알 수 없는 에러: ${error.response.statusText}`);
};

export const returnErrorFromHTTPError = async (
  error: HTTPError
): Promise<Error> => {
  const errorData = await error.response.json<ApiErrorBody>();

  if (errorData) {
    return new Error(`${errorData.message}`);
  }

  return new Error(`알 수 없는 에러: ${error.response.statusText}`);
};

export type ApiErrorBody = components['schemas']['ApiErrorBody'];
