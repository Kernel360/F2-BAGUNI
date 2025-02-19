import { useCreateFolder } from '@/queries/useCreateFolder';
import type { FolderIdType } from '@/types/FolderIdType';
import { isEmptyString } from '@/utils/isEmptyString';
import { FolderPlus } from 'lucide-react';
import { type KeyboardEvent, useCallback, useEffect, useRef } from 'react';
import {
  inputStyle,
  labelStyle,
  updateFolderNameInputLayoutStyle,
} from './updateFolderNameInput.css';

export function CreateRootChildFolderInput({
  rootFolderId,
  onClose,
}: CreateRootChildFolderInputProps) {
  const { mutateAsync: createFolder } = useCreateFolder();
  const containerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const onSubmit = useCallback(() => {
    const folderName = inputRef.current?.value.trim() ?? '';

    if (isEmptyString(folderName)) {
      return;
    }

    createFolder({ name: folderName, parentFolderId: rootFolderId });
    onClose();
  }, [rootFolderId, onClose, createFolder]);

  const onEnter = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter') {
      onSubmit();
    }
  };

  useEffect(
    function detectOutsideClick() {
      const handleClickOutside = (event: MouseEvent) => {
        if (
          containerRef.current &&
          !containerRef.current.contains(event.target as Node)
        ) {
          onSubmit();
          onClose();
        }
      };

      document.addEventListener('mousedown', handleClickOutside);
      return () => {
        document.removeEventListener('mousedown', handleClickOutside);
      };
    },
    [onSubmit, onClose],
  );

  useEffect(() => {
    if (inputRef.current) {
      inputRef.current.focus();
    }
  }, []);

  return (
    <div ref={containerRef} className={updateFolderNameInputLayoutStyle}>
      <label htmlFor={`folderInput-${rootFolderId}`} className={labelStyle}>
        <FolderPlus size={16} />
      </label>
      <input
        id={`folderInput-${rootFolderId}`}
        type="text"
        ref={inputRef}
        className={inputStyle}
        onKeyDown={onEnter}
      />
    </div>
  );
}

interface CreateRootChildFolderInputProps {
  rootFolderId: FolderIdType;
  onClose: () => void;
}
