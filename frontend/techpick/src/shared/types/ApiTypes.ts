import { NodeData } from '@/shared/types/NodeData';

export interface ApiDefaultFolderIdData {
  userId: number;
  RECYCLE_BIN: number;
  UNCLASSIFIED: number;
  ROOT: number;
}

export interface ApiStructureData {
  root: NodeData[];
  recycleBin: NodeData[];
}

export interface ApiFolderData {
  id: number;
  name: string;
  parentFolderId: number;
  userId: number;
}

export interface ApiTagData {
  tagId: number;
  tagName: string;
  tagOrder: number;
  colorNumber: number;
  userId: number;
}

export interface ApiLinkUrlData {
  id: number;
  url: string;
  imageUrl: string | null;
}

export interface ApiPickData {
  id: number;
  name: string;
  memo: string;
  folderId: number;
  userId: number;
  tagList: ApiTagData[];
  linkUrlResponse: ApiLinkUrlData;
}

export interface ApiPickLinkRequestData {
  url: string;
  title: string;
  description: string;
  imageUrl: string;
}

export interface ApiPickRequestType {
  memo: string;
  title: string;
  tagIdList: number[];
  linkRequest: ApiPickLinkRequestData;
}
