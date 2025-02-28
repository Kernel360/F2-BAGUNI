import type { CreateTagRequestType } from '@/types/CreateTagRequestType';
import type { TagType } from '@/types/TagType';
import type { UpdateTagRequestType } from '@/types/UpdateTagRequestType';

export type TagState = {
  tagList: TagType[];
  selectedTagList: TagType[];
  fetchingTagState: { isError: boolean; isPending: boolean; data: TagType[] };
};

export type TagAction = {
  replaceSelectedTagList: (tagList: TagType[]) => void;
  selectTag: (tag: TagType) => void;
  deselectTag: (tagId: TagType['id']) => void;
  updateSelectedTagList: (tag: TagType) => void;
  setTagList: (tagList: TagType[]) => void;
  fetchingTagList: () => Promise<void>;
  createTag: (tagData: CreateTagRequestType) => Promise<TagType | undefined>;
  deleteTag: (tagId: TagType['id']) => Promise<void>;
  updateTag: (updatedTagInfo: UpdateTagRequestType) => Promise<void>;
  popSelectedTag: () => void;
};
