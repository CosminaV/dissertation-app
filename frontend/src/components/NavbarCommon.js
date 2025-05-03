import React from "react";
import { Link } from "react-router-dom";
import "../styles/navbar.css";
import GradusLogo from "../assets/gradus-logo.svg";
import { useAuth } from "../context/AuthContext";

const NavbarCommon = ({ children, userName }) => {
    const { logout } = useAuth();

    const handleLogout = async () => {
        try {
            logout();
        } catch (e) {
            console.error("Logout failed", e);
        }
    };

    return (
        <nav className="navbar">
        <div className="navbar-logo">
            <img src={GradusLogo} alt="Gradus Logo" className="logo-image"/>
            <span className="logo-text">Gradus</span>
        </div>
        <div className="navbar-links">
            {children}
            <div className="navbar-user">
                {userName} ▼
                <div className="dropdown-content">
                    <Link to="/profile">Profile</Link>
                    <button onClick={handleLogout}>Logout</button>
                </div>
            </div>
        </div>
    </nav>
    );
};

export default NavbarCommon;