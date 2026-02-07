import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useSearchParams } from 'react-router-dom';
import { resetPasswordSchema } from '../utils/validation';
import { useResetPassword } from '../hooks/useAuth';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import Alert from '../components/ui/Alert';


const ResetPasswordPage = () => {
  const [searchParams] = useSearchParams();
  const tokenFromUrl = searchParams.get('token');
  const emailFromUrl = searchParams.get('email');

  const { mutate: resetPassword, isPending, error, isSuccess } = useResetPassword();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: {
      email: emailFromUrl || '',
      token: tokenFromUrl || '',
      newPassword: '',
      confirmPassword: '',
    },
  });

  const onSubmit = (data) => {
    // Remove confirmPassword before sending to API
    const { confirmPassword, ...resetData } = data;
    resetPassword(resetData);
  };

  // Success State - Show after password is reset
  if (isSuccess) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100 py-12 px-4 sm:px-6 lg:px-8">
        <div className="w-full max-w-md">
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            {/* Success Icon */}
            <div className="mb-4">
              <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-green-100">
                <svg 
                  className="h-10 w-10 text-green-600" 
                  fill="none" 
                  viewBox="0 0 24 24" 
                  stroke="currentColor"
                >
                  <path 
                    strokeLinecap="round" 
                    strokeLinejoin="round" 
                    strokeWidth={2} 
                    d="M5 13l4 4L19 7" 
                  />
                </svg>
              </div>
            </div>

            <h2 className="text-2xl font-bold text-gray-900 mb-2">
              Password Reset Successful!
            </h2>
            
            <p className="text-gray-600 mb-6">
              Your password has been successfully reset. You can now login with your new password.
            </p>

            <Link to="/login">
              <Button variant="primary" size="lg" className="w-full">
                Go to Login
              </Button>
            </Link>
          </div>
        </div>
      </div>
    );
  }

  // Invalid Link State - No token or email in URL
  if (!tokenFromUrl || !emailFromUrl) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100 py-12 px-4 sm:px-6 lg:px-8">
        <div className="w-full max-w-md">
          <div className="bg-white rounded-lg shadow-md p-8">
            <h2 className="text-2xl font-bold text-center mb-6 text-red-600">
              Invalid Reset Link
            </h2>

            <Alert
              type="error"
              message="This password reset link is invalid or has expired. Please request a new password reset link."
              className="mb-6"
            />

            <div className="space-y-3">
              <Link to="/forgot-password">
                <Button variant="primary" size="lg" className="w-full">
                  Request New Reset Link
                </Button>
              </Link>
              
              <Link to="/login">
                <Button variant="ghost" size="lg" className="w-full">
                  Back to Login
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Reset Password Form State
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-lg shadow-md p-8">
          <h2 className="text-2xl font-bold text-center mb-6">
            Reset Your Password
          </h2>

          {error && (
            <Alert
              type="error"
              message={error.response?.data?.message || 'Failed to reset password. The reset link may have expired.'}
              className="mb-4"
            />
          )}

          <Alert
            type="info"
            message="Please enter your new password below."
            className="mb-4"
          />

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            {/* Email - Pre-filled and disabled */}
            <Input
              label="Email"
              type="email"
              disabled
              className="bg-gray-100 cursor-not-allowed"
              error={errors.email?.message}
              {...register('email')}
            />

            {/* Token - Hidden field */}
            <input type="hidden" {...register('token')} />

            {/* New Password */}
            <Input
              label="New Password"
              type="password"
              placeholder="Enter your new password"
              error={errors.newPassword?.message}
              {...register('newPassword')}
            />

            {/* Confirm Password */}
            <Input
              label="Confirm New Password"
              type="password"
              placeholder="Re-enter your new password"
              error={errors.confirmPassword?.message}
              {...register('confirmPassword')}
            />

            {/* Password Requirements Info */}
            <div className="bg-blue-50 border border-blue-200 rounded-md p-4">
              <p className="text-sm font-medium text-blue-900 mb-2">
                Password Requirements:
              </p>
              <ul className="text-xs text-blue-800 space-y-1">
                <li className="flex items-start">
                  <span className="mr-2">•</span>
                  <span>At least 8 characters long</span>
                </li>
                <li className="flex items-start">
                  <span className="mr-2">•</span>
                  <span>At least one uppercase letter (A-Z)</span>
                </li>
                <li className="flex items-start">
                  <span className="mr-2">•</span>
                  <span>At least one lowercase letter (a-z)</span>
                </li>
                <li className="flex items-start">
                  <span className="mr-2">•</span>
                  <span>At least one digit (0-9)</span>
                </li>
                <li className="flex items-start">
                  <span className="mr-2">•</span>
                  <span>At least one special character (@$!%*?&)</span>
                </li>
              </ul>
            </div>

            <Button
              type="submit"
              variant="primary"
              size="lg"
              isLoading={isPending}
              className="w-full"
            >
              Reset Password
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
        </div>
      </div>
    </div>
  );
};

export default ResetPasswordPage;