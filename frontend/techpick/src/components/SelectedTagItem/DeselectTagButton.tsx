'use client';
import { useUpdatePickInfo } from '@/queries/useUpdatePickInfo';
import type { PickInfoType } from '@/types/PickInfoType';
import type { TagType } from '@/types/TagType';
import { X } from 'lucide-react';
import { DeselectTagButtonStyle } from './DeselectTagButton.css';

export function DeselectTagButton({
  tag,
  pickInfo,
  selectedTagList,
  onClick = () => {},
}: DeselectTagButtonProps) {
  const tagIdOrderedList = selectedTagList.map((tag) => tag.id);

  const { mutate: updatePickInfo } = useUpdatePickInfo();

  return (
    <button
      type="button"
      className={DeselectTagButtonStyle}
      onClick={() => {
        onClick();

        if (!tagIdOrderedList.includes(tag.id)) {
          return;
        }

        updatePickInfo({
          pickParentFolderId: pickInfo.parentFolderId,
          updatePickInfo: {
            id: pickInfo.id,
            tagIdOrderedList: tagIdOrderedList.filter(
              (tagId) => tagId !== tag.id,
            ),
          },
        });
      }}
    >
      <X size={8} />
    </button>
  );
}

interface DeselectTagButtonProps {
  tag: TagType;
  pickInfo: PickInfoType;
  selectedTagList: TagType[];
  onClick?: () => void;
}
