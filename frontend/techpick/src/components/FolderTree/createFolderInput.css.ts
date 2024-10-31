import { style } from '@vanilla-extract/css';
import { colorThemeContract, fontSize, sizes, space } from 'techpick-shared';

export const createFolderInputLayout = style({
  display: 'flex',
  alignItems: 'center',
  gap: space['4'],
  minWidth: sizes['3xs'],
  maxWidth: sizes['full'],
  padding: space['8'],
});

export const labelStyle = style({
  color: colorThemeContract.primary,
});

export const inputStyle = style({
  flexGrow: '1',
  outline: 'none',
  border: 'none',
  borderBottom: '1px solid',
  borderColor: colorThemeContract.primary,
  padding: '0',
  fontSize: fontSize['md'],
});
