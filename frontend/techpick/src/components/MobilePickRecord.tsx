'use client';

import { postUserPickViewEventLog } from '@/apis/eventLog/postUserPickViewEventLog';
import { useImageLoader } from '@/hooks/useImageLoader';
import { useOpenUrlInNewTab } from '@/hooks/useOpenUrlInNewTab';
import { useFetchTagList } from '@/queries/useFetchTagList';
import type { PickInfoType } from '@/types/PickInfoType';
import { getFilteredSelectedTagList } from '@/utils/getFilteredSelectedTagList';
import { SelectedTagItem } from './SelectedTagItem/SelectedTagItem';
import {
  backgroundImageStyle,
  contentStyle,
  imageSectionStyle,
  imageStyle,
  mainContentStyle,
  mobilePickRecordStyle,
  tagListStyle,
  titleStyle,
} from './mobilePickRecord.css';

export function MobilePickRecord({ pickInfo }: MobilePickRecordProps) {
  const link = pickInfo.linkInfo;
  const { openUrlInNewTab } = useOpenUrlInNewTab(link.url);
  const { imageStatus } = useImageLoader(link.imageUrl);
  const { data: tagList = [] } = useFetchTagList();
  const filteredSelectedTagList = getFilteredSelectedTagList({
    tagList,
    selectedTagIdList: pickInfo.tagIdOrderedList,
  }).slice(0, 5);

  const onClickLink = async () => {
    try {
      openUrlInNewTab();
      await postUserPickViewEventLog({ url: link.url, pickId: pickInfo.id });
    } catch {
      /*empty */
    }
  };

  return (
    // biome-ignore lint/a11y/useKeyWithClickEvents: <explanation>
    <div className={mobilePickRecordStyle} onClick={onClickLink}>
      <div className={contentStyle}>
        <div className={mainContentStyle}>
          <div className={imageSectionStyle}>
            {imageStatus === 'error' ? (
              <img
                src={'/image/default_image.svg'}
                alt=""
                className={imageStyle}
              />
            ) : (
              <>
                <img
                  src={link.imageUrl}
                  alt=""
                  className={backgroundImageStyle}
                />
                <img src={link.imageUrl} alt="" className={imageStyle} />
              </>
            )}
          </div>

          <div className={titleStyle}>{pickInfo.title}</div>
        </div>
        <div>
          {0 < filteredSelectedTagList.length && (
            <div className={tagListStyle}>
              {filteredSelectedTagList.map((tag) => (
                <SelectedTagItem
                  key={tag.name}
                  name={tag.name}
                  colorNumber={tag.colorNumber}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

interface MobilePickRecordProps {
  pickInfo: PickInfoType;
}
