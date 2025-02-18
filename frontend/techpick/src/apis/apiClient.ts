import { ERROR_MESSAGE_JSON } from '@/constants/errorMessageJson';
import { getAccessToken } from '@/utils/getAccessToken';
import { notifyError } from '@/utils/toast';
import * as Sentry from '@sentry/nextjs';
import ky, { HTTPError } from 'ky';
import { redirect } from 'next/navigation';
import { returnErrorFromHTTPError } from './error';

const isServer = typeof window === 'undefined';

export const apiClient = ky.create({
  credentials: 'include',
  prefixUrl: process.env.NEXT_PUBLIC_API,
  cache: 'no-store',
  hooks: {
    beforeRequest: [
      async (request) => {
        if (isServer) {
          const accessToken = await getAccessToken();
          if (accessToken) {
            request.headers.set(
              'Cookie',
              `${accessToken.name}=${accessToken.value}`,
            );
          }
        }
      },
    ],
    beforeError: [
      async (httpError) => {
        if (httpError instanceof HTTPError) {
          const error = await returnErrorFromHTTPError(httpError);
          Sentry.setContext('api_call', {
            url: httpError.request.url,
            method: httpError.request.method,
          });

          const parsedErrorMessage = error.message.split(' ');
          const errorCode = parsedErrorMessage.shift();

          if (errorCode && ERROR_MESSAGE_JSON[errorCode]) {
            if (errorCode === 'UNKNOWN') {
              Sentry.captureMessage('500 서버 에러', 'error');
            }

            if (errorCode === 'AU-001') {
              if (isServer) {
                redirect('/login');
              } else {
                window.location.href = '/login';
              }
            } else {
              if (!isServer) {
                notifyError(ERROR_MESSAGE_JSON[errorCode]);
              }
            }
          }
        }

        return httpError;
      },
    ],
  },
  retry: 1,
});
