'use client';

import { useEffect, useState } from 'react';
import type { PropsWithChildren } from 'react';

export function DeferredComponent({
  children,
  deferredMillisecond = 150,
}: PropsWithChildren<DeferredComponentProps>) {
  const [isDeferred, setIsDeferred] = useState(false);

  useEffect(() => {
    const timeoutId = setTimeout(() => {
      setIsDeferred(true);
    }, deferredMillisecond);
    return () => clearTimeout(timeoutId);
  }, [deferredMillisecond]);

  if (!isDeferred) {
    return null;
  }

  return <>{children}</>;
}

interface DeferredComponentProps {
  deferredMillisecond?: number;
}
