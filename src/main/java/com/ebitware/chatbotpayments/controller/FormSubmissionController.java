package com.ebitware.chatbotpayments.controller;

import com.ebitware.chatbotpayments.model.*;
import com.ebitware.chatbotpayments.service.CompanyService;
import lombok.RequiredArgsConstructor;
import com.ebitware.chatbotpayments.service.FormSubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/forms")
public class FormSubmissionController {

    private final CompanyService companyService;
    private final FormSubmissionService formSubmissionService;

    @PostMapping(value = "/submit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> submitForm(@Valid @RequestBody FormSubmissionRequest form) {
        formSubmissionService.saveSubmission(form, null);

        WorkspaceDTO workspaceDTO = createWorkspaceFromForm(form);
        Long companyId = companyService.createCompany(workspaceDTO);
        Map<String, Object> companyDetails = companyService.getCompanyDetails(companyId);

        Map<String, Object> response = new HashMap<>();
        response.put("httpStatus", HttpStatus.CREATED.value());
        response.put("message", "Company created successfully");
        response.put("data", companyDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private WorkspaceDTO createWorkspaceFromForm(FormSubmissionRequest form) {
        WorkspaceDTO workspace = new WorkspaceDTO();
        workspace.setActive(true);
        workspace.setMode("1");

        AccessDTO access = new AccessDTO();
        access.setId(3L);
        access.setName("security");
        access.setKeyName("security");
        access.setCreatedAt(ZonedDateTime.now());
        workspace.setAccessList(List.of(access));

        workspace.setAgents(5);
        workspace.setAppleBot(false);

        BotDTO bot = new BotDTO();
        bot.setName("");
        bot.setPort("");
        bot.setCustomMessage("default");
        bot.setMessage("Welcome! Select the desired option:");
        workspace.setBots(List.of(bot));

        workspace.setContacts(1000);
        workspace.setKeyName(form.getDescription());
        workspace.setMultipleAgents(true);
        workspace.setMultipleSupervisors(true);
        workspace.setName(form.getBusinessName());
        workspace.setSupervisors(1);
        workspace.setTypeWorkgroups("predetermined");

        UserDTO user = new UserDTO();
        String[] names = form.getDisplayName().split(" ");
        if (names.length >= 1) {
            user.setFirstName(names[0]);
        }
        if (names.length >= 2) {
            user.setLastName(names[1]);
        }
        if (names.length >= 3) {
            user.setSecondLastName(names[2]);
        }
        user.setEmail(form.getCorporateEmail());
        workspace.setUser(user);

        workspace.setWorkgroups(5);
        workspace.setWorkgroupsActive(true);

        return workspace;
    }
}
