package hu.bme.mit.theta.solver.smtlib.binary;

import hu.bme.mit.theta.solver.smtlib.SmtLibSolverProcessFailureException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkState;

public final class ContinousSolverBinary implements SolverBinary {

    private final Process solverProcess;
    private final PrintWriter solverInput;
    private final Reader solverOutput;

    public ContinousSolverBinary(final Path solverPath, final String[] args) {
        final var processCmd = new ArrayList<String>();
        processCmd.add(solverPath.toAbsolutePath().toString());
        processCmd.addAll(Arrays.asList(args));
        try {
            solverProcess = new ProcessBuilder(processCmd)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start();
            solverInput = new PrintWriter(solverProcess.getOutputStream(), true, StandardCharsets.US_ASCII);
            solverOutput = new InputStreamReader(solverProcess.getInputStream(), StandardCharsets.US_ASCII);
            checkState(solverProcess.isAlive());
        } catch (IOException e) {
            throw new SmtLibSolverProcessFailureException(e);
        }
    }

    @Override
    public String issueCommand(final String command) {
        checkState(solverProcess.isAlive());
        solverInput.println(command);
        return readResponse(solverOutput);
    }

    private String readResponse(final Reader solverOutput) {
        final var sb = new StringBuilder(256);
        final var readProcessor = new ReadProcessor();
        while (sb.length() == 0 || !readProcessor.isReady()) {
            Thread.yield();
            if (!solverProcess.isAlive()) {
                throw new SmtLibSolverProcessFailureException("Solver process terminated early");
            }
            try {
                while (solverOutput.ready() && !readProcessor.isReady()) {
                    readProcessor.step(sb, (char) solverOutput.read());
                }
            } catch (IOException e) {
                throw new SmtLibSolverProcessFailureException(e);
            }
        }
        return sb.toString().trim();
    }

    private static final class ReadProcessor {
        private enum ReadStatus {
            INIT, LINE, PARENTHESES, STRING, COMMENT, READY
        }

        private ReadProcessor.ReadStatus status = ReadProcessor.ReadStatus.INIT;
        private int level = 0;

        public void step(final StringBuilder sb, final char c) {
            switch (status) {
                case INIT:
                    if (c == '(') {
                        level++;
                        sb.append(c);
                        status = ReadProcessor.ReadStatus.PARENTHESES;
                    } else if (c == ';') {
                        status = ReadProcessor.ReadStatus.COMMENT;
                    } else if (Character.isAlphabetic(c)) {
                        sb.append(c);
                        status = ReadProcessor.ReadStatus.LINE;
                    }
                    break;
                case LINE:
                    if (c == '\n') {
                        status = ReadProcessor.ReadStatus.READY;
                    } else {
                        sb.append(c);
                    }
                    break;
                case PARENTHESES:
                    sb.append(c);
                    if (c == '(') {
                        level++;
                    } else if (c == ')') {
                        level--;
                        if (level == 0) {
                            status = ReadProcessor.ReadStatus.READY;
                        }
                    } else if (c == '"') {
                        status = ReadProcessor.ReadStatus.STRING;
                    }
                    break;
                case STRING:
                    sb.append(c);
                    if (c == '"') {
                        status = ReadProcessor.ReadStatus.PARENTHESES;
                    }
                    break;
                case COMMENT:
                    if (c == '\n') {
                        status = ReadProcessor.ReadStatus.INIT;
                    }
                    break;
                case READY:
                    throw new AssertionError();
            }
        }

        public boolean isReady() {
            return status == ReadProcessor.ReadStatus.READY;
        }
    }
}