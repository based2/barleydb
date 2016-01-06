package scott.barleydb.test;

/*
 * #%L
 * BarleyDB
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2016 Scott Sinclair
 * 			<scottysinclair@gmail.com>
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.example.mac.MacSpec;
import org.example.mi.MiSpec;
import org.example.mi.types.StructureType;
import org.example.mi.types.SyntaxType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import scott.barleydb.api.specification.DefinitionsSpec;
import scott.barleydb.api.specification.SpecRegistry;
import scott.barleydb.build.specification.ddlgen.GenerateDatabaseScript;
import scott.barleydb.build.specification.ddlgen.GenerateHsqlDatabaseScript;
import scott.barleydb.build.specification.ddlgen.GenerateMySqlDatabaseScript;
import scott.barleydb.build.specification.modelgen.GenerateDataModels;
import scott.barleydb.build.specification.modelgen.GenerateQueryModels;
import scott.barleydb.build.specification.staticspec.processor.StaticDefinitionProcessor;
import scott.barleydb.build.specification.vendor.MySqlSpecConverter;

/**
 * Tests generating an XML specification from a static definition
 * @author scott
 *
 */
public class TestGenerator {
	
	@Before
	public void setup() {
        File file = new File("target/generated/src/test/java/org/example/mi");
        file.mkdirs();
        file = new File("target/generated/src/test/java/org/example/mac");
        file.mkdirs();
        file = new File("target/generated/src/test/resources/");
        file.mkdirs();
	}

