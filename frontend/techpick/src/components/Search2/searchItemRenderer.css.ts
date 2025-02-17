import { style } from '@vanilla-extract/css';
import { colorVars, typography } from 'techpick-shared';

export const searchListItemContainer = style({
  display: 'flex',
  flexDirection: 'column',
  borderBottom: `1px solid ${colorVars.gray4}`,
  padding: '4px 8px',
  borderRadius: '4px',
  cursor: 'pointer',
  ':hover': {
    backgroundColor: colorVars.gold2,
  },
});

export const searchListItemTextContainer = style({
  display: 'flex',
  flex: 1,
  flexDirection: 'row',
  justifyContent: 'space-between',
});

export const searchListItemTitle = style({
  display: 'inline-block',
  width: '80%',
  color: colorVars.gray12,
  fontSize: typography.fontSize.sm,
  fontWeight: typography.fontWeights.bold,
  overflow: 'hidden',
  whiteSpace: 'nowrap',
  textOverflow: 'ellipsis',
});

export const searchListItemDate = style({
  display: 'inline-block',
  height: '100%',
  color: colorVars.gray10,
  fontSize: typography.fontSize.xs,
  whiteSpace: 'nowrap',
});
