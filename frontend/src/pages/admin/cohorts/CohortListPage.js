import React, { useEffect, useState } from "react";
import api from "../../../api";
import CohortForm from "../../../components/admin/CohortForm";
import "../../../styles/admin/cohorts.css";

const CohortListPage = () => {
    const [cohorts, setCohorts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);

    const fetchCohorts = async () => {
        try {
            const response = await api.get("/admin/cohorts");
            setCohorts(response.data);
        } catch (error) {
            console.error("Failed to fetch cohorts", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCohorts();
    }, []);

    return (
        <div className="page-container">
            <div className="header">
                <h2>Cohorts</h2>
                <button onClick={() => setShowForm(true)}>+ Add Cohort</button>
            </div>

            {loading ? (
                <p>Loading cohorts...</p>
            ) : (
                <div className="cohort-list">
                    {cohorts.length === 0 ? (
                        <p>No cohorts found.</p>
                    ) : (
                        cohorts.map((cohort) => (
                            <div key={cohort.id} className="cohort-card">
                                <strong>{cohort.name}</strong>
                            </div>
                        ))
                    )}
                </div>
            )}

            {showForm && (
                <CohortForm
                    onClose={() => setShowForm(false)}
                    onSuccess={() => {
                        fetchCohorts();
                        setShowForm(false);
                    }}
                />
            )}
        </div>
    );
};

export default CohortListPage;
