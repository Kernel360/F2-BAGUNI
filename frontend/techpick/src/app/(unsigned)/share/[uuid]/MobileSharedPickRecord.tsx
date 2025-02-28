'use client';
import { postSharedPickViewEventLog } from '@/apis/eventLog/postSharedPickViewEventLog';
import { SelectedTagItem } from '@/components/SelectedTagItem/SelectedTagItem';
import {
  backgroundImageStyle,
  contentStyle,
  imageSectionStyle,
  imageStyle,
  mainContentStyle,
  mobilePickRecordStyle,
  tagListStyle,
  titleStyle,
} from '@/components/mobilePickRecord.css';
import { useImageLoader } from '@/hooks/useImageLoader';
import { useOpenUrlInNewTab } from '@/hooks/useOpenUrlInNewTab';
import type { components } from '@/schema';

export function MobileSharedPickRecord({
  pickInfo,
  tagList,
  folderAccessToken,
}: MobileSharedPickRecordProps) {
  const link = pickInfo.linkInfo;
  const { openUrlInNewTab } = useOpenUrlInNewTab(link.url);
  const { imageStatus } = useImageLoader(link.imageUrl);
  const filteredSelectedTagList = pickInfo.tagIdxList
    ?.map((tagIdx) => {
      if (tagList[tagIdx]) {
        return tagList[tagIdx];
      }
    })
    .filter((value) => value !== undefined)
    .slice(0, 5);

  const onClickLink = async () => {
    try {
      openUrlInNewTab();
      postSharedPickViewEventLog({
        url: link.url,
        folderAccessToken,
      });
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
                <SelectedTagItem key={tag.name} tag={tag} />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

interface MobileSharedPickRecordProps {
  pickInfo: components['schemas']['baguni.domain.infrastructure.sharedFolder.dto.SharedFolderResult$SharedPickInfo'];
  tagList: components['schemas']['baguni.domain.infrastructure.sharedFolder.dto.SharedFolderResult$SharedTagInfo'][];
  folderAccessToken: string;
}
