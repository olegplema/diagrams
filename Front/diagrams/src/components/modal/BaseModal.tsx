import React, { useEffect, ReactNode } from 'react';
import Modal from 'react-modal';

interface IProps {
  isOpen: boolean;
  onRequestClose: () => void;
  children: ReactNode;
  contentLabel: string;
}

const BaseModal: React.FC<IProps> = ({ isOpen, onRequestClose, children, contentLabel }) => {
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
    <Modal
      isOpen={isOpen}
      onRequestClose={onRequestClose}
      contentLabel={contentLabel}
      className="outline-none w-[90%] max-w-[800px] mx-auto"
      overlayClassName="fixed inset-0 bg-black/50 flex items-center justify-center z-[1000]"
      shouldCloseOnOverlayClick={true}
    >
      <div className="relative bg-white rounded-lg border border-gray-300 p-5 max-h-[90vh] overflow-hidden">
        <button
          onClick={onRequestClose}
          className="absolute top-2 right-2 bg-red-500 text-white p-2 rounded hover:bg-red-600 transition-colors"
        >
          Close
        </button>
        {children}
      </div>
    </Modal>
  );
};

export default BaseModal;
