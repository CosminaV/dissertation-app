import React, { useState } from "react";
import api from "../api";
import { useNavigate, useLocation } from "react-router-dom";
import "../styles/setpassword.css";
import "../styles/form-inputs.css";
import {useAuth} from "../context/AuthContext";

const SetPasswordPage = () => {
    const [password, setPassword] = useState("");
    const [message, setMessage] = useState("");
    const location = useLocation();
    const email = location.state?.email;
    const navigate = useNavigate();
    const { logout } = useAuth();

    const handleSubmit = async (e) => {
        e.preventDefault();

        const payload = { email, password, changePasswordContext: "FIRST_TIME" };

        try {
            await api.post("/auth/set-password", payload);
            setMessage("Password set successfully! Redirecting to login...");
            setTimeout(() => {
                logout();
                navigate("/login")
            }, 2000);
        } catch (error) {
            console.error(error.response.data);
            setMessage(error.response.data.error);
        }
    };

    return (
        <div className="set-password-container">
            <h2 className="set-password-title">Set Your Password</h2>
            {message && <p className="message">{message}</p>}

            {!message.includes("successfully") && (
                <form className="set-password-form" onSubmit={handleSubmit}>
                    <input
                        className="input-field"
                        type="password"
                        placeholder="New Password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                    <button type="submit" className="submit-button">Save Password</button>
                </form>
            )}
        </div>
    );
};

export default SetPasswordPage;