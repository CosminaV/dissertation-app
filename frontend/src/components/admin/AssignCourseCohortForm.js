import React, { useEffect, useState } from "react";
import api from "../../api";
import "../../styles/modal.css";

const AssignCourseCohortForm = ({ course, onSuccess, onClose }) => {
    const [cohortId, setCohortId] = useState(0);
    const [teacherId, setTeacherId] = useState(0);
    const [academicYear, setAcademicYear] = useState(new Date().getFullYear());
    const [cohorts, setCohorts] = useState([]);
    const [teachers, setTeachers] = useState([]);
    const [errors, setErrors] = useState({});

    const fetchCohorts = async () => {
        try {
            const res = await api.get("/admin/cohorts");
            setCohorts(res.data);
            if (res.data.length > 0) {
                setCohortId(Number(res.data[0].id));
            }
        } catch (err) {
            console.error("Failed to fetch cohorts", err);
            alert("Failed to fetch cohorts");
        }
    };

    const fetchTeachers = async () => {
        try {
            const res = await api.get("/admin/users");
            const filtered = res.data.filter(u => u.role === "TEACHER");
            setTeachers(filtered);
            if (filtered.length > 0) {
                setTeacherId(Number(filtered[0].id));
            }
        } catch (err) {
            console.error("Failed to fetch teachers", err);
            alert("Failed to fetch teachers");
        }
    };

    useEffect(() => {
        fetchCohorts();
        fetchTeachers();
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrors({});

        try {
            await api.post("/admin/course-cohorts", {
                courseId: Number(course.id),
                cohortId: Number(cohortId),
                lectureTeacherId: Number(teacherId),
                academicYear: Number(academicYear)
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

    const currentYear = new Date().getFullYear();
    const academicYearOptions = [currentYear, currentYear + 1];

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <button className="close-button" onClick={onClose}>×</button>
                <h2>Assign Cohort and Lecture Teacher to {course.name} course</h2>
                {errors.general && <p className="error">{errors.general}</p>}
                <form onSubmit={handleSubmit}>
                    <select value={cohortId} onChange={(e) => setCohortId(Number(e.target.value))}>
                        {cohorts.map((c) => (
                            <option key={c.id} value={c.id}>
                                {c.name}
                            </option>
                        ))}
                    </select>
                    {errors.cohortId && <p className="error">{errors.cohortId}</p>}

                    <select value={teacherId} onChange={(e) => setTeacherId(Number(e.target.value))}>
                        {teachers.map((t) => (
                            <option key={t.id} value={t.id}>
                                {t.firstName} {t.lastName}
                            </option>
                        ))}
                    </select>
                    {errors.lectureTeacherId && <p className="error">{errors.lectureTeacherId}</p>}

                    <select value={academicYear} onChange={(e) => setAcademicYear(e.target.value)}>
                        {academicYearOptions.map(year => (
                            <option key={year} value={year}>{year}-{year+1}</option>
                        ))}
                    </select>
                    {errors.academicYear && <p className="error">{errors.academicYear}</p>}

                    <div className="modal-buttons">
                        <button type="button" onClick={onClose}>Cancel</button>
                        <button type="submit">Assign</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AssignCourseCohortForm;