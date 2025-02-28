import { style } from '@vanilla-extract/css';
import { colorVars } from 'techpick-shared';
import { SelectedTagCommonStyle } from './SelectedTagCommonStyle.css';

const { color } = colorVars;

export const overlayStyle = style({
  position: 'fixed',
  top: 0,
  left: 0,
  width: '336px',
  height: '244px',
  backgroundColor: 'transparent',
  pointerEvents: 'none',
});

export const tagDialogPortalLayout = style({
  position: 'absolute',
  top: '0',
  left: 0,
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
  color: color.font,
});

export const tagListStyle = style({
  maxHeight: '90px',
  border: '1px solid black',
  borderTop: 'none',
  overflowY: 'auto',
  '::-webkit-scrollbar': {
    display: 'none',
  },
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
  padding: '4px',

  // 선택된 상태일 때
  selectors: {
    '&[data-selected="true"]': {
      backgroundColor: colorVars.softPoint,
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

export const tagListItemContentStyle = style({
  maxWidth: `calc(${SelectedTagCommonStyle.width} - 38px)`, // 26px은 생성 텍스트의 영역 12px는 패딩
  height: '20px',
  lineHeight: '20px',
  borderRadius: '4px',
  padding: '0 4px',
  fontSize: '14px',
  whiteSpace: 'nowrap', // 줄 바꿈 방지
  overflow: 'hidden', // 넘치는 내용 숨김
  textOverflow: 'ellipsis', // 생략 부호 추가
  color: color.font,
});

export const tagCreateTextStyle = style({
  width: '28px',
  fontSize: '14px',
  color: color.font,
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
