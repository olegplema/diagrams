import React from 'react';

interface IProps {
  expression?: string;
  setExpression?: (expression: string) => void;
  validatorRegex: RegExp;
  placeholder: string;
}

const ExpressionInput: React.FC<IProps> = ({ expression, setExpression, validatorRegex, placeholder }) => {
  return (
    <div>
      <label className="block text-sm">Expression:</label>
      <input
        type="text"
        value={expression}
        onChange={(e) => setExpression && setExpression(e.target.value)}
        className={`w-full p-1 border rounded ${validatorRegex.test(expression || '') ? '' : 'border-red-500'}`}
        placeholder={placeholder}
      />
    </div>
  );
};

export default ExpressionInput;