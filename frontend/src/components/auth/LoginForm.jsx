import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link } from 'react-router-dom';
import { loginSchema } from '../../utils/validation';
import { useLogin } from '../../hooks/useAuth';
import Input from '../ui/Input';
import Button from '../ui/Button';
import Alert from '../ui/Alert';


export const LoginForm = () => {
  const { mutate: login, isPending, error } = useLogin();
  
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });

  const onSubmit = (data) => {
    login(data);
  };

  return (
    <div className="w-full max-w-md">
      <div className="bg-white rounded-lg shadow-md p-8">
        <h2 className="text-2xl font-bold text-center mb-6">
          Login to Procurement System
        </h2>

        {error && (
          <Alert 
            type="error" 
            message={error.response?.data?.message || 'Login failed'} 
            className="mb-4" 
          />
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input
            label="Email"
            type="email"
            placeholder="Enter your email"
            error={errors.email?.message}
            {...register('email')}
          />

          <Input
            label="Password"
            type="password"
            placeholder="Enter your password"
            error={errors.password?.message}
            {...register('password')}
          />

          <div className="flex items-center justify-between text-sm">
            <Link
              to="/forgot-password"
              className="text-blue-600 hover:text-blue-800"
            >
              Forgot password?
            </Link>
          </div>

          <Button
            type="submit"
            variant="primary"
            size="lg"
            isLoading={isPending}
            className="w-full"
          >
            Login
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-gray-600">
          Don't have an account?{' '}
          <Link
            to="/register"
            className="text-blue-600 hover:text-blue-800 font-medium"
          >
            Register here
          </Link>
        </p>
      </div>
    </div>
  );
};

export default LoginForm;