package com.profile.web.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller responsible for serving various app icon UI components
 * to be used by other services.
 */
@Controller
@RequestMapping("/inc")
public class ComponentsController {

    @ModelAttribute("FrontendUrl")
    public String getFrontendUrl(@Value("${frontend-url}") String contextPath) {
        return contextPath;
    }

    @GetMapping("/app-icon")
    public String renderAppIconComponent() {
        return "app-icon/app_icon";
    }

}
