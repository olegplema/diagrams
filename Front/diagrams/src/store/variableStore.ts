import { Variable } from '../types/types';
import { create } from 'zustand/react';

interface VariableStore {
  variables: Variable[];
  setVariables(variables: Variable[]): void;
  addVariable(variable: Variable): void;
  removeVariable(name: string): void;
}

export const useVariableStore = create<VariableStore>((set) => ({
  variables: [],
  setVariables: (variables) => set({ variables }),
  addVariable: (variable) =>
    set((state) => ({
      variables: [...state.variables, variable],
    })),
  removeVariable: (name) =>
    set((state) => ({
      variables: state.variables.filter((v) => v.name !== name),
    })),
}));