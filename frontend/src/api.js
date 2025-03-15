import axios from "axios";

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        "Content-Type": "application/json",
    },
    withCredentials: true, // for the http cookie
});

const publicEndpoints = ["/register", "/authenticate", "/refresh"];

// interceptor used for adding access token to each secure request
export const setAuthToken = (token) => {
    api.interceptors.request.use((config) => {
        const isPublic = publicEndpoints.some((endpoint) => config.url.includes(endpoint));

        if (!isPublic && token) {
            config.headers["Authorization"] = `Bearer ${token}`;
        } else {
            delete config.headers["Authorization"];
        }

        return config;
    });
};

export default api;
