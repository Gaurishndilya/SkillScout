package com.skillscout.core.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@Service
public class PdfParsingService {

    private final String PYTHON_SERVICE_URL = "http://localhost:8000/extract-pdf";
    private final RestTemplate restTemplate = new RestTemplate();

    public String extractTextFromPdf(MultipartFile file) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        // Convert MultipartFile to Resource so RestTemplate can compute it correctly
        ByteArrayResource fileAsResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename() != null ? file.getOriginalFilename() : "syllabus.pdf";
            }
        };
        body.add("file", fileAsResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // POST request to Python microservice
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(PYTHON_SERVICE_URL, requestEntity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("text");
            } else {
                throw new RuntimeException("Python parsing service returned an unexpected status: " + response.getStatusCode());
            }
        } catch (org.springframework.web.client.RestClientException e) {
            throw new RuntimeException("PDF Extraction Microservice is offline or unreachable at " + PYTHON_SERVICE_URL + ". Ensure the Python service is running.");
        }
    }
}
