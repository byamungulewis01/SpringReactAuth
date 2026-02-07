import { useLocation } from 'react-router-dom';
import LoginForm from '../components/auth/LoginForm';
import Alert from '../components/ui/Alert';


const LoginPage = () => {
  const location = useLocation();
  const message = location.state?.message;

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-md">
        {message && (
          <Alert type="success" message={message} className="mb-4" />
        )}
        <LoginForm />
      </div>
    </div>
  );
};

export default LoginPage;