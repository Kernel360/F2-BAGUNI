import { style } from '@vanilla-extract/css';
import { colorVars, fontSize, fontWeights, space } from 'techpick-shared';

export const selectedTagContentStyle = style({
  boxSizing: 'border-box',
  margin: space['8'],

  maxWidth: '228px',
  height: '20px',
  lineHeight: '18px',
  fontSize: fontSize.sm,
  fontWeight: fontWeights.normal,
  whiteSpace: 'nowrap', // 줄 바꿈 방지
  overflow: 'hidden', // 넘치는 내용 숨김
  textOverflow: 'ellipsis', // 생략 부호 추가
  color: colorVars.color.font,
});
