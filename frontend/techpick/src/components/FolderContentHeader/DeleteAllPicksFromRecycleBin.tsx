'use client';

import { useDisclosure } from '@/hooks/useDisclosure';
import { useDeleteAllPicksFromRecycleBin } from '@/queries/useDeleteAllPicksFromRecycleBin';
import { dialogOverlayStyle } from '@/styles/dialogStyle.css';
import * as Dialog from '@radix-ui/react-dialog';
import * as VisuallyHidden from '@radix-ui/react-visually-hidden';
import { XIcon } from 'lucide-react';
import {
  dialogContentStyle,
  redWideButtonStyle,
  sendWideButtonStyle,
} from './deleteAllPicksFromRecycleBin.css';
import { deleteAllPicksFormRecycleBunButtonStyle } from './folderContentHeaderButton.css';

export function DeleteAllPicksFromRecycleBin() {
  const { mutate: deleteAllPicks } = useDeleteAllPicksFromRecycleBin();
  const { isOpen, onClose, setIsOpen } = useDisclosure();

  const onHandleClick = () => {
    deleteAllPicks();
    onClose();
  };

  return (
    <Dialog.Root open={isOpen} onOpenChange={setIsOpen}>
      <Dialog.Trigger asChild>
        <button
          className={deleteAllPicksFormRecycleBunButtonStyle}
          type="button"
        >
          <XIcon size={12} />
          <span>전체 삭제하기</span>
        </button>
      </Dialog.Trigger>
      <Dialog.Overlay className={dialogOverlayStyle} />
      <Dialog.Portal>
        <Dialog.Content className={dialogContentStyle}>
          <VisuallyHidden.Root>
            <Dialog.Title>Delete All Picks</Dialog.Title>
            <Dialog.Description className="DialogDescription">
              delete all picks from recycle-bin
            </Dialog.Description>
          </VisuallyHidden.Root>
          <p>정말 삭제하시겠습니까?</p>
          <button
            type="button"
            className={redWideButtonStyle}
            onClick={onHandleClick}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                onHandleClick();
              }
            }}
          >
            삭제하기
          </button>
          <Dialog.Close asChild>
            <button type="button" className={sendWideButtonStyle}>
              취소
            </button>
          </Dialog.Close>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}
