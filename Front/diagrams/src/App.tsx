import React from 'react';
import '@xyflow/react/dist/style.css';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import 'prismjs/themes/prism-tomorrow.css';
import FlowchartEditor from './components/flowchart/FlowchartEditor';
import Header from './components/header/Header';


const queryClient = new QueryClient();

const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <div className="min-h-screen bg-gray-50">
        <Header />
        <main className="p-4">
          <FlowchartEditor />
        </main>
      </div>
    </QueryClientProvider>
  );
};

export default App;
