package com.rgfp.psd.logbook.controller;

import com.pnuema.java.barcode.Barcode;
import com.pnuema.java.barcode.EncodingType;
import com.rgfp.psd.logbook.domain.Label;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;

import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@RestController
public class GeneratorController {

    public static final String TITLE = "BIGMARKET";

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity downloadFileFromLocal(@PathVariable String fileName) {
        Path path = Paths.get(fileName);
        Resource resource = null;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping ("/generateZip")
    public String generateZip() throws IOException, ParseException {
        compress("images");
        return "ZIP File has been created !!! ";
    }

    public static void compress(String dirPath) {
        final Path sourceDir = Paths.get(dirPath);
        String zipFileName = dirPath.concat(".zip");
        try {
            final ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipFileName));
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    try {
                        Path targetFile = sourceDir.relativize(file);
                        outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                        byte[] bytes = Files.readAllBytes(file);
                        outputStream.write(bytes, 0, bytes.length);
                        outputStream.closeEntry();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void generateImages(String fileLocation) throws IOException, ParseException {
        FileInputStream file = new FileInputStream(new File(fileLocation));
        Workbook workbook = new XSSFWorkbook(file);

        Sheet sheet = workbook.getSheetAt(0);

        Map<Integer, List<String>> data = new HashMap<>();
        int i = 0;
        for (Row row : sheet) {
            if(i>=4){
                data.put(i, new ArrayList<String>());
                for (Cell cell : row) {
                    //System.out.println("Value: " + cell.toString() + " String Value: " + cell.getStringCellValue());
                    data.get(i).add(cell.toString());
                }
            }
            i++;
        }

        for (Map.Entry entrySet: data.entrySet()) {
            if((int)entrySet.getKey() > 0){
                ArrayList<String> lista = ((ArrayList<String>) entrySet.getValue());
                Barcode barcode = new Barcode();
                String code = lista.get(1).trim().replace("\"","").replace(".0","");
                Image img = barcode.encode(EncodingType.CODE128A, code);
                img = img.getScaledInstance(img.getWidth(null),80,BufferedImage.SCALE_REPLICATE);
                savePic(img,"PNG","uploads/images/Producto_" + code + ".png", code, lista.get(3), NumberFormat.getInstance().parse(lista.get(4)));
            }
        }
    }



    public void savePic(Image image, String extension, String fileDestination, String code, String description, Number price){

        int heightImage = 190;
        int widthImage = 300;
        int heightImageSmall = 200;
        int widthImageSmall = 720;

        BufferedImage bufferedImage = new BufferedImage(widthImage,heightImage, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = bufferedImage.getGraphics();

        Locale chile = new Locale("es", "CL");
        Currency pesos = Currency.getInstance(chile);
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(chile);


        try {
            graphics.setColor(Color.WHITE);
            Font fontTitle = new Font(Font.SERIF, Font.PLAIN, 25);
            Font fontCode = new Font(Font.SERIF, Font.PLAIN,25);
            Font fontDesc = new Font(Font.SERIF, Font.PLAIN, 20);
            Font fontPrice = new Font(Font.SERIF, Font.BOLD,25);

            graphics.fillRect(0,0,widthImage,heightImage);
            graphics.setColor(Color.BLACK);

            //graphics.setFont(fontTitle);
            FontMetrics fontMetrics = graphics.getFontMetrics();
            //graphics.drawString(TITLE,(widthImage - fontMetrics.stringWidth(TITLE))/2, fontMetrics.getHeight() + 5);

            graphics.drawImage(image, 5,  10, null);
            //graphics.drawImage(image, 315,  10, null);
            //graphics.drawImage(image, 625,  10, null);

            graphics.setFont(fontCode);
            fontMetrics = graphics.getFontMetrics();
            graphics.drawString(code,(300 - fontMetrics.stringWidth(code))/2,40 + image.getHeight(null));
            //graphics.drawString(code,310 + (310 - fontMetrics.stringWidth(code))/2,40 + image.getHeight(null));
            //graphics.drawString(code,625 + (305 - fontMetrics.stringWidth(code))/2,40 + image.getHeight(null));

            graphics.setFont(fontDesc);
            fontMetrics = graphics.getFontMetrics();
            description = (description.length() < 12 ) ? description :  description.substring(0,12);
            graphics.drawString(description,(300 - fontMetrics.stringWidth(description))/2,65 + image.getHeight(null));
            //graphics.drawString(description,310 + (310 - fontMetrics.stringWidth(description))/2,65 + image.getHeight(null));
            //graphics.drawString(description,625 + (310 - fontMetrics.stringWidth(description))/2,65 + image.getHeight(null));


            graphics.setFont(fontPrice);
            fontMetrics = graphics.getFontMetrics();
            String priceString = numberFormat.format(price);
            graphics.drawString(priceString,(300 - fontMetrics.stringWidth(priceString))/2,100 + image.getHeight(null));
            //graphics.drawString(priceString,310 + (310 - fontMetrics.stringWidth(priceString))/2,100 + image.getHeight(null));
            //graphics.drawString(priceString,625 + (310 - fontMetrics.stringWidth(priceString))/2,100 + image.getHeight(null));

            BufferedImage bufferedImageSmall = new BufferedImage(widthImageSmall,heightImageSmall,BufferedImage.TYPE_INT_BGR);
            bufferedImageSmall.getGraphics().drawImage(bufferedImage.getScaledInstance(widthImageSmall,heightImageSmall,BufferedImage.SCALE_REPLICATE),0,0,null);

            ImageIO.write(bufferedImage, extension, new File(fileDestination));
            //ImageIO.write(bufferedImageSmall, extension, new File(fileDestination));
            //System.out.println("BIGMARKET WIDTH: " + (widthImage - fontMetrics.stringWidth(TITLE))/2 + " W: " + widthImage + "Font H: " + fontMetrics.getHeight() + "BC W: " + image.getWidth(null));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
