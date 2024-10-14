import { useQuery } from '@tanstack/react-query';
import { getRootAndRecycleBinStructure } from '@/features/nodeManagement/api/queryFunctions';

export const useGetRootAndRecycleBinStructure = () => {
  return useQuery({
    queryKey: ['rootAndRecycleBinStructure'],
    queryFn: getRootAndRecycleBinStructure,
  });
};