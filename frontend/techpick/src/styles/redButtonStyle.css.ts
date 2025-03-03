import { style } from '@vanilla-extract/css';
import { colorVars } from 'techpick-shared';
import { baseButtonStyle } from './baseButtonStyle.css';

/**
 * @description color만 설정했습니다. width, height, padding 등은 직접 설정하셔야합니다.
 */
export const redSolidButtonStyle = style([
  baseButtonStyle,
  {
    backgroundColor: colorVars.red9,
    color: 'white',
    ':hover': { backgroundColor: colorVars.red10 },
    ':active': { backgroundColor: colorVars.red11 },
    ':focus': { backgroundColor: colorVars.red11 },
  },
]);

/**
 * @description color만 설정했습니다. width, height, padding 등은 직접 설정하셔야합니다.
 */
export const redOutlineButtonStyle = style([
  baseButtonStyle,
  {
    border: '1px solid',
    borderColor: colorVars.red7,
    backgroundColor: 'transparent',
    color: colorVars.red11,
    selectors: {
      '&:hover, &:focus': {
        backgroundColor: colorVars.red3,
      },
    },
  },
]);

/**
 * @description color만 설정했습니다. width, height, padding 등은 직접 설정하셔야합니다.
 */
export const redGhostButtonStyle = style([
  baseButtonStyle,
  {
    backgroundColor: 'transparent',
    color: colorVars.red11,
    selectors: {
      '&:hover, &:focus': {
        backgroundColor: colorVars.red3,
      },
    },
  },
]);

/**
 * @description color만 설정했습니다. width, height, padding 등은 직접 설정하셔야합니다.
 */
export const redLinkButtonStyle = style([
  baseButtonStyle,
  {
    backgroundColor: 'transparent',
    color: colorVars.red11,
    selectors: {
      '&:hover, &:focus': {
        backgroundColor: colorVars.red3,
        textDecoration: 'underline',
      },
    },
  },
]);
