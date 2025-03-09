import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api";

const Register = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        firstName: "",
        lastName: "",
        email: "",
        password: "",
    });

    const [errors, setErrors] = useState({});

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrors({});

        try {
            const response = await api.post("/register", formData);
            console.log(response.data);
            navigate("/login");
        } catch (err) {
            if (err.response && err.response.status === 400) {
                const errorData = err.response.data;

                if (errorData.errors && typeof errorData.errors === "object") {
                    setErrors(errorData.errors);
                } else if (errorData.error) {
                    setErrors({general: errorData.error});
                }
                else {
                    setErrors({ general: "Registration failed. Please check your input." });
                }
            } else {
                setErrors({general: "Server error. Please try again later."});
            }
        }
    };

    return (
        <div className="register-container">
            <h2>Register</h2>
            {errors.general && <p className="error">{errors.general}</p>}

            <form onSubmit={handleSubmit}>
                <input type="text" name="firstName" placeholder="First Name" value={formData.firstName} onChange={handleChange}  />
                {errors.firstName && <p className="error">{errors.firstName}</p>}
                <input type="text" name="lastName" placeholder="Last Name" value={formData.lastName} onChange={handleChange}  />
                {errors.lastName && <p className="error">{errors.lastName}</p>}
                <input type="email" name="email" placeholder="Email" value={formData.email} onChange={handleChange}  />
                {errors.email && <p className="error">{errors.email}</p>}
                <input type="password" name="password" placeholder="Password" value={formData.password} onChange={handleChange}/>
                {errors.password && <p className="error">{errors.password}</p>}
                <button type="submit">Register</button>
            </form>
        </div>
    );
};

export default Register;
