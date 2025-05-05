
import { HttpStatusCode, IHttpClient, IHttpConfig, IMap, IResponse } from '../../types';

const QUERY_LINK_OFFSET = 0;

export class HttpService {
  constructor(
    private readonly fetchingService: IHttpClient,
  ) {
    this.fetchingService = fetchingService;
  }

  public createQueryLink(base: string, args: IMap): string {
    let url = `${base}?`;

    Object.keys(args as object).forEach((parameter, index) => {
      if (Boolean(args[parameter])) {
        url = `${url}${
          index > QUERY_LINK_OFFSET ? '&' : ''
        }${parameter}=${String(args[parameter])}`;
      }
    });

    return url;
  }

  public async get<T>(url: string, config?: IHttpConfig): Promise<T> {
    return this.fetchingService
      .get<IResponse<T>>(url, {
        ...config,
        headers: {
          ...config?.headers,
          ...this.populateContentTypeHeaderConfig()
        }
      })
      .then((result) => {
        this.checkResponseStatus(result);
        return result.data;
      });
  }

  public async post<T, TD>(url: string, data: TD, config?: IHttpConfig): Promise<T> {
    return this.fetchingService
      .post<IResponse<T>, TD>(url, data, {
        ...config,
        headers: {
          ...this.populateContentTypeHeaderConfig(),
          ...config?.headers
        }
      })
      .then((result) => {
        this.checkResponseStatus(result);
        return result.data;
      });
  }

  public async put<T, TD>(url: string, data: TD, config?: IHttpConfig): Promise<T> {
    return this.fetchingService
      .put<IResponse<T>, TD>(url, data, {
        ...config,
        headers: {
          ...this.populateContentTypeHeaderConfig(),
          ...config?.headers
        }
      })
      .then((result) => {
        this.checkResponseStatus(result);
        return result.data;
      });
  }

  public async patch<T, TD>(url: string, data: TD, config?: IHttpConfig): Promise<T> {
    return this.fetchingService
      .patch<IResponse<T>, TD>(url, data, {
        ...config,
        headers: {
          ...this.populateContentTypeHeaderConfig(),
          ...config?.headers
        }
      })
      .then((result) => {
        this.checkResponseStatus(result);
        return result.data;
      });
  }

  public async delete<T>(url: string, config?: IHttpConfig): Promise<T> {
    return this.fetchingService
      .delete<IResponse<T>>(url, {
        ...config,
        headers: {
          ...config?.headers,
          ...this.populateContentTypeHeaderConfig()
        }
      })
      .then((result) => {
        this.checkResponseStatus(result);
        return result.data;
      });
  }

  public populateContentTypeHeaderConfig(): Record<string, string> {
    return {
      'Content-Type': 'application/json'
    };
  }

  private async checkResponseStatus<T>(result: IResponse<T>): Promise<void> {
    if (result.status >= HttpStatusCode.BAD_REQUEST && result.status < 600) {
      const errorData = {
        response: {
          status: result.status,
          data: result.data
        }
      };

      throw new Error(JSON.stringify(errorData));
    }
  }
}
