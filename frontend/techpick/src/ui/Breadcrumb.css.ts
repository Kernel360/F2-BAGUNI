import { globalStyle, style } from '@vanilla-extract/css';
import { colorVars, space, typography } from 'techpick-shared';

export const breadcrumbListStyle = style({
  display: 'flex',
  flexDirection: 'row',
  flexWrap: 'wrap',
  alignItems: 'center',
  overflowWrap: 'break-word',
  color: colorVars.gray8,
  fontSize: typography.fontSize.lg,
  lineHeight: typography.lineHeight.lg,
  fontWeight: typography.fontWeights.semibold,
});

export const breadcrumbItemStyle = style({
  display: 'inline-flex',
  alignItems: 'center',
});

export const breadcrumbLinkStyle = style({
  padding: space.px,
  borderRadius: '4px',
  fontPalette: colorVars.gray1,
  transitionProperty: 'color, background-color, border-color, text-decoration',
  transitionTimingFunction: 'cubic-bezier(0.4, 0, 0.2, 1)',
  transitionDuration: '150ms',
  ':hover': {
    transition: 'background-color text-decoration 0.3s ease',
  },
  ':focus': {
    outline: 'none',
    transition: 'background-color text-decoration 0.3s ease',
  },
  selectors: {
    '&:hover, &:focus': {
      fontPalette: colorVars.gray3,
      textDecoration: 'underline',
      // backgroundColor: colorVars.yellow3,
    },
  },
});

export const breadcrumbPageStyle = style({
  fontPalette: colorVars.gray4,
  fontWeight: typography.fontWeights.normal,
});

export const breadcrumbSeparatorStyle = style({
  display: 'flex',
  alignItems: 'center',
});

// svg icon size
globalStyle(`${breadcrumbSeparatorStyle}`, {
  width: space['16'],
  height: space['16'],
});

export const breadcrumbEllipsisStyle = style({
  display: 'flex',
  height: space['36'],
  width: space['36'],
  alignItems: 'center',
  justifyContent: 'center',
});
