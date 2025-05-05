import React, { useState, useEffect, CSSProperties } from 'react';
import Modal from 'react-modal';
import { Refractor, registerLanguage } from 'react-refractor';
import java from 'refractor/lang/java';
import 'prismjs/themes/prism-tomorrow.css';

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

  const customStyles: ReactModal.Styles = {
    overlay: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      backgroundColor: 'rgba(0, 0, 0, 0.5)',
      zIndex: 1000
    } as CSSProperties,
    content: {
      position: 'relative' as 'relative',
      inset: 'auto',
      overflow: 'hidden',
      maxHeight: '90vh',
      width: '90%',
      maxWidth: '800px',
      padding: '20px',
      margin: '0 auto',
      border: '1px solid #ccc',
      borderRadius: '8px',
      background: '#fff'
    }
  };

  return (
    <div>
      <button onClick={handleClick} className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600">
        Show Code
      </button>

      <Modal
        isOpen={isOpen}
        onRequestClose={closeModal}
        contentLabel="Code Modal"
        style={customStyles}
        shouldCloseOnOverlayClick={true}
        className="outline-none"
      >
        <button
          onClick={closeModal}
          className="absolute top-2 right-2 text-white bg-red-500 p-2 rounded"
        >
          Close
        </button>
        {generatedCodeData?.code ? (
          <div>
            <h2 className="text-xl font-bold mb-4">Generated Code</h2>
            <div className="overflow-auto max-h-[60vh]" style={{ contain: 'content' }}>
              <Refractor language="java" value={generatedCodeData.code} />
            </div>
          </div>
        ) : (
          <div>No code available</div>
        )}
      </Modal>
    </div>
  );
};

export default CodeModal;
