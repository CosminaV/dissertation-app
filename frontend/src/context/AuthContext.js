import {createContext, useState, useContext, useEffect, useCallback, useRef} from "react";
import api, {setupInterceptors, setCurrentAccessToken} from "../api";
import { useNavigate } from "react-router-dom";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [accessToken, setAccessToken] = useState(null);
    const navigate = useNavigate()
    const hasRefreshed = useRef(false);
    const [loading, setLoading] = useState(true);

    const login = (token) => {
        setAccessToken(token);
        setCurrentAccessToken(token);
        sessionStorage.setItem("wasLoggedIn", "true");
    };

    // remove the access token from context when user logs out
    const logout = useCallback(async () => {
        try {
            await api.post("/auth/logout");
        } catch (error) {
            console.error("Error during logout", error);
        }
        setAccessToken(null);
        setCurrentAccessToken(null);
        sessionStorage.removeItem("wasLoggedIn");
        hasRefreshed.current = false;
        navigate("/login");
    }, [navigate]);

    const refreshAccessToken = useCallback(async () => {
        setLoading(true);
        try {
            const response = await api.post("/auth/refresh");
            const newAccessToken = response.data.accessToken;
            if (newAccessToken) {
                login(newAccessToken);
                return newAccessToken;
            }
        } catch (error) {
            console.error("Refresh token expired or invalid. Logging user out.", error);
            setAccessToken(null);
            sessionStorage.removeItem("wasLoggedIn")
            navigate("/login");
            throw error; // so interceptor knows if the refresh fails
        } finally {
            setLoading(false);
        }
    }, [navigate]);

    useEffect(() => {
        setupInterceptors(refreshAccessToken, logout);
    }, [refreshAccessToken, logout]);

    useEffect(() => {
        // if (hasRefreshed.current) return;

        const wasLoggedIn = sessionStorage.getItem("wasLoggedIn");
        if (wasLoggedIn && !accessToken && !hasRefreshed.current) { // manual refresh
            console.log("Attempting refresh access token...")
            refreshAccessToken()
                .then(() => hasRefreshed.current = true)
                .catch(() => hasRefreshed.current = false);
        } else {
            setLoading(false);
        }
    }, [accessToken, refreshAccessToken]);

    return (
        <AuthContext.Provider value={{ accessToken, login, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
