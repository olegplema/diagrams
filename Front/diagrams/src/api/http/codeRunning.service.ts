import { HttpService } from './http.service';
import { IHttpClient, IRunCodeRequest, IRunCodeResponse } from '../../types/types';
import { mainAxios } from './mainAxios';

class CodeRunningService {
  constructor(private readonly httpService: HttpService) {}

  public async startRunningCode(req: IRunCodeRequest): Promise<IRunCodeResponse | void> {
    return this.httpService.post('/run', req);
  }
}

export const codeRunningService = new CodeRunningService(new HttpService(mainAxios as IHttpClient));
