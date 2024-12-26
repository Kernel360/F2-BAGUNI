import { style } from '@vanilla-extract/css';
import { fontSize } from 'techpick-shared';
import { redOutlineButtonStyle } from '@/styles/redButtonStyle.css';

export const myPageContentContainer = style({
  display: 'grid',
  gridTemplateColumns: '15% 75% 10%',
  alignItems: 'center',
  padding: '8px',
  fontSize: fontSize.sm,
});

export const cell = style({
  whiteSpace: 'nowrap',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  padding: '0 8px',
});

export const cancelButton = style([
  redOutlineButtonStyle,
  {
    minWidth: '70px',
    padding: '4px',
  },
]);
