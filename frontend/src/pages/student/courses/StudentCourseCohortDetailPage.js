import React, {useCallback, useEffect, useState} from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../../../api";
import "../../../styles/student/student-course-details.css";

const StudentCourseCohortDetailPage = () => {
    const { targetId } = useParams();
    const navigate = useNavigate();

    const [courseCohortInfo, setCourseCohortInfo] = useState(null);
    const [materials, setMaterials] = useState([]);
    const [exams, setExams] = useState([]);
    const [expandedTextMaterialId, setExpandedTextMaterialId] = useState(null);

    const fetchCourseCohortInfo =  useCallback(async () => {
        try {
            const res = await api.get(`/student/courses/course-cohorts/${targetId}`);
            setCourseCohortInfo(res.data);
        } catch (err) {
            console.error("Failed to load course cohort info", err);
            navigate("/not-found");
        }
    }, [targetId, navigate]);

    const fetchMaterials = useCallback(async () => {
        try {
            const res = await api.get(`/student/materials/course-cohorts/${targetId}/materials`);
            setMaterials(res.data);
        } catch (err) {
            console.error("Failed to load materials", err);
            alert("Could not load materials");
        }
    }, [targetId]);

    const fetchExams = useCallback(async () => {
        try {
            const res = await api.get(`/student/courses/course-cohorts/${targetId}/exams`);
            setExams(res.data);
        } catch (err) {
            console.error("Error loading exams", err);
            alert("Could not load exams!");
        }
    },[targetId]);

    useEffect(() => {
        fetchCourseCohortInfo();
        fetchMaterials();
        fetchExams();
    }, [fetchCourseCohortInfo, fetchMaterials, fetchExams]);

    const handleDownload = async (materialId) => {
        try {
            const res = await api.get(`/student/materials/download/${materialId}`, {
                responseType: "blob",
            });

            const contentDisposition = res.headers['content-disposition'] || res.headers['Content-Disposition'];
            const contentType = res.headers['content-type'];

            let filename = "downloaded-material";
            if (contentDisposition) {
                const match = contentDisposition.match(/filename="(.+?)"/);
                if (match) {
                    filename = match[1];
                }
            }

            const blob = new Blob([res.data], { type: contentType });
            const url = window.URL.createObjectURL(blob);

            const link = document.createElement("a");
            link.href = url;
            link.setAttribute("download", filename);
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (err) {
            console.error("Failed to download material", err);
            alert("Could not download material");
        }
    };

    if (!courseCohortInfo) return <p>Loading course cohort info...</p>;

    return (
        <div className="student-course-detail">
            <div className="student-course-detail-header">
                <div>
                    <h2>{courseCohortInfo.courseName}</h2>
                    <p>Cohort: {courseCohortInfo.target}</p>
                    <p>Academic year: {courseCohortInfo.academicYear}</p>
                    <p>Teacher: {courseCohortInfo.teacherName}</p>
                </div>
            </div>

            <h3>Text Materials</h3>
            {materials.filter(m => m.content != null).length === 0 ? (
                <p className="student-no-materials-message">No text materials available.</p>
            ) : (
                <div className="student-text-materials-list">
                    {materials.filter(m => m.content != null).map((material) => (
                        <div
                            key={material.id}
                            className={`student-text-material-item ${expandedTextMaterialId === material.id ? "expanded" : ""}`}
                            onClick={() => setExpandedTextMaterialId(expandedTextMaterialId === material.id ? null : material.id)}
                        >
                            <strong>{material.title}</strong>
                            {expandedTextMaterialId === material.id && (
                                <div className="student-text-material-content">
                                    <p>{material.content}</p>
                                    <p><strong>Uploaded At:</strong> {material.uploadDate || "-"}</p>
                                    <p><strong>Last Updated:</strong> {material.lastUpdatedAt || "-"}</p>
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            )}

            <h3>File Materials</h3>
            {materials.filter(m => m.content == null).length === 0 ? (
                <p className="student-no-materials-message">No file materials available.</p>
            ) : (
                <table className="student-materials-table">
                    <thead>
                    <tr>
                        <th>Title</th>
                        <th>Uploaded At</th>
                        <th>Last Updated</th>
                        <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    {materials.filter(m => m.content == null).map((material) => (
                        <tr key={material.id}>
                            <td>{material.title}</td>
                            <td>{material.uploadDate || "-"}</td>
                            <td>{material.lastUpdatedAt || "-"}</td>
                            <td>
                                <div className="student-action-buttons file-actions">
                                    <button onClick={() => handleDownload(material.id)}>Download</button>
                                </div>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}

            <h3>Exams</h3>
            {exams.length === 0 ? (
                <p className="student-no-exams-message">No exams created yet.</p>
            ) : (
                <div className="student-exam-card-container">
                    {exams.map(exam => (
                        <div key={exam.id} className="student-exam-card">
                            <strong
                                className="student-exam-title-link"
                                onClick={() => navigate(`/student/exams/${exam.id}`)}
                            >{exam.title}</strong>
                            <p>{exam.examDate}</p>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default StudentCourseCohortDetailPage;