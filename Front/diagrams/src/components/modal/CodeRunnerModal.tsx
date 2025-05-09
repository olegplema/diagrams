import React, { useState } from 'react';
import { LuTriangle } from 'react-icons/lu';
import BaseModal from './BaseModal';
import RunCodeButton from '../buttons/RunCodeButton';

interface CodeRunnerModalProps {
  onClick: () => void;
}

const CodeRunnerModal: React.FC<CodeRunnerModalProps> = ({ onClick }) => {
  const [isOpen, setIsOpen] = useState(false);

  const openModal = () => setIsOpen(true);
  const closeModal = () => setIsOpen(false);

  const handleClick = () => {
    onClick();
    openModal();
  };

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
