package web;

import dados.Dados;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/")
public class IndexController {

    @Autowired
    Dados dados;

    @RequestMapping
    public ModelAndView index() {
        ModelAndView mv = new ModelAndView("index");
        mv.addObject("helloWorld", "Olá mundo");
        return mv;
    }
}
