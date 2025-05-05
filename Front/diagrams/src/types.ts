
export type NodeType = 'start_thread' | 'input' | 'assign' | 'while' | 'condition' | 'print' | 'end';
export type VariableType = 'int' | 'double' | string;
export type Variable = { name: string; type: VariableType };

export interface IFlowNode {
  id: number;
  type: Exclude<NodeType, 'start_thread'>;
  variable?: string;
  expression?: string;
  next?: number | null;
  trueBranch?: number;
  falseBranch?: number;
  body?: number;
}
export interface IGenerateCodeRequest {
  variables: Array<Variable>;
  threads: Array<Array<IFlowNode>>
}

export interface IGenerateCodeResponse {
  message: string;
  code: string;
}
