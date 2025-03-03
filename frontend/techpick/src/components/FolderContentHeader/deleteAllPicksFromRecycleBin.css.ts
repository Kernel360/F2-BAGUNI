import { dialogContentLayoutStyle } from '@/styles/dialogStyle.css';
import { redSolidButtonStyle } from '@/styles/redButtonStyle.css';
import { sandOutlineButtonStyle } from '@/styles/sandButtonStyle.css';
import { style } from '@vanilla-extract/css';
import { colorVars, desktop } from 'techpick-shared';

export const wideButtonStyle = style({
  flexShrink: 0,
  width: '100%',
  height: '24px',
});

export const redWideButtonStyle = style([redSolidButtonStyle, wideButtonStyle]);

export const sendWideButtonStyle = style([
  sandOutlineButtonStyle,
  wideButtonStyle,
]);

export const dialogContentStyle = style([
  dialogContentLayoutStyle,
  {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
    padding: '12px',
    backgroundColor: colorVars.gold3,

    '@media': {
      [desktop]: {
        width: '400px',
        height: 'fit-content',
      },
    },
  },
]);
