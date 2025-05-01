import React, { useState } from "react";
import api from "../../api";
import "../../styles/modal.css";

const UploadFileMaterialForm = ({ targetId, role, onSuccess, onClose }) => {
    const [title, setTitle] = useState("");
    const [file, setFile] = useState(null);
    const [watermarkType, setWatermarkType] = useState("INVISIBLE");
    const [errors, setErrors] = useState({});

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrors({});

        if (!file) {
            setErrors({ file: "Please select a file." });
            return;
        }

        const formData = new FormData();
        formData.append("file", file);

        const data = {
            title,
            watermarkType,
        };

        if (role === "LECTURE") {
            data.courseCohortId = targetId;
        } else if (role === "PRACTICAL") {
            data.courseGroupId = targetId;
        }

        formData.append("data", new Blob([JSON.stringify(data)], { type: "application/json" }));

        try {
            await api.post("/teacher/materials/upload/file", formData, {
                headers: {
                    "Content-Type": "multipart/form-data"
                }
            });
            onSuccess();
        } catch (error) {
            if (error.response && error.response.status === 400) {
                const errorData = error.response.data;
                if (errorData.errors) {
                    setErrors(errorData.errors);
                } else if (errorData.error) {
                    setErrors({ general: errorData.error });
                }
            } else {
                setErrors({ general: error.response?.data?.error || "Unexpected error" });
            }
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <button className="close-button" onClick={onClose}>×</button>
                <h2>Upload File Material</h2>

                {errors.general && <p className="error">{errors.general}</p>}

                <form onSubmit={handleSubmit}>
                    <input
                        type="text"
                        placeholder="Material title"
                        value={title}
                        onChange={(e) => setTitle(e.target.value.trimStart())}
                    />
                    {errors.title && <p className="error">{errors.title}</p>}

                    <input
                        type="file"
                        onChange={(e) => setFile(e.target.files[0])}
                    />
                    {errors.file && <p className="error">{errors.file}</p>}

                    <select
                        value={watermarkType}
                        onChange={(e) => setWatermarkType(e.target.value)}
                    >
                        <option value="INVISIBLE">Invisible Watermark</option>
                        <option value="VISIBLE">Visible Watermark</option>
                        <option value="NONE">No Watermark</option>
                    </select>
                    {errors.watermarkType && <p className="error">{errors.watermarkType}</p>}

                    <div className="modal-buttons">
                        <button type="button" onClick={onClose}>Cancel</button>
                        <button type="submit">Upload</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default UploadFileMaterialForm;