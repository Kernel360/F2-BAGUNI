import { useEffect, useRef, useState } from 'react';
import { Command } from 'cmdk';
import { BarLoader } from 'react-spinners';
import { color } from 'techpick-shared';
import { notifyError, numberToRandomColor, useThemeStore } from '@/shared';
import {
  SelectedTagItem,
  SelectedTagListLayout,
  useTagStore,
  tagTypes,
} from '@/entities/tag';
import { DeleteTagDialog, DeselectTagButton } from '@/features/tag';
import { TagInfoEditPopoverButton } from '@/widgets/TagPicker/TagInfoEditPopoverButton';
import { useCalculateCommandListHeight } from './useCalculateCommandListHeight';
import {
  filterCommandItems,
  CREATABLE_TAG_KEYWORD,
  getRandomInt,
} from './TagAutocompleteDialog.lib';
import {
  tagDialogPortalLayout,
  commandInputStyle,
  tagListItemStyle,
  tagListItemContentStyle,
  tagCreateTextStyle,
  tagListStyle,
  tagListLoadingStyle,
} from './TagAutocompleteDialog.css';

export function TagAutocompleteDialog({
  open,
  onOpenChange,
  container,
}: TagSelectionDialogProps) {
  const [tagInputValue, setTagInputValue] = useState('');
  const [canCreateTag, setCanCreateTag] = useState(false);
  const tagInputRef = useRef<HTMLInputElement | null>(null);
  const selectedTagListRef = useRef<HTMLDivElement | null>(null);
  const randomNumber = useRef<number>(getRandomInt());
  const {
    tagList,
    selectedTagList,
    fetchingTagState,
    selectTag,
    fetchingTagList,
    createTag,
  } = useTagStore();
  const { commandListHeight } =
    useCalculateCommandListHeight(selectedTagListRef);
  const { isDarkMode } = useThemeStore();

  const focusTagInput = () => {
    tagInputRef.current?.focus();
  };

  const clearTagInputValue = () => {
    setTagInputValue('');
  };

  const onSelectTag = (tag: tagTypes.TagType) => {
    selectTag(tag);
    focusTagInput();
    clearTagInputValue();
  };

  const onSelectCreatableTag = async () => {
    try {
      const newTag = await createTag({
        tagName: tagInputValue,
        colorNumber: randomNumber.current,
      });
      randomNumber.current = getRandomInt();
      onSelectTag(newTag!);
    } catch (error) {
      if (error instanceof Error) {
        notifyError(error.message);
      }
    }
  };

  useEffect(
    function fetchTagList() {
      fetchingTagList();
    },
    [fetchingTagList]
  );

  useEffect(
    function checkIsCreatableTag() {
      const isUnique = !tagList.some((tag) => tag.tagName === tagInputValue);
      const isNotInitialValue = tagInputValue.trim() !== '';
      const isCreatable = isUnique && isNotInitialValue;

      setCanCreateTag(isCreatable);
    },
    [tagInputValue, tagList]
  );

  return (
    <Command.Dialog
      open={open}
      onOpenChange={onOpenChange}
      container={container?.current ?? undefined}
      className={tagDialogPortalLayout}
      filter={filterCommandItems}
    >
      {/**선택한 태그 리스트 */}
      <SelectedTagListLayout ref={selectedTagListRef} focusStyle="focus">
        {selectedTagList.map((tag) => (
          <SelectedTagItem key={tag.tagId} tag={tag}>
            <DeselectTagButton tag={tag} onClick={focusTagInput} />
          </SelectedTagItem>
        ))}

        <Command.Input
          className={commandInputStyle}
          ref={tagInputRef}
          value={tagInputValue}
          onValueChange={setTagInputValue}
        />
      </SelectedTagListLayout>

      {/**전체 태그 리스트 */}
      <Command.List
        className={tagListStyle}
        style={{ maxHeight: commandListHeight }}
      >
        {fetchingTagState.isPending && (
          <Command.Loading className={tagListLoadingStyle}>
            <BarLoader color={color.font} />
          </Command.Loading>
        )}

        {(!fetchingTagState.isPending || tagInputValue.trim()) !== '' && (
          <Command.Empty className={tagListItemStyle}>
            태그를 만들어보세요!
          </Command.Empty>
        )}

        {tagList.map((tag) => (
          <Command.Item
            key={tag.tagId}
            className={tagListItemStyle}
            onSelect={() => onSelectTag(tag)}
            keywords={[tag.tagName]}
          >
            <SelectedTagItem key={tag.tagId} tag={tag} />
            <TagInfoEditPopoverButton tag={tag} />
          </Command.Item>
        ))}

        {canCreateTag && (
          <Command.Item
            className={tagListItemStyle}
            value={tagInputValue}
            keywords={[CREATABLE_TAG_KEYWORD]}
            onSelect={onSelectCreatableTag}
            disabled={!canCreateTag}
          >
            <span
              className={tagListItemContentStyle}
              style={{
                backgroundColor: numberToRandomColor(
                  randomNumber.current,
                  isDarkMode ? 'dark' : 'light'
                ),
              }}
            >
              {tagInputValue}
            </span>
            <span className={tagCreateTextStyle}>생성</span>
          </Command.Item>
        )}
      </Command.List>

      {/**DeleteTagDialog를 닫고도 Command.Dialog가 켜져있기위해서 Command.Dialog 내부에 있어야합니다.*/}
      <DeleteTagDialog />
    </Command.Dialog>
  );
}

interface TagSelectionDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  container?: React.RefObject<HTMLElement>;
}
