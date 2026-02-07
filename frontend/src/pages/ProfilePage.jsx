import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link } from 'react-router-dom';
import { useUser, useUpdateProfile, useChangePassword } from '../hooks/useAuth';
import { updateProfileSchema, changePasswordSchema } from '../utils/validation';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import Alert from '../components/ui/Alert';


const ProfilePage = () => {
    const { data: user } = useUser();
  const [activeTab, setActiveTab] = useState('profile');
  const [message, setMessage] = useState(null);

  // Update Profile
  const { mutate: updateProfile, isLoading: isUpdating } = useUpdateProfile();
  const {
    register: registerProfile,
    handleSubmit: handleProfileSubmit,
    formState: { errors: profileErrors },
  } = useForm({
    resolver: zodResolver(updateProfileSchema),
    defaultValues: {
      firstName: user?.firstName || '',
      lastName: user?.lastName || '',
      phone: user?.phone || '',
    },
  });

  const onProfileSubmit = (data) => {
    updateProfile(data, {
      onSuccess: () => {
        setMessage({ type: 'success', text: 'Profile updated successfully!' });
      },
      onError: (error) => {
        setMessage({
          type: 'error',
          text: error.response?.data?.message || 'Failed to update profile',
        });
      },
    });
  };

  // Change Password
  const { mutate: changePassword, isLoading: isChangingPassword } = useChangePassword();
  const {
    register: registerPassword,
    handleSubmit: handlePasswordSubmit,
    formState: { errors: passwordErrors },
    reset: resetPasswordForm,
  } = useForm({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: {
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
    },
  });

  const onPasswordSubmit = (data) => {
    const { confirmPassword, ...passwordData } = data;
    changePassword(passwordData, {
      onSuccess: () => {
        resetPasswordForm();
        setMessage({
          type: 'success',
          text: 'Password changed successfully! Redirecting to login...',
        });
      },
      onError: (error) => {
        setMessage({
          type: 'error',
          text: error.response?.data?.message || 'Failed to change password',
        });
      },
    });
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900">Profile Settings</h1>
          <Link to="/dashboard">
            <Button variant="ghost">Back to Dashboard</Button>
          </Link>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {message && (
          <Alert
            type={message.type}
            message={message.text}
            onClose={() => setMessage(null)}
            className="mb-4"
          />
        )}

        <div className="bg-white rounded-lg shadow">
          {/* Tabs */}
          <div className="border-b border-gray-200">
            <nav className="flex">
              <button
                onClick={() => setActiveTab('profile')}
                className={`px-6 py-3 font-medium text-sm border-b-2 ${
                  activeTab === 'profile'
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700'
                }`}
              >
                Edit Profile
              </button>
              <button
                onClick={() => setActiveTab('password')}
                className={`px-6 py-3 font-medium text-sm border-b-2 ${
                  activeTab === 'password'
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700'
                }`}
              >
                Change Password
              </button>
            </nav>
          </div>

          {/* Tab Content */}
          <div className="p-6">
            {activeTab === 'profile' && (
              <form onSubmit={handleProfileSubmit(onProfileSubmit)} className="space-y-4">
                <h3 className="text-lg font-medium mb-4">Update Your Profile</h3>

                <Input
                  label="Email"
                  type="email"
                  value={user?.email}
                  disabled
                  className="bg-gray-100"
                />

                <div className="grid grid-cols-2 gap-4">
                  <Input
                    label="First Name"
                    error={profileErrors.firstName?.message}
                    {...registerProfile('firstName')}
                  />

                  <Input
                    label="Last Name"
                    error={profileErrors.lastName?.message}
                    {...registerProfile('lastName')}
                  />
                </div>

                <Input
                  label="Phone"
                  placeholder="+1234567890"
                  error={profileErrors.phone?.message}
                  {...registerProfile('phone')}
                />

                <Button
                  type="submit"
                  variant="primary"
                  size="lg"
                  isLoading={isUpdating}
                >
                  Update Profile
                </Button>
              </form>
            )}

            {activeTab === 'password' && (
              <form onSubmit={handlePasswordSubmit(onPasswordSubmit)} className="space-y-4">
                <h3 className="text-lg font-medium mb-4">Change Your Password</h3>

                <Alert
                  type="info"
                  message="After changing your password, you will be logged out and need to login again."
                />

                <Input
                  label="Current Password"
                  type="password"
                  error={passwordErrors.currentPassword?.message}
                  {...registerPassword('currentPassword')}
                />

                <Input
                  label="New Password"
                  type="password"
                  placeholder="Min 8 chars, 1 upper, 1 lower, 1 digit, 1 special"
                  error={passwordErrors.newPassword?.message}
                  {...registerPassword('newPassword')}
                />

                <Input
                  label="Confirm New Password"
                  type="password"
                  error={passwordErrors.confirmPassword?.message}
                  {...registerPassword('confirmPassword')}
                />

                <Button
                  type="submit"
                  variant="danger"
                  size="lg"
                  isLoading={isChangingPassword}
                >
                  Change Password
                </Button>
              </form>
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default ProfilePage;