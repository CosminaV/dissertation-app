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
    const [loginMode, setLoginMode] = useState("PASSWORD");
    const [error, setError] = useState("");

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");

        try {
            const endpoint = loginMode === "PASSWORD" ? "/auth/authenticate" : "/auth/authenticate-otp";
            const payload = loginMode === "PASSWORD"
                ? { email: formData.email, password: formData.password }
                : { email: formData.email, otp: formData.password };

            const response = await api.post(endpoint, payload);
            const { accessToken, needsPasswordSetup, faceImageRequired } = response.data;
            login(accessToken);

            if (needsPasswordSetup) {
                navigate("/set-password", { state: { email : formData.email } });
            } else if (faceImageRequired) {
                navigate("/upload-profile-image")
            } else {
                navigate("/dashboard");
            }
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
                <div className="toggle-login-mode">
                    <button
                        className={loginMode === "PASSWORD" ? "active" : ""}
                        onClick={() => setLoginMode("PASSWORD")}
                    >Password</button>
                    <button
                        className={loginMode === "OTP" ? "active" : ""}
                        onClick={() => setLoginMode("OTP")}
                    >One-Time Password</button>
                </div>

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
                        type={loginMode === "PASSWORD" ? "password" : "text"}
                        name="password"
                        placeholder={loginMode === "PASSWORD" ? "Password" : "One-Time Password"}
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