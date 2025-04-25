import React, { useEffect, useState } from "react";
import api from "../../api";
import "../../styles/modal.css";

const AssignCourseGroupForm = ({ studentGroupId, onSuccess, onClose }) => {
    const [courseId, setCourseId] = useState(0);
    const [teacherId, setTeacherId] = useState(0);
    const [academicYear, setAcademicYear] = useState(new Date().getFullYear());
    const [courses, setCourses] = useState([]);
    const [teachers, setTeachers] = useState([]);
    const [errors, setErrors] = useState({});

    useEffect(() => {
        const fetchData = async () => {
            try {
                const coursesRes = await api.get("/admin/courses");
                setCourses(coursesRes.data);
                if (coursesRes.data.length > 0) {
                    setCourseId(coursesRes.data[0].id);
                }

                const usersRes = await api.get("/admin/users");
                const teacherUsers = usersRes.data.filter(u => u.role === "TEACHER");
                setTeachers(teacherUsers);
                if (teacherUsers.length > 0) {
                    setTeacherId(teacherUsers[0].id);
                }
            } catch (err) {
                console.error("Failed to fetch courses or teachers", err);
                alert("Error loading form data.");
            }
        };

        fetchData();
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrors({});

        try {
            await api.post("/admin/course-groups", {
                courseId,
                studentGroupId,
                practicalTeacherId: teacherId,
                academicYear: Number(academicYear)
            });
            onSuccess();
        } catch (error) {
            if (error.response && error.response.status === 400) {
                const errorData = error.response.data;
                if (errorData.errors) {
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
                <h2>Assign Course to Group</h2>
                {errors.general && <p className="error">{errors.general}</p>}
                <form onSubmit={handleSubmit}>
                    <select value={courseId} onChange={e => setCourseId(Number(e.target.value))}>
                        {courses.map(c => (
                            <option key={c.id} value={c.id}>{c.name}</option>
                        ))}
                    </select>
                    {errors.courseId && <p className="error">{errors.courseId}</p>}

                    <select value={teacherId} onChange={e => setTeacherId(Number(e.target.value))}>
                        {teachers.map(t => (
                            <option key={t.id} value={t.id}>{t.firstName} {t.lastName}</option>
                        ))}
                    </select>
                    {errors.practicalTeacherId && <p className="error">{errors.practicalTeacherId}</p>}

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

export default AssignCourseGroupForm;