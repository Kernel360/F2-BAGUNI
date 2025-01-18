import { useTagStore } from '@/stores/tagStore';
import type { TagType } from '@/types/TagType';
import { X } from 'lucide-react';
import { DeselectTagButtonStyle } from './DeselectTagButton.css';

export function DeselectTagButton({
  tag,
  onClick = () => {},
}: DeselectTagButtonProps) {
  const { deselectTag } = useTagStore();

  return (
    <button
      type="button"
      className={DeselectTagButtonStyle}
      onClick={() => {
        deselectTag(tag.id);
        onClick();
      }}
      tabIndex={-1}
    >
      <X size={8} />
    </button>
  );
}

interface DeselectTagButtonProps {
  tag: TagType;
  onClick?: () => void;
}
