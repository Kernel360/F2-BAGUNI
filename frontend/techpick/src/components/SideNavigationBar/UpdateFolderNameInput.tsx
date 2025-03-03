'use client';

import { useFetchFolders } from '@/queries/useFetchFolders';
import { useUpdateFolderName } from '@/queries/useUpdateFolderName';
import type { FolderIdType } from '@/types/FolderIdType';
import { getFolderInfoByFolderId } from '@/utils/getFolderInfoByFolderId';
import { isEmptyString } from '@/utils/isEmptyString';
import { FolderPlus } from 'lucide-react';
import { useCallback, useContext, useEffect, useRef } from 'react';
import type { KeyboardEvent } from 'react';
import { UpdateFolderStatusContext } from './UpdateFolderStatusProvider';
import {
  inputStyle,
  labelStyle,
  updateFolderNameInputLayoutStyle,
} from './updateFolderNameInput.css';

/**
 *
 * @param folderId 이름을 변경할 폴더 id
 */
export function UpdateFolderNameInput({
  folderId,
}: UpdateFolderNameInputProps) {
  const { onCloseUpdateFolderInput } = useContext(UpdateFolderStatusContext);
  const { mutate: updateFolderName } = useUpdateFolderName();
  const { data: folderRecord } = useFetchFolders();
  const folderInfo = getFolderInfoByFolderId({ folderId, folderRecord });
  const containerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const onSubmit = useCallback(() => {
    const folderName = inputRef.current?.value.trim() ?? '';

    if (isEmptyString(folderName)) {
      return;
    }

    updateFolderName({ id: folderId, name: folderName });
    onCloseUpdateFolderInput();
  }, [folderId, onCloseUpdateFolderInput, updateFolderName]);

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
        }
      };

      document.addEventListener('mousedown', handleClickOutside);
      return () => {
        document.removeEventListener('mousedown', handleClickOutside);
      };
    },
    [onSubmit],
  );

  useEffect(
    function initializeFolderInput() {
      if (inputRef.current) {
        inputRef.current.value = folderInfo?.name ?? '';

        // 타이밍 이슈 탓으로 인해 setTimeout 사용
        setTimeout(() => inputRef.current?.focus(), 0);
      }
    },
    [folderInfo?.name],
  );

  return (
    <div ref={containerRef} className={updateFolderNameInputLayoutStyle}>
      <label htmlFor={`folderInput-${folderId}`} className={labelStyle}>
        <FolderPlus size={16} />
      </label>
      <input
        id={`folderInput-${folderId}`}
        type="text"
        ref={inputRef}
        className={inputStyle}
        onKeyDown={onEnter}
      />
    </div>
  );
}

interface UpdateFolderNameInputProps {
  folderId: FolderIdType;
}
