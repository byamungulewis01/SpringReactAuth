import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link } from 'react-router-dom';
import { registerSchema } from '../../utils/validation';
import { useRegister } from '../../hooks/useAuth';
import Input from '../ui/Input';
import Button from '../ui/Button';
import Alert from '../ui/Alert';


export const RegisterForm = () => {
  const { mutate: register, isLoading, error } = useRegister();
  
  const {
    register: registerField,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      confirmPassword: '',
    },
  });

  const onSubmit = (data) => {
    // Remove confirmPassword before sending to API
    const { confirmPassword, ...userData } = data;
    register(userData);
  };

  return (
    <div className="w-full max-w-md">
      <div className="bg-white rounded-lg shadow-md p-8">
        <h2 className="text-2xl font-bold text-center mb-6">
          Create Account
        </h2>

        {error && (
          <Alert type="error" message={error} className="mb-4" />
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="First Name"
              placeholder="John"
              error={errors.firstName?.message}
              {...registerField('firstName')}
            />

            <Input
              label="Last Name"
              placeholder="Doe"
              error={errors.lastName?.message}
              {...registerField('lastName')}
            />
          </div>

          <Input
            label="Email"
            type="email"
            placeholder="john.doe@example.com"
            error={errors.email?.message}
            {...registerField('email')}
          />

          <Input
            label="Password"
            type="password"
            placeholder="Min 8 chars, 1 upper, 1 lower, 1 digit, 1 special"
            error={errors.password?.message}
            {...registerField('password')}
          />

          <Input
            label="Confirm Password"
            type="password"
            placeholder="Re-enter your password"
            error={errors.confirmPassword?.message}
            {...registerField('confirmPassword')}
          />

          <Button
            type="submit"
            variant="primary"
            size="lg"
            isLoading={isLoading}
            className="w-full"
          >
            Register
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-gray-600">
          Already have an account?{' '}
          <Link
            to="/login"
            className="text-blue-600 hover:text-blue-800 font-medium"
          >
            Login here
          </Link>
        </p>
      </div>
    </div>
  );
};

export default RegisterForm;