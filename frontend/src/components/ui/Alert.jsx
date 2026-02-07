export const Alert = ({ 
  type = 'info', 
  message, 
  onClose,
  className = '' 
}) => {
  if (!message) return null;

  const styles = {
    success: 'bg-green-50 border-green-200 text-green-800',
    error: 'bg-red-50 border-red-200 text-red-800',
    warning: 'bg-yellow-50 border-yellow-200 text-yellow-800',
    info: 'bg-blue-50 border-blue-200 text-blue-800',
  };

  const icons = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ',
  };

  return (
    <div className={`border-l-4 p-4 ${styles[type]} ${className}`} role="alert">
      <div className="flex items-center justify-between">
        <div className="flex items-center">
          <span className="text-xl mr-2">{icons[type]}</span>
          <p className="font-medium">{message}</p>
        </div>
        {onClose && (
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 focus:outline-none"
          >
            <span className="text-xl">×</span>
          </button>
        )}
      </div>
    </div>
  );
};

export default Alert;