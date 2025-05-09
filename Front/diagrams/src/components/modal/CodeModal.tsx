import React, { useState, useEffect } from 'react';
import Modal from 'react-modal';
import { Refractor, registerLanguage } from 'react-refractor';
import java from 'refractor/lang/java';
import 'prismjs/themes/prism-tomorrow.css';
import { IoCodeSlash } from 'react-icons/io5';

registerLanguage(java);
Modal.setAppElement('#root');

interface IProps {
  generatedCodeData?: { code: string };
  onClick: () => void;
}

const CodeModal = ({ generatedCodeData, onClick }: IProps) => {
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
        className="bg-blue-500 text-white px-2 py-2 rounded hover:bg-blue-600 transition-colors"
      >
        <IoCodeSlash size={24} />
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
          {generatedCodeData?.code ? (
            <div>
              <h2 className="text-xl font-bold mb-4">Generated Code</h2>
              <div className="overflow-auto max-h-[60vh] contents">
                <Refractor language="java" value={generatedCodeData.code} />
              </div>
            </div>
          ) : (
            <div>No code available</div>
          )}
        </div>
      </Modal>
    </div>
  );
};

export default CodeModal;
