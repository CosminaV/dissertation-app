import React, { useState, useRef } from "react";
import { useNavigate } from "react-router-dom";
import api, {biometricsApi} from "../api";
import "../styles/upload-profile-image.css";

const UploadProfileImage = () => {
    const navigate = useNavigate();
    const [mode, setMode] = useState(null);
    const [image, setImage] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [message, setMessage] = useState("");
    const videoRef = useRef(null);
    const canvasRef = useRef(null);

    const startCamera = async () => {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ video: true });
            if (videoRef.current) videoRef.current.srcObject = stream;
        } catch (err) {
            console.error("Camera error:", err);
            setMessage("Unable to access camera.");
        }
    };

    const handleModeChange = (newMode) => {
        setMode(newMode);
        setImage(null);
        setMessage("");
        if (newMode === "CAMERA") startCamera();
    };

    const captureImage = () => {
        const video = videoRef.current;
        const canvas = canvasRef.current;
        if (video && canvas) {
            const context = canvas.getContext("2d");
            canvas.width = video.videoWidth;
            canvas.height = video.videoHeight;
            context.drawImage(video, 0, 0);
            canvas.toBlob(blob => {
                setImage(new File([blob], "captured.png", { type: "image/png" }));
            });
        }
    };

    const handleFileChange = (e) => {
        setImage(e.target.files[0]);
    };

    const uploadImage = async () => {
        if (!image) return;
        setUploading(true);
        setMessage("");

        const formData = new FormData();
        formData.append("file", image);

        try {
            await api.post("/user/profile-image", formData, {
                headers: { "Content-Type": "multipart/form-data" }
            });

            await biometricsApi.get("/profile-image/analyze-profile-image");
            navigate("/dashboard", { replace: true });
        } catch (err) {
            const detail = err.response?.data?.detail || err.response?.data?.error;
            setMessage(detail || "Something went wrong. Please try again.");
        } finally {
            setUploading(false);
        }
    };

    return (
        <div className="profile-uploader">
            <h2>Upload Profile Image</h2>

            <div className="toggle-mode">
                <button className={mode === "CAMERA" ? "active" : ""} onClick={() => handleModeChange("CAMERA")}>Use Camera</button>
                <button className={mode === "UPLOAD" ? "active" : ""} onClick={() => handleModeChange("UPLOAD")}>Upload from Computer</button>
            </div>

            {mode === "CAMERA" && (
                <div className="camera-container">
                    <video ref={videoRef} autoPlay />
                    <button onClick={captureImage}>Capture</button>
                    <canvas ref={canvasRef} style={{ display: "none" }} />
                </div>
            )}

            {mode === "UPLOAD" && (
                <div className="upload-container">
                    <input type="file" accept="image/*" onChange={handleFileChange} />
                </div>
            )}

            {image && (
                <>
                    <div className="preview">
                        <img src={URL.createObjectURL(image)} alt="Preview" />
                    </div>
                    <button onClick={uploadImage} disabled={uploading}>
                        {uploading ? "Uploading..." : "Upload"}
                    </button>
                </>
            )}

            {message && (
                <p className={`upload-message ${message.includes("failed") ? "error" : ""}`}>
                    {message}
                </p>
            )}
        </div>
    );
};

export default UploadProfileImage;