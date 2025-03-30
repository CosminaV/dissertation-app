import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api";
import { useAuth } from "../context/AuthContext";
import "../styles/login.css";
import "../styles/form-inputs.css";

const Login = () => {
    const navigate = useNavigate();
    const { login, accessToken, logout } = useAuth();
    const [formData, setFormData] = useState({ email: "", password: "" });
    const [error, setError] = useState("");

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");

        try {
            const response = await api.post("/auth/authenticate", formData);
            const accessToken = response.data.accessToken;
            login(accessToken);
            sessionStorage.setItem("wasLoggedIn", true);
            navigate("/dashboard");
        } catch (err) {
            if (err.response) {
                const data = err.response.data;

                if (data.errors) {
                    setError(Object.values(data.errors).join(" | "));
                } else if (data.error) {
                    setError(data.error);
                } else {
                    setError("Authentication failed. Please try again.");
                }
            } else {
                setError("Server error. Please try again later.");
            }
        }
    };

    if (accessToken) {
        return (
            <div className="already-logged-in">
                <h2>You are already logged in.</h2>
                <p>If you want to log in with another account, please log out first.</p>
                <button onClick={logout}>Logout</button>
            </div>
        );
    }

    return (
        <div className="login-page">
            <div className="login-card">
                <h2 className="login-title">Login - Gradus</h2>
                {error && <p className="error-message">{error}</p>}
                <form onSubmit={handleSubmit} className="login-form">
                    <input
                        type="email"
                        name="email"
                        placeholder="Email"
                        value={formData.email}
                        onChange={handleChange}
                        className="login-input"
                    />
                    <input
                        type="password"
                        name="password"
                        placeholder="Password"
                        value={formData.password}
                        onChange={handleChange}
                        className="login-input"
                    />
                    <button type="submit">Login</button>
                </form>
            </div>
        </div>
    );
};

export default Login;