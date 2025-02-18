/* eslint-disable import/namespace */
// This file configures the initialization of Sentry on the client.
// The config you add here will be used whenever a users loads a page in their browser.
// https://docs.sentry.io/platforms/javascript/guides/nextjs/

import * as Sentry from '@sentry/nextjs';

Sentry.init({
  dsn: 'https://ccec74582beec7789ae87df0139c6c48@o4508448586465280.ingest.us.sentry.io/4508520962916352',
  beforeSend(event) {
    // 개발 환경에서는 이벤트를 전송하지 않음
    if (process.env.NODE_ENV === 'development') {
      return null;
    }
    return event;
  },

  // Add optional integrations for additional features
  integrations: [
    Sentry.replayIntegration({
      /**
       * 모튼 텍스트를 마스킹하지 않습니다.
       */
      maskAllText: false,
      /**
       * 모튼 미디어를 차단하지 않습니다.
       */
      blockAllMedia: false,
    }),
  ],

  // Define how likely traces are sampled. Adjust this value in production, or use tracesSampler for greater control.
  tracesSampleRate: process.env.NODE_ENV === 'development' ? 0 : 1,

  // Define how likely Replay events are sampled.
  // This sets the sample rate to be 10%. You may want this to be 100% while
  // in development and sample at a lower rate in production
  replaysSessionSampleRate: process.env.NODE_ENV === 'development' ? 0 : 0.1,
  /**
   * 에러가 발생할 때 즉시 녹화가 시작되어 지속되는 리플레이 샘플 속도입니다.
   * 오류 전 최대 1분동안 이벤트를 기록합니다.
   * 1.0 - 0 사이의 값을 사용합니다. (1.0 권장)
   */
  replaysOnErrorSampleRate: process.env.NODE_ENV === 'development' ? 0 : 1.0,

  // Setting this option to true will print useful information to the console while you're setting up Sentry.
  debug: false,
});
