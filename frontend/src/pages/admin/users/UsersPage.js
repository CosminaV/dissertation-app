import React, { useEffect, useState } from "react";
import api from "../../../api";
import RegisterForm from "../../../components/admin/RegisterForm";
import "../../../styles/admin/userspage.css";
import {useNavigate} from "react-router-dom";

const UsersPage = () => {
    const [users, setUsers] = useState([]);
    const [roleFilter, setRoleFilter] = useState("ALL");
    const [statusFilter, setStatusFilter] = useState("ALL");
    const [showForm, setShowForm] = useState(false);
    const [sendingEmails, setSendingEmails] = useState(false);
    const navigate = useNavigate();

    const fetchUsers = async () => {
        try {
            const response = await api.get("/admin/users");
            setUsers(response.data);
        } catch (error) {
            console.error("Failed to fetch users", error);
            alert("Failed to fetch users");
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    const handleSendEmails = async () => {
        try {
            setSendingEmails(true);
            await api.get("/admin/send-otps");
            alert("OTP emails sent.");
        } catch (error) {
            console.error("Failed to send OTP emails", error);
            alert("Failed to send activation emails.");
        } finally {
            setSendingEmails(false);
        }
    };

    const filteredUsers = users.filter((u) => {
        const roleMatch = roleFilter === "ALL" || u.role === roleFilter;
        const statusMatch =
            statusFilter === "ALL" ||
            (statusFilter === "ACTIVE" && u.activated) ||
            (statusFilter === "INACTIVE" && !u.activated);
        return roleMatch && statusMatch;
    });

    return (
        <div className="admin-users-container">
            <div className="header">
                <h2>Manage Users</h2>
                <div className="actions">
                    <div className="custom-dropdown">
                        <div className="dropdown-label">{roleFilter}</div>
                        <div className="dropdown-options">
                            <div onClick={() => setRoleFilter("ALL")}>ALL</div>
                            <div onClick={() => setRoleFilter("TEACHER")}>TEACHER</div>
                            <div onClick={() => setRoleFilter("STUDENT")}>STUDENT</div>
                        </div>
                    </div>

                    <div className="custom-dropdown">
                        <div className="dropdown-label">{statusFilter}</div>
                        <div className="dropdown-options">
                            <div onClick={() => setStatusFilter("ALL")}>ALL STATUSES</div>
                            <div onClick={() => setStatusFilter("ACTIVE")}>ACTIVE</div>
                            <div onClick={() => setStatusFilter("INACTIVE")}>INACTIVE</div>
                        </div>
                    </div>

                    <button onClick={handleSendEmails} disabled={sendingEmails}>
                        {sendingEmails ? "Sending..." : "Send OTP Emails"}
                    </button>
                    <button onClick={() => setShowForm(true)}>+ Register New User</button>
                </div>
            </div>

            {showForm && (
                <RegisterForm
                    onSuccess={() => {
                        fetchUsers();
                        setShowForm(false);
                    }}
                    onClose={() => setShowForm(false)}
                />
            )}


            <div className="user-list">
                {filteredUsers.map((user) => (
                    <div className={`user-card ${user.role === "TEACHER" ? "clickable-teacher" : ""}`} key={user.id}
                        onClick={() => {
                            if (user.role === "TEACHER") {
                                navigate(`/admin/teachers/${user.id}`, { state: { teacher: user } });
                            }
                        }}>
                        <strong>{user.firstName} {user.lastName}</strong>
                        <p>{user.email}</p>
                        <span className={`role-tag ${user.role.toLowerCase()}`}>{user.role}</span>
                        <p className={`activation-status ${user.activated ? "active" : "inactive"}`}>
                            {user.activated ? "Activated" : "Not Activated"}
                        </p>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default UsersPage;