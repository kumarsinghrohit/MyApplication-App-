package com.profile.web.component;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserComponentsController {

    @GetMapping("/organisations/user-product-organisation-name")
    public String renderOrganisationInfo() {
        return "organisations/user_product_organisation_name";
    }
}
