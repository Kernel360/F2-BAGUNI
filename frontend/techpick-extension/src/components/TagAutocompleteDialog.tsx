import { notifyError } from '@/libs/@toast/notifyError';
import { useTagStore } from '@/stores/tagStore';
import { useThemeStore } from '@/stores/themeStore';
import type { TagType } from '@/types/TagType';
import { numberToRandomColor } from '@/utils/numberToRandomColor';
import * as Dialog from '@radix-ui/react-dialog';
import * as VisuallyHidden from '@radix-ui/react-visually-hidden';
import { Command } from 'cmdk';
import { useEffect, useRef, useState } from 'react';
import type { KeyboardEvent } from 'react';
import { BarLoader } from 'react-spinners';
import { colorVars } from 'techpick-shared';
import { DeleteTagDialog } from './DeleteTagDialog';
import { DeselectTagButton } from './DeselectTagButton';
import { SelectedTagItem } from './SelectedTagItem';
import { SelectedTagListLayout } from './SelectedTagListLayout';
import {
  commandInputStyle,
  tagCreateTextStyle,
  tagDialogPortalLayout,
  tagListItemContentStyle,
  tagListItemStyle,
  tagListLoadingStyle,
  tagListStyle,
} from './TagAutocompleteDialog.css';
import { overlayStyle } from './TagAutocompleteDialog.css';
import {
  CREATABLE_TAG_KEYWORD,
  filterCommandItems,
  getRandomInt,
} from './TagAutocompleteDialog.lib';
import { TagInfoEditPopoverButton } from './TagInfoEditPopoverButton';

export function TagAutocompleteDialog({
  open,
  onOpenChange,
  container,
}: TagSelectionDialogProps) {
  const [tagInputValue, setTagInputValue] = useState('');
  const [canCreateTag, setCanCreateTag] = useState(false);

  const tagInputRef = useRef<HTMLInputElement | null>(null);
  const selectedTagListRef = useRef<HTMLDivElement | null>(null);
  const isCreateFetchPendingRef = useRef<boolean>(false);
  const randomNumber = useRef<number>(getRandomInt());

  const {
    tagList,
    selectedTagList,
    fetchingTagState,
    selectTag,
    createTag,
    popSelectedTag,
  } = useTagStore();
  const { isDarkMode } = useThemeStore();

  const focusTagInput = () => {
    if (tagInputRef.current) {
      tagInputRef.current.focus();
      tagInputRef.current.scrollIntoView({
        behavior: 'smooth',
        block: 'start',
      });
    }
  };

  const checkIsCreatableTag = (value: string) => {
    const isUnique = !tagList.some((tag) => tag.name === value);
    const isNotInitialValue = value.trim() !== '';
    const isCreatable = isUnique && isNotInitialValue;

    setCanCreateTag(isCreatable);
  };

  const clearTagInputValue = () => {
    setTagInputValue('');
    setCanCreateTag(false);
  };

  const onSelectTag = (tag: TagType) => {
    selectTag(tag);
    clearTagInputValue();
    requestAnimationFrame(() => {
      focusTagInput();
    });
  };

  const onSelectCreatableTag = async () => {
    if (isCreateFetchPendingRef.current) {
      return;
    }

    try {
      isCreateFetchPendingRef.current = true;

      const newTag = await createTag({
        name: tagInputValue.trim(),
        colorNumber: randomNumber.current,
      });
      randomNumber.current = getRandomInt();

      if (newTag) {
        onSelectTag(newTag);
      }
    } catch (error) {
      if (error instanceof Error) {
        notifyError(error.message);
      }
    } finally {
      isCreateFetchPendingRef.current = false;
    }
  };

  const onBackspaceKeyPress = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Backspace' && tagInputValue === '' && !event.shiftKey) {
      popSelectedTag();
    }
  };

  // biome-ignore lint/correctness/useExhaustiveDependencies: <explanation>
  useEffect(() => {
    if (open) {
      requestAnimationFrame(() => {
        focusTagInput();
      });
    }
  }, [open]);

  return (
    <Dialog.Root open={open} onOpenChange={onOpenChange} modal={false}>
      <Dialog.Portal container={container?.current ?? undefined}>
        <Dialog.Overlay className={overlayStyle} />
        <Dialog.Content className={tagDialogPortalLayout}>
          <VisuallyHidden.Root>
            <Dialog.Title>태그 입력창</Dialog.Title>
            <Dialog.Description>
              북마크에 어울리는 태그를 입력해주세요
            </Dialog.Description>
          </VisuallyHidden.Root>

          <Command filter={filterCommandItems}>
            {/**선택한 태그 리스트 */}
            <SelectedTagListLayout
              ref={selectedTagListRef}
              focusStyle="focus"
              height="fixed"
            >
              {selectedTagList.map((tag) => (
                <SelectedTagItem key={tag.id} tag={tag}>
                  <DeselectTagButton tag={tag} onClick={focusTagInput} />
                </SelectedTagItem>
              ))}

              <Command.Input
                className={commandInputStyle}
                ref={tagInputRef}
                value={tagInputValue}
                onValueChange={(value) => {
                  checkIsCreatableTag(value);
                  setTagInputValue(value);
                }}
                onKeyDown={onBackspaceKeyPress}
              />
            </SelectedTagListLayout>

            {/**전체 태그 리스트 */}
            <Command.List className={tagListStyle}>
              {fetchingTagState.isPending && (
                <Command.Loading className={tagListLoadingStyle}>
                  <BarLoader color={colorVars.color.font} />
                </Command.Loading>
              )}

              {(!fetchingTagState.isPending || tagInputValue.trim()) !== '' && (
                <Command.Empty className={tagListItemStyle}>
                  태그를 만들어보세요!
                </Command.Empty>
              )}

              {tagList.map((tag) => (
                <Command.Item
                  key={tag.id}
                  className={tagListItemStyle}
                  onSelect={() => onSelectTag(tag)}
                  keywords={[tag.name]}
                >
                  <SelectedTagItem key={tag.id} tag={tag} />
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
                        isDarkMode ? 'dark' : 'light',
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
          </Command>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}

interface TagSelectionDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  container?: React.RefObject<HTMLElement>;
}
