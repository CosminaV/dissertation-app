import React, { useEffect, useMemo, useState, useRef } from "react";
import api from "../../../api";
import "../../../styles/student/student-courses.css";
import { useNavigate } from "react-router-dom";

const StudentCoursesPage = () => {
    const [courses, setCourses] = useState([]);
    const [academicYears, setAcademicYears] = useState([]);
    const [selectedYear, setSelectedYear] = useState(null);
    const [dropdownOpen, setDropdownOpen] = useState(false);
    const dropdownRef = useRef();
    const navigate = useNavigate();

    const fetchCourses = async () => {
        try {
            const res = await api.get("/student/courses");
            setCourses(res.data);

            const uniqueYears = Array.from(new Set(res.data.map(c => c.academicYear)));
            setAcademicYears(uniqueYears.sort((a, b) => b.localeCompare(a)));
            setSelectedYear(null);
        } catch (err) {
            console.error("Failed to fetch student courses", err);
            alert("Could not load student courses.");
        }
    };

    const handleClickOutside = (e) => {
        if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
            setDropdownOpen(false);
        }
    };

    useEffect(() => {
        fetchCourses();
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const filteredCourses = useMemo(() => {
        if (!selectedYear) return courses;
        return courses.filter(c => c.academicYear === selectedYear);
    }, [courses, selectedYear]);

    const grouped = useMemo(() => {
        const map = {};
        for (const course of filteredCourses) {
            const { educationLevel, yearOfStudy, semester } = course;
            map[educationLevel] ??= {};
            map[educationLevel][yearOfStudy] ??= {};
            map[educationLevel][yearOfStudy][semester] ??= [];
            map[educationLevel][yearOfStudy][semester].push(course);
        }
        return map;
    }, [filteredCourses]);

    return (
        <div className="student-course-page">
            <div className="student-course-header">
                <h2>My Courses</h2>
                <div className="student-academic-filter" ref={dropdownRef}>
                    <div
                        className="student-custom-academic-dropdown"
                        onClick={() => setDropdownOpen(prev => !prev)}
                    >
                        <div className="student-academic-dropdown-label">
                            {selectedYear || "All Years"}
                        </div>
                        {dropdownOpen && (
                            <div className="student-academic-dropdown-options">
                                <div onClick={() => { setSelectedYear(null); setDropdownOpen(false); }}>
                                    All Years
                                </div>
                                {academicYears.map(year => (
                                    <div key={year} onClick={() => { setSelectedYear(year); setDropdownOpen(false); }}>
                                        {year}
                                    </div>
                                ))}
                            </div>
                        )}
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
                                    <div className="student-course-grid">
                                        {list.map(course => (
                                            <div key={`${course.courseId}-${course.target}-${course.role}`}
                                                 className="student-course-card"
                                                 onClick={() => {
                                                     if (course.role === "PRACTICAL") {
                                                         navigate(`/student/courses/course-groups/${course.targetId}`);
                                                     } else if (course.role === "LECTURE") {
                                                         navigate(`/student/courses/course-cohorts/${course.targetId}`);
                                                     }
                                                 }}>
                                                <strong>{course.courseName}</strong>
                                                <p>Role: {course.role}</p>
                                                <p>{course.role === "LECTURE" ? "Cohort" : "Group"}: {course.target}</p>
                                                <p>Teacher: {course.teacherName}</p>
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
                <div className="student-no-courses-message">
                    No courses found for the selected academic year.
                </div>
            )}
        </div>
    );
};

export default StudentCoursesPage;