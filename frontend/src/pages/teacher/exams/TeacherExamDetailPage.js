import React, {useCallback, useEffect, useState} from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown, faChevronUp } from "@fortawesome/free-solid-svg-icons";
import { useParams, useNavigate } from "react-router-dom";
import api from "../../../api";
import "../../../styles/teacher/teacher-exam-detail.css";

const TeacherExamDetailPage = () => {
    const { examId } = useParams();
    const navigate = useNavigate();
    const [exam, setExam] = useState(null);
    const [expanded, setExpanded] = useState(false);

    const fetchExam = useCallback(async () => {
        try {
            const res = await api.get(`/teacher/exams/${examId}`);
            setExam(res.data);
        } catch (err) {
            console.error("Failed to fetch exam", err);
            navigate("/not-found");
        }
    }, [examId, navigate]);

    useEffect(() => {
        fetchExam();
    }, [fetchExam]);

    if (!exam) return <p>Loading exam details...</p>;

    return (
        <div className="teacher-exam-detail">
            <h2>{exam.title}</h2>
            <p><strong>Course:</strong> {exam.courseName}</p>
            <p><strong>Scheduled Date:</strong> {exam.examDate}</p>
            <p><strong>Duration:</strong> {exam.durationMinutes} minutes</p>
            <p><strong>Created at:</strong> {exam.createdAt}</p>

            {/*<div className="exam-actions">*/}
            {/*    <button*/}
            {/*        className="monitoring-dashboard-button"*/}
            {/*        onClick={() => navigate(`/teacher/exams/${examId}/monitoring`)}*/}
            {/*            >*/}
            {/*            Go to Monitoring Dashboard*/}
            {/*            </button>*/}
            {/*            </div>*/}


            {/*<div className="exam-toggle-questions" onClick={() => setExpanded(!expanded)}>*/}
            {/*    {expanded ? (*/}
            {/*        <>*/}
            {/*            Hide Questions <FontAwesomeIcon icon={faChevronUp} />*/}
            {/*        </>*/}
            {/*    ) : (*/}
            {/*        <>*/}
            {/*            Show Questions <FontAwesomeIcon icon={faChevronDown} />*/}
            {/*        </>*/}
            {/*    )}*/}
            {/*</div>*/}

            <div className="exam-top-actions">
                <div
                        className="exam-toggle-questions"
                        onClick={() => setExpanded(!expanded)}
                >
                    {expanded ? (
                        <>
                            Hide Questions <FontAwesomeIcon icon={faChevronUp} />
                        </>
                    ) : (
                        <>
                            Show Questions <FontAwesomeIcon icon={faChevronDown} />
                        </>
                    )}
                </div>

                <button
                    className="monitoring-dashboard-button"
                    onClick={() => navigate(`/teacher/exams/${examId}/monitoring`)}
                >
                    Go to Monitoring Dashboard
                </button>
            </div>

            {expanded && (
                <div className="question-list">
                    {exam.questions.length === 0 ? (
                        <p>No questions yet.</p>
                    ) : (
                        exam.questions.map((q, index) => (
                            <div key={index} className="question-item">
                                <strong>Q{index + 1}:</strong> {q.questionText}
                                <ul>
                                    {q.options.map((opt, idx) => (
                                        <li key={idx}>
                                            {idx === q.correctAnswerIndex ? <b>{opt}</b> : opt}
                                        </li>
                                    ))}
                                </ul>
                                {q.points != null && <p><strong>Points:</strong> {q.points}</p>}
                            </div>
                        ))
                    )}
                </div>
            )}

        </div>
    );
};

export default TeacherExamDetailPage;