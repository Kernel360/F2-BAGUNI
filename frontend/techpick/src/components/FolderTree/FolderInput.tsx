import { useEffect, useRef } from 'react';
import type { KeyboardEvent } from 'react';
import { FolderPlus } from 'lucide-react';
import { isEmptyString } from '@/utils';
import { folderInputLayout, inputStyle, labelStyle } from './folderInput.css';

export function FolderInput({
  onSubmit,
  onClickOutSide = () => {},
  initialValue = '',
}: FolderInputProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const submitIfNotEmptyString = () => {
    const folderName = inputRef.current?.value.trim() ?? '';
    if (isEmptyString(folderName)) return;

    onSubmit(folderName);
  };

  const onEnter = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter') {
      submitIfNotEmptyString();
    }
  };

  useEffect(function detectOutsideClick() {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        containerRef.current &&
        !containerRef.current.contains(event.target as Node)
      ) {
        submitIfNotEmptyString();
        onClickOutSide();
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  useEffect(function initializeFolderInput() {
    if (inputRef.current) {
      inputRef.current.focus();
      inputRef.current.value = initialValue;
    }
  }, []);

  return (
    <div ref={containerRef} className={folderInputLayout}>
      <label htmlFor="folderInput" className={labelStyle}>
        <FolderPlus size={24} />
      </label>
      <input
        id="folderInput"
        type="text"
        ref={inputRef}
        className={inputStyle}
        onKeyDown={onEnter}
      />
    </div>
  );
}

interface FolderInputProps {
  onSubmit: (value: string) => void;
  onClickOutSide?: () => void;
  initialValue?: string;
}
