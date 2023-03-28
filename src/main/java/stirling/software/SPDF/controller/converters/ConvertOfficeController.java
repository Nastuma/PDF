package stirling.software.SPDF.controller.converters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import stirling.software.SPDF.utils.PdfUtils;
import stirling.software.SPDF.utils.ProcessExecutor;
@Controller
public class ConvertOfficeController {

	
	@GetMapping("/file-to-pdf")
    public String convertToPdfForm(Model model) {
        model.addAttribute("currentPage", "file-to-pdf");
        return "convert/file-to-pdf";
    }

	@PostMapping("/file-to-pdf")
	public ResponseEntity<byte[]> processPdfWithOCR(@RequestParam("fileInput") MultipartFile inputFile) throws IOException, InterruptedException {
		
		//unused but can start server instance if startup time is to long
		//LibreOfficeListener.getInstance().start();
		
		byte[] pdfByteArray = convertToPdf(inputFile);
		return PdfUtils.bytesToWebResponse(pdfByteArray, inputFile.getOriginalFilename().replaceFirst("[.][^.]+$", "") + "_convertedToPDF.pdf");
	}
	
	
public byte[] convertToPdf(MultipartFile inputFile) throws IOException, InterruptedException {
    // Save the uploaded file to a temporary location
    Path tempInputFile = Files.createTempFile("input_", "." + getFileExtension(inputFile.getOriginalFilename()));
    inputFile.transferTo(tempInputFile.toFile());

    // Prepare the output file path
    Path tempOutputFile = Files.createTempFile("output_", ".pdf");

 // Run the LibreOffice command
    List<String> command = new ArrayList<>(Arrays.asList("unoconv", "-vvv",
            "-f",
            "pdf",
            "-o",
            tempOutputFile.toString(),
            tempInputFile.toString()));
    int returnCode = ProcessExecutor.getInstance(ProcessExecutor.Processes.LIBRE_OFFICE).runCommandWithOutputHandling(command);

    // Read the converted PDF file
    byte[] pdfBytes = Files.readAllBytes(tempOutputFile);

    // Clean up the temporary files
    Files.delete(tempInputFile);
    Files.delete(tempOutputFile);

    return pdfBytes;
}



private String getFileExtension(String fileName) {
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex == -1) {
        return "";
    }
    return fileName.substring(dotIndex + 1);
}
}
