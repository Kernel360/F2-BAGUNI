import { style } from '@vanilla-extract/css';
import {
  actionButtonWidth,
  selectedTagHeight,
} from '../SelectedTagItem/SelectedTagLayout.css';

export const DeselectTagButtonStyle = style({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  width: actionButtonWidth,
  height: selectedTagHeight,
  backgroundColor: 'transparent',
  cursor: 'pointer',
});
