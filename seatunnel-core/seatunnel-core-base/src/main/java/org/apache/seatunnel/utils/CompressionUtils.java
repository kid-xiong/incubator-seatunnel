/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.seatunnel.utils;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public final class CompressionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompressionUtils.class);

    private CompressionUtils() {
    }

    /**
     * Untar an input file into an output file.
     * <p>
     * The output file is created in the output folder, having the same name
     * as the input file, minus the '.tar' extension.
     *
     * @param inputFile the input .tar file
     * @param outputDir the output directory file.
     * @throws IOException           io exception
     * @throws FileNotFoundException file not found exception
     * @throws ArchiveException      archive exception
     */
    public static void unTar(final File inputFile, final File outputDir) throws  IOException, ArchiveException {

        LOGGER.info("Untaring {} to dir {}.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath());

        final List<File> untaredFiles = new LinkedList<>();
        try (final InputStream is = new FileInputStream(inputFile);
             final TarArchiveInputStream debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", is)) {
            TarArchiveEntry entry = null;
            while ((entry = (TarArchiveEntry) debInputStream.getNextEntry()) != null) {
                final File outputFile = new File(outputDir, entry.getName());
                if (!outputFile.toPath().normalize().startsWith(outputDir.toPath())) {
                    throw new IllegalStateException("Bad zip entry");
                }
                if (entry.isDirectory()) {
                    LOGGER.info("Attempting to write output directory {}.", outputFile.getAbsolutePath());
                    if (!outputFile.exists()) {
                        LOGGER.info("Attempting to create output directory {}.", outputFile.getAbsolutePath());
                        if (!outputFile.mkdirs()) {
                            throw new IllegalStateException(String.format("Couldn't create directory %s.", outputFile.getAbsolutePath()));
                        }
                    }
                } else {
                    LOGGER.info("Creating output file {}.", outputFile.getAbsolutePath());
                    final OutputStream outputFileStream = new FileOutputStream(outputFile);
                    IOUtils.copy(debInputStream, outputFileStream);
                    outputFileStream.close();
                }
                untaredFiles.add(outputFile);
            }
        }
    }

    /**
     * Ungzip an input file into an output file.
     * <p>
     * The output file is created in the output folder, having the same name
     * as the input file, minus the '.gz' extension.
     *
     * @param inputFile the input .gz file
     * @param outputDir the output directory file.
     * @return The {@link File} with the ungzipped content.
     * @throws IOException           io exception
     * @throws FileNotFoundException file not found exception
     */
    public static File unGzip(final File inputFile, final File outputDir) throws IOException {

        LOGGER.info("Unzipping {} to dir {}.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath());

        final File outputFile = new File(outputDir, inputFile.getName().substring(0, inputFile.getName().length() - 3));

        try (final FileInputStream fis = new FileInputStream(inputFile);
             final GZIPInputStream in = new GZIPInputStream(fis);
             final FileOutputStream out = new FileOutputStream(outputFile)) {
            IOUtils.copy(in, out);
        }
        return outputFile;
    }

}
