import React from 'react';
import { Handle, Position } from '@xyflow/react';

interface IProps {
  type: 'source' | 'target';
  position: Position;
  id: string;
  label?: string;
  isVisible?: boolean;
  className?: string;
  labelClassName?: string;
  onConnect?: () => void;
}

const HandleWrapper: React.FC<IProps> = ({
  type,
  position,
  id,
  label,
  isVisible = true,
  className,
  labelClassName,
  onConnect,
}) => {
  return (
    <Handle
      type={type}
      position={position}
      id={id}
      className={`${className} ${isVisible ? '' : 'opacity-0 pointer-events-none'}`}
      onConnect={onConnect}
    >
      <div className={`${labelClassName} ${isVisible ? '' : 'opacity-0 pointer-events-none'}`}>
        {label}
      </div>
    </Handle>
  );
};

export default HandleWrapper;
