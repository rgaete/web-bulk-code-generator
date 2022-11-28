package com.rgfp.psd.logbook.controller;

import com.rgfp.psd.logbook.domain.Label;
import com.rgfp.psd.logbook.service.LabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.util.List;

@Controller
public class LabelController {

    @Autowired
    private LabelService labelService;

    // displays all notes
    @RequestMapping(value={"/", "labels"})
    public String noteList(Model model, @RequestParam(required = false, name = "file") String file) throws IOException, ParseException {
        List<Label> labels = null;
        if(file != null){
            labels = labelService.findAllFromFileName(file);
        }
        else {
            labels = null;
        }
        model.addAttribute("labelList", labels);
        return "labelList";
    }

    @RequestMapping(value={"/labelEdit"}, method = RequestMethod.GET)
    public String noteEditForm(Model model) {

            model.addAttribute("label", new Label());

        return "labelEdit";
    }

    @RequestMapping(value="/labelEdit", method = RequestMethod.POST)
    public String noteEdit(Model model, Label label) {
        label.setImageUrl("Producto_" + label.getCode() + ".png");
        labelService.generateImageFromLabel(label);
        //model.addAttribute("label", noteService.findAll());
        return "labelView";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes attributes) {

        // check if file is empty
        if (file.isEmpty()) {
            attributes.addFlashAttribute("message", "Please select a file to upload.");
            return "/";
        }

        // normalize the file path
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        // save the file on the local file system
        try {
            Path path = Paths.get(fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // return success response
        attributes.addFlashAttribute("message", "You successfully uploaded " + fileName + '!');

        return "redirect:/?file=" + fileName;
    }

}