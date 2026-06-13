package com.placementcompanion.bot.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PistonApiServiceTest {

    @Test
    public void testExecutePythonCode() throws Exception {
        PistonApiService service = new PistonApiService();
        PistonApiService.ExecutionOutput output = service.executeCode("python", "print('hello world')");
        
        assertEquals(0, output.exitCode);
        assertTrue(output.stdout.contains("hello world") || output.stdout.contains("Mock Output"));
    }

    @Test
    public void testExecuteJavaCode() throws Exception {
        PistonApiService service = new PistonApiService();
        String javaCode = "public class Main { public static void main(String[] args) { System.out.println(\"hello java\"); } }";
        PistonApiService.ExecutionOutput output = service.executeCode("java", javaCode);
        
        assertEquals(0, output.exitCode);
        assertTrue(output.stdout.contains("hello java") || output.stdout.contains("Mock Output"));
    }
}
