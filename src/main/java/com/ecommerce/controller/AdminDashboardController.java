package com.ecommerce.controller;

import com.ecommerce.dto.DashboardStats;
import com.ecommerce.dto.SalesAnalytics;
import com.ecommerce.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {
    
    @Autowired
    private AdminDashboardService dashboardService;
    
    /**
     * Get dashboard statistics
     * GET /api/admin/dashboard/stats
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        DashboardStats stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get sales analytics for a period
     * GET /api/admin/analytics/sales?period=MONTH
     */
    @GetMapping("/analytics/sales")
    public ResponseEntity<SalesAnalytics> getSalesAnalytics(
            @RequestParam(defaultValue = "MONTH") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        SalesAnalytics analytics = dashboardService.getSalesAnalytics(period, startDate, endDate);
        return ResponseEntity.ok(analytics);
    }
    
    /**
     * Get today's sales analytics
     * GET /api/admin/analytics/today
     */
    @GetMapping("/analytics/today")
    public ResponseEntity<SalesAnalytics> getTodayAnalytics() {
        SalesAnalytics analytics = dashboardService.getSalesAnalytics("TODAY", null, null);
        return ResponseEntity.ok(analytics);
    }
    
    /**
     * Get this week's sales analytics
     * GET /api/admin/analytics/week
     */
    @GetMapping("/analytics/week")
    public ResponseEntity<SalesAnalytics> getWeekAnalytics() {
        SalesAnalytics analytics = dashboardService.getSalesAnalytics("WEEK", null, null);
        return ResponseEntity.ok(analytics);
    }
    
    /**
     * Get this month's sales analytics
     * GET /api/admin/analytics/month
     */
    @GetMapping("/analytics/month")
    public ResponseEntity<SalesAnalytics> getMonthAnalytics() {
        SalesAnalytics analytics = dashboardService.getSalesAnalytics("MONTH", null, null);
        return ResponseEntity.ok(analytics);
    }
    
    /**
     * Get this year's sales analytics
     * GET /api/admin/analytics/year
     */
    @GetMapping("/analytics/year")
    public ResponseEntity<SalesAnalytics> getYearAnalytics() {
        SalesAnalytics analytics = dashboardService.getSalesAnalytics("YEAR", null, null);
        return ResponseEntity.ok(analytics);
    }
    
    /**
     * Get custom period sales analytics
     * GET /api/admin/analytics/custom?startDate=2026-01-01&endDate=2026-12-31
     */
    @GetMapping("/analytics/custom")
    public ResponseEntity<SalesAnalytics> getCustomAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        SalesAnalytics analytics = dashboardService.getSalesAnalytics("CUSTOM", startDate, endDate);
        return ResponseEntity.ok(analytics);
    }
}
