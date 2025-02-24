import { NON_EXISTENT_TAG_ID } from '@/constants/nonExistentTagId';
import {
  convertToFuzzyItems,
  useFuzzyFilter,
} from '@/libs/@fuzzyFilter/useFuzzyFilter';
import { notifyError } from '@/libs/@toast/notifyError';
import { useTagStore } from '@/stores/tagStore';
import type { TagType } from '@/types/TagType';
import { getTagSortableContextId } from '@/utils/getTagSortableContextId';
import {
  SortableContext,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import * as Dialog from '@radix-ui/react-dialog';
import * as VisuallyHidden from '@radix-ui/react-visually-hidden';
import { Command } from 'cmdk';
import { GripVerticalIcon } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import type { KeyboardEvent } from 'react';
import { BarLoader } from 'react-spinners';
import { colorVars } from 'techpick-shared';
import { DeleteTagDialog } from './DeleteTagDialog';
import { DeselectTagButton } from './DeselectTagButton';
import { SelectedTagItem } from './SelectedTagItem';
import { SelectedTagListLayout } from './SelectedTagListLayout';
import {
  alignItemCenter,
  commandInputStyle,
  createTagListItemStyle,
  displayFlex,
  dragHandlerStyle,
  tagCreateTextStyle,
  tagDialogPortalLayout,
  tagListItemStyle,
  tagListLoadingStyle,
  tagListStyle,
} from './TagAutocompleteDialog.css';
import { overlayStyle } from './TagAutocompleteDialog.css';
import { getRandomInt } from './TagAutocompleteDialog.lib';
import { TagDndContext } from './TagDndContext';
import { TagInfoEditPopoverButton } from './TagInfoEditPopoverButton';
import { TagSortableDraggable } from './TagSortableDraggable';

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
  const tagListWithOrder = tagList.map((tag, index) => ({ ...tag, index }));
  const fuzzyFilter = useFuzzyFilter();
  const fuzzyTagList = convertToFuzzyItems({
    items: tagListWithOrder,
    getId: (tag) => tag.id ?? 0,
    getValue: (tag) => tag.name ?? '',
    getKeywords: (tag) => [tag.name ?? ''],
  });
  const filteredTagList = fuzzyFilter(fuzzyTagList, tagInputValue);
  const sortableContextIdList = canCreateTag
    ? [
        ...filteredTagList.map((tag) => getTagSortableContextId(tag.id)),
        `tag-${NON_EXISTENT_TAG_ID}`,
      ]
    : filteredTagList.map((tag) => getTagSortableContextId(tag.id));
  const createTagOrder =
    filteredTagList[filteredTagList.length - 1]?.index ?? NON_EXISTENT_TAG_ID;

  const focusTagInput = () => {
    if (tagInputRef.current) {
      tagInputRef.current.focus();
      tagInputRef.current.scrollIntoView({
        behavior: 'smooth',
        block: 'start',
      });
    }
  };

  const updateCanCreateTag = (value: string) => {
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

  // biome-ignore lint/correctness/useExhaustiveDependencies: <explanation>
  useEffect(
    function checkUpdateCanCreateTag() {
      updateCanCreateTag(tagInputValue);
    },
    [tagList],
  );

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

          <Command shouldFilter={false}>
            {/**선택한 태그 리스트 */}
            <SelectedTagListLayout
              ref={selectedTagListRef}
              focusStyle="focus"
              height="fixed"
            >
              {selectedTagList.map((tag) => (
                <SelectedTagItem
                  key={tag.id}
                  name={tag.name}
                  colorNumber={tag.colorNumber}
                >
                  <DeselectTagButton tag={tag} onClick={focusTagInput} />
                </SelectedTagItem>
              ))}

              <Command.Input
                className={commandInputStyle}
                ref={tagInputRef}
                value={tagInputValue}
                onValueChange={(value) => {
                  updateCanCreateTag(value);
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

              <TagDndContext>
                <SortableContext
                  items={sortableContextIdList}
                  strategy={verticalListSortingStrategy}
                >
                  {filteredTagList.map((tag) => (
                    <TagSortableDraggable
                      key={tag.id}
                      tagId={tag.id}
                      tagOrder={tag.index}
                    >
                      <Command.Item
                        key={tag.id}
                        className={tagListItemStyle}
                        onSelect={() => onSelectTag(tag)}
                        keywords={[tag.name]}
                      >
                        <div className={`${displayFlex} ${alignItemCenter}`}>
                          <div className={dragHandlerStyle}>
                            <GripVerticalIcon size={16} />
                          </div>
                          <SelectedTagItem
                            key={tag.id}
                            name={tag.name}
                            colorNumber={tag.colorNumber}
                          />
                        </div>
                        <TagInfoEditPopoverButton tag={tag} />
                      </Command.Item>
                    </TagSortableDraggable>
                  ))}

                  {canCreateTag && (
                    <TagSortableDraggable
                      tagId={NON_EXISTENT_TAG_ID}
                      tagOrder={createTagOrder}
                    >
                      <Command.Item
                        className={createTagListItemStyle}
                        onSelect={onSelectCreatableTag}
                      >
                        <span className={tagCreateTextStyle}>생성</span>
                        <SelectedTagItem
                          name={tagInputValue}
                          colorNumber={randomNumber.current}
                        />
                      </Command.Item>
                    </TagSortableDraggable>
                  )}
                </SortableContext>
              </TagDndContext>
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
