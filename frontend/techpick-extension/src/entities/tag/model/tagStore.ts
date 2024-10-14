import { create } from 'zustand';
import { immer } from 'zustand/middleware/immer';
import { HTTPError } from 'ky';
import { handleHTTPError } from '@/shared';
import { TagType, TagUpdateType, CreateTagRequestType } from '../type';
import { getTagList, createTag, deleteTag, updateTag } from '../api';

type TagState = {
  tagList: TagType[];
  selectedTagList: TagType[];
  fetchingTagState: { isError: boolean; isPending: boolean; data: TagType[] };
};

type TagAction = {
  selectTag: (tag: TagType) => void;
  deselectTag: (tagId: TagType['tagId']) => void;
  updateSelectedTagList: (tag: TagType) => void;
  fetchingTagList: () => Promise<void>;
  createTag: (tagData: CreateTagRequestType) => Promise<TagType | undefined>;
  deleteTag: (tagId: TagType['tagId']) => Promise<void>;
  updateTag: (updatedTag: TagUpdateType) => Promise<void>;
};

const initialState: TagState = {
  tagList: [],
  selectedTagList: [],
  fetchingTagState: { isError: false, isPending: false, data: [] },
};

export const useTagStore = create<TagState & TagAction>()(
  immer((set) => ({
    ...initialState,
    selectTag: (tag: TagType) =>
      set((state) => {
        const exist = state.selectedTagList.some((t) => t.tagId === tag.tagId);

        // 이미 선택된 태그인지 확인
        if (exist) {
          return;
        }

        state.selectedTagList.push(tag);
      }),

    deselectTag: (tagId) =>
      set((state) => {
        state.selectedTagList = state.selectedTagList.filter(
          (t) => t.tagId !== tagId
        );
      }),

    updateSelectedTagList: (updatedTag) => {
      set((state) => {
        const index = state.selectedTagList.findIndex(
          (tag) => tag.tagId === updatedTag.tagId
        );

        if (index === -1) {
          return;
        }

        state.selectedTagList[index] = {
          ...updatedTag,
        };
      });
    },

    fetchingTagList: async () => {
      try {
        set((state) => {
          state.fetchingTagState.isPending = true;
        });

        const remoteTagList = await getTagList();

        set((state) => {
          state.tagList = [...remoteTagList];
          state.fetchingTagState.isPending = false;
        });
      } catch (error) {
        if (error instanceof HTTPError) {
          set((state) => {
            state.fetchingTagState.isPending = false;
            state.fetchingTagState.isError = true;
          });
        }
      }

      return;
    },

    createTag: async (tagData) => {
      try {
        const newTag = await createTag(tagData);

        set((state) => {
          state.tagList.push(newTag!);
        });

        return newTag;
      } catch (error) {
        if (error instanceof HTTPError) {
          await handleHTTPError(error);
        }

        return;
      }
    },

    deleteTag: async (tagId: number) => {
      let temporalDeleteTargetTag: TagType | undefined;
      let deleteTargetTagIndex = -1;
      let isSelected = false;
      let deleteTargetSelectedIndex = -1;

      try {
        set((state) => {
          deleteTargetTagIndex = state.tagList.findIndex(
            (tag) => tag.tagId === tagId
          );
          deleteTargetSelectedIndex = state.selectedTagList.findIndex(
            (tag) => tag.tagId === tagId
          );

          if (deleteTargetTagIndex !== -1) {
            temporalDeleteTargetTag = {
              ...state.tagList[deleteTargetTagIndex],
            };
            state.tagList.splice(deleteTargetTagIndex, 1);
          }

          if (deleteTargetSelectedIndex !== -1) {
            isSelected = true;
            state.selectedTagList.splice(deleteTargetSelectedIndex, 1);
          }
        });

        await deleteTag(tagId);
      } catch (error) {
        set((state) => {
          if (!temporalDeleteTargetTag) {
            return;
          }

          state.tagList.splice(
            deleteTargetTagIndex,
            0,
            temporalDeleteTargetTag
          );

          if (isSelected) {
            state.selectedTagList.splice(
              deleteTargetSelectedIndex,
              0,
              temporalDeleteTargetTag
            );
          }
        });

        if (error instanceof HTTPError) {
          await handleHTTPError(error);
        }
      }
    },

    updateTag: async (updatedTag) => {
      let previousTag: TagType | undefined;
      let previousSelectedTag: TagType | undefined;

      try {
        set((state) => {
          const index = state.tagList.findIndex(
            (tag) => tag.tagId === updatedTag.tagId
          );

          if (index !== -1) {
            previousTag = { ...state.tagList[index] };
            state.tagList[index] = {
              userId: state.tagList[index].userId,
              ...updatedTag,
            };
          }

          const selectedTagListIndex = state.selectedTagList.findIndex(
            (tag) => tag.tagId === data.tagId
          );

          if (selectedTagListIndex !== -1) {
            previousSelectedTag = {
              ...state.selectedTagList[selectedTagListIndex],
            };
            state.selectedTagList[selectedTagListIndex] = {
              ...updatedTag,
              userId: state.selectedTagList[selectedTagListIndex].userId,
            };
          }
        });

        const dataList = await updateTag(updatedTag);
        const data = dataList[0];
      } catch (error) {
        set((state) => {
          if (previousTag) {
            const index = state.tagList.findIndex(
              (tag) => tag.tagId === previousTag?.tagId
            );
            state.tagList[index] = previousTag;
          }

          if (previousSelectedTag) {
            const selectedIndex = state.selectedTagList.findIndex(
              (tag) => tag.tagId === previousSelectedTag?.tagId
            );
            state.selectedTagList[selectedIndex] = previousSelectedTag;
          }
        });

        if (error instanceof HTTPError) {
          await handleHTTPError(error);
        }
      }
    },
  }))
);
