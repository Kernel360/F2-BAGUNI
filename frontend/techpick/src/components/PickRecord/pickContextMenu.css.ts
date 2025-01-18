import { style } from '@vanilla-extract/css';
import { borderRadius, colorVars, sizes, space } from 'techpick-shared';

export const contextMenuContentLayout = style({
  width: sizes.min,
  height: sizes.min,
  border: '1px solid',
  borderColor: colorVars.gold8,
  borderRadius: borderRadius.base,
  backgroundColor: colorVars.gold5,
});

export const contextMenuItemStyle = style({
  display: 'flex',
  alignItems: 'center',
  gap: space['8'],
  width: sizes['6xs'],
  height: sizes['12xs'],
  padding: space['8'],
  cursor: 'pointer',

  selectors: {
    '&[data-highlighted]': {
      outline: colorVars.primaryFaded,
      backgroundColor: colorVars.primary,
    },
  },
});
