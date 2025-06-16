import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api from "../../../api";
import "../../../styles/teacher/teacher-exam-monitoring.css";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faChevronDown, faChevronUp, faCheckCircle, faTimesCircle} from "@fortawesome/free-solid-svg-icons";

const classLabels = {
    0: "No Cheating",
    1: "Soft Cheating",
    2: "Cheating"
};

const TeacherExamMonitoringPage = () => {
    const { examId } = useParams();
    const [students, setStudents] = useState([]);
    const [predictionsByStudent, setPredictionsByStudent] = useState({});
    const [expandedStudentId, setExpandedStudentId] = useState(null);
    const [submissionsByStudent, setSubmissionsByStudent] = useState({});
    const [expandedSubmissionId, setExpandedSubmissionId] = useState(null);
    const [gradeInputs, setGradeInputs] = useState({});
    const [gradedSubs, setGradedSubs] = useState(new Set());

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [studentsRes, predictionsRes, submissionsRes] = await Promise.all([
                    api.get(`/teacher/exams/${examId}/students`),
                    api.get(`/streaming/predictions/history`, { params: { examId } }),
                    api.get(`/streaming/submissions/history`, { params: { examId } })
            ]);

                setStudents(studentsRes.data);

                const groupedPredictions = {};
                for (const pred of predictionsRes.data) {
                    const sid = pred.studentId;
                    if (!groupedPredictions[sid]) groupedPredictions[sid] = [];
                    groupedPredictions[sid].push({
                        windowIndex: pred.windowIndex,
                        predictedClass: pred.predictedClass,
                        maxProb: Math.max(...pred.probabilities),
                        timestamp: pred.timestamp
                    });
                }
                setPredictionsByStudent(groupedPredictions);

                const groupedSubmissions = {};
                submissionsRes.data.forEach(s => {
                    groupedSubmissions[s.studentId] = groupedSubmissions[s.studentId] || [];
                    groupedSubmissions[s.studentId].push(s);
                    if (s.grade != null) {
                        // if backend already has an assigned grade, mark it so we hide input
                        setGradedSubs(prev => new Set(prev).add(s.examSubmissionId));
                    }
                });
                setSubmissionsByStudent(groupedSubmissions);
            } catch (err) {
                console.error("Failed to load monitoring data", err);
            }
        };

        fetchData();
    }, [examId]);

    useEffect(() => {
        const source = new EventSource(`https://localhost:8443/api/streaming?examId=${examId}`);
        source.addEventListener("prediction", (event) => {
            const data = JSON.parse(event.data);
            const { studentId, windowIndex, predictedClass, probabilities, timestamp } = data;

            setPredictionsByStudent(prev => {
                const newState = { ...prev };
                const newPrediction = {
                    windowIndex,
                    predictedClass,
                    maxProb: Math.max(...probabilities),
                    timestamp: timestamp
                };

                const existing = newState[studentId] || [];
                if (!existing.some(p => p.windowIndex === windowIndex)) {
                    newState[studentId] = [...existing, newPrediction].sort(
                        (a, b) => a.windowIndex - b.windowIndex
                    );
                }

                return newState;
            });
        });

        source.addEventListener("submissionComplete", e => {
            const sub = JSON.parse(e.data);
            setSubmissionsByStudent(prev => {
                const next = { ...prev };
                const arr = next[sub.studentId] || [];
                arr.push(sub);
                next[sub.studentId] = arr;
                if (sub.grade !== null) {
                    setGradedSubs(ps => new Set(ps).add(sub.examSubmissionId));
                }
                return next;
            });
        });

        source.onerror = (err) => {
            console.error("SSE connection error:", err);
            source.close();
        };

        return () => {
            source.close();
        };
    }, [examId]);

    const onGradeChange = (submissionId, value) => {
        setGradeInputs(gs => ({ ...gs, [submissionId]: value }));
    };

    const submitGrade = async (submissionId) => {
        const grade = parseFloat(gradeInputs[submissionId]);
        if (isNaN(grade)) return alert("Please enter a valid number");
        try {
            await api.patch(
                `/teacher/exam-submissions/${submissionId}/grade`,
            { grade }
            );
            // mark it as graded so the input and button dissappear
            setGradedSubs(ps => new Set(ps).add(submissionId));
            // inject the new grade into the map
            setSubmissionsByStudent(prev => {
                const next = { ...prev };
                for (const studentId in next) {
                    next[studentId] = next[studentId].map(sub =>
                        sub.examSubmissionId === submissionId
                            ? { ...sub, grade: grade + 1.0 }   // overwrite grade
                            : sub
                    );
                }
                return next;
            });

        } catch (err) {
            console.error(err);
            alert("Failed to submit grade");
        }
    };

    return (
        <div className="teacher-exam-monitoring">
            <h2>Exam Monitoring</h2>
            {students.map(student => (
                <div key={student.id} className="student-section">
                    <div
                        className="student-header"
                        onClick={() => setExpandedStudentId(prev => prev === student.id ? null : student.id)}
                    >
                        <strong>{student.firstName} {student.lastName}</strong> ({student.email})
                    </div>
                    {expandedStudentId === student.id && (
                        <>
                            <div className="student-predictions">
                                {predictionsByStudent[student.id]?.length ? (
                                    <table>
                                        <thead>
                                        <tr>
                                            <th>Window</th>
                                            <th>Predicted Class</th>
                                            <th>Confidence</th>
                                            <th>Timestamp</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {predictionsByStudent[student.id].map((p, idx) => {
                                            return (
                                                <tr key={idx}>
                                                    <td>{p.windowIndex}</td>
                                                    <td>
                                                        {p.predictedClass} – <strong>{classLabels[p.predictedClass]}</strong>
                                                    </td>
                                                    <td>{(p.maxProb * 100).toFixed(2)}%</td>
                                                    <td>{p.timestamp}</td>
                                                </tr>
                                            );
                                        })}
                                        </tbody>
                                    </table>
                                ) : (
                                    <p>No predictions yet.</p>
                                )}
                            </div>

                            {(submissionsByStudent[student.id] || []).length > 0 && (
                                <div
                                    className="student-exam-toggle-questions"
                                    onClick={() =>
                                        setExpandedSubmissionId(sid =>
                                            sid === student.id ? null : student.id
                                        )
                                    }
                                >
                                    {expandedSubmissionId === student.id ? (
                                        <>
                                            Hide Submission Info{" "}
                                            <FontAwesomeIcon icon={faChevronUp} />
                                        </>
                                    ) : (
                                        <>
                                            See Submission Info{" "}
                                            <FontAwesomeIcon icon={faChevronDown} />
                                        </>
                                    )}
                                </div>
                            )}

                            {expandedSubmissionId === student.id && (
                                <div className="submission-info-panel">
                                    {(submissionsByStudent[student.id] || []).map(sub => {
                                        // pick final grade: override or computed score
                                        const officePoint = 1.0;
                                        const maxPoints = 9.0;
                                        const isAssigned = sub.grade != null;
                                        const finalGrade = isAssigned ? (sub.grade - officePoint) : sub.score;
                                        const label = isAssigned  ? `/ ${maxPoints} (assigned)` : `/ ${maxPoints} (computed)`;
                                        return (
                                            <div key={sub.examSubmissionId} className="submission-block">
                                                <p><em>Submitted at: {sub.submittedAt}</em></p>
                                                <p><strong>Score:</strong> {sub.score.toFixed(2)} / 9</p>
                                                <p className="final-grade">
                                                    <strong>Final Grade:</strong> {finalGrade.toFixed(2)} {label}
                                                </p>
                                                <ul className="answers-list">
                                                    {sub.answers.map(a => (
                                                        <li
                                                            key={a.questionId}
                                                            className={a.isCorrect ? "correct" : "incorrect"}
                                                        >
                                                            Q{a.questionId}: choice #{a.selectedIndex}{" "}
                                                            {a.isCorrect
                                                                ? <FontAwesomeIcon icon={faCheckCircle} />
                                                                : <FontAwesomeIcon icon={faTimesCircle} />
                                                            }
                                                        </li>
                                                    ))}
                                                </ul>

                                                {!gradedSubs.has(sub.examSubmissionId) && (
                                                    <div className="grade-entry">
                                                        <input
                                                            type="number"
                                                            placeholder="Grade…"
                                                            value={gradeInputs[sub.examSubmissionId] || ""}
                                                            onChange={e =>
                                                                onGradeChange(sub.examSubmissionId, e.target.value)
                                                            }
                                                        />
                                                        <button onClick={() => submitGrade(sub.examSubmissionId)}>
                                                            Submit Grade
                                                        </button>
                                                    </div>
                                                )}
                                            </div>
                                        );
                                    })}
                                </div>
                            )}
                        </>
                    )}
                </div>
            ))}
        </div>
    );
};

export default TeacherExamMonitoringPage;