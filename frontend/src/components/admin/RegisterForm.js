import React, { useState } from "react";
import api from "../../api";
import "../../styles/admin/register.css";
import "../../styles/form-inputs.css";
import "../../styles/modal.css";

const RegisterForm = ({ onSuccess, onClose }) => {
    const [formData, setFormData] = useState({
        firstName: "",
        lastName: "",
        email: "",
        role: "STUDENT",
        educationLevel: "BACHELOR"
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
            setFormData({
                firstName: "", lastName: "", email: "", role: "STUDENT", educationLevel: "BACHELOR" });
        } catch (error) {
            if (error.response && error.response.status === 400) {
                const errorData = error.response.data;

                if (errorData.errors && typeof errorData.errors === "object") {
                    setErrors(errorData.errors);
                } else if (errorData.error) {
                    setErrors({ general: errorData.error });
                }
            } else {
                setErrors({ general: error.response?.data?.error || "Could not register. Please try again." });
            }
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <button className="close-button" onClick={onClose}>×</button>
                <h2>Register New User</h2>
                {message && <p className="success">{message}</p>}
                {errors.general && <p className="error">{errors.general}</p>}

                <form onSubmit={handleSubmit} className="register-form">
                    <input
                        type="text"
                        name="firstName"
                        placeholder="First Name"
                        value={formData.firstName}
                        onChange={handleChange}
                    />
                    {errors.firstName && <p className="error">{errors.firstName}</p>}

                    <input
                        type="text"
                        name="lastName"
                        placeholder="Last Name"
                        value={formData.lastName}
                        onChange={handleChange}
                    />
                    {errors.lastName && <p className="error">{errors.lastName}</p>}

                    <input
                        type="email"
                        name="email"
                        placeholder="Email"
                        value={formData.email}
                        onChange={handleChange}
                    />
                    {errors.email && <p className="error">{errors.email}</p>}

                    <select
                        name="role"
                        value={formData.role}
                        onChange={handleChange}>
                        <option value="STUDENT">Student</option>
                        <option value="TEACHER">Teacher</option>
                    </select>

                    {formData.role === "STUDENT" && (
                        <select
                            name="educationLevel"
                            value={formData.educationLevel}
                            onChange={handleChange}>
                            <option value="BACHELOR">Bachelor</option>
                            <option value="MASTER">Master</option>
                            <option value="PHD">PhD</option>
                        </select>
                    )}
                    <button type="submit" className="submit-button">Register</button>
                </form>
            </div>
        </div>
    );
};

export default RegisterForm;
