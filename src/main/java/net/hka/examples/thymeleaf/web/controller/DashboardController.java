package net.hka.examples.thymeleaf.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Dashboard demo controller.
 * <p>
 * Demonstrates how a main page can embed multiple independent sub-pages via
 * {@code <iframe>}. Each sub-page can be reloaded separately (with its own
 * request parameters) without a full page refresh.
 * </p>
 *
 * <ul>
 *   <li>{@code /dashboard}              – main page with two iframes</li>
 *   <li>{@code /dashboard/stats}        – sub-page 1: static stats card</li>
 *   <li>{@code /dashboard/report}       – sub-page 2: report with category filter</li>
 * </ul>
 */
@Controller
public class DashboardController {

    /**
     * Exposes the current module name so the navigation bar in
     * {@code fragments/header.html} can highlight the active entry.
     */
    @ModelAttribute("module")
    String module() {
        return "dashboard";
    }

    /** 1. Main page: hosts two iframes pointing at the sub-pages. */
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard/layout";
    }

    /** 2. Sub-page one: real-time system stats (static for the demo). */
    @GetMapping("/dashboard/stats")
    public String statsPage(Model model) {
        model.addAttribute("activeUsers", 1314);
        model.addAttribute("systemStatus", "运行正常 (Healthy)");
        return "dashboard/stats";
    }

    /**
     * 3. Sub-page two: dynamic report. The {@code category} request parameter
     * is echoed back to keep the dropdown's selected option in sync after a
     * partial reload.
     */
    @GetMapping("/dashboard/report")
    public String reportPage(
            @RequestParam(name = "category", defaultValue = "tech") String category,
            Model model) {

        model.addAttribute("currentCategory", category);

        if ("tech".equals(category)) {
            model.addAttribute("reportTitle", "🚀 科技板块报表");
            model.addAttribute("reportData", "AI大模型、芯片算力、云服务市场需求持续暴涨。");
        } else if ("finance".equals(category)) {
            model.addAttribute("reportTitle", "💰 金融板块报表");
            model.addAttribute("reportData", "银行信贷规模稳定，证券市场迎来新一轮政策利好。");
        } else if ("lifestyle".equals(category)) {
            model.addAttribute("reportTitle", "🛍️ 消费板块报表");
            model.addAttribute("reportData", "线下零售与文旅市场复苏，客流量创近期新高。");
        }

        return "dashboard/report";
    }
}
