'use client';

import { postSharedPickViewEventLog } from '@/apis/eventLog/postSharedPickViewEventLog';
import { PickDateColumnLayout } from '@/components/PickRecord/PickDateColumnLayout';
import { PickImageColumnLayout } from '@/components/PickRecord/PickImageColumnLayout';
import { PickTagColumnLayout } from '@/components/PickRecord/PickTagColumnLayout';
import { PickTitleColumnLayout } from '@/components/PickRecord/PickTitleColumnLayout';
import { Separator } from '@/components/PickRecord/Separator';
import {
  dateTextStyle,
  externalLinkIconStyle,
  imageStyle,
  linkLayoutStyle,
  pickImageStyle,
  pickRecordLayoutStyle,
  pickTitleSectionStyle,
} from '@/components/PickRecord/pickRecord.css';
import {
  tagDialogTriggerLayout,
  tagPickerLayout,
  tagPickerPlaceholderStyle,
} from '@/components/PickTagPicker/pickTagPicker.css';
import { SelectedTagItem } from '@/components/SelectedTagItem/SelectedTagItem';
import { SelectedTagListLayout } from '@/components/SelectedTagListLayout/SelectedTagListLayout';
import { useImageLoader } from '@/hooks/useImageLoader';
import { useOpenUrlInNewTab } from '@/hooks/useOpenUrlInNewTab';
import type { components } from '@/schema';
import type { TagType } from '@/types/TagType';
import { formatDateString } from '@/utils/formatDateString';
import { ExternalLink as ExternalLinkIcon } from 'lucide-react';
import Image from 'next/image';
import { useState } from 'react';

export function SharePickRecord({
  pickInfo,
  tagList,
  folderAccessToken,
}: SharePickRecordProps) {
  const link = pickInfo.linkInfo;
  const { openUrlInNewTab } = useOpenUrlInNewTab(link.url);
  const [isHovered, setIsHovered] = useState(false);
  const { imageStatus } = useImageLoader(link.imageUrl);

  const onClickLink = async () => {
    try {
      openUrlInNewTab();
      await postSharedPickViewEventLog({
        url: link.url,
        folderAccessToken,
      });
    } catch {
      /*empty */
    }
  };

  return (
    // biome-ignore lint/a11y/useKeyWithClickEvents: <explanation>
    <div
      className={pickRecordLayoutStyle}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      onClick={onClickLink}
    >
      <PickImageColumnLayout>
        <div className={pickImageStyle}>
          {imageStatus === 'loading' && <div />}

          {imageStatus === 'loaded' && (
            <img
              src={link.imageUrl}
              alt=""
              width="96px"
              height="47.25px"
              className={imageStyle}
            />
          )}

          {imageStatus === 'error' && (
            <Image src={'/image/default_image.svg'} alt="" fill sizes="96px" />
          )}
        </div>
      </PickImageColumnLayout>
      {isHovered && (
        <div className={linkLayoutStyle}>
          <ExternalLinkIcon className={externalLinkIconStyle} strokeWidth={2} />
        </div>
      )}

      <Separator />

      <PickTitleColumnLayout>
        <div className={pickTitleSectionStyle}>{pickInfo.title}</div>
      </PickTitleColumnLayout>

      <Separator />

      <PickTagColumnLayout>
        <div className={tagPickerLayout}>
          <div className={tagDialogTriggerLayout}>
            {pickInfo.tagIdxList?.length === 0 && (
              <p className={tagPickerPlaceholderStyle}>태그가 없습니다.</p>
            )}
            <SelectedTagListLayout>
              {pickInfo.tagIdxList?.map((tagIdx) => (
                <SelectedTagItem
                  key={tagIdx}
                  tag={{ id: tagIdx, ...tagList[tagIdx] } as TagType}
                />
              ))}
            </SelectedTagListLayout>
          </div>
        </div>
      </PickTagColumnLayout>

      <Separator />

      <PickDateColumnLayout>
        <div className={dateTextStyle}>
          {formatDateString(pickInfo.updatedAt as string)}
        </div>
      </PickDateColumnLayout>
    </div>
  );
}

interface SharePickRecordProps {
  pickInfo: components['schemas']['baguni.domain.infrastructure.sharedFolder.dto.SharedFolderResult$SharedPickInfo'];
  tagList: components['schemas']['baguni.domain.infrastructure.sharedFolder.dto.SharedFolderResult$SharedTagInfo'][];
  folderAccessToken: string;
}
