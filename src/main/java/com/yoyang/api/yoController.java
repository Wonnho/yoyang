package com.yoyang.api;

import java.util.Objects;
import java.util.Optional;

import com.yoyang.carer.Carer;
import com.yoyang.carer.CarerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api")
public class yoController {

    private final Optional<CarerRepository> carerRepository;

    public yoController(Optional<CarerRepository> carerRepository) {
        this.carerRepository = carerRepository;
    }

    @GetMapping("/greetings")
    public ModelAndView getGreeting() {
        return new ModelAndView("greetings");
    }

    @GetMapping("/createCarer")
    public ModelAndView showCarerForm() {
        return new ModelAndView("registerCarer");
    }

}
