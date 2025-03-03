import { getShareFolderById } from '@/apis/folder/getShareFolderById';
import { FolderContentLayout } from '@/components/FolderContentLayout';
import { MobileEmptyPickRecordImage } from '@/components/MobileEmptyPickRecordImage';
import { PickContentLayout } from '@/components/PickContentLayout';
import { PickRecordHeader } from '@/components/PickRecord/PickRecordHeader';
import { ROUTES } from '@/constants/route';
import { ScreenLogger } from '@/libs/@eventlog/ScreenLogger';
import { isLoginUser } from '@/utils/isLoginUser';
import { FolderOpenIcon } from 'lucide-react';
import type { Metadata } from 'next';
import dynamic from 'next/dynamic';
import Link from 'next/link';
import { LandingPageLinkButton } from './LandingPageLinkButton';
import { MobileSharedPickRecord } from './MobileSharedPickRecord';
import { SharePickRecord } from './SharePickRecord';
import { SignUpLinkButton } from './SignUpLinkButton';
import {
  buttonSectionStyle,
  currentFolderNameSectionStyle,
  desktopVisibleStyle,
  folderContentHeaderStyle,
  folderNameStyle,
  folderOpenIconStyle,
  homeNavigateButtonStyle,
  mobileAndTabletVisibleStyle,
  mobilePickRecordListStyle,
} from './page.css';
const EmptyPickRecordImage = dynamic(
  () =>
    import('@/components/EmptyPickRecordImage').then(
      (mod) => mod.EmptyPickRecordImage,
    ),
  {
    ssr: false,
  },
);

export async function generateMetadata({
  params,
}: {
  params: { uuid: string };
}): Promise<Metadata> {
  const { uuid } = params;
  const sharedFolder = await getShareFolderById(uuid);
  const { pickList } = sharedFolder;

  const ogImageUrl = new URL(
    `${process.env.NEXT_PUBLIC_IMAGE_URL}/api/generate-og-image`,
  );
  ogImageUrl.searchParams.set('uuid', uuid);

  return {
    title: `${sharedFolder.folderName} 폴더 공유 페이지`,
    description: `${pickList.length}개의 북마크가 공유되었습니다.`,
    openGraph: {
      images: [ogImageUrl.toString()],
    },
  };
}

export default async function Page({ params }: { params: { uuid: string } }) {
  const { uuid } = params;
  const sharedFolder = await getShareFolderById(uuid);
  const isLoggedIn = await isLoginUser();
  const pickList = sharedFolder.pickList;

  return (
    <ScreenLogger
      eventName="shared_page_view"
      logInfo={{
        folderUUID: uuid,
        folderName: sharedFolder.folderName,
        pickCount: pickList.length,
      }}
    >
      <FolderContentLayout>
        <div className={folderContentHeaderStyle}>
          <div className={currentFolderNameSectionStyle}>
            <FolderOpenIcon size={28} className={folderOpenIconStyle} />
            <h1 className={folderNameStyle}>{sharedFolder.folderName}</h1>
          </div>

          <div className={buttonSectionStyle}>
            {isLoggedIn ? (
              <Link href={ROUTES.HOME}>
                {/* biome-ignore lint/a11y/useButtonType: <explanation> */}
                <button className={homeNavigateButtonStyle}>홈으로 가기</button>
              </Link>
            ) : (
              <>
                <LandingPageLinkButton />
                <SignUpLinkButton />
              </>
            )}
          </div>
        </div>
        <div className={desktopVisibleStyle}>
          <PickContentLayout>
            <PickRecordHeader />
            {pickList.length === 0 ? (
              <EmptyPickRecordImage
                title="공유된 북마크가 없습니다."
                description="폴더 내 공유된 북마크가 존재하지 않습니다."
              />
            ) : (
              pickList.map((pick) => {
                return (
                  <SharePickRecord
                    key={pick.title}
                    pickInfo={pick}
                    tagList={sharedFolder.tagList}
                    folderAccessToken={uuid}
                  />
                );
              })
            )}
          </PickContentLayout>
        </div>

        <div className={mobileAndTabletVisibleStyle}>
          {pickList.length === 0 ? (
            <MobileEmptyPickRecordImage />
          ) : (
            <div className={mobilePickRecordListStyle}>
              {pickList.map((pick) => {
                return (
                  <MobileSharedPickRecord
                    key={pick.title}
                    pickInfo={pick}
                    tagList={sharedFolder.tagList}
                    folderAccessToken={uuid}
                  />
                );
              })}
            </div>
          )}
        </div>
      </FolderContentLayout>
    </ScreenLogger>
  );
}
