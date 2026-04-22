package com.skillscout.core.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import java.util.List;

@AiService
public interface AiSkillExtractionService {

    public static class SkillResult {
        public String skill;
        public String reason;
        public String resource_name;
        public String resource_url;
    }

    public static class SkillExtraction {
        public List<SkillResult> skills;
    }

    @SystemMessage({
        "You are an expert career intelligence AI.",
        "Your task is to analyze an academic syllabus text.",
        "Identify the top 4 to 5 most in-demand technical or foundational skills related to this syllabus based on current global job market trends.",
        "Recommend exactly one free, high-quality learning resource for each.",
        "Return the result strictly as a JSON object containing a 'skills' array, where each element has 'skill', 'reason', 'resource_name', and 'resource_url'."
    })
    SkillExtraction extractSkills(String syllabusText);
}
