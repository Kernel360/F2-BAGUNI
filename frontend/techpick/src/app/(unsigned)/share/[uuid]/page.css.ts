import {
  orangeGhostButtonStyle,
  orangeOutlineButtonStyle,
  orangeSolidButtonStyle,
} from '@/styles/orangeButtonStyle.css';
import { style } from '@vanilla-extract/css';
import {
  colorVars,
  desktop,
  fontWeights,
  mobile,
  tablet,
  zIndex,
} from 'techpick-shared';

export const folderContentHeaderStyle = style({
  display: 'flex',
  justifyContent: 'space-between',
  backgroundColor: colorVars.gold2,

  '@media': {
    [`${mobile}, ${tablet}`]: {
      position: 'sticky',
      top: 0,
      width: '100vw',
      height: '44px',
      padding: '0 12px',
      zIndex: zIndex.level11,
    },
    [desktop]: {
      padding: '0 16px',
    },
  },
});

export const folderOpenIconStyle = style({
  flexShrink: '0',
});

export const folderNameStyle = style({
  flexShrink: 1,
  flexGrow: 0,
  display: 'inline-block',
  height: '28px',
  lineHeight: '24px',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
  overflow: 'hidden',

  '@media': {
    [desktop]: {
      fontSize: '24px',
      fontWeight: fontWeights.medium,
    },
  },
});

export const buttonSectionStyle = style({
  display: 'flex',
  gap: '12px',
  alignItems: 'center',

  '@media': {
    [desktop]: {
      padding: '16px',
    },
  },
});

export const sharedPageButtonStyle = style({
  '@media': {
    [mobile]: {
      width: '72px',
      height: '32px',
      fontSize: '14px',
    },
    [`${tablet}, ${desktop}`]: { width: '100px', height: '32px' },
  },
});

export const homeNavigateButtonStyle = style([
  sharedPageButtonStyle,
  orangeGhostButtonStyle,
]);

export const loginButtonStyle = style([
  sharedPageButtonStyle,
  orangeOutlineButtonStyle,
]);

export const signUpButtonStyle = style([
  sharedPageButtonStyle,
  orangeSolidButtonStyle,
]);

export const desktopVisibleStyle = style({
  '@media': {
    [`${mobile},${tablet}`]: {
      display: 'none',
    },
  },
});

export const mobileAndTabletVisibleStyle = style({
  '@media': {
    [desktop]: {
      display: 'none',
    },
  },
});

export const mobilePickRecordListStyle = style({
  padding: '0 12px',
});

export const currentFolderNameSectionStyle = style({
  display: 'inline-flex',
  alignItems: 'center',
  gap: 8,
  flex: '1 0',
  maxWidth: '100%',
  minWidth: 0,
});
