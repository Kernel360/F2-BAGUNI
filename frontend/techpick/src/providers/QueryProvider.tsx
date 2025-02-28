'use client';

import { getQueryClient } from '@/libs/@react-query/getQueryClient';
import { taskScheduler } from '@/libs/@react-query/taskScheduler';
import { QueryClientProvider, notifyManager } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import type { PropsWithChildren } from 'react';

export const QueryProvider = ({ children }: PropsWithChildren) => {
  const queryClient = getQueryClient();
  notifyManager.setScheduler(taskScheduler);

  return (
    <QueryClientProvider client={queryClient}>
      {children} <ReactQueryDevtools />
    </QueryClientProvider>
  );
};
