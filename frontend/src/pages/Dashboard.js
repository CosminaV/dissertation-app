import React, {useEffect, useState} from "react";
import api from "../api";

const Dashboard = () => {
    const [error, setError] = useState("");
    const [data, setData] = useState("");

    const fetchProtectedData = async () => {
        try {
            const response = await api.get("/demo");
            setData(response.data);
        } catch (err) {
            console.error("Error fetching protected data:", err);
            if (err.response.data.error) {
                setError("Unauthorized");
            }
        }
    };

    useEffect(() => {
        fetchProtectedData();
    }, []);

    return (
        <div>
            <h2>Dashboard</h2>
            <button onClick={fetchProtectedData}>Get protected data</button>
            {data && <p>{data}</p>}
            {error && <p className="error">{error}</p>}
        </div>
    );
};

export default Dashboard;
