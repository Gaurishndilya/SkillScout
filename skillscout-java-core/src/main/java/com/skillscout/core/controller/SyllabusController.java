package com.skillscout.core.controller;

import com.skillscout.core.service.AiSkillExtractionService;
import com.skillscout.core.service.JobScrapingService;
import com.skillscout.core.service.PdfParsingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/syllabus")
@CrossOrigin(origins = "http://localhost:3000") // Allow Next.js frontend
public class SyllabusController {

    private final PdfParsingService pdfParsingService;
    private final AiSkillExtractionService aiSkillExtractionService;
    private final JobScrapingService jobScrapingService;

    public SyllabusController(PdfParsingService pdfParsingService, 
                              AiSkillExtractionService aiSkillExtractionService,
                              JobScrapingService jobScrapingService) {
        this.pdfParsingService = pdfParsingService;
        this.aiSkillExtractionService = aiSkillExtractionService;
        this.jobScrapingService = jobScrapingService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeSyllabus(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || file.getOriginalFilename() == null || !file.getOriginalFilename().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body(Map.of("error", "File must be a valid PDF"));
        }

        try {
            // 1. Python PDF Extraction
            String extractedText = pdfParsingService.extractTextFromPdf(file);
            if (extractedText == null || extractedText.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "The uploaded PDF contains no extractable text. Please ensure it's not simply an image."));
            }

            // 2. LangChain4j AI Orchestration
            AiSkillExtractionService.SkillExtraction aiResponse;
            try {
                aiResponse = aiSkillExtractionService.extractSkills(extractedText);
            } catch (Exception aiException) {
                aiException.printStackTrace();
                return ResponseEntity.status(503).body(Map.of("error", "Google AI is currently processing high traffic and declined the request. Please wait a moment and try again."));
            }

            if (aiResponse == null || aiResponse.skills == null || aiResponse.skills.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "AI could not identify any in-demand skills from this specific academic text."));
            }
            
            // 3. Web Scraping for real-time Job listings per skill
            List<Map<String, Object>> skillsWithJobs = new ArrayList<>();
            for (AiSkillExtractionService.SkillResult aiSkill : aiResponse.skills) {
                List<JobScrapingService.JobListing> jobs = new ArrayList<>();
                try {
                    jobs = jobScrapingService.scrapeEntryLevelJobs(aiSkill.skill);
                } catch (Exception scrapingEx) {
                    // Soft-fail: if scraping throws an error we still return the AI skill but with an empty jobs list!
                    System.err.println("Scraping failed for skill: " + aiSkill.skill);
                }
                skillsWithJobs.add(Map.of(
                    "skillInfo", aiSkill,
                    "jobs", jobs
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "extractedLength", extractedText.length(),
                "skillsWithJobs", skillsWithJobs
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected core system error occurred: " + e.getMessage()));
        }
    }
}
