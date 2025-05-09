import { Node } from '@xyflow/react';
import { BlockType } from './BlockType';
import { VariableType } from './VariableType';

// export type NodeType = 'start_thread' | 'input' | 'assign' | 'while' | 'condition' | 'print' | 'end';
// export type VariableType = 'int' | 'double' | string;
export type Variable = { name: string; type: VariableType };
export type IdMap = Record<string, number>;
export type WhileNodesIdMap = Record<string, WhileNodeInfo>;
// Type for storing WHILE node connection information
export interface WhileNodeInfo {
  bodyEntryId: string;
  endNodeId: string;
  lastNodeIds?: string[];
}
export interface NodeData {
  type: BlockType;
  variable?: string;
  expression?: string;
  setVariable?: (variable: string) => void;
  setExpression?: (expression: string) => void;
  // variables: Variable[];
  deleteNode?: () => void;

  [key: string]: unknown;
}

export interface CustomNode extends Node<NodeData, 'custom'> {
  data: NodeData;
}

export interface IFlowNode {
  id: number;
  type: Exclude<BlockType, BlockType.START_THREAD>;
  variable?: string;
  expression?: string;
  next?: number | null;
  trueBranch?: number;
  falseBranch?: number;
  body?: number;
}

export interface IRunCodeRequest {
  variables: Array<Variable>;
  threads: Array<Array<IFlowNode>>;
}

export interface IRunCodeResponse {}

export interface IGenerateCodeRequest {
  variables: Array<Variable>;
  threads: Array<Array<IFlowNode>>;
}

export interface IGenerateCodeResponse {
  message: string;
  code: string;
}

export interface IHttpConfig {
  url?: string;
  headers?: Record<string, string>;
  params?: unknown;
  data?: unknown;
  responseType?: string;
  onUploadProgress?: (progressEvent: ProgressEvent) => void;
}

export type IMap = Record<string, unknown>;

export interface IHttpClient {
  get: <T>(url: string, config?: IHttpConfig) => Promise<T>;
  post: <T, TD>(url: string, data: TD, config?: IHttpConfig) => Promise<T>;
  put: <T, TD>(url: string, data: TD, config?: IHttpConfig) => Promise<T>;
  delete: <T>(url: string, config?: IHttpConfig) => Promise<T>;
  patch: <T, TD>(url: string, data: TD, config?: IHttpConfig) => Promise<T>;
}

export enum HttpStatusCode {
  CONTINUE = 100,

  SWITCHING_PROTOCOLS = 101,

  PROCESSING = 102,

  OK = 200,

  CREATED = 201,

  ACCEPTED = 202,

  NON_AUTHORITATIVE_INFORMATION = 203,

  NO_CONTENT = 204,

  RESET_CONTENT = 205,

  PARTIAL_CONTENT = 206,

  MULTI_STATUS = 207,

  ALREADY_REPORTED = 208,

  IM_USED = 226,

  MULTIPLE_CHOICES = 300,

  MOVED_PERMANENTLY = 301,

  FOUND = 302,

  SEE_OTHER = 303,

  NOT_MODIFIED = 304,

  USE_PROXY = 305,

  SWITCH_PROXY = 306,

  TEMPORARY_REDIRECT = 307,

  PERMANENT_REDIRECT = 308,

  BAD_REQUEST = 400,

  UNAUTHORIZED = 401,

  PAYMENT_REQUIRED = 402,

  FORBIDDEN = 403,

  NOT_FOUND = 404,

  METHOD_NOT_ALLOWED = 405,

  NOT_ACCEPTABLE = 406,

  PROXY_AUTHENTICATION_REQUIRED = 407,

  REQUEST_TIMEOUT = 408,

  CONFLICT = 409,

  GONE = 410,

  LENGTH_REQUIRED = 411,

  PRECONDITION_FAILED = 412,

  PAYLOAD_TOO_LARGE = 413,

  URI_TOO_LONG = 414,

  UNSUPPORTED_MEDIA_TYPE = 415,

  RANGE_NOT_SATISFIABLE = 416,

  EXPECTATION_FAILED = 417,

  I_AM_A_TEAPOT = 418,

  MISDIRECTED_REQUEST = 421,

  UNPROCESSABLE_ENTITY = 422,

  LOCKED = 423,

  FAILED_DEPENDENCY = 424,

  UPGRADE_REQUIRED = 426,

  PRECONDITION_REQUIRED = 428,

  TOO_MANY_REQUESTS = 429,

  REQUEST_HEADER_FIELDS_TOO_LARGE = 431,

  UNAVAILABLE_FOR_LEGAL_REASONS = 451,

  INTERNAL_SERVER_ERROR = 500,

  NOT_IMPLEMENTED = 501,

  BAD_GATEWAY = 502,

  SERVICE_UNAVAILABLE = 503,

  GATEWAY_TIMEOUT = 504,

  HTTP_VERSION_NOT_SUPPORTED = 505,

  VARIANT_ALSO_NEGOTIATES = 506,

  INSUFFICIENT_STORAGE = 507,

  LOOP_DETECTED = 508,

  NOT_EXTENDED = 510,

  NETWORK_AUTHENTICATION_REQUIRED = 511,
}

export interface IResponse<T = object | Array<object>> {
  status: HttpStatusCode;
  data: T;
}

export enum CONFIRM_MESSAGES {
  SENDED = 'Sended',
  DELETED = 'Deleted',
  COMPLITED = 'Complited',
  DONE = 'Done',
  CONFIRMED = 'Confirmed',
}
