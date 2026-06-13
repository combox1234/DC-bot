package com.placementcompanion.bot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CompanyGuideServiceTest {

    @Test
    public void testLoadGuidesAndLookup() {
        CompanyGuideService service = new CompanyGuideService(new ObjectMapper());
        service.loadGuides(); // Trigger loading of JSON file

        assertTrue(service.getAvailableCompanies().size() > 0, "Should load at least one company guide");
        
        // Test case-insensitive lookup
        CompanyGuideService.CompanyGuide amazonGuide = service.getGuide("AMAZON");
        assertNotNull(amazonGuide, "Should find Amazon guide");
        assertEquals("Amazon", amazonGuide.getName());
        assertFalse(amazonGuide.getProcess().isEmpty(), "Process should not be empty");
        
        CompanyGuideService.CompanyGuide tcsGuide = service.getGuide("tcs");
        assertNotNull(tcsGuide, "Should find TCS guide");
        assertEquals("TCS", tcsGuide.getName());

        CompanyGuideService.CompanyGuide unknownGuide = service.getGuide("unknown_company_123");
        assertNull(unknownGuide, "Should return null for unknown company");
    }
}
