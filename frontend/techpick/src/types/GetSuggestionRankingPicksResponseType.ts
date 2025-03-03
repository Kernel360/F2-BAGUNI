import type { components } from '@/schema';
import type { ConcreteType } from './ConcreteType';

export interface GetSuggestionRankingPicksResponseType {
  weeklyViewRanking: ConcreteType<
    components['schemas']['baguni.domain.infrastructure.link.dto.LinkInfo']
  >[];
  monthlyPickRanking: ConcreteType<
    components['schemas']['baguni.domain.infrastructure.link.dto.LinkInfo']
  >[];
}
