import type { DnDCurrentType } from './DnDCurrentType';

export interface TagSortableObjectType extends DnDCurrentType {
  type: 'tag';
  tagOrder: number;
}
