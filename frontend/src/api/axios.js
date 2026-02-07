import axios from 'axios';

/**
 * Axios instance configured for secure cookie-based authentication
 * 
 * Key Security Features:
 * - withCredentials: true (sends HttpOnly cookies automatically)
 * - Auto token refresh on 401 errors
 * - Request/Response interceptors for error handling
 * 
 * Place at: src/api/axios.js
 */

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

// Create axios instance
const axiosInstance = axios.create({
  baseURL: API_URL,
  withCredentials: true, // CRITICAL: This sends cookies with every request
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 seconds timeout
});

// Flag to prevent multiple refresh attempts
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// Request Interceptor
axiosInstance.interceptors.request.use(
  (config) => {
    // Log requests in development
    if (import.meta.env.DEV) {
      console.log(`🚀 Request: ${config.method?.toUpperCase()} ${config.url}`);
    }
    return config;
  },
  (error) => {
    console.error('❌ Request Error:', error);
    return Promise.reject(error);
  }
);

// Response Interceptor
axiosInstance.interceptors.response.use(
  (response) => {
    // Log responses in development
    if (import.meta.env.DEV) {
      console.log(`✅ Response: ${response.config.method?.toUpperCase()} ${response.config.url}`, response.status);
    }
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    // If error is not 401 or request already retried, reject
    if (error.response?.status !== 401 || originalRequest._retry) {
      return Promise.reject(error);
    }

    // If already refreshing, queue this request
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      })
        .then(() => {
          return axiosInstance(originalRequest);
        })
        .catch((err) => {
          return Promise.reject(err);
        });
    }

    originalRequest._retry = true;
    isRefreshing = true;

    try {
      // Attempt to refresh token
      await axiosInstance.post('/auth/refresh');
      
      // Token refreshed successfully
      processQueue(null);
      isRefreshing = false;
      
      // Retry original request
      return axiosInstance(originalRequest);
    } catch (refreshError) {
      // Refresh failed - user needs to login again
      processQueue(refreshError);
      isRefreshing = false;
      
      // Clear any auth state
      localStorage.removeItem('user');
      
      // Redirect to login (will be handled by React Router)
      window.location.href = '/login';
      
      return Promise.reject(refreshError);
    }
  }
);

export default axiosInstance;