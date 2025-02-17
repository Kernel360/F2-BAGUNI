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

/**
 * CurrentPathIndicator는 휴지통과 미분류에서는 보이지 않습니다.
 */
export function CurrentPathIndicator({
  folderInfo,
}: CurrentPathIndicatorProps) {
  const { data: folderRecord } = useFetchFolders();
  const ancestorFolderList = getAncestorFolderListFromLeaf({
    leaf: folderInfo,
    folderRecord: folderRecord,
  });

  return (
    <div>
      <Breadcrumb>
        <BreadcrumbList>
          {ancestorFolderList.map((folderInfo, index) => {
            if (
              folderInfo.folderType === 'GENERAL' ||
              folderInfo.folderType === 'ROOT'
            ) {
              return (
                <div key={folderInfo.id} className={breadcrumbItemLayout}>
                  {index !== 0 && <BreadcrumbSeparator />}
                  <BreadcrumbItem className={breadcrumbItemStyle}>
                    {folderInfo.folderType === 'GENERAL' && (
                      <BreadcrumbLink className={breadcrumbLinkStyle} asChild>
                        <Link href={ROUTES.FOLDER_DETAIL(folderInfo.id)}>
                          {folderInfo.name}
                        </Link>
                      </BreadcrumbLink>
                    )}
                    {folderInfo.folderType === 'ROOT' && '내 폴더'}
                  </BreadcrumbItem>
                </div>
              );
            }

            return null;
          })}
        </BreadcrumbList>
      </Breadcrumb>
    </div>
  );
}

interface CurrentPathIndicatorProps {
  folderInfo: FolderType | null | undefined;
}
