import DOMPurify from 'dompurify';
import { Button, Text } from '@/libs/@components';
import { notifyError, notifySuccess } from '@/libs/@toast';
import { useChangeFocusUsingArrowKey } from '@/hooks';
import { useTagStore } from '@/stores';
import { FolderType, TagType } from '@/types';
import { updatePick } from '@/apis';
import { TagPicker } from '@/components';
import { ThumbnailImage } from './ThumbnailImage';
import {
  pickFormLayout,
  formFieldLayout,
  titleInputStyle,
  submitButtonLayout,
  labelLayout,
} from './CreatePickForm.css';
import { useEffect, useRef, useState } from 'react';
import { FolderSelect } from './FolderSelect';

export function UpdatePickForm({
  id,
  title,
  tagList,
  imageUrl,
  folderId,
  folderInfoList,
}: UpdatePickFormProps) {
  const titleInputRef = useRef<HTMLInputElement>(null);
  const tagPickerRef = useRef<HTMLDivElement>(null);
  const memoInputRef = useRef<HTMLTextAreaElement>(null);
  const submitButtonRef = useRef<HTMLButtonElement>(null);
  const { selectedTagList, replaceSelectedTagList } = useTagStore();
  useChangeFocusUsingArrowKey([
    titleInputRef,
    tagPickerRef,
    memoInputRef,
    submitButtonRef,
  ]);

  const currentSelectedFolderInfo = folderInfoList.find(
    (folder) => folder.id === folderId
  );
  const [selectedFolderId, setSelectedFolderId] = useState(
    `${currentSelectedFolderInfo?.id ?? folderInfoList[0].id}`
  );

  useEffect(
    function onUpdatePickFormLoad() {
      replaceSelectedTagList(tagList);
    },
    [tagList, replaceSelectedTagList]
  );

  const onSubmit = () => {
    const userModifiedTitle = titleInputRef.current?.value ?? '';

    updatePick({
      id,
      title: DOMPurify.sanitize(userModifiedTitle),
      tagIdOrderedList: selectedTagList.map((tag) => tag.id),
    })
      .then(() => {
        notifySuccess('수정되었습니다!');
      })
      .catch(() => {
        notifyError(`북마크가 실패했습니다!`);
      });
  };

  return (
    <form className={pickFormLayout} onSubmit={(e) => e.preventDefault()}>
      <div className={formFieldLayout}>
        <ThumbnailImage image={imageUrl} />
        <input
          type="text"
          defaultValue={title}
          ref={titleInputRef}
          className={titleInputStyle}
        />
      </div>
      <div className={formFieldLayout}>
        <div className={labelLayout}>
          <Text size="2xl" asChild>
            <label htmlFor="">폴더</label>
          </Text>
        </div>
        <FolderSelect
          folderInfoList={folderInfoList}
          selectedFolderId={selectedFolderId}
          setSelectedFolderId={setSelectedFolderId}
        />
      </div>
      <div className={formFieldLayout}>
        <div className={labelLayout}>
          <Text size="2xl" asChild>
            <label htmlFor="">태그</label>
          </Text>
        </div>
        <TagPicker ref={tagPickerRef} />
      </div>

      <div className={submitButtonLayout}>
        <Button onClick={onSubmit} ref={submitButtonRef}>
          제출
        </Button>
      </div>
    </form>
  );
}

interface UpdatePickFormProps {
  id: number;
  title: string;
  tagList: TagType[];
  imageUrl: string;
  folderId: number;
  folderInfoList: FolderType[];
}