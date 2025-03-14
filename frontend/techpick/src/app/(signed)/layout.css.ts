import { mobileNavigationHeight } from '@/components/mobileNavigationBar.css';
import { style } from '@vanilla-extract/css';
import { colorVars } from 'techpick-shared';

export const pageContainerLayout = style({
  display: 'flex',
  flexDirection: 'row',
  height: '100dvh',
});

export const mobilePageContainerStyle = style({
  width: '100%',
  height: `calc(100dvh - ${mobileNavigationHeight})`,
  padding: '0 12px',
  backgroundColor: colorVars.gold3,
  overflowY: 'auto',
});
