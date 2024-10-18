'use client';

import { PropsWithChildren } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { useGetPickQuery } from '../api';
import {
  pickCardLayout,
  cardImageSectionStyle,
  cardTitleSectionStyle,
  cardDescriptionSectionStyle,
  cardImageStyle,
  defaultCardImageSectionStyle,
  skeleton,
  linkStyle,
} from './pickCard.css';

export function PickCard({
  children,
  pickId,
}: PropsWithChildren<PickCardProps>) {
  const { data: pickData, isLoading, isError } = useGetPickQuery(pickId);

  if (isLoading) {
    return (
      <div className={`${pickCardLayout} ${skeleton}`}>
        <div className={`${cardImageSectionStyle} ${skeleton}`}>
          <div className={defaultCardImageSectionStyle} />
        </div>
      </div>
    );
  }

  if (isError || !pickData) {
    return <p>oops! something is wrong</p>;
  }

  const { memo, title, linkUrlResponse } = pickData;
  const { imageUrl, url } = linkUrlResponse;

  return (
    <Link href={url} target="_blank" className={linkStyle}>
      <div className={pickCardLayout}>
        <div className={cardImageSectionStyle}>
          {imageUrl ? (
            <Image
              src={imageUrl}
              width={278}
              height={64}
              className={cardImageStyle}
              alt=""
            />
          ) : (
            <div className={defaultCardImageSectionStyle} />
          )}
        </div>

        <div className={cardTitleSectionStyle}>
          <p>{title}</p>
        </div>
        <div className={cardDescriptionSectionStyle}>
          <p>{memo}</p>
        </div>
        <div>{children}</div>
      </div>
    </Link>
  );
}
interface PickCardProps {
  pickId: number;
}
