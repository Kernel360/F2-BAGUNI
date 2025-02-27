import type { UniqueIdentifier } from '@dnd-kit/core';

export interface DnDCurrentType {
  id: UniqueIdentifier;
  sortable: {
    containerId: string | null;
    items: UniqueIdentifier[];
    index: number;
  };
}
