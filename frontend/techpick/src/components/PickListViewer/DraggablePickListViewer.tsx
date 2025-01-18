import type { PickInfoType } from '@/types/PickInfoType';
import type { PickRenderModeType } from '@/types/PickRenderModeType';
import type { ReactNode } from 'react';
import { PickDnDCard } from './PickDnDCard';
import { PickDnDCardListLayout } from './PickDnDCardListLayout';
import { PickDndListItem } from './PickDndListItem';
import { PickDndListItemLayout } from './PickDndListItemLayout';
import type {
  PickViewItemComponentProps,
  PickViewItemListLayoutComponentProps,
} from './PickListViewer';

export function DraggablePickListViewer({
  pickList,
  viewType = 'list',
  folderId,
}: PickListViewerProps) {
  const { PickViewItemComponent, PickViewItemListLayoutComponent } =
    DND_PICK_LIST_VIEW_TEMPLATES[viewType];

  return (
    <PickViewItemListLayoutComponent folderId={folderId} viewType={viewType}>
      {pickList.map((pickInfo) => (
        <PickViewItemComponent key={pickInfo.id} pickInfo={pickInfo} />
      ))}
    </PickViewItemListLayoutComponent>
  );
}

interface PickListViewerProps {
  pickList: PickInfoType[];
  folderId: number;
  viewType?: PickRenderModeType;
}

const DND_PICK_LIST_VIEW_TEMPLATES: Record<
  PickRenderModeType,
  DnDViewTemplateValueType
> = {
  card: {
    PickViewItemComponent: PickDnDCard,
    PickViewItemListLayoutComponent: PickDnDCardListLayout,
  },
  list: {
    PickViewItemComponent: PickDndListItem,
    PickViewItemListLayoutComponent: PickDndListItemLayout,
  },
};

interface DnDViewTemplateValueType {
  PickViewItemListLayoutComponent: (
    props: PickViewDnDItemListLayoutComponentProps,
  ) => ReactNode;
  PickViewItemComponent: (props: PickViewDnDItemComponentProps) => ReactNode;
}

export type PickViewDnDItemListLayoutComponentProps =
  PickViewItemListLayoutComponentProps<{
    folderId: number;
    viewType: PickRenderModeType;
  }>;

export type PickViewDnDItemComponentProps = PickViewItemComponentProps;
