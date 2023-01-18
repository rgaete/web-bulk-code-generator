package com.rg.barcode.service;

import com.pnuema.java.barcode.Barcode;
import com.pnuema.java.barcode.EncodingType;
import com.rg.barcode.repository.LabelRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.rg.barcode.domain.Label;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.*;

@Service
public class LabelService {

    private List<Label> allLabels;

    @Autowired
    private LabelRepository labelRepository;

    List<Label> getAllLabels() {
        return allLabels;
    }

    public List<Label> generateLabel(String fileLocation) throws IOException, ParseException {
        ArrayList<Label> labels = new ArrayList<>();
        FileInputStream file = new FileInputStream(new File("uploads/files/" + fileLocation));
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        Map<Integer, List<String>> data = new HashMap<>();
        int i = 0;
        for (Row row : sheet) {
            if(i>=4){
                data.put(i, new ArrayList<String>());
                for (Cell cell : row) {
                    data.get(i).add(cell.toString());
                }
            }
            i++;
        }

        for (Map.Entry entrySet: data.entrySet()) {
            if((int)entrySet.getKey() > 0){
                try {
                    ArrayList<String> values = ((ArrayList<String>) entrySet.getValue());
                    Label label = new Label();
                    String code = values.get(1).trim().replace("\"","").replace(".0","");
                    if(code!= "") {
                        System.out.println("CODE: " + code);
                        Number price = NumberFormat.getInstance().parse(values.get(4));
                        label.setCode(code);
                        String description = values.get(3);
                        label.setDescription(description);
                        label.setPrice(price);
                        label.setImageUrl("Producto_" + code + ".png");
                        labels.add(label);
                        labelRepository.save(label);
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        labels.stream().forEach(label -> {generateImageFromLabel(label);});

        return labels;
    }

    public List<Label> findAllFromFileName(String filename) throws IOException, ParseException {
        List<Label> labelList = generateLabel(filename);
        return labelList;
    }

    public String saveAllFromFileName(String filename) throws IOException, ParseException {
        List<Label> labelList = generateLabel(filename);
        return "Success!!";
    }

    public void generateImageFromLabel(Label label){

        Barcode barcode = new Barcode();
        String code = label.getCode();
        Image img = barcode.encode(EncodingType.CODE128A, code);
        img = img.getScaledInstance(img.getWidth(null),80,BufferedImage.SCALE_REPLICATE);

        int heightImage = 190;
        int widthImage = 300;
        int heightImageSmall = 200;
        int widthImageSmall = 720;

        BufferedImage bufferedImage = new BufferedImage(widthImage,heightImage, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = bufferedImage.getGraphics();

        Locale chile = new Locale("es", "CL");
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(chile);


        try {
            graphics.setColor(Color.WHITE);
            Font fontCode = new Font(Font.SERIF, Font.PLAIN,25);
            Font fontDesc = new Font(Font.SERIF, Font.PLAIN, 20);
            Font fontPrice = new Font(Font.SERIF, Font.BOLD,25);

            graphics.fillRect(0,0,widthImage,heightImage);
            graphics.setColor(Color.BLACK);

            FontMetrics fontMetrics = graphics.getFontMetrics();

            graphics.drawImage(img, 5,  10, null);

            graphics.setFont(fontCode);
            fontMetrics = graphics.getFontMetrics();
            graphics.drawString(code,(300 - fontMetrics.stringWidth(code))/2,40 + img.getHeight(null));


            graphics.setFont(fontDesc);
            fontMetrics = graphics.getFontMetrics();
            String description = label.getDescription();
            description = (description.length() < 12 ) ? description :  description.substring(0,12);
            graphics.drawString(description,(300 - fontMetrics.stringWidth(description))/2,65 + img.getHeight(null));

            graphics.setFont(fontPrice);
            fontMetrics = graphics.getFontMetrics();
            String priceString = numberFormat.format(label.getPrice());
            graphics.drawString(priceString,(300 - fontMetrics.stringWidth(priceString))/2,100 + img.getHeight(null));

            BufferedImage bufferedImageSmall = new BufferedImage(widthImageSmall,heightImageSmall,BufferedImage.TYPE_INT_BGR);
            bufferedImageSmall.getGraphics().drawImage(bufferedImage.getScaledInstance(widthImageSmall,heightImageSmall,BufferedImage.SCALE_REPLICATE),0,0,null);

            label.setImageUrl("Producto_" + label.getCode() + ".png");
            File output = new File("uploads/images/" + label.getImageUrl());
            ImageIO.write(bufferedImage, "PNG", output);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public List<Label> findAll() {
        return (List<Label>) labelRepository.findAll();
    }

    public Optional<Label> getLabelById(Long id) {
        return labelRepository.findById(id);
    }
}