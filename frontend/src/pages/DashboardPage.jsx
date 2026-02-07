import { Link } from 'react-router-dom';
import { useUser, useLogout } from '../hooks/useAuth';
import Button from '../components/ui/Button';

const DashboardPage = () => {
  const { data: user } = useUser();
  const { mutate: logout, isPending } = useLogout();

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900">
            Procurement System
          </h1>
          <div className="flex items-center gap-4">
            <span className="text-gray-700">
              Welcome, {user?.firstName} {user?.lastName}
            </span>
            <Link to="/profile">
              <Button variant="ghost" size="sm">
                Profile
              </Button>
            </Link>
            <Button
              variant="danger"
              size="sm"
              onClick={() => logout()}
              isLoading={isPending}
            >
              Logout
            </Button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold mb-4">Dashboard</h2>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {/* User Info Card */}
            <div className="border rounded-lg p-4">
              <h3 className="font-medium text-gray-900 mb-2">User Information</h3>
              <div className="space-y-2 text-sm text-gray-600">
                <p><strong>Email:</strong> {user?.email}</p>
                <p><strong>Name:</strong> {user?.firstName} {user?.lastName}</p>
                <p><strong>Phone:</strong> {user?.phone || 'Not set'}</p>
                <p><strong>Department:</strong> {user?.departmentName || 'Not assigned'}</p>
              </div>
            </div>

            {/* Roles Card */}
            <div className="border rounded-lg p-4">
              <h3 className="font-medium text-gray-900 mb-2">Roles</h3>
              <div className="space-y-2">
                {user?.roles?.map((role) => (
                  <span
                    key={role}
                    className="inline-block bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded mr-2"
                  >
                    {role}
                  </span>
                ))}
              </div>
            </div>

            {/* Account Status Card */}
            <div className="border rounded-lg p-4">
              <h3 className="font-medium text-gray-900 mb-2">Account Status</h3>
              <div className="space-y-2 text-sm text-gray-600">
                <p>
                  <strong>Status:</strong>{' '}
                  <span className="text-green-600">Active</span>
                </p>
                <p>
                  <strong>Last Login:</strong>{' '}
                  {user?.lastLoginAt
                    ? new Date(user.lastLoginAt).toLocaleString()
                    : 'N/A'}
                </p>
              </div>
            </div>
          </div>

          {/* Quick Actions */}
          <div className="mt-8">
            <h3 className="font-medium text-gray-900 mb-4">Quick Actions</h3>
            <div className="flex gap-4">
              <Link to="/profile">
                <Button variant="primary">Edit Profile</Button>
              </Link>
              <Link to="/profile?tab=password">
                <Button variant="secondary">Change Password</Button>
              </Link>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default DashboardPage;