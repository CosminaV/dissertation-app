import React, {useEffect, useMemo, useState} from "react";
import { useNavigate } from "react-router-dom";
import api from "../../../api";
import "../../../styles/admin/student-groups.css";
import StudentGroupForm from "../../../components/admin/StudentGroupForm";

const StudentGroupListPage = () => {
    const [groups, setGroups] = useState([]);
    const [showForm, setShowForm] = useState(false);
    const navigate = useNavigate();

    const fetchGroups = async () => {
        try {
            const response = await api.get("/admin/student-groups");
            setGroups(response.data);
        } catch (error) {
            console.error("Failed to fetch student groups", error);
            alert("Error fetching student groups.");
        }
    };

    useEffect(() => {
        fetchGroups();
    }, []);

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

    return (
        <div className="group-page">
            <div className="group-header">
                <h2>Student Groups</h2>
                <button onClick={() => setShowForm(true)}>+ Create New Group</button>
            </div>

            {/*<div className="group-list">*/}
            {/*    {groups.map(group => (*/}
            {/*        <div className="group-card" key={group.id} onClick={() => navigate(`/admin/student-groups/${group.id}`)}>*/}
            {/*            <strong>{group.name}{group.cohortName}</strong>*/}
            {/*            <p>{group.educationLevel}</p>*/}
            {/*            <p>{group.students.length} students</p>*/}
            {/*        </div>*/}
            {/*    ))}*/}
            {/*</div>*/}

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
                                                onClick={() => navigate(`/admin/student-groups/${group.id}`)}>
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
                    }}
                    onClose={() => setShowForm(false)}
                />
            )}
        </div>
    );
};

export default StudentGroupListPage;
