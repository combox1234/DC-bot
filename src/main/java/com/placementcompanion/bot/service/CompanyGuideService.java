package com.placementcompanion.bot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CompanyGuideService {

    private final ObjectMapper objectMapper;
    private Map<String, CompanyGuide> guides = new HashMap<>();

    public CompanyGuideService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadGuides() {
        try {
            ClassPathResource resource = new ClassPathResource("company_guides.json");
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    guides = objectMapper.readValue(is, new TypeReference<Map<String, CompanyGuide>>() {});
                    System.out.println("Loaded " + guides.size() + " company guides.");
                }
            } else {
                System.err.println("company_guides.json not found in resources!");
            }
        } catch (Exception e) {
            System.err.println("Failed to load company guides: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public CompanyGuide getGuide(String companyName) {
        if (companyName == null) return null;
        return guides.get(companyName.toLowerCase().trim());
    }

    public Set<String> getAvailableCompanies() {
        return guides.keySet();
    }

    public static class CompanyGuide {
        private String name;
        private String process;
        private List<String> rounds;
        private List<Map<String, String>> faqs;
        private List<String> tips;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getProcess() { return process; }
        public void setProcess(String process) { this.process = process; }

        public List<String> getRounds() { return rounds; }
        public void setRounds(List<String> rounds) { this.rounds = rounds; }

        public List<Map<String, String>> getFaqs() { return faqs; }
        public void setFaqs(List<Map<String, String>> faqs) { this.faqs = faqs; }

        public List<String> getTips() { return tips; }
        public void setTips(List<String> tips) { this.tips = tips; }
    }
}
