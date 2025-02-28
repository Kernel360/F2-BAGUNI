import type { components } from '@/schema';
import type { ConcreteType } from './ConcreteType';

export type MoveTagRequestType = ConcreteType<
  components['schemas']['baguni.api.application.tag.dto.TagApiRequest$Move']
>;
