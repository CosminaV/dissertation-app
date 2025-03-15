import React, { useState } from "react";
import api from "../api";

const Dashboard = () => {
    const [error, setError] = useState("");

    const fetchProtectedData = async (e) => {
        e.preventDefault();
        setError("");
        try {
            const response = await api.get("/demo");
            setError(response.data);
        } catch (err) {
            console.error("Error fetching protected data:", err);
            if (err.response) {
                const data = err.response.data;

                if (data.error) {
                    setError(data.error);
                } else {
                    setError("Unauthorized.");
                }
            } else {
                setError("Server error. Please try again later.");
            }
        }
    };

    return (
        <div>
            <h2>Dashboard</h2>
            <button onClick={fetchProtectedData}>Get Protected Data</button>
            {error && <p className="error">{error}</p>}
        </div>
    );
};

export default Dashboard;
