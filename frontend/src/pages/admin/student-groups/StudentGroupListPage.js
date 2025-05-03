import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../../api";
import "../../../styles/admin/student-groups.css";
import StudentGroupForm from "../../../components/admin/StudentGroupForm";

const StudentGroupListPage = () => {
    const [groups, setGroups] = useState([]);
    const [academicYears, setAcademicYears] = useState([]);
    const [selectedYear, setSelectedYear] = useState(null);
    const [dropdownOpen, setDropdownOpen] = useState(false);
    const [showForm, setShowForm] = useState(false);
    const navigate = useNavigate();

    const fetchGroups = async (year = null) => {
        try {
            const endpoint = year ? `/admin/student-groups?academicYear=${year}` : "/admin/student-groups";
            const response = await api.get(endpoint);
            setGroups(response.data);
        } catch (error) {
            console.error("Failed to fetch student groups", error);
            alert("Error fetching student groups.");
        }
    };

    const fetchAcademicYears = async () => {
        try {
            const response = await api.get("/admin/student-groups/academic-years");
            setAcademicYears(response.data);
        } catch (error) {
            console.error("Failed to fetch academic years", error);
        }
    };

    useEffect(() => {
        fetchGroups();
        fetchAcademicYears();
    }, []);

    useEffect(() => {
        fetchGroups(selectedYear);
    }, [selectedYear]);

    const grouped = useMemo(() => {
        const map = {};
        for (const group of groups) {
            const { educationLevel, yearOfStudy, cohortName } = group;
            if (!map[educationLevel]) map[educationLevel] = {};
            if (!map[educationLevel][yearOfStudy]) map[educationLevel][yearOfStudy] = {};
            if (!map[educationLevel][yearOfStudy][cohortName]) map[educationLevel][yearOfStudy][cohortName] = [];
            map[educationLevel][yearOfStudy][cohortName].push(group);
        }
        return map;
    }, [groups]);

    const handleYearClick = (yearString) => {
        setDropdownOpen(false);
        if (!yearString) {
            setSelectedYear(null);
        } else {
            const numericYear = parseInt(yearString.substring(0, 4), 10);
            setSelectedYear(numericYear);
        }
    };

    return (
        <div className="group-page">
            <div className="group-header">
                <h2>Student Groups</h2>

                <div className="academic-filter-sg">
                    <div className="custom-academic-sg-dropdown">
                        <div
                            className="academic-sg-dropdown-label"
                            onClick={() => setDropdownOpen(prev => !prev)}
                        >
                            {selectedYear
                                ? academicYears.find(y => y.startsWith(selectedYear.toString()))
                                : "Current Student Groups"}
                        </div>

                        {dropdownOpen && (
                            <div className="academic-sg-dropdown-options">
                                <div onClick={() => handleYearClick(null)}>Current Student Groups</div>
                                {academicYears.map(year => (
                                    <div key={year} onClick={() => handleYearClick(year)}>
                                        {year}
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>

                {!selectedYear && (
                    <button onClick={() => setShowForm(true)}>+ Create New Group</button>
                )}
            </div>

            {Object.entries(grouped).map(([level, years]) => (
                <div key={level}>
                    <h3>{level}</h3>
                    {Object.entries(years).map(([year, cohorts]) => (
                        <div key={year}>
                            <h4>Year {year}</h4>
                            {Object.entries(cohorts).map(([cohort, groupList]) => (
                                <div key={cohort}>
                                    <h5>Cohort {cohort}</h5>
                                    <div className="group-list">
                                        {groupList.map(group => (
                                            <div
                                                className="group-card"
                                                key={group.id}
                                                onClick={() =>
                                                    navigate(`/admin/student-groups/${group.id}` + (selectedYear ? `?academicYear=${selectedYear}` : ""))
                                                }
                                            >
                                                <strong>{group.name}</strong>
                                                <p>{group.students.length} students</p>
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
                <StudentGroupForm
                    onSuccess={() => {
                        setShowForm(false);
                        fetchGroups();
                        fetchAcademicYears();
                    }}
                    onClose={() => setShowForm(false)}
                />
            )}
        </div>
    );
};

export default StudentGroupListPage;