    @Test
    public void testGenerateMacXmlSpec() throws Exception {
        SpecRegistry registry = new SpecRegistry();
        StaticDefinitionProcessor processor = new StaticDefinitionProcessor();
        @SuppressWarnings("unused")
        DefinitionsSpec macSpec = processor.process(new MacSpec(), registry);

        JAXBContext jc = JAXBContext.newInstance(SpecRegistry.class, StructureType.class, SyntaxType.class, MiSpec.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(registry, System.out);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        marshaller.marshal(registry, bout);
        byte[] data1 = bout.toByteArray();

        Unmarshaller um = jc.createUnmarshaller();
        registry = (SpecRegistry)um.unmarshal(new ByteArrayInputStream(data1));

        bout = new ByteArrayOutputStream();
        marshaller.marshal(registry, bout);
        byte[] data2 = bout.toByteArray();
        assertTrue(Arrays.equals(data1, data2));


        FileOutputStream fout = new FileOutputStream(new File("target/generated/src/test/java/org/example/mac/macspec.xml"));
        marshaller.marshal(registry, fout);
        fout.flush();
        fout.close();
    }

    @Test
    public void testGenerateMiXmlSpec() throws Exception {
        SpecRegistry registry = new SpecRegistry();
        StaticDefinitionProcessor processor = new StaticDefinitionProcessor();
        @SuppressWarnings("unused")
        DefinitionsSpec miSpec = processor.process(new MiSpec(), registry);

        JAXBContext jc = JAXBContext.newInstance(SpecRegistry.class, StructureType.class, SyntaxType.class, MiSpec.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        marshaller.marshal(registry, bout);
        byte[] data1 = bout.toByteArray();

        Unmarshaller um = jc.createUnmarshaller();
        registry = (SpecRegistry)um.unmarshal(new ByteArrayInputStream(data1));

        bout = new ByteArrayOutputStream();
        marshaller.marshal(registry, bout);
        byte[] data2 = bout.toByteArray();
        assertTrue(Arrays.equals(data1, data2));

        File file = new File("target/generated/src/test/java/org/example/mi/mispec.xml");
        FileOutputStream fout = new FileOutputStream(file);
        marshaller.marshal(registry, fout);
        fout.flush();
        fout.close();
    }

    @Test
    public void generateDDLForHsqldb() throws IOException {
        SpecRegistry registry = new SpecRegistry();
        StaticDefinitionProcessor processor = new StaticDefinitionProcessor();

        DefinitionsSpec miSpec = processor.process(new MiSpec(), registry);

        DefinitionsSpec macSpec = registry.getDefinitionsSpec("org.example.mac");

        GenerateDatabaseScript gen = new GenerateHsqlDatabaseScript();

        System.out.println(gen.generateScript(macSpec));
        System.out.println();
        System.out.println(gen.generateScript(miSpec));

        try ( Writer out = new FileWriter("target/generated/src/test/resources/hsqldb-schema.sql"); ) {
            out.write("---\n--- Schema generated by Sort static definitions ---\n---\n---\n");
            out.write(gen.generateScript(macSpec));
            out.write('\n');
            out.write(gen.generateScript(miSpec));
            out.flush();
        }
    }

    @Ignore
    @Test
    public void generateDDLForMySql() throws IOException {
        SpecRegistry registry = new SpecRegistry();
        StaticDefinitionProcessor processor = new StaticDefinitionProcessor();

        DefinitionsSpec miSpec = processor.process(new MiSpec(), registry);

        DefinitionsSpec macSpec = registry.getDefinitionsSpec("org.example.mac");

        GenerateDatabaseScript gen = new GenerateMySqlDatabaseScript();

        System.out.println(gen.generateScript( MySqlSpecConverter.convertSpec(macSpec)) );
        System.out.println();
        System.out.println(gen.generateScript( MySqlSpecConverter.convertSpec(miSpec)) );

        try ( Writer out = new FileWriter("target/generated/src/test/resources/mysql-schema.sql"); ) {
            out.write("/*\n Schema generated by Sort static definitions\n*/\n");
            out.write(gen.generateScript(MySqlSpecConverter.convertSpec(macSpec)) );
            out.write('\n');
            out.write(gen.generateScript(MySqlSpecConverter.convertSpec(miSpec)) );
            out.flush();
        }
    }

    @Test
    public void generateModels() throws IOException {
        generateMacModels();
        generateMiModels();
    }


    @Test
    public void generateMacModels() throws IOException {
        SpecRegistry registry = new SpecRegistry();
        StaticDefinitionProcessor processor = new StaticDefinitionProcessor();

        DefinitionsSpec macSpec = processor.process(new MacSpec(), registry);

      //  deleteFiles("src/test/java/org/example/mac/model");

        GenerateDataModels generateModels = new GenerateDataModels();
        generateModels.generateDataModels("target/generated/src/test/java", macSpec);

      //  deleteFiles("src/test/java/org/example/mac/query");
        GenerateQueryModels generateQueryModels = new GenerateQueryModels();
        generateQueryModels.generateQueryModels("target/generated/src/test/java", macSpec);
}

    @Test
    public void generateMiModels() throws IOException {
        SpecRegistry registry = new SpecRegistry();
        StaticDefinitionProcessor processor = new StaticDefinitionProcessor();

        DefinitionsSpec miSpec = processor.process(new MiSpec(), registry);

      //  deleteFiles("src/test/java/org/example/mi/model");
        GenerateDataModels generateDataModels = new GenerateDataModels();
        generateDataModels.generateDataModels("target/generated/src/test/java", miSpec);

    //    deleteFiles("src/test/java/org/example/mi/query");
        GenerateQueryModels generateQueryModels = new GenerateQueryModels();
        generateQueryModels.generateQueryModels("target/generated/src/test/java", miSpec);
    }

    @Test
    public void generateCleanScript() throws IOException {
        SpecRegistry registry = new SpecRegistry();
        StaticDefinitionProcessor processor = new StaticDefinitionProcessor();

        DefinitionsSpec miSpec = processor.process(new MiSpec(), registry);

        DefinitionsSpec macSpec = registry.getDefinitionsSpec("org.example.mac");

        GenerateDatabaseScript gen = new GenerateHsqlDatabaseScript();

        System.out.println(gen.generateCleanScript(miSpec));
        System.out.println();
        System.out.println(gen.generateCleanScript(macSpec));

        try ( Writer out = new FileWriter("target/generated/src/test/resources/clean.sql"); ) {
            out.write("/*\n Clean script generated by Sort static definitions\n*/");
            out.write(gen.generateCleanScript(miSpec));
            out.write('\n');
            out.write(gen.generateCleanScript(macSpec));
            out.flush();
        }
    }

    @Test
    public void generateDropScript() throws IOException {
        SpecRegistry registry = new SpecRegistry();
        StaticDefinitionProcessor processor = new StaticDefinitionProcessor();

        DefinitionsSpec miSpec = processor.process(new MiSpec(), registry);

        DefinitionsSpec macSpec = registry.getDefinitionsSpec("org.example.mac");

        GenerateDatabaseScript gen = new GenerateHsqlDatabaseScript();

        System.out.println(gen.generateDropScript(miSpec));
        System.out.println();
        System.out.println(gen.generateDropScript(macSpec));

        try ( Writer out = new FileWriter("target/generated/src/test/resources/drop.sql"); ) {
            out.write("/*\n Clean script generated by Sort static definitions\n*/");
            out.write(gen.generateDropScript(miSpec));
            out.write('\n');
            out.write(gen.generateDropScript(macSpec));
            out.flush();
        }
    }
//
//    private void deleteFiles(String string) {
//        File dir = new File(string);
//        if (!dir.exists()) {
//            return;
//        }
//        for (File file: dir.listFiles()) {
//            if (file.isFile()) {
//                file.delete();
//            }
//        }
//    }

}