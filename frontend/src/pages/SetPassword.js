import React, { useEffect, useState } from "react";
import api from "../api";
import { useNavigate } from "react-router-dom";
import "../styles/setpassword.css";
import "../styles/form-inputs.css";

const SetPasswordPage = () => {
    const [email, setEmail] = useState("");
    const [token, setToken] = useState("");
    const [password, setPassword] = useState("");
    const [message, setMessage] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        const params = new URLSearchParams(window.location.search);
        const emailParam = params.get("email");
        const tokenParam = params.get("token");

        if (emailParam && tokenParam) {
            setEmail(emailParam);
            setToken(tokenParam);
        } else {
            setMessage("Invalid or incomplete link.");
        }
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();

        const payload = { email, password, token };

        try {
            await api.post("/auth/set-password", payload);
            setMessage("Password set successfully! Redirecting to login...");
            setTimeout(() => navigate("/login"), 2000);
        } catch (error) {
            console.error(error.response.data);
            setMessage("An error occurred while setting the password.");
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