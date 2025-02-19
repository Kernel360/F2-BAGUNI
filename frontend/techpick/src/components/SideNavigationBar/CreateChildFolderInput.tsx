'use client';

import { useCreateFolder } from '@/queries/useCreateFolder';
import type { FolderIdType } from '@/types/FolderIdType';
import { isEmptyString } from '@/utils/isEmptyString';
import { FolderPlus } from 'lucide-react';
import {
  type KeyboardEvent,
  useCallback,
  useContext,
  useEffect,
  useRef,
} from 'react';
import { CreateChildFolderStatusContext } from './CreateFolderStatusProvider';
import {
  inputStyle,
  labelStyle,
  updateFolderNameInputLayoutStyle,
} from './updateFolderNameInput.css';

export function CreateChildFolderInput({
  parentFolderId,
}: CreateFolderInputProps) {
  const { isCreateChildFolderOpen, onCloseCreateFolderInput } = useContext(
    CreateChildFolderStatusContext,
  );
  const { mutateAsync: createFolder } = useCreateFolder();
  const containerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const onSubmit = useCallback(() => {
    const folderName = inputRef.current?.value.trim() ?? '';

    if (isEmptyString(folderName)) {
      return;
    }

    createFolder({ name: folderName, parentFolderId });
    onCloseCreateFolderInput();
  }, [parentFolderId, onCloseCreateFolderInput, createFolder]);

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
          onCloseCreateFolderInput();
        }
      };

      document.addEventListener('mousedown', handleClickOutside);
      return () => {
        document.removeEventListener('mousedown', handleClickOutside);
      };
    },
    [onSubmit, onCloseCreateFolderInput],
  );

  if (!isCreateChildFolderOpen) {
    return null;
  }

  return (
    <div ref={containerRef} className={updateFolderNameInputLayoutStyle}>
      <label htmlFor={`folderInput-${parentFolderId}`} className={labelStyle}>
        <FolderPlus size={16} />
      </label>
      <input
        id={`folderInput-${parentFolderId}`}
        type="text"
        ref={inputRef}
        className={inputStyle}
        onKeyDown={onEnter}
      />
    </div>
  );
}

interface CreateFolderInputProps {
  parentFolderId: FolderIdType;
  depth: number;
}
