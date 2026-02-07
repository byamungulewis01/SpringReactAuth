import axiosInstance from '../axios';

/**
 * Authentication API endpoints
 * All requests automatically include HttpOnly cookies via withCredentials: true
 * 
 * Place at: src/api/endpoints/auth.js
 */

export const authAPI = {
  /**
   * Register a new user
   */
  register: async (userData) => {
    const response = await axiosInstance.post('/auth/register', userData);
    return response.data;
  },

  /**
   * Login user
   * Returns user data (tokens are in HttpOnly cookies)
   */
  login: async (credentials) => {
    const response = await axiosInstance.post('/auth/login', credentials);
    return response.data;
  },

  /**
   * Logout user
   * Clears HttpOnly cookies on backend
   */
  logout: async () => {
    const response = await axiosInstance.post('/auth/logout');
    return response.data;
  },

  /**
   * Get current user profile
   */
  getProfile: async () => {
    const response = await axiosInstance.get('/auth/profile');
    return response.data;
  },

  /**
   * Update user profile
   */
  updateProfile: async (profileData) => {
    const response = await axiosInstance.put('/auth/profile', profileData);
    return response.data;
  },

  /**
   * Change password
   */
  changePassword: async (passwordData) => {
    const response = await axiosInstance.post('/auth/change-password', passwordData);
    return response.data;
  },

  /**
   * Request password reset
   */
  forgotPassword: async (email) => {
    const response = await axiosInstance.post('/auth/forgot-password', { email });
    return response.data;
  },

  /**
   * Reset password with token
   */
  resetPassword: async (resetData) => {
    const response = await axiosInstance.post('/auth/reset-password', resetData);
    return response.data;
  },

  /**
   * Refresh access token
   * Called automatically by axios interceptor
   */
  refreshToken: async () => {
    const response = await axiosInstance.post('/auth/refresh');
    return response.data;
  },
};

export default authAPI;