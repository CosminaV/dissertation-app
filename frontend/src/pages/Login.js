import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api";
import { useAuth } from "../context/AuthContext";
import { setAuthToken } from "../api";

const Login = () => {
    const navigate = useNavigate();
    const { login } = useAuth();
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
            setAuthToken(accessToken);
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

    return (
        <div className="login-container">
            <h2>Login</h2>
            {error && <p className="error">{error}</p>}
            <form onSubmit={handleSubmit}>
                <input type="email" name="email" placeholder="Email" value={formData.email} onChange={handleChange} />
                <input type="password" name="password" placeholder="Password" value={formData.password} onChange={handleChange} />
                <button type="submit">Login</button>
            </form>
            <p>Don't have an account? <Link to="/register">Register here</Link></p>
        </div>
    );
};

export default Login;
