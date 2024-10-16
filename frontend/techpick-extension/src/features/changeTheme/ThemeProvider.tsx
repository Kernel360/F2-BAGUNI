'use client';

import { useThemeStore } from './themeStore';
import { lightTheme, darkTheme, commonTheme } from 'techpick-shared';

export const ThemeProvider = ({ children }: { children: React.ReactNode }) => {
  const { isDarkMode } = useThemeStore();

  const currentTheme = isDarkMode ? darkTheme : lightTheme;
  return <div className={`${commonTheme} ${currentTheme}`}>{children}</div>;
};
