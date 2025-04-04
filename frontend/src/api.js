import axios from "axios";

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        "Content-Type": "application/json",
    },
    withCredentials: true, // for the http cookie
});

const publicEndpoints = ["/authenticate", "/refresh", "/authenticate-otp"];

let currentAccessToken = null;

// TODO
// method used to manually update the current access token in a global variable in order to be used by the interceptor
// otherwise, it can use an old access token
// it is used in login() and logout()
// for future: you can use a method in AuthContext smth like const getToken = () => accessToken and send this method as param to setupInterceptors
// and also can get rid of currentAccessToken variable
export const setCurrentAccessToken = (token) => {
    currentAccessToken = token;
}

// interceptor used for adding access token to each secure request
export const setupInterceptors = (refreshAccessToken, logout) => {
    api.interceptors.request.use((config) => {
        const isPublic = publicEndpoints.some((endpoint) => config.url.includes(endpoint));

        if (!isPublic && currentAccessToken) {
            config.headers["Authorization"] = `Bearer ${currentAccessToken}`;
        } else {
            delete config.headers["Authorization"];
        }

        return config;
    }, (error) => Promise.reject(error));

    api.interceptors.response.use((response) => response,
        async (error) => {
            const originalRequest = error.config;

            if (error.response && error.response.status === 401) {
                const errorMessage = error.response.data?.error;

                if (errorMessage === "Token expired or invalid" && !originalRequest._retry) {
                    originalRequest._retry = true;
                    try {
                        const newAccessToken = await refreshAccessToken();
                        originalRequest.headers["Authorization"] = `Bearer ${newAccessToken}`;
                        return api(originalRequest);
                    } catch (error) {
                        console.error("Failed to refresh access token, logging out user");
                        await logout();
                        return Promise.reject(error);
                    }
                }
            }
            return Promise.reject(error);
        })
}

export default api;
