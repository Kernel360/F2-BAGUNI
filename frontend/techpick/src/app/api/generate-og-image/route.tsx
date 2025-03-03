import { getShareFolderById } from '@/apis/folder/getShareFolderById';
import { ImageResponse } from '@vercel/og';
/* eslint-disable jsx-a11y/alt-text */
import type { NextRequest } from 'next/server';

const styles = {
  1: { width: '1200px', height: '630px' },
  2: { width: '600px', height: '630px' },
  4: { width: '600px', height: '315px' },
  8: { width: '300px', height: '315px' },
  16: { width: '300px', height: '157.5px' },
};

async function isValidImageUrl(url: string): Promise<boolean> {
  try {
    const res = await fetch(url, { method: 'HEAD' });
    return (
      res.ok && res.headers.get('Content-Type')?.startsWith('image/') === true
    );
  } catch {
    return false;
  }
}

const getImageStyle = (index: number) => {
  return styles[index as keyof typeof styles] || styles[16];
};

export async function GET(req: NextRequest) {
  const { searchParams } = new URL(req.url);
  const uuid = searchParams.get('uuid');

  if (!uuid) {
    return <img src="/image/og_image.png" alt="" />;
  }

  const width = 1200;
  const height = 630;

  let images: string[] = [];
  let imageCount = 0;

  try {
    const sharedFolder = await getShareFolderById(uuid);
    const { pickList } = sharedFolder;

    const imageUrls = pickList
      .map((pick) => pick.linkInfo.imageUrl)
      .filter((url) => url && url !== '')
      .slice(0, 16); // 최대 16개까지 허용

    const ogImageUrl = imageUrls.filter((image) => image !== undefined);

    const validImageUrls = await Promise.all(
      ogImageUrl.map(async (url) =>
        (await isValidImageUrl(url)) ? url : null,
      ),
    );
    const filteredImageUrls = validImageUrls.filter((url) => url !== null);

    if (filteredImageUrls.length === 0) {
      return <img src="/image/og_image.png" alt="" />;
    }

    let adjustedCount = 1;

    switch (true) {
      case filteredImageUrls.length >= 16:
        adjustedCount = 16;
        break;
      case filteredImageUrls.length >= 8:
        adjustedCount = 8;
        break;
      case filteredImageUrls.length >= 4:
        adjustedCount = 4;
        break;
      case filteredImageUrls.length >= 2:
        adjustedCount = 2;
        break;
    }

    images = filteredImageUrls.slice(0, adjustedCount);
    imageCount = images.length;
  } catch {
    return <img src="/image/og_image.png" alt="" />;
  }

  return new ImageResponse(
    <div
      style={{
        display: 'flex',
        flexWrap: 'wrap',
        width: '1200px',
        height: '630px',
      }}
    >
      {images.map((url: string, index: number) => (
        <img
          // biome-ignore lint/a11y/noRedundantAlt: <explanation>
          alt="open graph image"
          // biome-ignore lint/suspicious/noArrayIndexKey: <explanation>
          key={index}
          src={url}
          style={{
            ...getImageStyle(imageCount),
            objectFit: 'cover',
          }}
        />
      ))}
    </div>,
    { width, height },
  );
}
