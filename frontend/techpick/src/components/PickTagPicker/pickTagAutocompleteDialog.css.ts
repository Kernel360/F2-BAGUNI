import { style } from '@vanilla-extract/css';
import { colorVars, fontSize } from 'techpick-shared';

const { color } = colorVars;

export const tagDialogPortalLayout = style({
  zIndex: '1',
  backgroundColor: colorVars.lightGray,
  boxShadow: '4px 4px 0px 0px rgba(0, 0, 0, 0.2)',
});

export const commandInputStyle = style({
  display: 'flex',
  flex: '1 1 60px',
  minWidth: '64px',
  height: '20px',
  outline: 'none',
  border: 'none',
  padding: '0 4px',
  fontSize: fontSize.sm,
  color: color.font,
  margin: 0,
});

export const tagListStyle = style({
  minWidth: '288px',
  maxHeight: '150px',
  border: `1px solid ${colorVars.gold8}`,
  borderTop: `0.5px solid ${colorVars.gold8}`,
  overflowY: 'auto',

  '::-webkit-scrollbar': {
    display: 'none',
  },
  backgroundColor: colorVars.lightGray,
  boxShadow: '4px 4px 0px 0px rgba(0, 0, 0, 0.2)',
});

export const tagListLoadingStyle = style({
  display: 'flex',
  justifyContent: 'center',
  alignItems: 'center',
  height: '20px',
});

export const tagListItemStyle = style({
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  backgroundColor: 'transparent',
  padding: '4px 0',

  // 선택된 상태일 때
  selectors: {
    '&[data-selected="true"]': {
      backgroundColor: colorVars.gold4,
    },
    '&[data-disabled="true"]': {
      display: 'none',
    },
  },
});

export const createTagListItemStyle = style([
  tagListItemStyle,
  { justifyContent: 'start' },
]);

export const tagCreateTextStyle = style({
  width: '28px',
  fontSize: '14px',
  color: color.font,
});

export const dialogOverlayStyle = style({
  zIndex: 1,
});

export const displayFlex = style({
  display: 'flex',
});

export const alignItemCenter = style({
  alignItems: 'center',
});

export const dragHandlerStyle = style({
  width: '20px',
  height: '20px',
  display: 'flex',
  justifyContent: 'center',
  alignItems: 'center',
  color: '#9F9E9B',
  cursor: 'pointer',
});
