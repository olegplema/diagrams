import { useMutation } from '@tanstack/react-query';
import { queryKeys } from '../consts/queryKeys';
import { IRunCodeRequest } from '../types/types';
import { codeRunningService } from '../api/http/codeRunning.service';

export const useStartRunCode = () => {
  const {
    isPending,
    data,
    isError,
    mutateAsync: startRunningCode,
  } = useMutation({
    mutationKey: queryKeys.startRunningCode,
    mutationFn: (data: IRunCodeRequest) => codeRunningService.startRunningCode(data),
  });

  return {
    isPending,
    data,
    isError,
    startRunningCode,
  };
};
