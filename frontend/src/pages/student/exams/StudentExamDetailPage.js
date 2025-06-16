import React, { useRef, useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Webcam from "react-webcam";
import { FaCheckCircle, FaTimesCircle } from "react-icons/fa";
import api, { biometricsApi } from "../../../api";
import { useAuth } from "../../../context/AuthContext";
import "../../../styles/student/student-exam-detail.css";
import {faChevronRight} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

const StudentExamDetailPage = () => {
    const { examId } = useParams();
    const navigate = useNavigate();
    const { accessToken } = useAuth();

    const webcamRef = useRef(null);

    const [exam, setExam] = useState(null);
    const [submissionInfo, setSubmissionInfo] = useState(null);
    const [questions, setQuestions] = useState([]);
    const [password, setPassword] = useState("");
    const [passwordVerified, setPasswordVerified] = useState(false);
    const [verifiedFace, setVerifiedFace] = useState(false);
    const [showWebcam, setShowWebcam] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [result, setResult] = useState(null);
    const [streaming, setStreaming] = useState(false);
    const [feedback, setFeedback] = useState(null);
    const [submissionId, setSubmissionId] = useState(null);

    const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
    const [answers, setAnswers] = useState([]);
    const [timeLeft, setTimeLeft] = useState(null);

    const wsRef = useRef(null);
    const timerRef = useRef(null);

    const fetchExam = useCallback(async () => {
        try {
            const res = await api.get(`/student/exams/${examId}`);
            setExam(res.data);
        } catch (err) {
            console.error("Failed to fetch exam", err);
            navigate("/not-found");
        }
    }, [examId, navigate]);

    const fetchSubmissionInfo = useCallback(async () => {
        try {
            const res = await api.get(`/student/exams/${examId}/submission-info`);
            setSubmissionInfo(res.data);
        } catch (err) {
            console.error("Failed to fetch exam submission info", err);
        }
    }, [examId]);
    
    useEffect(() => {
        fetchExam();
        fetchSubmissionInfo();
    }, [fetchExam, fetchSubmissionInfo]);

    const fetchQuestions = useCallback(async () => {
        try {
            const res = await api.get(`/student/exams/${examId}/questions`);
            setQuestions(res.data);
        } catch (err) {
            console.error("Failed to fetch questions", err);
        }
    }, [examId]);

    const handleVerifyPassword = async () => {
        try {
            await api.post(`/student/exams/${examId}/verify-password`, { password });
            setPasswordVerified(true);
        } catch (err) {
            alert("Invalid password. Try again.");
        }
    };

    const handleVerifyFace = async () => {
        setShowWebcam(true);
        setResult(null);

        setTimeout(async () => {
            const screenshot = webcamRef.current.getScreenshot();
            if (screenshot) {
                setIsLoading(true);
                const blob = await (await fetch(screenshot)).blob();
                const formData = new FormData();
                formData.append("file", blob, "photo.jpg");

                try {
                    const response = await biometricsApi.post("/verify-face", formData, {
                        headers: { "Content-Type": "multipart/form-data" },
                    });

                    if (response.data.verified) {
                        setResult("success");
                        setVerifiedFace(true);
                        setTimeout(() => setShowWebcam(false), 1500);
                    } else {
                        setResult("failure");
                    }
                } catch (error) {
                    console.error("Verification failed", error);
                    setResult("failure");
                } finally {
                    setIsLoading(false);
                }
            } else {
                setShowWebcam(false);
                alert("Couldn't access webcam.");
            }
        }, 2000);
    };

    const handleStartExam = async () => {
        try {
            const res = await api.post(`/student/exams/${examId}/start`, { examId });
            setSubmissionId(res.data.submissionId);
            setStreaming(true);
            await fetchQuestions();
            setTimeLeft(exam.durationMinutes * 60);
        } catch (err) {
            alert("Failed to start the exam.");
        }
    };

    useEffect(() => {
        if (!streaming || !webcamRef.current) {
            return;
        }

        const ws = new WebSocket(`wss://localhost:8000/ws/exam?token=${accessToken}`);
        wsRef.current = ws;

        ws.onopen = () => {
            const interval = setInterval(() => {
                const screenshot = webcamRef.current.getScreenshot();
                if (screenshot && ws.readyState === WebSocket.OPEN) {
                    ws.send(JSON.stringify({
                        image: screenshot,
                        submissionId,
                    }));
                }
            }, 500);
            timerRef.current = interval;
        };

        ws.onmessage = (event) => {
            const data = JSON.parse(event.data);
            console.log(data)
            const { face_verified, head_direction, eye_direction, message, phone_detected, live_face_detected } = data;
            const parts = [];
            if (message) {
                parts.push(message);
            }
            if (face_verified) {
                parts.push(face_verified);
            }
            if (head_direction) {
                parts.push(`Head: ${head_direction}`);
            }
            if (eye_direction) {
                parts.push(`Eyes: ${eye_direction}`);
            }
            if (phone_detected !== undefined) {
                parts.push(`Mobile phone detected: ${phone_detected ? "Yes" : "No"}`);
            }
            if (live_face_detected !== undefined) {
                parts.push(`Live face detected: ${live_face_detected ? "Yes" : "No"}`);
            }
            if (parts.length > 0) {
                setFeedback(parts.join(" | "));
            } else {
                setFeedback(null);
            }
        };

        ws.onclose = () => {
            clearInterval(timerRef.current);
            timerRef.current = null;
            wsRef.current = null;
            console.log("WebSocket closed");
        };

        return () => {
            if (wsRef.current) {
                wsRef.current.close();
            }
            if (timerRef.current) {
                clearTimeout(timerRef.current);
                timerRef.current = null;
            }
        };
    }, [streaming, accessToken, submissionId]);

    const handleAnswerSelect = (questionId, selectedIndex) => {
        setAnswers((prev) => [...prev.filter(a => a.questionId !== questionId), { questionId, selectedIndex }]);
    };

    const handleNextQuestion = () => {
        setCurrentQuestionIndex((prev) =>
            prev < questions.length - 1 ? prev + 1 : prev
        );
    };

    const handleSubmit = useCallback(async () => {
        try {
            await api.post(`/student/exam-submissions/${submissionId}/submit`, {
            answers,
            });
            setStreaming(false);
            setTimeLeft(null);
            setFeedback(null);
            await fetchSubmissionInfo();
            if (wsRef.current) {
                wsRef.current.close();
            }
            if (timerRef.current) {
                clearInterval(timerRef.current);
            }
            alert("Exam submitted successfully.");
        } catch (err) {
            console.error(err);
            alert("Submission failed. Something went wrong.");
        }
    }, [answers, fetchSubmissionInfo, submissionId]);

    useEffect(() => {
        if (!streaming || !timeLeft || timeLeft <= 0) return;
        const timer = setInterval(() => {
            setTimeLeft((prev) => {
                if (prev === 1) handleSubmit();
                return prev - 1;
            });
        }, 1000);
        return () => clearInterval(timer);
    }, [streaming, timeLeft, handleSubmit]);

    if (!exam) return <p>Loading exam details...</p>;

    return (
        <div className="student-exam-detail">
            {(showWebcam || streaming) && (
                <div className="verify-webcam-modal">
                    <Webcam
                        audio={false}
                        ref={webcamRef}
                        screenshotFormat="image/jpeg"
                        className="verify-webcam-feed"
                    />
                    {isLoading && <div className="webcam-spinner" />}
                    {!isLoading && !streaming && result === "success" && <FaCheckCircle className="icon success" />}
                    {!isLoading && !streaming && result === "failure" && <FaTimesCircle className="icon failure" />}
                </div>
            )}

            {streaming && feedback && <div className="student-warning">{feedback}</div>}

            {!streaming && (
                <>
                    <h2>{exam.title}</h2>
                    <p><strong>Course:</strong> {exam.courseName}</p>
                    <p><strong>Scheduled Date:</strong> {exam.examDate}</p>
                    <p><strong>Duration:</strong> {exam.durationMinutes} minutes</p>
                    {submissionInfo && (
                        <>
                            <p><strong>Started at:</strong> {submissionInfo.startedAt}</p>
                            <p><strong>Submitted at:</strong> {submissionInfo.submittedAt}</p>
                        </>
                    )}
                </>
            )}

            {!submissionInfo?.submittedAt && !passwordVerified && (
                <div className="password-check">
                    <input
                        type="password"
                        placeholder="Enter exam password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                    <button onClick={handleVerifyPassword}>Verify Password</button>
                </div>
            )}

            {!submissionInfo?.submittedAt && passwordVerified && !verifiedFace && (
                <button className="verify-student-button" onClick={handleVerifyFace}>
                    Verify Face
                </button>
            )}

            {!submissionInfo?.submittedAt && verifiedFace && !streaming && (
                <button className="start-exam-button" onClick={handleStartExam}>
                    Start Exam
                </button>
            )}

            {timeLeft !== null && (
                <div className="exam-timer">Time left: {Math.floor(timeLeft / 60)}:{String(timeLeft % 60).padStart(2, "0")}</div>
            )}

            {streaming && timeLeft !== null && questions.length > 0 && currentQuestionIndex < questions.length && (
                <div className="question-block">
                    <h3>Question {currentQuestionIndex + 1}</h3>
                    <p>{questions[currentQuestionIndex].text}</p>
                    {questions[currentQuestionIndex].options.map((opt, i) => (
                        <div key={i}>
                            <label>
                                <input
                                    type="radio"
                                    name={`q${questions[currentQuestionIndex].id}`}
                                    value={i}
                                    onChange={() => handleAnswerSelect(questions[currentQuestionIndex].id, i)}
                                    checked={answers.some(a => a.questionId === questions[currentQuestionIndex].id && a.selectedIndex === i)}
                                />
                                {opt}
                            </label>
                        </div>
                    ))}

                    {currentQuestionIndex < questions.length - 1 && (
                        <div className="next-question-arrow" onClick={handleNextQuestion}>
                            <FontAwesomeIcon icon={faChevronRight}/> Next
                        </div>
                    )}
                </div>
            )}

            {streaming && timeLeft !== null && currentQuestionIndex === questions.length - 1 && (
                <div className="submit-exam-wrapper">
                    <button className="submit-exam-button" onClick={handleSubmit}>Submit Exam</button>
                </div>
            )}
        </div>
    );
};

export default StudentExamDetailPage;