package com.signature.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class AssetsFolderInitializer {

    @PostConstruct
    public void createFolders() {
        String[] paths = {"assets/docs", "assets/img"};
        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                System.out.println("📁 Dossier créé: " + dir.getAbsolutePath() + " => " + created);
            } else {
                System.out.println("✅ Dossier déjà présent: " + dir.getAbsolutePath());
            }
        }
    }
}
