'use client';
import { useFetchFolders } from '@/queries/useFetchFolders';
import type { FolderIdType } from '@/types/FolderIdType';
import { getChildFolderListByParentFolderId } from '@/utils/getChildFolderListByParentFolderId';
import { getFolderSortableContextId } from '@/utils/getFolderSortableContextId';
import {
  SortableContext,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { CreateChildFolderInput } from './CreateChildFolderInput';
import { CreateFolderStatusProvider } from './CreateFolderStatusProvider';
import { DeleteFolderStatusProvider } from './DeleteFolderStatusProvider';
import { EditableFolderItem } from './EditableFolderItem';
import { ShareFolderStatusProvider } from './ShareFolderStatusProvider';
import { UpdateFolderStatusProvider } from './UpdateFolderStatusProvider';

/**
 *
 * 폴더 생성, 이름 변경, 공유 여부, 삭제, 드래그 앤 드랍으로 위치 수정이 가능한
 *
 * 유저가 생성한 폴더 목록을 렌더링합니다.
 */
export function EditableFolderNavigationItemList({
  folderId,
  depth,
}: EditableFolderNavigationItemListProps) {
  const { data: folderRecord } = useFetchFolders();
  const childFolderList = getChildFolderListByParentFolderId({
    folderRecord,
    folderId,
  });
  const childDepth = typeof depth === 'number' ? depth + 1 : 0;

  return (
    <UpdateFolderStatusProvider>
      <ShareFolderStatusProvider>
        <DeleteFolderStatusProvider>
          <CreateFolderStatusProvider>
            {depth !== 'root' && <EditableFolderItem folderId={folderId} />}
            <CreateChildFolderInput
              parentFolderId={folderId}
              depth={childDepth}
            />
            <SortableContext
              id={`${folderId}`}
              items={childFolderList.map((childFolder) =>
                getFolderSortableContextId(childFolder.id),
              )}
              strategy={verticalListSortingStrategy}
            >
              {childFolderList.map((childFolder) => {
                return (
                  <EditableFolderNavigationItemList
                    key={childFolder.id}
                    folderId={childFolder.id}
                    depth={childDepth}
                  />
                );
              })}
            </SortableContext>
          </CreateFolderStatusProvider>
        </DeleteFolderStatusProvider>
      </ShareFolderStatusProvider>
    </UpdateFolderStatusProvider>
  );
}

interface EditableFolderNavigationItemListProps {
  folderId: FolderIdType;
  depth: number | 'root';
}
