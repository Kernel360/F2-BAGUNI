import { forwardRef, useRef, useState } from 'react';
import {
  useTagStore,
  SelectedTagItem,
  SelectedTagListLayout,
} from '@/entities/tag';
import { TagAutocompleteDialog } from './TagAutocompleteDialog';
import { tagPickerLayout, tagDialogTriggerLayout } from './TagPicker.css';

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
    };

    return (
      <div ref={tagInputContainerRef} className={tagPickerLayout}>
        <div
          className={tagDialogTriggerLayout}
          onClick={openDialog}
          onKeyDown={onEnterKeyDown}
          tabIndex={0}
          ref={tabFocusRef}
        >
          <SelectedTagListLayout>
            {selectedTagList.map((tag) => (
              <SelectedTagItem key={tag.id} tag={tag} />
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
  }
);
