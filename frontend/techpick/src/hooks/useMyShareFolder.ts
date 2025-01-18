import { deleteMyShareFolder } from '@/apis/folder/deleteShareFolder';
import { useTreeStore } from '@/stores/dndTreeStore/dndTreeStore';
import { notifyError, notifySuccess } from '@/utils/toast';
import { useMemo } from 'react';

export function useMyShareFolder() {
  const { checkIsShareFolder, updateFolderAccessTokenByFolderId, treeDataMap } =
    useTreeStore();

  const myShareFolders = useMemo(() => {
    const folders = Object.values(treeDataMap);

    return folders
      .filter((folder) => checkIsShareFolder(folder.id))
      .map((folder) => ({
        sourceFolderId: folder.id,
        sourceFolderName: folder.name,
        sourceFolderCreatedAt: folder.createdAt,
        sourceFolderUpdatedAt: folder.updatedAt,
        folderAccessToken: folder.folderAccessToken ?? '',
      }));
  }, [checkIsShareFolder, treeDataMap]);

  const handleDeleteMyShareFolder = async (sourceFolderId: number) => {
    const deleteAccessToken = treeDataMap[sourceFolderId].folderAccessToken;

    updateFolderAccessTokenByFolderId(sourceFolderId, null);
    notifySuccess('공유 폴더가 삭제되었습니다.');
    try {
      await deleteMyShareFolder(sourceFolderId);
    } catch {
      notifyError('공유 폴더 삭제에 실패했습니다.');
      updateFolderAccessTokenByFolderId(sourceFolderId, deleteAccessToken);
    }
  };

  return { myShareFolders, handleDeleteMyShareFolder };
}
