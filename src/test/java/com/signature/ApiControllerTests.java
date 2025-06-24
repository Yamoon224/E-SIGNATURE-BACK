package com.signature;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Pour exécuter les tests dans un ordre précis
public class ApiControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    private static String jwtToken;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Order(0)
    public void testDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            Assertions.assertTrue(connection.isValid(2), "Invalid Database Connection");
            System.out.println("✅ Database Connection Done.");
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed !");
            System.err.println("Cause : " + e.getMessage());
            // On stoppe ici volontairement pour éviter de fausser les autres tests
            throw new RuntimeException("Database connection fail : " + e.getMessage(), e);
        }
    }


    @Test
    @Order(1)
    public void testRegister() throws Exception {
        try {
            String json = """
                {
                    "email": "jean@example.com",
                    "password": "monMotDePasse123"
                }
                """;
    
            MvcResult result = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andReturn();

            int status = result.getResponse().getStatus();
            String content = result.getResponse().getContentAsString();

            if (status != 200) {
                System.err.println("❌ Test Register Failed : Status = " + status);
                System.err.println("Error Message : " + content);
            }

            // Assertions.assertEquals(200, status, "Le statut HTTP de la réponse n'est pas 200");
            // Assertions.assertTrue(content.contains("success"), "La réponse ne contient pas 'success'");
            
            if (status == 200) {
                System.out.println("✅ Test Register: Passed");
            }
    
        } catch (Exception e) {
            System.err.println("❌ Test Register: Fail - " + e.getMessage());
            e.printStackTrace();
            Assertions.fail("Test Register Failed : " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    public void testLogin() throws Exception {
        String json = """
            {
                "email": "jean@example.com",
                "password": "monMotDePasse123"
            }
            """;

        try {            
            MvcResult result = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

            // Récupérer le token JWT depuis la réponse
            String responseBody = result.getResponse().getContentAsString();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            jwtToken = jsonNode.get("token").asText();

            Assertions.assertNotNull(jwtToken, "Le token JWT ne doit pas être null");
            
            System.out.println("✅ Test Login: Passed");
        } catch (Exception e) {
            System.err.println("❌ Test Login : Fail - " + e.getMessage());
            e.printStackTrace();
            Assertions.fail("Test Login Failed : " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    public void testGetDocumentsByUser() throws Exception {
        try {
            mockMvc.perform(get("/api/documents/user")
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());

            System.out.println("✅ Test Get Documents: Passed");
        } catch (Exception e) {
            System.err.println("❌ Test Get Documents : Fail - " + e.getMessage());
            e.printStackTrace();
            Assertions.fail("Test Get Documents Failed : " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    public void testUploadDocument() throws Exception {
        try {
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "fichier.pdf",
                "application/pdf",
                "Dummy PDF content".getBytes()
            );

            mockMvc.perform(multipart("/api/documents/upload")
                    .file(file)
                    .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("uploaded")));
            System.out.println("✅ Test Upload Document: Passed");
        } catch (Exception e) {
            System.err.println("❌ Test Upload Document : Fail - " + e.getMessage());
            e.printStackTrace();
            Assertions.fail("Test Upload Document Failed : " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    public void testSignDocumentWithImage() throws Exception {
        try {
            // image PNG en mémoire pour éviter le NullPointerException
            BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            byte[] imageBytes = baos.toByteArray();
    
            MockMultipartFile image = new MockMultipartFile(
                "signatureImage",
                "signature.png",
                "image/png",
                imageBytes
            );
    
            mockMvc.perform(multipart("/api/documents/1/sign")
                    .file(image)
                    .param("signatureText", "Approved By M. KEITA")
                    .header("Authorization", "Bearer " + jwtToken)
                    .with(request -> {
                        request.setMethod("POST");
                        return request;
                    }))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("signed")));
    
            System.out.println("✅ Test Sign Document With Image: Passed");
    
        } catch (Exception e) {
            System.err.println("❌ Test Sign Document With Image: Fail - " + e.getMessage());
            e.printStackTrace();
            Assertions.fail("Test Sign Document With Image failed : " + e.getMessage());
        }
    }

    @Test
    @Order(6)
    public void testSignDocumentWithoutImage() throws Exception {
        try {
            mockMvc.perform(multipart("/api/documents/1/sign")
                    .param("signatureText", "M. KEITA")
                    .header("Authorization", "Bearer " + jwtToken)
                    .with(request -> {
                        request.setMethod("POST");
                        return request;
                    }))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("signed")));
            
                System.out.println("✅ Test Sign Document Without Image: Passed");
        } catch (Exception e) {
            System.err.println("❌ Test Sign Document Without Image: Fail - " + e.getMessage());
            e.printStackTrace();
            Assertions.fail("Test Sign Document Without Image failed : " + e.getMessage());
        }
    }
}
