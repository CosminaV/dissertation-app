import React, { useEffect, useState } from "react";
import api from "../../api";
import "../../styles/modal.css";

const MoveStudentForm = ({ student, currentGroupId, educationLevel, onClose, onSuccess }) => {
    const [groups, setGroups] = useState([]);
    const [targetGroupId, setTargetGroupId] = useState("");

    useEffect(() => {
        const fetchGroups = async () => {
            try {
                const res = await api.get("/admin/student-groups");
                const filtered = res.data.filter(
                    (g) => g.educationLevel === educationLevel && g.id !== currentGroupId
                );
                setGroups(filtered);
                if (filtered.length > 0) {
                    setTargetGroupId(filtered[0].id);
                }
            } catch (err) {
                console.error("Failed to fetch groups", err);
            }
        };

        fetchGroups();
    }, [educationLevel, currentGroupId]);

    const handleMove = async () => {
        try {
            await api.put(`/admin/student-groups/${targetGroupId}/move-student/${student.id}`);
            onSuccess();
        } catch (err) {
            console.error("Failed to move student", err);
            alert("Could not move the student: " + err.response.data.error);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <h2>Move {student.firstName} {student.lastName}</h2>

                {groups.length === 0 ? (
                    <p>No other groups available for this education level.</p>
                ) : (
                    <>
                        <select value={targetGroupId} onChange={(e) => setTargetGroupId(e.target.value)}>
                            {groups.map((g) => (
                                <option key={g.id} value={g.id}>
                                    {g.name} ({g.cohortName})
                                </option>
                            ))}
                        </select>

                        <div className="modal-buttons">
                            <button onClick={onClose}>Cancel</button>
                            <button onClick={handleMove}>Move</button>
                        </div>
                    </>
                )}
            </div>
        </div>
    );
};

export default MoveStudentForm;
