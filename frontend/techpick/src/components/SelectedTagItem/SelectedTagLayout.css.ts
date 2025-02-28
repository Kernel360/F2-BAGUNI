import { style } from '@vanilla-extract/css';

export const selectedTagMaxWidth = '264px';
export const selectedTagHeight = '20px';
export const actionButtonWidth = '20px';

export const selectedTagLayoutStyle = style({
  display: 'inline-flex',
  alignItems: 'center',
  maxWidth: selectedTagMaxWidth,
  height: selectedTagHeight,
  margin: 0,
  borderRadius: '4px',
});
