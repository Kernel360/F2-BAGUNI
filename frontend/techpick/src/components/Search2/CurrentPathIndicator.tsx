'use client';
import { ROUTES } from '@/constants/route';
import { useFetchFolders } from '@/queries/useFetchFolders';
import type { FolderType } from '@/types/FolderType';
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbSeparator,
} from '@/ui/Breadcrumb';
import { getAncestorFolderListFromLeaf } from '@/utils/getAncestorFolderListFromLeaf';
import Link from 'next/link';
import {
  breadcrumbItemLayout,
  breadcrumbItemStyle,
  breadcrumbLinkStyle,
} from './currentPathIndicator.css';

export function CurrentPathIndicator({
  folderInfo,
  onClose,
}: CurrentPathIndicatorProps) {
  const { data: folderRecord } = useFetchFolders();
  let ancestorFolderList = getAncestorFolderListFromLeaf({
    leaf: folderInfo,
    folderRecord: folderRecord,
  });

  if (ancestorFolderList.length === 0 && folderInfo) {
    ancestorFolderList = [folderInfo];
  }

  return (
    <div>
      <Breadcrumb>
        <BreadcrumbList>
          {ancestorFolderList.map((folderInfo, index) => {
            return (
              <div key={folderInfo.id} className={breadcrumbItemLayout}>
                {index !== 0 && <BreadcrumbSeparator />}
                <BreadcrumbItem className={breadcrumbItemStyle}>
                  {folderInfo.folderType === 'ROOT' ? (
                    '내 폴더'
                  ) : (
                    <BreadcrumbLink className={breadcrumbLinkStyle} asChild>
                      <Link
                        href={ROUTES.FOLDER_DETAIL(folderInfo.id)}
                        onClick={(e) => {
                          e.stopPropagation();
                          onClose();
                        }}
                      >
                        {folderInfo.name}
                      </Link>
                    </BreadcrumbLink>
                  )}
                </BreadcrumbItem>
              </div>
            );
          })}
        </BreadcrumbList>
      </Breadcrumb>
    </div>
  );
}

interface CurrentPathIndicatorProps {
  folderInfo: FolderType | null | undefined;
  onClose: () => void;
}
