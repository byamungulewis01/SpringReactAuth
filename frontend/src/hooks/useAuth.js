import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import authAPI from '../api/endpoints/auth';


// Query Keys
export const authKeys = {
  user: ['user'],
  profile: ['profile'],
};

/**
 * Get Current User
 * This is the SINGLE SOURCE OF TRUTH for auth state
 */
export const useUser = () => {
  return useQuery({
    queryKey: authKeys.user,
    queryFn: async () => {
      try {
        const data = await authAPI.getProfile();
        return data;
      } catch (error) {
        // If 401, user is not authenticated
        if (error.response?.status === 401) {
          return null;
        }
        throw error;
      }
    },
    retry: false,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

/**
 * Check if user is authenticated
 * Derived from useUser
 */
export const useIsAuthenticated = () => {
  const { data: user } = useUser();
  return !!user;
};

/**
 * Login Mutation
 */
export const useLogin = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: authAPI.login,
    onSuccess: (data) => {
      // Set user data in cache
      queryClient.setQueryData(authKeys.user, data.user);
      navigate('/dashboard');
    },
  });
};

/**
 * Register Mutation
 */
export const useRegister = () => {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: authAPI.register,
    onSuccess: () => {
      navigate('/login', { 
        state: { message: 'Registration successful! Please login.' } 
      });
    },
  });
};

/**
 * Logout Mutation
 */
export const useLogout = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: authAPI.logout,
    onSuccess: () => {
      // Clear all cached data
      queryClient.clear();
      navigate('/login');
    },
    onError: () => {
      // Logout locally even if API call fails
      queryClient.clear();
      navigate('/login');
    },
  });
};

/**
 * Update Profile Mutation
 */
export const useUpdateProfile = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: authAPI.updateProfile,
    onSuccess: (data) => {
      // Update user data in cache
      queryClient.setQueryData(authKeys.user, data);
    },
  });
};

/**
 * Change Password Mutation
 */
export const useChangePassword = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: authAPI.changePassword,
    onSuccess: () => {
      queryClient.clear();
      navigate('/login', { 
        state: { message: 'Password changed successfully! Please login again.' } 
      });
    },
  });
};

/**
 * Forgot Password Mutation
 */
export const useForgotPassword = () => {
  return useMutation({
    mutationFn: authAPI.forgotPassword,
  });
};

/**
 * Reset Password Mutation
 */
export const useResetPassword = () => {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: authAPI.resetPassword,
    onSuccess: () => {
      navigate('/login', { 
        state: { message: 'Password reset successful! Please login.' } 
      });
    },
  });
};