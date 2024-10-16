import React from 'react';
import { NodeApi } from 'react-arborist';

import Image from 'next/image';
import { linkWrapper } from '@/features/nodeManagement/ui/pick.css';
import { useDragHook } from '@/features/nodeManagement/hooks/useDragHook';

export function Pick({ node }: { node: NodeApi }) {
  const ref = useDragHook(node);

  return (
    <div
      ref={ref as unknown as React.LegacyRef<HTMLDivElement>}
      className={linkWrapper}
    >
      <Image
        src={`image/ic_doc.svg`}
        alt={`${node.data.name}'s image`}
        width={64}
        height={64}
      />
      {node.data.name}
    </div>
  );
}