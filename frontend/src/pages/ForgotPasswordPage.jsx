import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link } from 'react-router-dom';
import { forgotPasswordSchema } from '../utils/validation';
import { useForgotPassword } from '../hooks/useAuth';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import Alert from '../components/ui/Alert';


const ForgotPasswordPage = () => {
  const [emailSent, setEmailSent] = useState(false);
  const { mutate: forgotPassword, isLoading, error } = useForgotPassword();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(forgotPasswordSchema),
  });

  const onSubmit = (data) => {
    forgotPassword(data.email, {
      onSuccess: () => {
        setEmailSent(true);
      },
    });
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-lg shadow-md p-8">
          <h2 className="text-2xl font-bold text-center mb-6">
            Forgot Password
          </h2>

          {emailSent ? (
            <div className="text-center">
              <Alert
                type="success"
                message="Password reset email sent! Please check your inbox."
                className="mb-4"
              />
              <p className="text-sm text-gray-600 mb-4">
                If you don't see the email, please check your spam folder.
              </p>
              <Link to="/login">
                <Button variant="primary" size="lg" className="w-full">
                  Back to Login
                </Button>
              </Link>
            </div>
          ) : (
            <>
              {error && (
                <Alert type="error" message={error.response?.data?.message || 'Failed to send reset email'} className="mb-4" />
              )}

              <p className="text-sm text-gray-600 mb-4">
                Enter your email address and we'll send you a link to reset your password.
              </p>

              <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                <Input
                  label="Email"
                  type="email"
                  placeholder="Enter your email"
                  error={errors.email?.message}
                  {...register('email')}
                />

                <Button
                  type="submit"
                  variant="primary"
                  size="lg"
                  isLoading={isLoading}
                  className="w-full"
                >
                  Send Reset Link
                </Button>
              </form>

              <p className="mt-6 text-center text-sm text-gray-600">
                Remember your password?{' '}
                <Link
                  to="/login"
                  className="text-blue-600 hover:text-blue-800 font-medium"
                >
                  Login here
                </Link>
              </p>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default ForgotPasswordPage;