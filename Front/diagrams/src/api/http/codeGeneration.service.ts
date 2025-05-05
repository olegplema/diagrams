import { mainAxios } from './mainAxios';
import { IGenerateCodeRequest, IGenerateCodeResponse, IHttpClient } from '../../types';
import { HttpService } from './http.service';

class CodeGenerationService {

  constructor(private readonly httpService: HttpService) {
  }

  public async generateCode(body: IGenerateCodeRequest): Promise<IGenerateCodeResponse> {
    return this.httpService.post("/generate-code", body);
  }
}

export const codeGenerationService =
  new CodeGenerationService(new HttpService(mainAxios as IHttpClient));
