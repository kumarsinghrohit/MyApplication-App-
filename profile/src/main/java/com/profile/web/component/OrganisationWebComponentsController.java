package com.profile.web.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/inc")
class OrganisationWebComponentsController {
    private static final String ORGANISATION_NAME = "organisationName";

    @ModelAttribute("frontendUrl")
    public String getFrontendUrl(@Value("${frontend-url}") String contextPath) {
        return contextPath;
    }

    @GetMapping("/user-app-organisation-name")
    public String getUserProductOrganisationNameById(@PathVariable(name = "id", required = false, value = "") String id) {
        return "organisations/user_product_organisation_name";
    }

    @GetMapping("/app-organisation-name/{id}")
    public String getProductOrganisationNameById(@PathVariable String id, Model model) {
        model.addAttribute(ORGANISATION_NAME, "Dummy Organisation");
        return "organisations/product_organisation_name";
    }

    @GetMapping("/organisation-website/{id}")
    public String getOrganisationWebsiteById(@PathVariable String id, Model model) {
        model.addAttribute("organisationWebsite", "http://store.service");
        model.addAttribute(ORGANISATION_NAME, "Dummy Organisation");
        return "organisations/organisation_website";
    }
}