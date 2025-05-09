import React, { useState } from 'react';
import { Refractor, registerLanguage } from 'react-refractor';
import java from 'refractor/lang/java';
import 'prismjs/themes/prism-tomorrow.css';
import { IoCodeSlash } from 'react-icons/io5';
import BaseModal from './BaseModal';
import { LuTriangle } from 'react-icons/lu';
import GenerateCodeButton from '../buttons/GenerateCodeButton';

registerLanguage(java);

interface CodeModalProps {
  generatedCodeData?: { code: string };
  onClick: () => void;
}

const CodeModal: React.FC<CodeModalProps> = ({ generatedCodeData, onClick }) => {
  const [isOpen, setIsOpen] = useState(false);

  const openModal = () => setIsOpen(true);
  const closeModal = () => setIsOpen(false);

  const handleClick = () => {
    onClick();
    openModal();
  };

  return (
    <div>
      <GenerateCodeButton handleClick={handleClick} />
      <BaseModal isOpen={isOpen} onRequestClose={closeModal} contentLabel="Code Modal">
        {generatedCodeData?.code ? (
          <div>
            <h2 className="text-xl font-bold mb-4">Generated Code</h2>
            <div className="overflow-auto max-h-[60vh]">
              <Refractor language="java" value={generatedCodeData.code} />
            </div>
          </div>
        ) : (
          <div>No code available</div>
        )}
      </BaseModal>
    </div>
  );
};

export default CodeModal;
