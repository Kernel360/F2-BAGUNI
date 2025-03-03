import { dialogContentLayoutStyle } from '@/styles/dialogStyle.css';
import { style } from '@vanilla-extract/css';

export const dialogContent = style([
  dialogContentLayoutStyle,
  {
    background: 'white',
    padding: '16px',
    width: '100%',
    minWidth: '300px',
    maxWidth: '800px',
  },
]);

export const searchListContainer = style({
  display: 'flex',
  flexDirection: 'row',
  height: 'auto',
  margin: 'auto',
  justifyContent: 'space-between',
  gap: '16px',
});

export const filterContainer = style({
  gap: '16px',
  marginBottom: '8px',
});

export const showFilterContainer = style({
  display: 'flex',
});

export const filterGroup = style({
  display: 'flex',
  flexDirection: 'column',
  gap: '8px',
});

export const filterLabel = style({
  fontSize: '12px',
  fontWeight: 'bold',
  color: '#555',
});

export const filterSelect = style({
  padding: '8px',
  border: '1px solid #ccc',
  borderRadius: '4px',
  fontSize: '14px',
});

export const searchResultContainer = style({
  maxHeight: '300px',
  marginTop: '16px',
});

export const searchResultItem = style({
  padding: '8px 0',
  borderBottom: '1px solid #eee',
});

export const noResult = style({
  textAlign: 'center',
  color: '#aaa',
  marginTop: '16px',
});
