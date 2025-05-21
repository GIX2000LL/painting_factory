package pl.lucas.painting_factory.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import pl.lucas.painting_factory.model.Vehicle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class FileService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Vehicle> readVehiclesFromFile(String filePath) throws IOException {
        return objectMapper.readValue(new File(filePath), new TypeReference<>() {});
    }

    public void saveFile(byte[] bytes, Path path) throws IOException {
        Files.write(path, bytes);
    }
}