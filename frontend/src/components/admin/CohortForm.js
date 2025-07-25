import React, { useState } from "react";
import api from "../../api";
import "../../styles/modal.css";

const CohortForm = ({ onClose, onSuccess }) => {
    const [name, setName] = useState("");
    const [educationLevel, setEducationLevel] = useState("BACHELOR");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!name.trim()) {
            alert("Cohort name is required.");
            return;
        }

        try {
            setLoading(true);
            await api.post("/admin/cohorts", {
                name,
                educationLevel
            });
            onSuccess();
        } catch (error) {
            console.error("Failed to create cohort", error);
            alert(error.response.data.error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <h2>Add Cohort</h2>
                <form onSubmit={handleSubmit}>
                    <input
                        type="text"
                        placeholder="Cohort name"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                    />
                    <select
                        name="educationLevel"
                        value={educationLevel}
                        onChange={(e) => setEducationLevel(e.target.value)}>
                        <option value="BACHELOR">Bachelor</option>
                        <option value="MASTER">Master</option>
                        <option value="PHD">PhD</option>
                    </select>
                    <div className="modal-buttons">
                        <button type="button" onClick={onClose} disabled={loading}>
                            Cancel
                        </button>
                        <button type="submit" disabled={loading}>
                            {loading ? "Saving..." : "Save"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default CohortForm;
