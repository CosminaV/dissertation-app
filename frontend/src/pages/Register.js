import React, { useState } from "react";
import api from "../api";
import "../styles/register.css";
import "../styles/form-inputs.css";

const Register = ({ onSuccess, onClose }) => {
    const [formData, setFormData] = useState({
        firstName: "",
        lastName: "",
        email: "",
        role: "STUDENT",
    });

    const [errors, setErrors] = useState({});
    const [message, setMessage] = useState("");

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrors({});
        setMessage("");

        try {
            await api.post("/admin/register", formData);
            setMessage("User registered successfully.");
            onSuccess?.();
            setFormData({ firstName: "", lastName: "", email: "", role: "STUDENT" });
        } catch (error) {
            if (error.response && error.response.status === 400) {
                const errorData = error.response.data;

                if (errorData.errors && typeof errorData.errors === "object") {
                    setErrors(errorData.errors);
                } else if (errorData.error) {
                    setErrors({ general: errorData.error });
                } else {
                    setErrors({ general: "Registration failed. Please check your input." });
                }
            } else {
                setErrors({ general: "Server error. Please try again later." });
            }
        }
    };

    return (
        <div className="register-modal">
            <button className="close-button" onClick={onClose}>×</button>
            <h2 className="register-title">Register New User</h2>
            {message && <p className="success">{message}</p>}
            {errors.general && <p className="error">{errors.general}</p>}

            <form onSubmit={handleSubmit} className="register-form">
                <input
                    className="input-field"
                    type="text"
                    name="firstName"
                    placeholder="First Name"
                    value={formData.firstName}
                    onChange={handleChange}
                />
                {errors.firstName && <p className="error">{errors.firstName}</p>}

                <input
                    className="input-field"
                    type="text"
                    name="lastName"
                    placeholder="Last Name"
                    value={formData.lastName}
                    onChange={handleChange}
                />
                {errors.lastName && <p className="error">{errors.lastName}</p>}

                <input
                    className="input-field"
                    type="email"
                    name="email"
                    placeholder="Email"
                    value={formData.email}
                    onChange={handleChange}
                />
                {errors.email && <p className="error">{errors.email}</p>}

                <select
                    className="input-field"
                    name="role"
                    value={formData.role}
                    onChange={handleChange}
                >
                    <option value="STUDENT">Student</option>
                    <option value="TEACHER">Teacher</option>
                </select>

                <button type="submit" className="submit-button">Register</button>
            </form>
        </div>
    );
};

export default Register;
