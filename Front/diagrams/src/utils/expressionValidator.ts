import { NodeData } from '../types/types';

export const validateExpression = (type: NodeData['type'], value: string): boolean => {
  if (!value) return false;
  switch (type) {
    case 'input':
      return true;
    case 'assign':
      return /^\w+\s*=\s*(\w+|\d+|"[^"]*")$/.test(value);
    case 'while':
    case 'condition':
      return /^\w+\s*(==|<|>)\s*\d+$/.test(value);
    case 'print':
      return /^\w+$|^"[^"]*"$/.test(value);
    default:
      return true;
  }
};