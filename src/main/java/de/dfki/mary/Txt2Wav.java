package de.dfki.mary;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;

import marytts.LocalMaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.util.data.audio.MaryAudioUtils;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class Txt2Wav {

	static final String NAME = "txt2wav";
	static final String IN_OPT = "input";
	static final String OUT_OPT = "output";
        static final String VOICE_OPT = "voice";

	public static void main(String[] args) throws MaryConfigurationException {
		// init CLI options, args
		Options options = new Options();
		Option outputOption = Option.builder("o").longOpt(OUT_OPT).hasArg().argName("FILE").desc("Write output to FILE")
				.required().build();
		Option inputOption = Option.builder("i").longOpt(IN_OPT).hasArg().argName("FILE")
				.desc("Read input from FILE\n(otherwise, read from command line argument)").build();
		Option voiceOption = Option.builder("v").longOpt(VOICE_OPT).hasArg().argName("STRING")
				.desc("Get the voice name STRING [default: cmu-slt-hsmm]").build();
		options.addOption(outputOption);
		options.addOption(inputOption);
		options.addOption(voiceOption);
		HelpFormatter formatter = new HelpFormatter();
		CommandLineParser parser = new DefaultParser();
		CommandLine line = null;
		try {
			line = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("Error parsing command line options: " + e.getMessage());
			formatter.printHelp(NAME, options, true);
			System.exit(1);
		}

		// get output option
		String outputFileName = null;
		if (line.hasOption(OUT_OPT)) {
			outputFileName = line.getOptionValue(OUT_OPT);
			if (!FilenameUtils.getExtension(outputFileName).equals("wav")) {
				outputFileName += ".wav";
			}
		} else {
			System.err.println("Please provide an output wav filename.");
			formatter.printHelp(NAME, options, true);
			System.exit(1);
		}

		// get input
		String inputText = null;
		if (line.hasOption(IN_OPT)) {
			String inputFileName = line.getOptionValue(IN_OPT);
			File file = new File(inputFileName);
			try {
				inputText = FileUtils.readFileToString(file, Charset.defaultCharset());
			} catch (IOException e) {
				System.err.println("Could not read from file " + inputFileName + ": " + e.getMessage());
				System.exit(1);
			}
		} else {
			try {
				inputText = line.getArgList().get(0);
			} catch (IndexOutOfBoundsException e) {
				// ignore
			}
		}
		if (inputText == null) {
			System.err.println("Please provide an input text.");
			formatter.printHelp(NAME, options, true);
			System.exit(1);
		}


		// get inputthe voice name
		String voiceName = null;
		if (line.hasOption(VOICE_OPT)) {
                        voiceName = line.getOptionValue(VOICE_OPT);
		} else {
                        voiceName = "cmu-slt-hsmm";
                }
                System.out.println("Voice used is " + voiceName);

		// init mary
		LocalMaryInterface mary = null;
		try {
			mary = new LocalMaryInterface();
		} catch (MaryConfigurationException e) {
			System.err.println("Could not initialize MaryTTS interface: " + e.getMessage());
			throw e;
		}

                // Set voice / language
                mary.setVoice(voiceName);

		// synthesize
		AudioInputStream audio = null;
		try {
			audio = mary.generateAudio(inputText);
		} catch (SynthesisException e) {
			System.err.println("Synthesis failed: " + e.getMessage());
			System.exit(1);
		}

		// write to output
		double[] samples = MaryAudioUtils.getSamplesAsDoubleArray(audio);
		try {
			MaryAudioUtils.writeWavFile(samples, outputFileName, audio.getFormat());
			System.out.println("Output written to " + outputFileName);
		} catch (IOException e) {
			System.err.println("Could not write to file: " + outputFileName + "\n" + e.getMessage());
			System.exit(1);
		}
	}
}
