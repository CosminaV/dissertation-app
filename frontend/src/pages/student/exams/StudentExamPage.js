// import React, { useRef, useState, useEffect } from 'react';
// import Webcam from 'react-webcam';
// import { FaCheckCircle, FaTimesCircle } from 'react-icons/fa';
// import { biometricsApi } from '../../../api';
// import { useAuth } from "../../../context/AuthContext";
// import "../../../styles/student/student-exams.css";
//
// const StudentExamPage = () => {
//     const webcamRef = useRef(null);
//     const [showWebcam, setShowWebcam] = useState(false);
//     const [isLoading, setIsLoading] = useState(false);
//     const [result, setResult] = useState(null);
//     const [verified, setVerified] = useState(false);
//     const [streaming, setStreaming] = useState(false);
//     const [feedback, setFeedback] = useState(null);
//     const {accessToken} = useAuth();
//
//     const handleVerify = async () => {
//         setShowWebcam(true);
//         setResult(null);
//
//         setTimeout(async () => {
//             const screenshot = webcamRef.current.getScreenshot();
//
//             if (screenshot) {
//                 setIsLoading(true);
//                 const blob = await (await fetch(screenshot)).blob();
//                 const formData = new FormData();
//                 formData.append('file', blob, 'photo.jpg');
//
//                 try {
//                     const response = await biometricsApi.post('/verify-face', formData, {
//                         headers: { 'Content-Type': 'multipart/form-data' }
//                     });
//
//                     if (response.data.verified) {
//                         setResult('success');
//                         setVerified(true);
//                         setTimeout(() => setShowWebcam(false), 1500);
//                     } else {
//                         setResult('failure');
//                     }
//                 } catch (error) {
//                     console.error("Verification failed", error);
//                     setResult('failure');
//                 } finally {
//                     setIsLoading(false);
//                 }
//             } else {
//                 setShowWebcam(false);
//                 alert("Couldn't access webcam.");
//             }
//         }, 2000);
//     };
//
//     useEffect(() => {
//         let interval = null;
//         let ws;
//
//         if (streaming && webcamRef.current) {
//             ws = new WebSocket('wss://localhost:8000/ws/exam');
//
//             ws.onopen = () => {
//                 interval = setInterval(() => {
//                     const screenshot = webcamRef.current.getScreenshot();
//                     if (screenshot && ws.readyState === WebSocket.OPEN) {
//                         ws.send(JSON.stringify({
//                             image: screenshot,
//                             token: accessToken
//                         }));
//                     }
//                 }, 500);
//             };
//
//             ws.onmessage = (event) => {
//                 const data = JSON.parse(event.data);
//                 console.log("websocket message: ", data);
//                 if (data.head_direction || data.eye_direction) {
//                     setFeedback(`Head: ${data.head_direction} | Eyes: ${data.eye_direction}`);
//                 } else if (data.message) {
//                     setFeedback(data.message)
//                 }
//                 else {
//                     setFeedback(null);
//                 }
//             };
//
//             ws.onclose = () => {
//                 clearInterval(interval);
//                 console.log("WebSocket closed");
//             };
//
//             return () => {
//                 ws.close();
//                 clearInterval(interval);
//             };
//         }
//     }, [streaming, accessToken]);
//
//     return (
//         <div className="student-exam-page-container">
//             <h2>Welcome to the Exam</h2>
//
//             {!verified && (
//                 <button className="verify-student-button" onClick={handleVerify}>
//                     Verify Me
//                 </button>
//             )}
//
//             {verified && !streaming && (
//                 <button className="start-exam-button" onClick={() => setStreaming(true)}>
//                     Start Exam
//                 </button>
//             )}
//
//             {(showWebcam || streaming) && (
//                 <div className="verify-webcam-modal">
//                     <Webcam
//                         audio={false}
//                         ref={webcamRef}
//                         screenshotFormat="image/jpeg"
//                         className="verify-webcam-feed"
//                     />
//                     {isLoading && <div className="spinner" />}
//                     {!isLoading && !streaming && result === 'success' && <FaCheckCircle className="icon success" />}
//                     {!isLoading && !streaming && result === 'failure' && <FaTimesCircle className="icon failure" />}
//                 </div>
//             )}
//
//             {feedback && (
//                 <div className="student-warning">
//                     {feedback}
//                 </div>
//             )}
//         </div>
//     );
// };
//
// export default StudentExamPage;