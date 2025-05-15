import React, { useEffect, useState, useRef } from 'react';
import { LuTriangle } from 'react-icons/lu';
import BaseModal from './BaseModal';
import RunCodeButton from '../buttons/RunCodeButton';
import { useStartRunCode } from '../../hooks/useStartRunCode';
import { useFlowchartStore } from '../../store/useFlowchartStore';
import { CustomNode } from '../../types/types';
import { Edge } from '@xyflow/react';

interface IProps {
  nodes: CustomNode[];
  edges: Edge[];
}

interface Message {
  id: number;
  content: string;
}

const CodeRunnerModal: React.FC<IProps> = ({ nodes, edges }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputValue, setInputValue] = useState('');
  const [currentMessageIndex, setCurrentMessageIndex] = useState(0);
  const [waitingForInput, setWaitingForInput] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const { generateJSON } = useFlowchartStore();
  const { startRunningCode } = useStartRunCode();
  const socketRef = useRef<WebSocket | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const [shouldSend, setShouldSend] = useState(false);

  useEffect(() => {
    if (isOpen) {
      const wsUrl = process.env.REACT_APP_WS_URL || 'ws://localhost:8888/connect-ws';

      const ws = new WebSocket(wsUrl);

      ws.onopen = () => {
        console.log('WebSocket connection established');
        setIsConnected(true);
      };

      ws.onclose = event => {
        socketRef.current = null;
        setIsConnected(false);
        setWaitingForInput(false);
        setTimeout(() => {
          if (isOpen) {
            console.log('Attempting to reconnect WebSocket...');
            socketRef.current = new WebSocket(wsUrl);
            socketRef.current.onopen = ws.onopen;
            socketRef.current.onclose = ws.onclose;
            socketRef.current.onerror = ws.onerror;
            socketRef.current.onmessage = ws.onmessage;
          }
        }, 3000);
      };

      ws.onerror = error => {
        console.error('WebSocket error:', error);
      };

      ws.onmessage = event => {
        console.log('Raw WebSocket message received:', event.data);
        handleWebSocketMessage(event.data);
      };

      socketRef.current = ws;
    } else {
      if (socketRef.current) {
        console.log('Closing WebSocket connection...');
        socketRef.current.close();
        socketRef.current = null;
        setIsConnected(false);
        setSessionId(null);
      }
    }
  }, [isOpen]);

  useEffect(() => {
    if (shouldSend && sessionId) {
      const { threads, variables } = generateJSON(nodes, edges);
      startRunningCode({ clientSocketId: sessionId, threads, variables })
        .then(() => console.log('Code execution request sent'))
        .catch(err => console.error('Error starting code execution:', err));
      setShouldSend(false);
    }
  }, [sessionId, shouldSend, setShouldSend]);

  const handleWebSocketMessage = (data: string) => {
    try {
      const jsonData = JSON.parse(data);

      const messageType = jsonData.type.toLowerCase();

      if (messageType === 'input') {
        setMessages(prev => [...prev, { id: prev.length, content: `> ${jsonData.message}` }]);
        setCurrentMessageIndex(prev => prev + 1);
        setWaitingForInput(true);
      } else if (messageType === 'print') {
        setMessages(prev => [...prev, { id: prev.length, content: `> ${jsonData.message}` }]);
        setCurrentMessageIndex(prev => prev + 1);
        setWaitingForInput(false);
      } else if (messageType === 'session') {
        setSessionId(jsonData.message);
      } else {
        setMessages(prev => [
          ...prev,
          { id: prev.length, content: `> Unknown message type: ${messageType}` },
        ]);
        setCurrentMessageIndex(prev => prev + 1);
      }
    } catch (e) {}
  };

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, currentMessageIndex]);

  const openModal = async () => {
    setIsOpen(true);
    setShouldSend(true);
  };

  const closeModal = () => {
    setIsOpen(false);
    setMessages([]);
    setCurrentMessageIndex(0);
    setInputValue('');
  };

  const handleClear = () => {
    setMessages([]);
    setCurrentMessageIndex(0);
    setInputValue('');
    setWaitingForInput(false);
  };

  const handleClick = () => {
    openModal();
  };

  const handleInputSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (
      inputValue.trim() &&
      waitingForInput &&
      socketRef.current &&
      socketRef.current.readyState === WebSocket.OPEN
    ) {
      socketRef.current.send(inputValue);
      setMessages(prev => [...prev, { id: prev.length, content: `< ${inputValue}` }]);
      setCurrentMessageIndex(prev => prev + 1);
      setInputValue('');
      setWaitingForInput(false);
    } else {
      console.log('Input submission failed:', {
        hasInput: !!inputValue.trim(),
        waitingForInput,
        socketExists: !!socketRef.current,
        socketState: socketRef.current?.readyState,
      });
    }
  };

  return (
    <div>
      <RunCodeButton handleClick={handleClick} />
      <BaseModal isOpen={isOpen} onRequestClose={closeModal} contentLabel="Code Runner Modal">
        <div className="bg-gray-900 text-green-400 font-mono p-4 rounded-lg h-[400px] flex flex-col">
          <div className="flex justify-between items-center mb-2">
            <span className="text-sm">
              Terminal {isConnected ? '(Connected)' : '(Disconnected)'}
            </span>
            <div>
              <span className="mr-2 text-xs">
                {sessionId ? `Session: ${sessionId.substring(0, 12)}...` : 'No Session'}
              </span>
              <button
                onClick={handleClear}
                className="bg-red-600 hover:bg-red-700 text-white px-2 py-1 rounded text-sm"
              >
                Clear
              </button>
            </div>
          </div>
          <div className="flex-1 overflow-y-auto p-2 bg-gray-800 rounded">
            {messages.length === 0 && (
              <div className="text-gray-500 italic">Waiting for messages...</div>
            )}
            {messages.slice(0, currentMessageIndex).map(msg => (
              <div key={msg.id} className="mb-1">
                <span>
                  {msg.content.includes('<') ? (
                    <span className="text-blue-400">{msg.content}</span>
                  ) : (
                    msg.content
                  )}
                </span>
              </div>
            ))}
            <div ref={messagesEndRef} />
          </div>
          {waitingForInput && isConnected && (
            <form onSubmit={handleInputSubmit} className="mt-2">
              <div className="flex">
                <input
                  type="text"
                  value={inputValue}
                  onChange={e => setInputValue(e.target.value)}
                  className="flex-1 bg-gray-700 text-green-400 p-2 rounded-l focus:outline-none"
                  placeholder="Enter value..."
                  autoFocus
                />
                <button
                  type="submit"
                  className="bg-green-700 hover:bg-green-600 text-white px-3 py-2 rounded-r"
                  disabled={!inputValue.trim()}
                >
                  Send
                </button>
              </div>
            </form>
          )}
        </div>
      </BaseModal>
    </div>
  );
};

export default CodeRunnerModal;
