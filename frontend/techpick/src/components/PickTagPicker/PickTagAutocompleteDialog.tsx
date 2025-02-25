import { useCreateTag } from '@/queries/useCreateTag';
import { useFetchTagList } from '@/queries/useFetchTagList';
import { useUpdatePickInfo } from '@/queries/useUpdatePickInfo';
import { useUpdatePickStore } from '@/stores/updatePickStore';
import type { PickInfoType } from '@/types/PickInfoType';
import type { TagType } from '@/types/TagType';
import {
  SortableContext,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import * as Dialog from '@radix-ui/react-dialog';
import * as VisuallyHidden from '@radix-ui/react-visually-hidden';
import { Command } from 'cmdk';
import { GripVerticalIcon } from 'lucide-react';
import { useCallback, useEffect, useRef, useState } from 'react';
import type { CSSProperties, KeyboardEvent } from 'react';
import { BarLoader } from 'react-spinners';
import { colorVars } from 'techpick-shared';
import { DeselectTagButton } from '../SelectedTagItem/DeselectTagButton';
import { SelectedTagItem } from '../SelectedTagItem/SelectedTagItem';
import { SelectedTagListLayout } from '../SelectedTagListLayout/SelectedTagListLayout';
import { DeleteTagDialog } from './DeleteTagDialog';
import { getRandomInt } from './PickTagAutocompleteDialog.lib';
import { NON_EXISTENT_TAG_ID } from './PickTagPicker.constants';
import { TagDndContext } from './TagDndContext';
import { TagInfoEditPopoverButton } from './TagInfoEditPopoverButton';
import { TagSortableDraggable } from './TagSortableDraggable';
import { getTagSortableContextId } from './getTagSortableContextId';
import {
  alignItemCenter,
  commandInputStyle,
  createTagListItemStyle,
  dialogOverlayStyle,
  displayFlex,
  dragHandlerStyle,
  tagCreateTextStyle,
  tagDialogPortalLayout,
  tagListItemStyle,
  tagListLoadingStyle,
  tagListStyle,
} from './pickTagAutocompleteDialog.css';
import { convertToFuzzyItems, useFuzzyFilter } from './useFuzzyFilter';

export function PickTagAutocompleteDialog({
  open,
  onOpenChange,
  pickInfo,
  selectedTagList,
  floatingStyles,
  setFloating,
  container,
}: PickTagAutocompleteDialogProps) {
  const [tagInputValue, setTagInputValue] = useState('');
  const [canCreateTag, setCanCreateTag] = useState(false);
  const tagInputRef = useRef<HTMLInputElement | null>(null);
  const selectedTagListRef = useRef<HTMLDivElement | null>(null);
  const isCreateFetchPendingRef = useRef<boolean>(false);
  const randomNumber = useRef<number>(getRandomInt());
  const tagIdOrderedList = selectedTagList.map((tag) => tag.id);
  const { data: tagList = [], isLoading } = useFetchTagList();
  const { mutateAsync: createTag } = useCreateTag();
  const { mutate: updatePickInfo } = useUpdatePickInfo();
  const setCurrentUpdateTagPickIdToNull = useUpdatePickStore(
    (state) => state.setCurrentUpdateTagPickIdToNull,
  );
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

  const focusTagInput = useCallback(() => {
    tagInputRef.current?.focus();
    tagInputRef.current?.scrollIntoView({
      behavior: 'smooth',
      block: 'center',
    });
  }, []);

  const updateCanCreateTag = useCallback(
    (value: string) => {
      const isUnique = !tagList.some((tag) => tag.name === value.trim());
      const isNotInitialValue = value.trim() !== '';
      const isCreatable = isUnique && isNotInitialValue;

      setCanCreateTag(isCreatable);
    },
    [tagList],
  );

  const clearTagInputValue = () => {
    setTagInputValue('');
    updateCanCreateTag('');
  };

  const onSelectTag = (tag: TagType) => {
    if (tagIdOrderedList.includes(tag.id)) {
      return;
    }

    const newTagIdOrderedList = [...tagIdOrderedList, tag.id];

    focusTagInput();
    clearTagInputValue();
    updatePickInfo({
      pickParentFolderId: pickInfo.parentFolderId,
      updatePickInfo: {
        id: pickInfo.id,
        tagIdOrderedList: newTagIdOrderedList,
      },
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
      onSelectTag(newTag);
    } catch {
      /* empty */
    } finally {
      isCreateFetchPendingRef.current = false;
    }
  };

  const onBackspaceKeyPress = (event: KeyboardEvent<HTMLInputElement>) => {
    if (
      event.key === 'Backspace' &&
      tagInputValue === '' &&
      0 < tagIdOrderedList.length
    ) {
      const newTagIdOrderedList = [...tagIdOrderedList];
      newTagIdOrderedList.pop();

      updatePickInfo({
        pickParentFolderId: pickInfo.parentFolderId,
        updatePickInfo: {
          id: pickInfo.id,
          tagIdOrderedList: newTagIdOrderedList,
        },
      });
    }
  };

  useEffect(
    function onOpenPickTagAutocompleteDialog() {
      if (open) {
        requestAnimationFrame(() => {
          focusTagInput();
        });
      }
    },
    [open, focusTagInput],
  );

  // biome-ignore lint/correctness/useExhaustiveDependencies: <explanation>
  useEffect(
    function checkUpdateCanCreateTag() {
      updateCanCreateTag(tagInputValue);
    },
    [tagList],
  );

  return (
    <Dialog.Root
      open={open}
      onOpenChange={(open) => {
        if (!open) {
          setCurrentUpdateTagPickIdToNull();
        }
        onOpenChange(open);
      }}
      modal
    >
      <Dialog.Portal container={container}>
        <Dialog.Overlay className={dialogOverlayStyle} />
        <Dialog.Content
          style={{ ...floatingStyles }}
          ref={setFloating}
          className={tagDialogPortalLayout}
        >
          <VisuallyHidden.Root>
            <Dialog.Title>tag autocomplete</Dialog.Title>
            <Dialog.Description>select tag</Dialog.Description>
          </VisuallyHidden.Root>

          <Command shouldFilter={false}>
            {/**선택한 태그 리스트 */}
            <SelectedTagListLayout ref={selectedTagListRef} focusStyle="focus">
              {selectedTagList.map((tag) => (
                <SelectedTagItem
                  key={tag.id}
                  name={tag.name}
                  colorNumber={tag.colorNumber}
                >
                  <DeselectTagButton
                    tag={tag}
                    onClick={() => {
                      focusTagInput();
                    }}
                    pickInfo={pickInfo}
                    selectedTagList={selectedTagList}
                  />
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
              {isLoading && (
                <Command.Loading className={tagListLoadingStyle}>
                  <BarLoader color={colorVars.color.font} />
                </Command.Loading>
              )}

              {!isLoading &&
                tagInputValue.trim() === '' &&
                tagList.length === 0 && (
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
                        className={tagListItemStyle}
                        onSelect={() => {
                          onSelectTag(tag);
                        }}
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

                        <TagInfoEditPopoverButton
                          tag={tag}
                          container={container}
                        />
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

interface PickTagAutocompleteDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  pickInfo: PickInfoType;
  selectedTagList: TagType[];
  setFloating: ((node: HTMLElement | null) => void) &
    ((node: HTMLElement | null) => void);
  floatingStyles: CSSProperties;
  container: HTMLDivElement | null;
}
