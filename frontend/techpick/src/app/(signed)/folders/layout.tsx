import type { PropsWithChildren } from 'react';
import { FolderTree, FolderAndPickDndContextProvider } from '@/components';
import { CurrentPathIndicator } from '@/components/FolderPathIndicator/CurrentPathIndicator';
import { SearchWidget } from '@/components/SearchWidget/SearchWidget';
import {
  pageContainerLayout,
  ListViewerLayout,
  ListViewerHeaderLayout,
  ListViewerHeaderMainLayout,
  ListViewerHeaderSubLayout,
} from './layout.css';

export default function FolderLayout({ children }: PropsWithChildren) {
  return (
    <div className={pageContainerLayout}>
      <FolderAndPickDndContextProvider>
        <FolderTree />
        <div className={ListViewerLayout}>
          <div className={ListViewerHeaderLayout}>
            <div className={ListViewerHeaderMainLayout}>
              <SearchWidget />
            </div>
            <div className={ListViewerHeaderSubLayout}>
              <CurrentPathIndicator />
            </div>
          </div>
          {children}
        </div>
      </FolderAndPickDndContextProvider>
    </div>
  );
}
