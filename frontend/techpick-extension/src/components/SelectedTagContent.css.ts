import { style } from '@vanilla-extract/css';
import { fontSize, fontWeights, space } from 'techpick-shared';
import {
  actionButtonWidth,
  selectedTagMaxWidth,
} from './SelectedTagLayout.css';

export const selectedTagContentStyle = style({
  boxSizing: 'border-box',
  maxWidth: `calc(${selectedTagMaxWidth} - ${actionButtonWidth})`,
  height: '20px',
  padding: `0px ${space['8']}`,
  lineHeight: '18px',
  fontSize: fontSize.sm,
  fontWeight: fontWeights.normal,
  whiteSpace: 'nowrap', // 줄 바꿈 방지
  overflow: 'hidden', // 넘치는 내용 숨김
  textOverflow: 'ellipsis', // 생략 부호 추가
});
