'use client';

import type { PropsWithChildren } from 'react';
import * as ContextMenu from '@radix-ui/react-context-menu';
import { FolderPen, FolderX, ScreenShare } from 'lucide-react';
import { getPortalContainer } from '@/utils';
import {
  contextMenuContentLayout,
  contextMenuItemStyle,
} from './folderContextMenu.css';

export function FolderContextMenu({
  showRenameInput,
  deleteFolder,
  shareFolderById,
  onShow = () => {},
  children,
}: PropsWithChildren<FolderContextMenuProps>) {
  const portalContainer = getPortalContainer();

  return (
    <ContextMenu.Root
      onOpenChange={(open) => {
        if (open) {
          onShow();
        }

        return;
      }}
    >
      <ContextMenu.Trigger>{children}</ContextMenu.Trigger>
      <ContextMenu.Portal container={portalContainer}>
        <ContextMenu.Content className={contextMenuContentLayout}>
          <ContextMenu.Item
            onSelect={showRenameInput}
            className={contextMenuItemStyle}
          >
            <FolderPen />
            <p>폴더명 변경</p>
          </ContextMenu.Item>
          <ContextMenu.Item
            className={contextMenuItemStyle}
            onSelect={deleteFolder}
          >
            <FolderX />
            <p>휴지통으로 이동</p>
          </ContextMenu.Item>
          <ContextMenu.Item
            className={contextMenuItemStyle}
            onSelect={shareFolderById}
          >
            <ScreenShare />
            <p>공유하기</p>
          </ContextMenu.Item>
        </ContextMenu.Content>
      </ContextMenu.Portal>
    </ContextMenu.Root>
  );
}

interface FolderContextMenuProps {
  showRenameInput: () => void;
  deleteFolder: () => void;
  shareFolderById: () => void;
  onShow?: () => void;
}
