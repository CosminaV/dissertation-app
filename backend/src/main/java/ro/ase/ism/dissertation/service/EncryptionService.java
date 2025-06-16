package ro.ase.ism.dissertation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTransitOperations;
import ro.ase.ism.dissertation.dto.exam.AnswerResponse;
import ro.ase.ism.dissertation.exception.ExamCryptoException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EncryptionService {

    private final VaultTransitOperations vaultTransitOperations;
    private final ObjectMapper objectMapper;

    public String encryptAnswers(List<AnswerResponse> answerResponses) {
        try {
            String plaintext = objectMapper.writeValueAsString(answerResponses);
            return vaultTransitOperations.encrypt("exam-aes-gcm", plaintext);
        } catch (JsonProcessingException e) {
            throw new ExamCryptoException("Could not serialize answers");
        }
    }

    public List<AnswerResponse> decryptAnswers(String ciphertext) {
        String json = vaultTransitOperations.decrypt("exam-aes-gcm", ciphertext);
        try {
            return objectMapper.readValue(json, new TypeReference<>(){});
        } catch (JsonProcessingException e) {
            throw new ExamCryptoException("Could not deserialize answers");
        }
    }
}
