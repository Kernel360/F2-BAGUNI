import { style } from '@vanilla-extract/css';
import { colorThemeContract, commonThemeContract } from 'techpick-shared';

export const linkEditorLabel = style({
  display: 'flex',
  gap: commonThemeContract.space.sm,
});

export const searchSection = style({
  display: 'flex',
  gap: commonThemeContract.space.sm,
});

export const linkEditorSection = style({
  width: '100%',
  height: '100%',
  display: 'flex',
  flexDirection: 'column',
});

export const linkEditorHeader = style({
  width: '100%',
  height: '56px',
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  borderBottom: `1px solid ${colorThemeContract.color.border}`,
  padding: `0 ${commonThemeContract.space.md}`,
});

export const linkEditor = style({
  width: '100%',
  height: '100%',
  display: 'flex',
  flexDirection: 'column',
});

export const folderViewSection = style({
  width: '100%',
  height: '160px',
  display: 'flex',
  borderBottom: `1px solid ${colorThemeContract.color.border}`,
  padding: `${commonThemeContract.space.lg} ${commonThemeContract.space.md}`,
});

export const folderWrapper = style({
  display: 'flex',
  flexDirection: 'column',
  justifyContent: 'center',
  alignItems: 'center',
  width: '100px',
  height: '100px',
  padding: `0 ${commonThemeContract.space.md}`,
  fontWeight: 300,
  textAlign: 'center',
  borderRadius: '4px',
  ':hover': {
    backgroundColor: '#efefef',
  },
});

export const linkViewSection = style({
  width: '100%',
  height: '100%',
  display: 'flex',
  alignItems: 'start',
  padding: `${commonThemeContract.space.lg} ${commonThemeContract.space.md}`,
});

export const linkEditorSectionFooter = style({
  width: '100%',
  height: '56px',
  borderTop: `1px solid ${colorThemeContract.color.border}`,
});
