import { useTagStore } from '@/stores/tagStore';
import { forwardRef, useRef, useState } from 'react';
import { SelectedTagItem } from './SelectedTagItem';
import { SelectedTagListLayout } from './SelectedTagListLayout';
import { TagAutocompleteDialog } from './TagAutocompleteDialog';
import {
  tagDialogTriggerLayout,
  tagPickerLayout,
  tagPickerPlaceholderStyle,
} from './TagPicker.css';

export const TagPicker = forwardRef<HTMLDivElement>(
  function TagPickerWithRef(_props, tabFocusRef) {
    const [open, setOpen] = useState(false);
    const tagInputContainerRef = useRef<HTMLDivElement>(null);
    const { selectedTagList } = useTagStore();

    const openDialog = () => {
      setOpen(true);
    };

    const onEnterKeyDown = (e: React.KeyboardEvent<HTMLDivElement>) => {
      if (e.key !== 'Enter') {
        return;
      }

      openDialog();
      e.preventDefault();
    };

    return (
      <div ref={tagInputContainerRef} className={tagPickerLayout}>
        <div
          className={tagDialogTriggerLayout}
          onClick={openDialog}
          onKeyDown={onEnterKeyDown}
          // biome-ignore lint/a11y/noNoninteractiveTabindex: <explanation>
          tabIndex={0}
          ref={tabFocusRef}
        >
          {selectedTagList.length === 0 && (
            <p className={tagPickerPlaceholderStyle}>태그를 넣어주세요</p>
          )}
          <SelectedTagListLayout height="fixed">
            {selectedTagList.map((tag) => (
              <SelectedTagItem
                key={tag.name}
                name={tag.name}
                colorNumber={tag.colorNumber}
              />
            ))}
          </SelectedTagListLayout>
        </div>

        <TagAutocompleteDialog
          open={open}
          onOpenChange={setOpen}
          container={tagInputContainerRef}
        />
      </div>
    );
  },
);
