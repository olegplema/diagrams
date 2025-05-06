import { useMutation } from '@tanstack/react-query';
import { queryKeys } from '../consts/queryKeys';
import { codeGenerationService } from '../api/http/codeGeneration.service';
import { IGenerateCodeRequest } from '../types/types';

export function useCodeGeneration() {

  const {
    isPending,
    data: generatedCodeData,
    isError,
    mutateAsync: generate,
  } = useMutation({
    mutationKey: queryKeys.generatedCode,
    mutationFn: (data: IGenerateCodeRequest) => codeGenerationService.generateCode(data),
  });

  return {
    generate,
    isPending,
    generatedCodeData,
    isError,
  };
}
