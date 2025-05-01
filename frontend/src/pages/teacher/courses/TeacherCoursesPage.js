import React, { useEffect, useMemo, useState } from "react";
import api from "../../../api";
import "../../../styles/teacher/teacher-courses.css";
import { useNavigate } from "react-router-dom";

const TeacherCoursesPage = () => {
    const [courses, setCourses] = useState([]);
    const [academicYears, setAcademicYears] = useState([]);
    const [selectedYear, setSelectedYear] = useState(null);

    const navigate = useNavigate();

    const fetchCourses = async () => {
        try {
            const res = await api.get("/teacher/courses");
            setCourses(res.data);
        } catch (err) {
            console.error("Failed to fetch teacher courses:", err);
            alert("Failed to fetch courses");
        }
    };

    const fetchAcademicYears = async () => {
        try {
            const res = await api.get("/teacher/courses/academic-years");
            setAcademicYears(res.data);
            if (res.data.length > 0) {
                setSelectedYear(res.data[0]);
            }
        } catch (err) {
            console.error("Failed to fetch academic years:", err);
            alert("Failed to fetch academic years");
        }
    };

    useEffect(() => {
        fetchCourses();
        fetchAcademicYears();
    }, []);

    const filteredCourses = useMemo(() => {
        if (!selectedYear) return courses;
        return courses.filter(c => c.academicYear === selectedYear);
    }, [courses, selectedYear]);

    const grouped = useMemo(() => {
        const map = {};
        for (const course of filteredCourses) {
            const { educationLevel, yearOfStudy, semester } = course;
            if (!map[educationLevel]) map[educationLevel] = {};
            if (!map[educationLevel][yearOfStudy]) map[educationLevel][yearOfStudy] = {};
            if (!map[educationLevel][yearOfStudy][semester]) map[educationLevel][yearOfStudy][semester] = [];
            map[educationLevel][yearOfStudy][semester].push(course);
        }
        return map;
    }, [filteredCourses]);

    return (
            <div className="teacher-course-page">
                <div className="teacher-course-header">
                    <h2>My Courses</h2>
                    <div className="academic-filter">
                        <div className="custom-academic-dropdown">
                            <div className="academic-dropdown-label">{selectedYear || "All Years"}</div>
                            <div className="academic-dropdown-options">
                                <div onClick={() => setSelectedYear(null)}>All Years</div>
                                {academicYears.map(year => (
                                    <div key={year} onClick={() => setSelectedYear(year)}>
                                        {year}
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                </div>

                {Object.entries(grouped).map(([level, years]) => (
                    <div key={level}>
                        <h3>{level}</h3>
                        {Object.entries(years).map(([year, semesters]) => (
                            <div key={year}>
                                <h4>Year {year}</h4>
                                {Object.entries(semesters).map(([semester, list]) => (
                                    <div key={semester}>
                                        <h5>Semester {semester}</h5>
                                        <div className="teacher-course-grid">
                                            {list.map(course => (
                                                <div
                                                    key={`${course.courseId}-${course.target}-${course.role}`}
                                                    className="teacher-course-card"
                                                    onClick={() => {
                                                        if (course.role === "PRACTICAL") {
                                                            navigate(`/teacher/courses/course-groups/${course.targetId}`);
                                                        } else if (course.role === "LECTURE") {
                                                            navigate(`/teacher/courses/course-cohorts/${course.targetId}`);
                                                        }
                                                    }}>
                                                    <strong>{course.courseName}</strong>
                                                    <p>Role: {course.role}</p>
                                                    <p>{course.role === "LECTURE" ? "Cohort" : "Group"}: {course.target}</p>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        ))}
                    </div>
                ))}

                {filteredCourses.length === 0 && (
                    <div className="no-courses-message">
                        No courses found for the selected academic year.
                    </div>
                )}
            </div>
    );
};

export default TeacherCoursesPage;