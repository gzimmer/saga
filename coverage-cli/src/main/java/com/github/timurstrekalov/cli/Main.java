package com.github.timurstrekalov.cli;

import com.github.timurstrekalov.CoverageGenerator;
import com.google.common.collect.ImmutableList;
import org.apache.commons.cli.*;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(final String[] args) throws IOException, ParseException {
        final Option baseDirOpt = new Option("b", "base-dir", true, "(required) base directory for test search");
        baseDirOpt.setArgs(1);

        final Option includeOpt = new Option("i", "include", true,
                "(required) Comma-separated list of Ant-style paths to the tests to run");
        includeOpt.setArgs(1);

        final Option excludeOpt = new Option("e", "exclude", true,
                "Comma-separated list of Ant-style paths to the tests to exclude from run");
        excludeOpt.setArgs(1);

        final Option outputDirOpt = new Option("o", "output-dir", true, "(required) the output directory for coverage reports");
        outputDirOpt.setArgs(1);

        final Option outputInstrumentedFilesOpt = new Option("f", "output-instrumented-files", true,
                "Whether to output instrumented files (default is false)");
        outputInstrumentedFilesOpt.setArgs(0);

        final Option noInstrumentPatternOpt = new Option("n", "no-instrument-pattern", true,
                "Regular expression patterns to match classes to exclude from instrumentation");
        noInstrumentPatternOpt.setArgs(Option.UNLIMITED_VALUES);

        final Option helpOpt = new Option("h", "help", false, "Print this message");
        final Options options = new Options();

        options.addOption(baseDirOpt);
        options.addOption(includeOpt);
        options.addOption(excludeOpt);
        options.addOption(outputDirOpt);
        options.addOption(outputInstrumentedFilesOpt);
        options.addOption(noInstrumentPatternOpt);
        options.addOption(helpOpt);

        try {
            CommandLineParser parser = new GnuParser();
            CommandLine line = parser.parse(options, args, false);

            baseDirOpt.setRequired(true);
            includeOpt.setRequired(true);
            outputDirOpt.setRequired(true);

            options.addOption(baseDirOpt);
            options.addOption(includeOpt);
            options.addOption(outputDirOpt);

            if (line.hasOption('h')) {
                printHelpAndExit(options);
            }

            parser = new GnuParser();
            line = parser.parse(options, args);

            final CoverageGenerator gen = new CoverageGenerator();
            final File baseDir = new File(line.getOptionValue('b'));

            final List files = FileUtils.getFiles(baseDir, line.getOptionValue('i'), line.getOptionValue('e'));
            final File[] tests = new File[files.size()];

            for (int i = 0; i < files.size(); i++) {
                tests[i] = (File) files.get(i);
            }

            gen.setTests(tests);
            gen.setOutputDir(new File(line.getOptionValue('o')));

            final String outputInstrumentedFiles = line.getOptionValue('f');
            if (outputInstrumentedFiles != null) {
                gen.setOutputInstrumentedFiles(true);
            }

            final String[] noInstrumentPatterns = line.getOptionValues('n');
            if (noInstrumentPatterns != null) {
                gen.setNoInstrumentPatterns(ImmutableList.copyOf(noInstrumentPatterns));
            }

            gen.run();
        } catch (final MissingOptionException e) {
            System.err.println(e.getMessage());
            printHelpAndExit(options);
        } catch (final UnrecognizedOptionException e) {
            System.err.println(e.getMessage());
            printHelpAndExit(options);
        } catch (final ParseException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void printHelpAndExit(final Options options) {
        new HelpFormatter().printHelp("java -jar coverage.jar", options, true);
        System.exit(1);
    }

}