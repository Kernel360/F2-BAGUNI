'use client';

import { postUserPickViewEventLog } from '@/apis/eventLog/postUserPickViewEventLog';
import { useImageLoader } from '@/hooks/useImageLoader';
import { useOpenUrlInNewTab } from '@/hooks/useOpenUrlInNewTab';
import { useFetchTagList } from '@/queries/useFetchTagList';
import { useUpdatePickInfo } from '@/queries/useUpdatePickInfo';
import { usePickStore } from '@/stores/pickStore';
import { useUpdatePickStore } from '@/stores/updatePickStore';
import type { PickViewItemComponentProps } from '@/types/PickViewItemComponentProps';
import { formatDateString } from '@/utils/formatDateString';
import { getFilteredSelectedTagList } from '@/utils/getFilteredSelectedTagList';
import { ExternalLink as ExternalLinkIcon } from 'lucide-react';
import Image from 'next/image';
import { useState } from 'react';
import { PickTagPicker } from '../PickTagPicker/PickTagPicker';
import { PickDateColumnLayout } from './PickDateColumnLayout';
import { PickImageColumnLayout } from './PickImageColumnLayout';
import { PickRecordTitleInput } from './PickRecordTitleInput';
import { PickTagColumnLayout } from './PickTagColumnLayout';
import { PickTitleColumnLayout } from './PickTitleColumnLayout';
import { Separator } from './Separator';
import {
  backgroundImageStyle,
  dateTextStyle,
  externalLinkIconStyle,
  imageStyle,
  linkLayoutStyle,
  pickImageStyle,
  pickRecordLayoutStyle,
  pickTitleSectionStyle,
} from './pickRecord.css';

export function PickRecord({ pickInfo }: PickViewItemComponentProps) {
  const link = pickInfo.linkInfo;
  const { mutate: updatePickInfo } = useUpdatePickInfo();
  const { openUrlInNewTab } = useOpenUrlInNewTab(link.url);
  const currentUpdateTitlePickId = useUpdatePickStore(
    (state) => state.currentUpdateTitlePickId,
  );
  const setCurrentUpdateTitlePickId = useUpdatePickStore(
    (state) => state.setCurrentUpdateTitlePickId,
  );
  const setCurrentUpdateTitlePickIdToNull = useUpdatePickStore(
    (state) => state.setCurrentUpdateTitlePickIdToNull,
  );
  const [isHovered, setIsHovered] = useState(false);
  const isUpdateTitle = currentUpdateTitlePickId === pickInfo.id;
  const isDragging = usePickStore((state) => state.isDragging);
  const { imageStatus } = useImageLoader(link.imageUrl);
  const { data: tagList = [] } = useFetchTagList();

  const filteredSelectedTagList = getFilteredSelectedTagList({
    tagList,
    selectedTagIdList: pickInfo.tagIdOrderedList,
  });

  const onClickLink = async () => {
    try {
      openUrlInNewTab();
      await postUserPickViewEventLog({ url: link.url, pickId: pickInfo.id });
    } catch {
      /*empty */
    }
  };

  return (
    <div
      className={pickRecordLayoutStyle}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <PickImageColumnLayout>
        <div className={pickImageStyle}>
          {imageStatus === 'loading' && <div />}

          {imageStatus === 'loaded' && (
            <>
              <img
                src={link.imageUrl}
                alt=""
                width="96px"
                height="47.25px"
                className={backgroundImageStyle}
              />
              <img
                src={link.imageUrl}
                alt=""
                width="96px"
                height="47.25px"
                className={imageStyle}
              />
            </>
          )}

          {imageStatus === 'error' && (
            <Image src={'/image/default_image.svg'} alt="" fill sizes="96px" />
          )}
        </div>
      </PickImageColumnLayout>
      {isHovered && !isDragging && (
        // biome-ignore lint/a11y/useKeyWithClickEvents: <explanation>
        <div className={linkLayoutStyle} onClick={onClickLink}>
          <ExternalLinkIcon className={externalLinkIconStyle} strokeWidth={2} />
        </div>
      )}

      <Separator />

      <PickTitleColumnLayout>
        <div
          className={pickTitleSectionStyle}
          onDoubleClick={(event) => {
            setCurrentUpdateTitlePickId(pickInfo.id);
            event.stopPropagation();
          }}
        >
          {pickInfo.title}
        </div>
        {isUpdateTitle && (
          <PickRecordTitleInput
            initialValue={pickInfo.title}
            onSubmit={(newTitle) => {
              updatePickInfo({
                pickParentFolderId: pickInfo.parentFolderId,
                updatePickInfo: {
                  ...pickInfo,
                  title: newTitle,
                },
              });
              setCurrentUpdateTitlePickIdToNull();
            }}
            onClickOutSide={() => {
              setCurrentUpdateTitlePickIdToNull();
            }}
          />
        )}
      </PickTitleColumnLayout>

      <Separator />

      <PickTagColumnLayout>
        <PickTagPicker
          pickInfo={pickInfo}
          selectedTagList={filteredSelectedTagList}
        />
      </PickTagColumnLayout>

      <Separator />

      <PickDateColumnLayout>
        <div className={dateTextStyle}>
          {formatDateString(pickInfo.updatedAt)}
        </div>
      </PickDateColumnLayout>
    </div>
  );
}
