import { Noto_Sans_KR } from 'next/font/google';
import { PORTAL_CONTAINER_ID } from '@/constants';
import { ToastProvider, ThemeProvider } from '@/providers';
import { QueryProvider } from '@/providers/QueryProvider';
import type { Metadata } from 'next';
import '@/styles/reset.css';

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

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <ThemeProvider classname={`${notoSansKR.className}`}>
        <ToastProvider>
          <QueryProvider>
            {children}
            <div id={PORTAL_CONTAINER_ID} />
          </QueryProvider>
        </ToastProvider>
      </ThemeProvider>
    </html>
  );
}
