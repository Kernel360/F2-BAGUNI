import { style } from '@vanilla-extract/css';
import { colorVars } from 'techpick-shared';

export const recommendedPickCarouselSectionStyle = style({
  paddingTop: '12px',
  paddingBottom: '20px',
});

export const recommendedPickCarouselStyle = style({
  flexShrink: 0,
});

export const recommendSectionDescription = style({
  fontSize: '20px',
  fontWeight: '500',
});

export const pointTextStyle = style({
  color: colorVars.primary,
});

export const recommendSectionLayoutStyle = style({
  margin: 'auto',
  padding: '20px',
  width: 'fit-content',
});

export const recommendPageTitleStyle = style({
  display: 'inline-block',
  fontSize: '24px',
  fontWeight: '700',
});

export const recommendPageDescriptionSectionStyle = style({
  display: 'flex',
  alignItems: 'end',
  gap: '8px',
});

export const recommendPageDescriptionStyle = style({
  paddingTop: '4px',
  fontSize: '12px',
  color: colorVars.sand11,
  paddingBottom: '4px',
});

export const recommendContentSectionStyle = style({
  height: 'calc(100dvh - 92px)',
  paddingTop: '12px',
  display: 'flex',
  flexDirection: 'column',
  justifyContent: 'space-around',
  width: 'fit-content',
});
