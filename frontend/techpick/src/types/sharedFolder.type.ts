import type { components } from '@/schema';

export type ShareFolderRequestType = number;

export type ShareFolderResponseType =
  components['schemas']['techpick.api.application.sharedFolder.dto.SharedFolderApiResponse$Create'];

export type ShareFolderReadFolderFullResponseType =
  components['schemas']['techpick.api.application.sharedFolder.dto.SharedFolderApiResponse$ReadFolderFull'];
