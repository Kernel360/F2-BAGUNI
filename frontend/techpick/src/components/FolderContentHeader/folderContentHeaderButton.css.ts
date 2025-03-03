import { orangeOutlineButtonStyle } from '@/styles/orangeButtonStyle.css';
import { redOutlineButtonStyle } from '@/styles/redButtonStyle.css';
import { style } from '@vanilla-extract/css';

export const commonButtonStyle = style({
  display: 'flex',
  flexShrink: 0,
  gap: '4px',
  justifyContent: 'center',
  alignItems: 'center',
  width: '112px',
  height: '24px',
  fontSize: '12px',
  cursor: 'pointer',
});

export const chromeExtensionLinkButtonStyle = style([
  orangeOutlineButtonStyle,
  commonButtonStyle,
]);

export const deleteAllPicksFormRecycleBunButtonStyle = style([
  redOutlineButtonStyle,
  commonButtonStyle,
]);
