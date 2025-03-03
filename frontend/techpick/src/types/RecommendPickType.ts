import type { components } from '@/schema';
import type { ConcreteType } from './ConcreteType';

export type RecommendPickType = ConcreteType<
  Omit<
    components['schemas']['baguni.domain.infrastructure.link.dto.LinkInfo'],
    'count'
  >
>;
