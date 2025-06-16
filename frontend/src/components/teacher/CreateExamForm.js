import React, { useState } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faEye, faEyeSlash } from "@fortawesome/free-solid-svg-icons";
import "../../styles/modal.css";
import "../../styles/teacher/create-exam.css";
import api from "../../api";

const CreateExamForm = ({ targetId, onSuccess, onClose }) => {
    const [title, setTitle] = useState("");
    const [examDate, setExamDate] = useState("");
    const [duration, setDuration] = useState("");
    const [password, setPassword] = useState("");
    const [customPoints, setCustomPoints] = useState(false);
    const [questions, setQuestions] = useState([
        { questionText: "", options: ["", "", ""], correctAnswerIndex: null, points: "" }
    ]);
    const [showPassword, setShowPassword] = useState(false);
    const [errors, setErrors] = useState({});

    const addQuestion = () => {
        setQuestions([...questions, { questionText: "", options: ["", "", ""], correctAnswerIndex: null, points: "" }]);
    };

    const addOption = (qIndex) => {
        if (questions[qIndex].options.length < 5) {
            const updated = [...questions];
            updated[qIndex].options.push("");
            setQuestions(updated);
        }
    };

    const removeOption = (qIndex, oIndex) => {
        const updated = [...questions];
        updated[qIndex].options.splice(oIndex, 1);
        setQuestions(updated);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrors({});

        const payload = {
            title,
            durationMinutes: parseInt(duration),
            examDate,
            password,
            courseCohortId: parseInt(targetId),
            questions: questions.map(q => ({
                questionText: q.questionText,
                options: q.options,
                correctAnswerIndex: q.correctAnswerIndex,
                points: customPoints ? parseFloat(q.points) : null
            }))
        };

        try {
            await api.post("/teacher/exams", payload);
            onSuccess();
        } catch (error) {
            const newErrors = {};
            if (error.response && error.response.status === 400) {
                const data = error.response.data;
                if (data.errors) {
                    for (const [key, message] of Object.entries(data.errors)) {
                        if (key.startsWith("questions[")) {
                            const match = key.match(/questions\[(\d+)](?:\.(\w+))?/);
                            if (match) {
                                const index = parseInt(match[1]);
                                const field = match[2] || "_";
                                if (!newErrors.questions) newErrors.questions = {};
                                if (!newErrors.questions[index]) newErrors.questions[index] = {};
                                newErrors.questions[index][field] = message;
                            }
                        } else {
                            newErrors[key] = message;
                        }
                    }
                } else if (data.error) {
                    newErrors.general = data.error;
                }
            } else {
                newErrors.general = error.response?.data?.error || "Unexpected error";
            }
            setErrors(newErrors);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content exam-modal">
                <button className="close-button" onClick={onClose}>×</button>
                <h2>Create Exam</h2>
                {errors.general && <p className="error">{errors.general}</p>}

                <form onSubmit={handleSubmit}>
                    <input type="text" placeholder="Exam title" value={title} onChange={(e) => setTitle(e.target.value)} />
                    {errors.title && <p className="error">{errors.title}</p>}

                    <input type="datetime-local" value={examDate} onChange={(e) => setExamDate(e.target.value)} />
                    {errors.examDate && <p className="error">{errors.examDate}</p>}

                    <input type="number" placeholder="Duration (minutes)" value={duration} onChange={(e) => setDuration(e.target.value)} />
                    {errors.durationMinutes && <p className="error">{errors.durationMinutes}</p>}

                    <div className="password-input-wrapper">
                        <input
                            type={showPassword ? "text" : "password"}
                            placeholder="Exam password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />
                        <span className="password-toggle-icon" onClick={() => setShowPassword(!showPassword)}>
                            <FontAwesomeIcon icon={showPassword ? faEyeSlash : faEye} />
                        </span>
                    </div>
                    {errors.password && <p className="error">{errors.password}</p>}

                    <label className="custom-points-checkbox-row">
                        <span>Use custom points per question</span>
                        <input
                            type="checkbox"
                            checked={customPoints}
                            onChange={() => setCustomPoints(!customPoints)}
                        />
                    </label>

                    {questions.map((q, qIndex) => (
                        <div key={qIndex} className="create-question-block">
                            <strong>Question {qIndex + 1}</strong>
                            <div className="option-hint">Each question must have between 3 and 5 options.</div>

                            <textarea
                                placeholder="Enter question text"
                                value={q.questionText}
                                onChange={(e) => {
                                    const updated = [...questions];
                                    updated[qIndex].questionText = e.target.value;
                                    setQuestions(updated);
                                }}
                            />
                            {errors.questions?.[qIndex]?.questionText && (
                                <p className="error">{errors.questions[qIndex].questionText}</p>
                            )}

                            {q.options.map((opt, oIndex) => (
                                <div key={oIndex} className="option-row">
                                    <input
                                        type="radio"
                                        name={`correct-${qIndex}`}
                                        checked={q.correctAnswerIndex === oIndex}
                                        onChange={() => {
                                            const updated = [...questions];
                                            updated[qIndex].correctAnswerIndex = oIndex;
                                            setQuestions(updated);
                                        }}
                                    />
                                    <textarea
                                        className="option-text"
                                        value={opt}
                                        onChange={(e) => {
                                            const updated = [...questions];
                                            updated[qIndex].options[oIndex] = e.target.value;
                                            setQuestions(updated);
                                        }}
                                    />
                                    {q.options.length > 3 && (
                                        <button
                                            type="button"
                                            className="remove-option"
                                            onClick={() => removeOption(qIndex, oIndex)}
                                        >×</button>
                                    )}
                                </div>
                            ))}
                            {errors.questions?.[qIndex]?.options && (
                                <p className="error">{errors.questions[qIndex].options}</p>
                            )}
                            {errors.questions?.[qIndex]?.correctAnswerIndex && (
                                <p className="error">{errors.questions[qIndex].correctAnswerIndex}</p>
                            )}

                            {q.options.length < 5 && (
                                <button type="button" className="add-option" onClick={() => addOption(qIndex)}>+ Add Option</button>
                            )}

                            {customPoints && (
                                <>
                                    <input
                                        type="number"
                                        placeholder="Points"
                                        value={q.points}
                                        onChange={(e) => {
                                            const updated = [...questions];
                                            updated[qIndex].points = e.target.value;
                                            setQuestions(updated);
                                        }}
                                    />
                                    {errors.questions?.[qIndex]?.points && (
                                        <p className="error">{errors.questions[qIndex].points}</p>
                                    )}
                                </>
                            )}
                        </div>
                    ))}

                    <button type="button" className="add-question" onClick={addQuestion}>+ Add Question</button>

                    <div className="modal-buttons">
                        <button type="button" onClick={onClose}>Cancel</button>
                        <button type="submit">Create Exam</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default CreateExamForm;