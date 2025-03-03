import type { components } from '@/schema';
import type { ConcreteType } from './ConcreteType';

export type GetSuggestionBlogArticleListResponseType = ConcreteType<
  components['schemas']['baguni.domain.infrastructure.link.dto.BlogLinkInfo']
>[];
