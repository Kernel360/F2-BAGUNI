'use client';

import lottie, { type AnimationItem } from 'lottie-web';
import { useEffect, useRef } from 'react';
import { Gap } from './Gap';
import {
  emptyPickRecordImageLayoutStyle,
  emptyPickRecordImageStyle,
  emptyPickRecordTextLayoutStyle,
  titleTextStyle,
} from './emptyPickRecordImage.css';

export function EmptyPickRecordImage({
  title = '북마크가 없습니다',
  description = '북마크를 추가해보세요!',
}: EmptyPickRecordImageProps) {
  const container = useRef<HTMLDivElement>(null);

  useEffect(() => {
    let anim: AnimationItem | null = null;

    if (typeof window !== 'undefined' && container.current) {
      anim = lottie.loadAnimation({
        container: container.current,
        renderer: 'svg',
        loop: false,
        autoplay: true,
        path: '/lottie/emptyPickRecordImage.json',
      });
    }

    return () => {
      if (anim) {
        anim.destroy();
      }
    };
  }, []);

  return (
    <div className={emptyPickRecordImageLayoutStyle}>
      <div className={emptyPickRecordImageStyle} ref={container} />
      <div className={emptyPickRecordTextLayoutStyle}>
        <p className={titleTextStyle}>{title}</p>
        <Gap verticalSize="gap4">
          <p> {description}</p>
        </Gap>
      </div>
    </div>
  );
}

interface EmptyPickRecordImageProps {
  title?: string;
  description?: string;
}
