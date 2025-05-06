import { NodeData } from '../../types/types';
import React from 'react';
import StartThreadBlock from './StartThreadBlock';
import InputBlock from './InputBlock';
import AssignBlock from './AssignBlock';
import WhileLoopBlock from './WhileLoopBlock';
import ConditionBlock from './ConditionBlock';
import PrintBlock from './PrintBlock';
import EndBlock from './EndBlock';
import { BlockType } from '../../types/BlockType';

interface IProps {
  data: NodeData;
  id: string;
}

const FlowNode: React.FC<IProps> = ({ data, id }) => {
  switch (data.type) {
    case BlockType.START_THREAD:
      return <StartThreadBlock data={data} id={id} />;
    case BlockType.INPUT:
      return <InputBlock data={data} id={id} />;
    case BlockType.ASSIGN:
      return <AssignBlock data={data} id={id} />;
    case BlockType.WHILE:
      return <WhileLoopBlock data={data} id={id} />;
    case BlockType.CONDITION:
      return <ConditionBlock data={data} id={id} />;
    case BlockType.PRINT:
      return <PrintBlock data={data} id={id} />;
    case BlockType.END:
      return <EndBlock data={data} id={id} />;
    default:
      return null;
  }
};

export default FlowNode;