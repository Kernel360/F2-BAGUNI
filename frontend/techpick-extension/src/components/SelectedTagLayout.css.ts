import { style } from '@vanilla-extract/css';
import { SelectedTagCommonStyle } from './SelectedTagCommonStyle.css';

export const selectedTagLayoutStyle = style({
  display: 'inline-flex',
  alignItems: 'center',
  maxWidth: `calc(${SelectedTagCommonStyle.width})`,
  height: '20px',
  borderRadius: '4px',
});
