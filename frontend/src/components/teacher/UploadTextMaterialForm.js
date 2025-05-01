import React, { useState } from "react";
import api from "../../api";
import "../../styles/modal.css";

const UploadTextMaterialForm = ({ targetId, role, onSuccess, onClose }) => {
    const [title, setTitle] = useState("");
    const [content, setContent] = useState("");
    const [errors, setErrors] = useState({});

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrors({});

        try {
            const requestBody = {
                title,
                content,
                courseGroupId: role === "PRACTICAL" ? targetId : null,
                courseCohortId: role === "LECTURE" ? targetId : null,
            };
            await api.post("/teacher/materials/upload/text", requestBody);
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
                <h2>Upload Text Material</h2>

                {errors.general && <p className="error">{errors.general}</p>}

                <form onSubmit={handleSubmit}>
                    <input
                        type="text"
                        placeholder="Title"
                        value={title}
                        onChange={(e) => setTitle(e.target.value.trimStart())}
                    />
                    {errors.title && <p className="error">{errors.title}</p>}

                    <textarea
                        placeholder="Content"
                        value={content}
                        onChange={(e) => setContent(e.target.value.trimStart())}
                    />
                    {errors.content && <p className="error">{errors.content}</p>}

                    <div className="modal-buttons">
                        <button type="button" onClick={onClose}>Cancel</button>
                        <button type="submit">Upload</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default UploadTextMaterialForm;