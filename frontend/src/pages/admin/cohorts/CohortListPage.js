import React, {useEffect, useMemo, useState} from "react";
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

    const groupedCohorts = useMemo(() => {
        return cohorts.reduce((acc, cohort) => {
            const level = cohort.educationLevel;
            if (!acc[level]) acc[level] = [];
            acc[level].push(cohort);
            return acc;
        }, {});
    },[cohorts]);

    return (
        <div className="page-container">
            <div className="header">
                <h2>Cohorts</h2>
                <button onClick={() => setShowForm(true)}>+ Add Cohort</button>
            </div>

            {loading ? (
                <p>Loading cohorts...</p>
            ) : cohorts.length === 0 ? (
                <p>No cohorts found.</p>
            ) : (
                Object.entries(groupedCohorts).map(([level, list]) => (
                    <div key={level}>
                        <h3>{level}</h3>
                        <div className="cohort-list">
                            {list.map((cohort) => (
                                <div key={cohort.id} className="cohort-card">
                                    <strong>{cohort.name}</strong>
                                </div>
                            ))}
                        </div>
                    </div>
                ))
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
