import React, { useEffect, useState } from "react";
import api from "../../api";
import "../../styles/admin/assign-students.css";
import "../../styles/modal.css";

const AssignStudentsForm = ({ groupId, educationLevel, onClose, onSuccess }) => {
    const [students, setStudents] = useState([]);
    const [selected, setSelected] = useState([]);

    useEffect(() => {
        const fetchUsers = async () => {
            try {
                const res = await api.get("/admin/students", {
                    params: {
                        unassignedOnly: true,
                        educationLevel: educationLevel
                    }
                });
                setStudents(res.data);
            } catch (e) {
                console.error("Failed to load users", e);
            }
        };
        fetchUsers();
    }, [educationLevel]);

    const toggleSelection = (id) => {
        setSelected((prev) =>
            prev.includes(id) ? prev.filter((sid) => sid !== id) : [...prev, id]
        );
    };

    const handleAssign = async () => {
        try {
            await api.post(`/admin/student-groups/${groupId}/assign-students`, {
                studentIds: selected,
            });
            onSuccess();
        } catch (err) {
            console.error("Failed to assign students", err);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <h2>Assign Students</h2>
                <div className="student-list">
                    {students.length === 0 ? (
                        <p>No eligible students found.</p>
                    ) : (
                        students.map((s) => (
                            <div
                                key={s.id}
                                className={`student-item ${selected.includes(s.id) ? "selected" : ""}`}
                                onClick={() => toggleSelection(s.id)}>
                                {s.firstName} {s.lastName} ({s.email})
                            </div>
                        ))
                    )}
                </div>
                <div className="modal-buttons">
                    <button onClick={onClose}>Cancel</button>
                    <button onClick={handleAssign} disabled={selected.length === 0}>
                        Assign
                    </button>
                </div>
            </div>
        </div>
    );
};

export default AssignStudentsForm;
