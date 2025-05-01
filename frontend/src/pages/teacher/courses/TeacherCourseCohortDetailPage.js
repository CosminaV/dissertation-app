import React, { useEffect, useState, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../../../api";
import "../../../styles/teacher/teacher-course-details.css";
import UploadTextMaterialForm from "../../../components/teacher/UploadTextMaterialForm";
import UploadFileMaterialForm from "../../../components/teacher/UploadFileMaterialForm";

const TeacherCourseCohortDetailPage = () => {
    const { targetId } = useParams();
    const navigate = useNavigate();

    const [courseCohortInfo, setCourseCohortInfo] = useState(null);
    const [materials, setMaterials] = useState([]);
    const [showUploadTextModal, setShowUploadTextModal] = useState(false);
    const [showUploadFileModal, setShowUploadFileModal] = useState(false);
    const [editedMaterial, setEditedMaterial] = useState(null);
    const [editedTitle, setEditedTitle] = useState("");
    const [editedContent, setEditedContent] = useState("");
    const [expandedTextMaterialId, setExpandedTextMaterialId] = useState(null);

    const fetchCourseCohortInfo = useCallback(async () => {
        try {
            const res = await api.get(`/teacher/courses/course-cohorts/${targetId}`);
            setCourseCohortInfo(res.data);
        } catch (err) {
            console.error("Error loading course cohort info", err);
            navigate("/not-found");
        }
    }, [targetId, navigate]);

    const fetchMaterials = useCallback(async () => {
        try {
            const res = await api.get(`/teacher/materials/course-cohorts/${targetId}/materials`);
            setMaterials(res.data);
        } catch (err) {
            console.error("Error loading materials", err);
            alert("Could not load materials!");
        }
    }, [targetId]);

    useEffect(() => {
        fetchCourseCohortInfo();
        fetchMaterials();
    }, [fetchCourseCohortInfo, fetchMaterials]);

    const handleDownload = async (materialId) => {
        try {
            const res = await api.get(`/teacher/materials/download/${materialId}`, {
                responseType: "blob"
            });

            const contentDisposition = res.headers['content-disposition'] || res.headers['Content-Disposition'];
            const contentType = res.headers['content-type'];

            let filename = "downloaded-material";
            console.log(contentDisposition)
            if (contentDisposition) {
                console.log(contentDisposition)
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
            alert("Could not download material!");
        }
    };

    const handleDelete = async (materialId) => {
        if (!window.confirm("Are you sure you want to delete this material?")) {
            return;
        }

        try {
            await api.delete(`/teacher/materials/${materialId}`);
            fetchMaterials(); // Refresh the materials list
        } catch (err) {
            console.error("Failed to delete material", err);
            alert("Could not delete material!");
        }
    };

    const handleEdit = (material) => {
        setEditedMaterial(material);
        setEditedTitle(material.title || "");
        setEditedContent(material.content || "");
    };

    const handleSaveEdit = async (e) => {
        e.preventDefault();
        try {
            await api.patch(`/teacher/materials/${editedMaterial.id}`, {
                title: editedTitle,
                content: editedMaterial.content != null ? editedContent : null,
            });
            setEditedMaterial(null);
            fetchMaterials();
        } catch (error) {
            console.error("Failed to update material", error);
            alert("Could not update material");
        }
    };

    if (!courseCohortInfo) return <p>Loading course cohort info...</p>;

    return (
        <div className="teacher-course-detail">
            <div className="course-header">
                <div>
                    <h2>{courseCohortInfo.courseName}</h2>
                    <p>Cohort: {courseCohortInfo.target}</p>
                    <p>Academic year: {courseCohortInfo.academicYear}</p>
                </div>
                <div className="course-actions">
                    <button onClick={() => setShowUploadTextModal(true)}>Upload Text Material</button>
                    <button onClick={() => setShowUploadFileModal(true)}>Upload File Material</button>
                </div>
            </div>

            <h3>Text Materials</h3>
            <div className="text-materials-list">
                {materials.filter(m => m.content != null).map((material) => (
                    <div
                        key={material.id}
                        className={`text-material-item ${
                            expandedTextMaterialId === material.id ? "expanded" : ""
                        }`}
                        onClick={() =>
                            setExpandedTextMaterialId(
                                expandedTextMaterialId === material.id ? null : material.id
                            )
                        }
                    >
                        <strong>{material.title}</strong>
                        {expandedTextMaterialId === material.id && (
                            <div className="text-material-content">
                                <p>{material.content}</p>
                                <p><strong>Uploaded At:</strong> {material.uploadDate || "-"}</p>
                                <p><strong>Last Updated:</strong> {material.lastUpdatedAt || "-"}</p>
                                <div className="action-buttons text-actions">
                                    <button
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            handleEdit(material);
                                        }}
                                    >
                                        Edit
                                    </button>
                                    <button
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            handleDelete(material.id);
                                        }}
                                    >
                                        Delete
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                ))}
            </div>

            <h3>File Materials</h3>
            <table className="materials-table">
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
                            <div className="action-buttons file-actions">
                                <button onClick={() => handleDownload(material.id)}>Download</button>
                                <button onClick={() => handleEdit(material)}>Edit</button>
                                <button onClick={() => handleDelete(material.id)}>Delete</button>
                            </div>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>

            {showUploadTextModal && (
                <UploadTextMaterialForm
                    targetId={targetId}
                    role={courseCohortInfo.role}
                    onClose={() => setShowUploadTextModal(false)}
                    onSuccess={() => {
                        fetchMaterials();
                        setShowUploadTextModal(false);
                    }}
                />
            )}

            {showUploadFileModal && (
                <UploadFileMaterialForm
                    targetId={targetId}
                    role={courseCohortInfo.role}
                    onClose={() => setShowUploadFileModal(false)}
                    onSuccess={() => {
                        fetchMaterials();
                        setShowUploadFileModal(false);
                    }}
                />
            )}

            {editedMaterial && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <button className="close-button" onClick={() => setEditedMaterial(null)}>×</button>
                        <h2>Edit Material</h2>

                        <form onSubmit={handleSaveEdit}>
                            <input
                                type="text"
                                value={editedTitle}
                                onChange={(e) => setEditedTitle(e.target.value)}
                                placeholder="Material Title"
                            />
                            {editedMaterial.content != null && (
                                <textarea
                                    value={editedContent}
                                    onChange={(e) => setEditedContent(e.target.value)}
                                    placeholder="Material Content"
                                    rows="6"
                                />
                            )}
                            <div className="modal-buttons">
                                <button type="button" onClick={() => setEditedMaterial(null)}>Cancel</button>
                                <button type="submit">Save Changes</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default TeacherCourseCohortDetailPage;