// This file configures the initialization of Sentry on the server.
// The config you add here will be used whenever the server handles a request.
// https://docs.sentry.io/platforms/javascript/guides/nextjs/

import * as Sentry from '@sentry/nextjs';

Sentry.init({
  dsn: 'https://ccec74582beec7789ae87df0139c6c48@o4508448586465280.ingest.us.sentry.io/4508520962916352',

  // Define how likely traces are sampled. Adjust this value in production, or use tracesSampler for greater control.
  tracesSampleRate: process.env.NODE_ENV === 'development' ? 0 : 1,

  // Setting this option to true will print useful information to the console while you're setting up Sentry.
  debug: false,
});
