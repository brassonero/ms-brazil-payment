package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.entity.Person;
import com.ebitware.chatbotpayments.model.CompanyModeEnum;
import com.ebitware.chatbotpayments.model.WorkspaceDTO;
import com.ebitware.chatbotpayments.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PersonServiceImpl {

    private final PersonRepository personRepository;

    public PersonServiceImpl(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public void validateEmail(WorkspaceDTO request) {
        if (personRepository.existsByEmail(request.getUser().getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getUser().getEmail());
        }

        if (CompanyModeEnum.fromCode(request.getMode()) == CompanyModeEnum.PLATFORM
                && (request.getAccessList() == null || request.getAccessList().isEmpty())) {
            throw new RuntimeException("The company needs at least one access");
        }

        Long companyId = personRepository.createCompany(request);

        log.info("{}", companyId);
    }
}
