package com.rg.barcode.controller;

import com.rg.barcode.domain.Label;
import com.rg.barcode.service.FilesStorageService;
import com.rg.barcode.service.LabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

@Controller
public class LabelController {

    @Autowired
    private LabelService labelService;

    @Autowired
    private FilesStorageService filesStorageService;

    // displays all notes
    @RequestMapping(value={"/", "labels"})
    public String noteList(Model model, @RequestParam(required = false, name = "file") String fileName) {

        List<Label> labels = null;
        if(fileName != null){
            try {
                labels = labelService.findAllFromFileName(fileName);
                model.addAttribute("message", " Archivo cargado correctamente: " + fileName);
            } catch (ParseException | IOException e) {
                model.addAttribute("message", " Ocurrió un error al leer el archivo: " + fileName);
            }
        }
        else {
            labels = labelService.findAll();
        }
        model.addAttribute("labelList", labels);
        return "labelList";
    }

    @RequestMapping(value={"/labelView/{id}"}, method = RequestMethod.GET)
    public String noteEditForm(@PathVariable(required = false) Long id, Model model) {
        Optional<Label> label = labelService.getLabelById(id);
        model.addAttribute("label", label.get());

        return "labelView";
    }

    @RequestMapping(value={"/labelEdit"}, method = RequestMethod.GET)
    public String labelEditForm(Model model) {

            model.addAttribute("label", new Label());

        return "labelEdit";
    }

    @GetMapping("labelView/uploads/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        Resource file = filesStorageService.load(filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @RequestMapping(value="/labelEdit", method = RequestMethod.POST)
    public String labelEdit(Model model, Label label) {
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
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            Path path = Paths.get("uploads/files/" + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            attributes.addFlashAttribute("message", "Ocurrió un error al cargar el archivo " + fileName + '!');
        }

        attributes.addFlashAttribute("message", " Archivo cargado correctamente: " + fileName + '!');

        return "redirect:/?file=" + fileName;
    }

}