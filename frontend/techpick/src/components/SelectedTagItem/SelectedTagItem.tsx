'use client';

import { useThemeStore } from '@/stores/themeStore';
import { numberToRandomColor } from '@/utils/numberToRandomColor';
import type { CSSProperties } from 'react';
import { SelectedTagContent } from './SelectedTagContent';
import { SelectedTagLayout } from './SelectedTagLayout';

export function SelectedTagItem({
  name,
  colorNumber,
  children,
}: SelectedTagItemProps) {
  const { isDarkMode } = useThemeStore();
  const backgroundColor = numberToRandomColor(
    colorNumber,
    isDarkMode ? 'dark' : 'light',
  );
  const style: CSSProperties = { backgroundColor };

  return (
    <SelectedTagLayout style={style}>
      <SelectedTagContent>{name}</SelectedTagContent>
      {children}
    </SelectedTagLayout>
  );
}

interface SelectedTagItemProps {
  name: string;
  colorNumber: number;
  children?: React.ReactNode;
}
