import React, { useEffect, useRef, useState } from 'react';
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

  const connectWebSocket = () => {
    if (socketRef.current?.readyState === WebSocket.OPEN) {
      console.log('WebSocket already connected, skipping connect');
      return;
    }

    const wsUrl = process.env.REACT_APP_WS_URL || 'ws://localhost:8888/connect-ws';
    const ws = new WebSocket(wsUrl);

    ws.onopen = () => {
      console.log('WebSocket connection established');
      setIsConnected(true);
      setShouldSend(true); // Trigger code execution after connection
    };

    ws.onclose = event => {
      console.log('WebSocket closed:', event.reason);
      socketRef.current = null;
      setIsConnected(false);
      setWaitingForInput(false);
      setSessionId(null);
    };

    ws.onerror = error => {
      console.error('WebSocket error:', error);
      ws.close();
    };

    ws.onmessage = event => {
      console.log('Raw WebSocket message received:', event.data);
      handleWebSocketMessage(event.data);
    };

    socketRef.current = ws;
  };

  useEffect(() => {
    if (isOpen) {
      connectWebSocket();
    } else {
      if (socketRef.current) {
        console.log('Closing WebSocket connection...');
        socketRef.current.close(1000, 'Modal closed');
        socketRef.current = null;
      }
      setIsConnected(false);
      setSessionId(null);
      setWaitingForInput(false);
      setMessages([]);
      setCurrentMessageIndex(0);
      setInputValue('');
    }

    return () => {
      if (socketRef.current) {
        socketRef.current.close(1000, 'Component unmounted');
        socketRef.current = null;
      }
    };
  }, [isOpen]);

  useEffect(() => {
    if (shouldSend && sessionId && isConnected) {
      const { threads, variables } = generateJSON(nodes, edges);
      console.log(generateJSON(nodes, edges));
      startRunningCode({ clientSocketId: sessionId, threads, variables })
        .then(() => console.log('Code execution request sent'))
        .catch(err => {
          console.error('Error starting code execution:', err);
          setMessages(prev => [
            ...prev,
            { id: prev.length, content: `> Error: Failed to start code execution` },
          ]);
          setCurrentMessageIndex(prev => prev + 1);
        });
      setShouldSend(false);
    }
  }, [sessionId, shouldSend, isConnected]);

  const handleWebSocketMessage = (data: string) => {
    try {
      const jsonData = JSON.parse(data);
      console.log('Parsed WebSocket message:', jsonData);
      const messageType = jsonData.type?.toLowerCase();

      if (!messageType) {
        console.error('Message has no type property:', jsonData);
        setMessages(prev => [
          ...prev,
          { id: prev.length, content: `> Error: Invalid message format` },
        ]);
        setCurrentMessageIndex(prev => prev + 1);
        return;
      }

      if (messageType === 'input') {
        console.log('Processing input request:', jsonData.message);
        setMessages(prev => [...prev, { id: prev.length, content: `> ${jsonData.message}` }]);
        setCurrentMessageIndex(prev => prev + 1);
        setWaitingForInput(true);
      } else if (messageType === 'print') {
        console.log('Processing print message');
        setMessages(prev => [...prev, { id: prev.length, content: `> ${jsonData.message}` }]);
        setCurrentMessageIndex(prev => prev + 1);
        setWaitingForInput(false);
      } else if (messageType === 'session') {
        console.log('Processing session message');
        setSessionId(jsonData.message);
      } else {
        console.log('Unknown message type:', messageType);
        setMessages(prev => [
          ...prev,
          { id: prev.length, content: `> Unknown message type: ${messageType}` },
        ]);
        setCurrentMessageIndex(prev => prev + 1);
      }
    } catch (e) {
      console.error('Error handling WebSocket message:', e);
      setMessages(prev => [
        ...prev,
        { id: prev.length, content: `> Error parsing message: ${data}` },
      ]);
      setCurrentMessageIndex(prev => prev + 1);
    }
  };

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, currentMessageIndex]);

  const openModal = async () => {
    setIsOpen(true);
    setMessages([]); // Reset messages
    setCurrentMessageIndex(0); // Reset message index
    setInputValue(''); // Reset input
    setWaitingForInput(false); // Reset input state
    setSessionId(null); // Reset session
    setIsConnected(false); // Reset connection state
    setShouldSend(true); // Trigger code execution
  };

  const closeModal = () => {
    setIsOpen(false);
    setMessages([]);
    setCurrentMessageIndex(0);
    setInputValue('');
    setWaitingForInput(false);
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
      console.log('Sending input value to server:', inputValue);
      try {
        socketRef.current.send(inputValue);
        setMessages(prev => [...prev, { id: prev.length, content: `< ${inputValue}` }]);
        setCurrentMessageIndex(prev => prev + 1);
        setInputValue('');
        setWaitingForInput(false);
      } catch (error) {
        console.error('Error sending input to server:', error);
        setMessages(prev => [
          ...prev,
          { id: prev.length, content: `> Error sending input to server` },
        ]);
        setCurrentMessageIndex(prev => prev + 1);
      }
    } else {
      console.log('Input submission failed:', {
        hasInput: !!inputValue.trim(),
        waitingForInput,
        socketExists: !!socketRef.current,
        socketState: socketRef.current?.readyState,
      });

      setMessages(prev => [
        ...prev,
        {
          id: prev.length,
          content: `> Error: Unable to send input. Connection state: ${
            socketRef.current
              ? socketRef.current.readyState === WebSocket.OPEN
                ? 'Connected'
                : 'Not ready'
              : 'Disconnected'
          }`,
        },
      ]);
      setCurrentMessageIndex(prev => prev + 1);
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
              {waitingForInput ? ' - Waiting for input' : ''}
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
                  disabled={!inputValue.trim() || !isConnected}
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
