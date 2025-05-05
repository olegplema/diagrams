import { AxiosInstance } from 'axios';
import { mainAxios } from './mainAxios';
import { IGenerateCodeRequest, IGenerateCodeResponse } from '../../types';

class CodeGenerationService {
  constructor(private readonly axiosInstance: AxiosInstance) {
  }

  public async generateCode(body: IGenerateCodeRequest): Promise<IGenerateCodeResponse> {
    const response = await this.axiosInstance.post<IGenerateCodeResponse>("/generate-code", body);
    return response.data;
  }
}

export const codeGenerationService = new CodeGenerationService(mainAxios);
