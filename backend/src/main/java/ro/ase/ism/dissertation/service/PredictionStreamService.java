package ro.ase.ism.dissertation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ro.ase.ism.dissertation.dto.exam.PredictionRecordResponse;
import ro.ase.ism.dissertation.dto.exam.SubmissionCompletedEventResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionStreamService {

    private final Map<Integer, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter register(Integer examId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.computeIfAbsent(examId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(examId, emitter));
        emitter.onTimeout(() -> removeEmitter(examId, emitter));
        emitter.onError(e -> removeEmitter(examId, emitter));

        return emitter;
    }

    public void publishPredictionEvent(Integer examId, PredictionRecordResponse prediction) {
        sendEvent(examId, "prediction", prediction);
    }

    public void publishSubmissionEvent(Integer examId, SubmissionCompletedEventResponse event) {
        sendEvent(examId, "submissionComplete", event);
    }

    private void sendEvent(Integer examId, String name, Object data) {
        List<SseEmitter> list = emitters.getOrDefault(examId, Collections.emptyList());
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event()
                        .name(name)
                        .data(data));
            } catch (IOException ex) {
                removeEmitter(examId, emitter);
                log.warn("Emitter failed and was removed for exam {}", examId, ex);
            }
        }
    }

    private void removeEmitter(Integer examId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(examId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emitters.remove(examId);
            }
        }
    }
}
