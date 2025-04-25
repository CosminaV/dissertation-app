import React, { useState, useEffect } from "react";
import api from "../../api";
// import "../../styles/admin/student-groups.css";
import "../../styles/modal.css";

const StudentGroupForm = ({ onSuccess, onClose }) => {
    const [name, setName] = useState("");
    const [yearOfStudy, setYearOfStudy] = useState(1);
    const [educationLevel, setEducationLevel] = useState("BACHELOR");
    const [studyYearsOptions, setStudyYearsOptions] = useState({});
    const [cohortId, setCohortId] = useState("");
    const [cohorts, setCohorts] = useState([]);
    const [errors, setErrors] = useState({});

    const fetchCohorts = async () => {
        try {
            const response = await api.get("/admin/cohorts");
            setCohorts(response.data);
            if (response.data.length > 0) {
                setCohortId(response.data[0].id);
            }
        } catch (err) {
            console.error("Failed to fetch cohorts", err);
            alert("Failed to fetch cohorts");
        }
    };

    const fetchStudyYears = async () => {
        try {
            const res = await api.get("/common/study-years");
            setStudyYearsOptions(res.data);
        } catch (error) {
            console.error("Failed to fetch study years", error);
            alert("Failed to fetch study years");
        }
    }

    useEffect(() => {
        fetchCohorts();
        fetchStudyYears();
    }, []);

    const availableYears = studyYearsOptions[educationLevel] || [];

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrors({});

        try {
            await api.post("/admin/student-groups", {
                name,
                yearOfStudy,
                educationLevel,
                cohortId,
            });
            onSuccess();
        } catch (error) {
            if (error.response && error.response.status === 400) {
                const errorData = error.response.data;

                if (errorData.errors && typeof errorData.errors === "object") {
                    setErrors(errorData.errors);
                } else if (errorData.error) {
                    setErrors({ general: errorData.error });
                }
            } else {
                setErrors({ general: error.response?.data?.error || "Unexpected error" });
            }
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <h2>Create Student Group</h2>
                {errors.general && <p className="error">{errors.general}</p>}
                <form onSubmit={handleSubmit}>
                    <input
                        type="text"
                        placeholder="Group name"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                    />
                    {errors.name && <p className="error">{errors.name}</p>}

                    <select
                        value={educationLevel}
                        onChange={(e) => {
                            const newLevel = e.target.value;
                            setEducationLevel(newLevel);
                            const available = studyYearsOptions[newLevel];
                            if (available && available.length > 0) {
                                setYearOfStudy(available[0]); // pick the first valid year automatically
                            }
                        }}>
                        <option value="BACHELOR">Bachelor</option>
                        <option value="MASTER">Master</option>
                        <option value="PHD">PhD</option>
                    </select>
                    {errors.educationLevel && <p className="error">{errors.educationLevel}</p>}

                    <select
                        value={yearOfStudy}
                        onChange={(e) => setYearOfStudy(Number(e.target.value))}
                        disabled={!availableYears.length}>
                        {availableYears.map((year) => (
                            <option key={year} value={year}>
                                Year {year}
                            </option>
                        ))}
                    </select>
                    {errors.yearOfStudy && <p className="error">{errors.yearOfStudy}</p>}

                    <select
                        value={cohortId}
                        onChange={(e) => setCohortId(e.target.value)}>
                        {cohorts.map((cohort) => (
                            <option key={cohort.id} value={cohort.id}>
                                {cohort.name}
                            </option>
                        ))}
                    </select>
                    <div className="modal-buttons">
                        <button type="button" onClick={onClose}>Cancel</button>
                        <button type="submit">Create</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default StudentGroupForm;
