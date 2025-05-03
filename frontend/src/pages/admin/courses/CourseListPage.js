import React, {useEffect, useMemo, useState} from "react";
import api from "../../../api";
import CreateCourseForm from "../../../components/admin/CreateCourseForm";
import "../../../styles/admin/courses.css";
import {useNavigate} from "react-router-dom";

const CourseListPage = () => {
    const [courses, setCourses] = useState([]);
    const [showForm, setShowForm] = useState(false);
    const navigate = useNavigate();

    const fetchCourses = async () => {
        try {
            const res = await api.get("/admin/courses");
            setCourses(res.data);
        } catch (err) {
            console.error("Failed to fetch courses", err);
            alert("Failed to fetch courses");
        }
    };

    useEffect(() => {
        fetchCourses();
    }, []);

    const groupedCourses = useMemo(() => {
        const groups = {};
        for (const course of courses) {
            const { educationLevel, yearOfStudy, semester } = course;
            groups[educationLevel] ??= {};
            groups[educationLevel][yearOfStudy] ??= {};
            groups[educationLevel][yearOfStudy][semester] ??= [];
            groups[educationLevel][yearOfStudy][semester].push(course);
        }
        return groups;
    }, [courses]);

    return (
        <div className="course-page">
            <div className="admin-course-header">
                <h2>Courses</h2>
                <button onClick={() => setShowForm(true)}>+ Add New Course</button>
            </div>

            {Object.entries(groupedCourses).map(([level, years]) => (
                <div key={level}>
                    <h3>{level}</h3>
                    {Object.entries(years).map(([year, semesters]) => (
                        <div key={year}>
                            <h4>Year {year}</h4>
                            {Object.entries(semesters).map(([sem, courseList]) => (
                                <div key={sem}>
                                    <h5>Semester {sem}</h5>
                                    <div className="course-list">
                                        {courseList.map(course => (
                                            <div className="course-card" key={course.id}>
                                                <strong>{course.name}</strong>
                                                    <button onClick={() => {
                                                        navigate(`/admin/courses/${course.id}/course-cohorts`, {
                                                            state: {courseName: course.name}
                                                    })}}>
                                                        View Cohort Assignments
                                                    </button>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            ))}
                        </div>
                    ))}
                </div>
            ))}

            {showForm && (
                <CreateCourseForm
                    onSuccess={() => {
                        fetchCourses();
                        setShowForm(false);
                    }}
                    onClose={() => setShowForm(false)}
                />
            )}
        </div>
    );
};

export default CourseListPage;
