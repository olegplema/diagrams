import React, { useEffect, useState } from 'react';
import Modal from 'react-modal';
import { LuTriangle } from 'react-icons/lu';

interface IProps {
  onClick: () => void;
}

const CodeRunnerModal: React.FC<IProps> = ({ onClick }) => {
  const [isOpen, setIsOpen] = useState(false);

  const openModal = () => setIsOpen(true);
  const closeModal = () => setIsOpen(false);

  const handleClick = () => {
    onClick();
    openModal();
  };

  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'auto';
    }

    return () => {
      document.body.style.overflow = 'auto';
    };
  }, [isOpen]);

  return (
    <div>
      <button
        onClick={handleClick}
        className="bg-green-400 text-white px-2 py-2 rounded hover:bg-green-600 transition-colors flex items-center justify-center"
      >
        <LuTriangle className="rotate-90" size={24} />
      </button>

      <Modal
        isOpen={isOpen}
        onRequestClose={closeModal}
        contentLabel="Code Modal"
        className="outline-none w-[90%] max-w-[800px] mx-auto"
        overlayClassName="fixed inset-0 bg-black/50 flex items-center justify-center z-[1000]"
        shouldCloseOnOverlayClick={true}
      >
        <div className="relative bg-white rounded-lg border border-gray-300 p-5 max-h-[90vh] overflow-hidden">
          <button
            onClick={closeModal}
            className="absolute top-2 right-2 bg-red-500 text-white p-2 rounded hover:bg-red-600 transition-colors"
          >
            Close
          </button>
        </div>
      </Modal>
    </div>
  );
};

export default CodeRunnerModal;
