import { useThemeStore } from '@/stores/themeStore';
import type { TagType } from '@/types/TagType';
import { numberToRandomColor } from '@/utils/numberToRandomColor';
import type { CSSProperties } from 'react';
import { SelectedTagContent } from './SelectedTagContent';
import { SelectedTagLayout } from './SelectedTagLayout';

export function SelectedTagItem({ tag, children }: SelectedTagItemProps) {
  const { isDarkMode } = useThemeStore();
  const backgroundColor = numberToRandomColor(
    tag.colorNumber,
    isDarkMode ? 'dark' : 'light',
  );
  const style: CSSProperties = { backgroundColor };

  return (
    <SelectedTagLayout style={style}>
      <SelectedTagContent>{tag.name}</SelectedTagContent>
      {children}
    </SelectedTagLayout>
  );
}

interface SelectedTagItemProps {
  tag: TagType;
  children?: React.ReactNode;
}
