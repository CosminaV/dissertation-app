import React, {useEffect, useState} from "react";
import api from "../../api";
import "../../styles/modal.css";

const CreateCourseForm = ({ onSuccess, onClose }) => {
    const [courseName, setCourseName] = useState("");
    const [yearOfStudy, setYearOfStudy] = useState(1);
    const [semester, setSemester] = useState(1);
    const [educationLevel, setEducationLevel] = useState("BACHELOR");
    const [studyYearsOptions, setStudyYearsOptions] = useState({});
    const [errors, setErrors] = useState({});

    const fetchStudyYears = async () => {
        try {
            const res = await api.get("/common/study-years");
            setStudyYearsOptions(res.data);
        } catch (error) {
            console.error("Failed to fetch study years", error);
            alert("Failed to fetch study years");
        }
    }

    useEffect(() => {
        fetchStudyYears();
    }, []);

    const availableYears = studyYearsOptions[educationLevel] || [];

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrors({});

        try {
            await api.post("/admin/courses", {
                name: courseName,
                yearOfStudy,
                semester,
                educationLevel,
            });
            onSuccess();
        } catch (error) {
            if (error.response && error.response.status === 400) {
                const errorData = error.response.data;

                if (errorData.errors && typeof errorData.errors === "object") {
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
                <h2>Create New Course</h2>
                {errors.general && <p className="error">{errors.general}</p>}
                <form onSubmit={handleSubmit}>
                    <input
                        type="text"
                        placeholder="Course Name"
                        value={courseName}
                        onChange={(e) => setCourseName(e.target.value)}
                    />
                    {errors.name && <p className="error">{errors.name}</p>}

                    <select
                        value={educationLevel}
                        onChange={(e) => {
                            const newLevel = e.target.value;
                            setEducationLevel(newLevel);
                            const available = studyYearsOptions[newLevel];
                            if (available && available.length > 0) {
                                setYearOfStudy(available[0]); // pick the first valid year automatically
                            }
                        }}>
                        <option value="BACHELOR">Bachelor</option>
                        <option value="MASTER">Master</option>
                        <option value="PHD">PhD</option>
                    </select>
                    {errors.educationLevel && <p className="error">{errors.educationLevel}</p>}

                    <select
                        value={yearOfStudy}
                        onChange={(e) => setYearOfStudy(Number(e.target.value))}
                        disabled={!availableYears.length}>
                        {availableYears.map((year) => (
                            <option key={year} value={year}>
                                Year {year}
                            </option>
                        ))}
                    </select>
                    {errors.yearOfStudy && <p className="error">{errors.yearOfStudy}</p>}

                    <select
                        value={semester}
                        onChange={(e) => setSemester(Number(e.target.value))}>
                        <option value={1}>Semester 1</option>
                        <option value={2}>Semester 2</option>
                    </select>
                    {errors.semester && <p className="error">{errors.semester}</p>}

                    <div className="modal-buttons">
                        <button type="button" onClick={onClose}>Cancel</button>
                        <button type="submit">Create</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default CreateCourseForm;