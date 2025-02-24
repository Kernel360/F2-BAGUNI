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
  padding: '4px',

  // 선택된 상태일 때
  selectors: {
    '&[data-selected="true"]': {
      backgroundColor: colorVars.gold4,
    },
    '&[data-disabled="true"]': {
      display: 'none',
    },
    '&:focus': { backgroundColor: colorVars.gold4 },
  },
});

export const tagListItemContentStyle = style({
  maxWidth: 'calc(288px - 38px)', // 26px은 생성 텍스트의 영역 12px는 패딩
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

export const dialogOverlayStyle = style({
  zIndex: 1,
});
