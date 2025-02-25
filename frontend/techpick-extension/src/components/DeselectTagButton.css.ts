import { style } from '@vanilla-extract/css';
import { actionButtonWidth, selectedTagHeight } from './SelectedTagLayout.css';

export const DeselectTagButtonStyle = style({
  display: 'flex',
  justifyContent: 'center',
  alignItems: 'center',
  width: actionButtonWidth,
  height: selectedTagHeight,
  backgroundColor: 'transparent',
  cursor: 'pointer',
});
