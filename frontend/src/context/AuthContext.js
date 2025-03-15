import {createContext, useState, useContext, useEffect, useCallback, useRef} from "react";
import api from "../api";
import { useNavigate } from "react-router-dom";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [accessToken, setAccessToken] = useState(null);
    const navigate = useNavigate()
    const hasRefreshed = useRef(false);
    const [loading, setLoading] = useState(true);

    const login = (token) => {
        setAccessToken(token);
    };

    // remove the access token from context when user logs out
    const logout = useCallback(async () => {
        try {
            await api.post("/auth/logout");
        } catch (error) {
            console.error("Error during logout", error);
        }
        setAccessToken(null);
        navigate("/login");
    }, [navigate]);

    const refreshAccessToken = useCallback(async () => {
        setLoading(true);
        try {
            const response = await api.post("/auth/refresh");

            if (response.data.accessToken) {
                setAccessToken(response.data.accessToken);
                navigate("/dashboard", {replace: true});
            }
        } catch (error) {
            console.error("Refresh token expired or invalid", error);
            setAccessToken(null);
            sessionStorage.removeItem("wasLoggedIn")
            navigate("/login");
        } finally {
            setLoading(false);
        }
    }, [navigate]);

    useEffect(() => {
        if (hasRefreshed.current) return;

        const wasLoggedIn = sessionStorage.getItem("wasLoggedIn");
        if (wasLoggedIn && !accessToken) { // manual refresh
            console.log("Attempting refresh access token...")
            refreshAccessToken();
            hasRefreshed.current = true;
        }
    }, [accessToken, refreshAccessToken]);

    return (
        <AuthContext.Provider value={{ accessToken, login, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
