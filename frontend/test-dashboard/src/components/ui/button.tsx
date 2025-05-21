import React from 'react';

export const Button = ({ children, onClick, className = '', size = 'md' }) => {
  const sizeClasses = {
    sm: 'px-3 py-1 text-sm',
    md: 'px-4 py-2 text-base',
    lg: 'px-6 py-3 text-lg'
  };

  return (
    <button
      onClick={onClick}
      className={`bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition duration-200 ${sizeClasses[size]} ${className}`}
    >
      {children}
    </button>
  );
};
