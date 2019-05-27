package com.intershop.gradle.analysis.gradle.fixtures

import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import java.util.zip.Deflater


class JarArchiveCreator {
    int minimumSizeKB = 0
    int maximumSizeKB = 0
    Random random = new Random(1L)
    byte[] charsToUse = "abcdefghijklmnopqrstuvwxyz0123456789".getBytes()

    File createJar(File artifactFile) {
        try {
            artifactFile.withOutputStream { stream ->
                JarOutputStream out = new JarOutputStream(stream, new Manifest());
                out.setLevel(Deflater.NO_COMPRESSION)
                try {
                    addJarEntry(out, artifactFile.name + ".properties", "testcontent")
                    if (minimumSizeKB > 0) {
                        int sizeInBytes
                        if (maximumSizeKB > minimumSizeKB) {
                            sizeInBytes = (minimumSizeKB + random.nextInt(maximumSizeKB - minimumSizeKB)) * 1024
                        } else {
                            sizeInBytes = minimumSizeKB * 1024
                        }
                        addGeneratedUncompressedJarEntry(out, "generated.txt", sizeInBytes)
                    }
                } finally {
                    out.close()
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace()
            System.out.println("Error: " + ex.getMessage())
        }
        return artifactFile;
    }

    private void addJarEntry(JarOutputStream out, String name, String content) {
        // Add archive entry
        JarEntry entry = new JarEntry(name)
        entry.setTime(System.currentTimeMillis())
        out.putNextEntry(entry)

        // Write file to archive
        def contentBytes = content.getBytes("utf-8")
        out.write(contentBytes, 0, contentBytes.length)
    }

    private void addGeneratedUncompressedJarEntry(JarOutputStream out, String name, int sizeInBytes) {
        JarEntry entry = new JarEntry(name)
        entry.setTime(System.currentTimeMillis())
        out.putNextEntry(entry)

        for (int i = 0; i < sizeInBytes; i++) {
            out.write(charsToUse, i % charsToUse.length, 1)
        }
    }

}
