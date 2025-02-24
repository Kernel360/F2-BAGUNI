import { QueryProvider } from '@/libs/@react-query/QueryProvider';
import { Noto_Sans_KR } from 'next/font/google';
import '@/styles/reset.css';
import { PORTAL_CONTAINER_ID } from '@/constants/portalContainerId';
import { UserIdentifyProvider } from '@/libs/@eventlog/UserIdentifyProvider';
import { ThemeProvider } from '@/providers/ThemeProvider';
import { ToastProvider } from '@/providers/ToastProvider';
import { getUserIdForServer } from '@/utils/getUserIdForServer';
import type { Metadata } from 'next';
import { preventOverscrollBehaviorY } from './layout.css';

const notoSansKR = Noto_Sans_KR({ weight: 'variable', subsets: ['latin'] });
export const metadata: Metadata = {
  title: '바구니 | 깔끔한 북마크 관리',
  description: '깔끔한 북마크 관리, 바구니',
  icons: {
    icon: '/favicon.ico',
  },
  openGraph: {
    images: [
      {
        url: `${process.env.NEXT_PUBLIC_IMAGE_URL}/image/og_image.png`,
        width: 1200,
        height: 630,
      },
    ],
  },
};

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const userId = await getUserIdForServer();

  /**
   * body 태그는 ThemeProvider에 존재합니다.
   * 해당 이유는 radix의 portal이 default로 document.body에 적용되고,
   * 해당 포탈로 생기는 영역에 테마를 주입하기 위해서입니다.
   */
  return (
    <html lang="ko" className={preventOverscrollBehaviorY}>
      <UserIdentifyProvider userId={userId}>
        <ThemeProvider classname={`${notoSansKR.className}`}>
          <ToastProvider>
            <QueryProvider>
              {children}
              <div id={PORTAL_CONTAINER_ID} />
            </QueryProvider>
          </ToastProvider>
        </ThemeProvider>
      </UserIdentifyProvider>
    </html>
  );
}
