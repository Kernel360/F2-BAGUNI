import { style } from '@vanilla-extract/css';

export const mobileEmptyPickRecordImageStyle = style({
  height: 'calc(100dvh - 52px)',
});

export const emptyPickListLayoutStyle = style({
  position: 'absolute',
  top: '50%',
  left: '50%',
  transform: 'translate(-50%, -50%)',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  width: '70vw',
});
