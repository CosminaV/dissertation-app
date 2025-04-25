import React, {useCallback, useEffect, useState} from "react";
import {useLocation, useParams} from "react-router-dom";
import api from "../../../api";
import "../../../styles/admin/course-cohorts.css";
import AssignCourseCohortForm from "../../../components/admin/AssignCourseCohortForm";

const CourseCohortsPage = () => {
    const { courseId } = useParams();
    const location = useLocation();
    const courseName = location.state?.courseName || "Course";

    const [courseCohorts, setCourseCohorts] = useState([]);
    const [showAssignForm, setShowAssignForm] = useState(false);

    const fetchCourseCohorts = useCallback(async () => {
        try {
            const res = await api.get(`/admin/courses/${courseId}/course-cohorts`);
            setCourseCohorts(res.data);
        } catch (err) {
            console.error("Failed to fetch course cohorts", err);
            alert("Failed to fetch course cohorts");
        }
    }, [courseId])

    useEffect(() => {
        fetchCourseCohorts();
    }, [fetchCourseCohorts]);

    return (
        <div className="course-cohorts-page">
            <div className="course-cohorts-header">
                <h2>Course Cohorts for {courseName}</h2>
                <button onClick={() => setShowAssignForm(true)}>+ Assign New Cohort</button>
            </div>

            {courseCohorts.length > 0 ? (
                <table className="course-cohort-table">
                    <thead>
                    <tr>
                        <th>Cohort</th>
                        <th>Lecture Teacher</th>
                        <th>Practical Teacher</th>
                        <th>Academic Year</th>
                    </tr>
                    </thead>
                    <tbody>
                    {courseCohorts.map(cohort => (
                        <tr key={cohort.id}>
                            <td>{cohort.cohortName}</td>
                            <td>{cohort.lectureTeacherName}</td>
                            <td>{cohort.practicalTeacherName}</td>
                            <td>{cohort.academicYear}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            ) : (
                <p>No cohorts assigned to this course yet.</p>
            )}

            {showAssignForm && (
                <AssignCourseCohortForm
                    course={{id: courseId, name: courseName}}
                    onSuccess={() => {
                        fetchCourseCohorts();
                        setShowAssignForm(false);
                    }}
                    onClose={() => setShowAssignForm(false)}
                />
            )}
        </div>
    );
};

export default CourseCohortsPage;