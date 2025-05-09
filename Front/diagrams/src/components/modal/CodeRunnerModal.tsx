import React, { useEffect, useState } from 'react';
import { LuTriangle } from 'react-icons/lu';
import BaseModal from './BaseModal';
import RunCodeButton from '../buttons/RunCodeButton';
import useWebSocket from 'react-use-websocket';
import { useStartRunCode } from '../../hooks/useStartRunCode';
import { useFlowchartStore } from '../../store/useFlowchartStore';
import { CustomNode } from '../../types/types';
import { Edge } from '@xyflow/react';

interface IProps {
  nodes: CustomNode[];
  edges: Edge[];
}

const CodeRunnerModal: React.FC<IProps> = ({ nodes, edges }) => {
  const [isOpen, setIsOpen] = useState(false);

  const { generateJSON } = useFlowchartStore();
  const { startRunningCode } = useStartRunCode();

  const openModal = () => setIsOpen(true);
  const closeModal = () => setIsOpen(false);

  const handleClick = () => {
    openModal();
    if (sessionId != null) {
      const { threads, variables } = generateJSON(nodes, edges);
      console.log(JSON.stringify(threads, null, 2));
      startRunningCode({ clientSocketId: sessionId, threads, variables }).then();
    }
  };

  const [sessionId, setSessionId] = useState<string | null>(null);

  const { sendMessage, lastMessage, readyState } = useWebSocket(
    process.env.REACT_APP_WS_URL as string,
    {
      onOpen: () => console.log('Connected to WebSocket'),
      onClose: () => console.log('WebSocket closed'),
      shouldReconnect: () => true,
    }
  );

  useEffect(() => {
    if (lastMessage !== null && sessionId === null) {
      setSessionId(lastMessage.data);
      console.log('Received sessionId:', sessionId);
    }
    console.log('Received sessionId:', lastMessage?.data);
  }, [lastMessage]);

  return (
    <div>
      <RunCodeButton handleClick={handleClick} />
      <BaseModal isOpen={isOpen} onRequestClose={closeModal} contentLabel="Code Runner Modal">
        No Content
      </BaseModal>
    </div>
  );
};

export default CodeRunnerModal;
